package special.org.background.tasks;

import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import special.org.beans.MongodbDetailMap;
import special.org.beans.MongodbTemplateMap;
import special.org.configs.subconfig.WatchingCollectionConfig;
import special.org.endpoints.search.fulltext.TextMarker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.time.Duration;
import java.util.*;

@Component
public class UpdateDb {
    private static final Logger LOGGER_UPDATE_DB = LoggerFactory.getLogger(UpdateDb.class);

    private final TaskScheduler scheduler;
    private final MongodbTemplateMap templates;
    private final MongodbDetailMap details;

    private final Map<WatchKey, File> mapKey2Dir = new HashMap<>();


    @Autowired
    public UpdateDb(TaskScheduler scheduler, MongodbTemplateMap templates, MongodbDetailMap details) {
        this.scheduler = scheduler;
        this.templates = templates;
        this.details = details;
    }

    // run this on start-up
    @PostConstruct
    public void updateDB() {
        for (var dbName : this.templates.keySet()) {
            var collections = details.get(dbName).getCollections();
            var template = templates.get(dbName);
            for (var collection : collections) {
                this.scheduler.scheduleAtFixedRate(
                        () -> {
                            this.watchCollection(template, collection.getCollectionName());
                            LOGGER_UPDATE_DB.info("Registered |{}|-|{}| for watching", dbName, collection.getCollectionName());
                        },
                        Duration.ofSeconds(30)
                );
            }
        }
    }

    private void watchCollection(MongoTemplate mongoTemplate, WatchingCollectionConfig collectionConfig) {
        // Select the collection to query
        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionConfig.getCollectionName());

        // Create pipeline for operationType filter
        List<Bson> pipeline = new ArrayList<>();
        pipeline.add(Aggregates.match(
                        Filters.in(
                                "operationType",
                                Arrays.asList("insert", "update", "delete")
                        )
                )
        );

        // watch only text field
        List<Bson> fieldFilters = collectionConfig.getTextFields().stream()
                .map(fieldStr -> Filters.exists(fieldStr, true))
                .toList();

        pipeline.add(Aggregates.match(Filters.or(fieldFilters)));

        // Create the Change Stream
        ChangeStreamIterable<Document> changeStream = collection.watch(pipeline)
                .fullDocument(FullDocument.UPDATE_LOOKUP);

        // Iterate over the Change Stream
        for (ChangeStreamDocument<Document> changeEvent : changeStream) {
            if (changeEvent == null || changeEvent.getOperationType() == null) continue;
            // Process the change event here
            switch (changeEvent.getOperationType()) {
                case INSERT:
                    Document document = changeEvent.getFullDocument();
                    if (document == null) break;
                    documentInserted(document, collectionConfig.getTextFields());
                    break;
                case UPDATE:
                    System.out.println("MongoDB Change Stream detected an update");
                    break;
                case DELETE:
                    System.out.println("MongoDB Change Stream detected a delete");
                    break;
            }
        }
    }

    private void documentInserted(@NonNull Document insertedDocument, List<String> textFields) {
        String refId = insertedDocument.getObjectId("_id").toString();
        Map<String, String> textIndexes = new HashMap<>();

        // Assuming the watching fields are embedded within the inserted document
        for (String fieldName : textFields) {
            var value = insertedDocument.getString(fieldName);
            textIndexes.put(fieldName, value);
        }

        // Create a new TextMarker instance
        TextMarker textMarker = new TextMarker();
        textMarker.setRefId(refId);
        textMarker.setTextIndexes(textIndexes);
    }


    private void watchDatabases(String rootFolderPathStr, Map<String, List<WatchingCollectionConfig>> databases) {
        final Collection<String> databasesName = databases.keySet();

        try {
            Path rootPath = Path.of(rootFolderPathStr);

            // try restore all on start
            for (Map.Entry<String, MongoTemplate> templateEntries : templates.entrySet()) {
                String databaseName = templateEntries.getKey();
                // restore data
                LOGGER_UPDATE_DB.info("Startup task: Sync {}.", databaseName);
                restore.restoreDatabase(databaseName, rootFolderPathStr + File.separator + databaseName, databases.get(databaseName));

                // ensure textIndex for each database, each collection
                for (WatchingCollectionConfig collection : resourceWatching.getDatabases().get(templateEntries.getKey())) {
                    textIndex.createTextIndex(templateEntries.getValue(), collection.getCollectionName(), collection.getTextFields());
                }
            }

            // Register the folder to be watched for create, modify, and delete events
            this.registerAllDatabases(rootPath, databasesName); //, StandardWatchEventKinds.ENTRY_DELETE);
            LOGGER_UPDATE_DB.info("Watch system ready");
            while (true) {
                // this will block until there are changes so while true is feasible
                WatchKey watchKey = watchService.take();

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // data lost somehow
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        LOGGER_UPDATE_DB.info("File changes overflow");
                        continue;
                    }

                    Path changedCollectionPath = (Path) event.context();
                    File changedDir = mapKey2Dir.get(watchKey);
                    File collectionFile = changedDir.toPath().resolve(changedCollectionPath).toFile();
                    String changedDatabaseName = changedDir.getName();

                    if (!databasesName.contains(changedDatabaseName)) {
                        LOGGER_UPDATE_DB.info("File {} has been {}, but it's parent `{}` is not in databases list", collectionFile, kind, changedDatabaseName);
                        continue;
                    }

                    List<String> collectionsName = databases.get(changedDatabaseName).stream().map(WatchingCollectionConfig::getCollectionName).toList();

                    String collectionName = extractCollectionNameFromBsonFile(collectionFile);
                    if (collectionName == null) continue;

                    // not the collection we are watching for
                    if (!collectionsName.contains(collectionName)) {
                        LOGGER_UPDATE_DB.info("File {} has been {}, but it is not in collections list. Ignored", collectionFile, kind);
                        continue;
                    }

                    LOGGER_UPDATE_DB.info("File {} has been {}. Updating", collectionFile, kind);
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        textIndex.createTextIndex(
                                templates.get(changedDatabaseName),
                                collectionName,
                                resourceWatching
                                        .getDatabases()
                                        .get(changedDatabaseName).stream()
                                        .filter(collection -> Objects.equals(collection.getCollectionName(), collectionName))
                                        .findFirst().map(WatchingCollectionConfig::getTextFields)
                                        .orElse(Collections.emptyList())
                        );
                    } else {
                        // modify
                    }

                    restore.restoreCollection(changedDatabaseName, collectionName, collectionFile.getPath());
                }

                boolean reset = watchKey.reset();
                if (!reset) {
                    LOGGER_UPDATE_DB.error("Error resetting WatchKey");
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER_UPDATE_DB.error("Error watching folder " + rootFolderPathStr, e);
        }
    }

    private String extractCollectionNameFromBsonFile(File collectionBson) {
        String collectionFileName = collectionBson.getName();
        int lastPos = collectionFileName.lastIndexOf(".bson");
        if (lastPos <= 0) {
            LOGGER_UPDATE_DB.info("File {} is not format-able to collection name. Need .bson file", collectionFileName);
            return null;
        }

        return collectionFileName.substring(0, lastPos);
    }

    private void registerAllDatabases(Path root, Collection<String> databases) throws IOException {
        File[] subEntries = root.toFile().listFiles(subFile -> {
            if (!subFile.isDirectory()) return false;
            return databases.contains(subFile.getName());
        });
        if (subEntries == null) return;
        for (var dbFolder : subEntries) {
            WatchKey key = dbFolder.toPath().register(UpdateDb.watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
            this.mapKey2Dir.put(key, dbFolder);
            LOGGER_UPDATE_DB.info("Registered {}", dbFolder);
        }
    }

}

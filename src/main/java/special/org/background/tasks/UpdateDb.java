package special.org.background.tasks;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import special.org.bash.mongo.MongoRestore;
import special.org.configs.MongodbTemplates;
import special.org.configs.ResourceWatching;
import special.org.configs.ResourceWatchingCollection;
import special.org.mongodb.templates.CreateTextIndex;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

@Service
public class UpdateDb {
    private static final WatchService watchService;
    private static final Logger LOGGER_UPDATE_DB = LoggerFactory.getLogger(UpdateDb.class);

    static {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (UnsupportedOperationException | IOException e) {
            LOGGER_UPDATE_DB.error("OS does not support WatchService");
            throw new RuntimeException(e);
        }
    }

    private final ResourceWatching resourceWatching;
    private final MongoRestore restore;
    private final TaskScheduler scheduler;
    private final Map<WatchKey, File> mapKey2Dir = new HashMap<>();
    private final MongodbTemplates templates;
    private final CreateTextIndex textIndex;


    @Autowired
    public UpdateDb(ResourceWatching resourceWatching, MongoRestore restore, TaskScheduler scheduler, MongodbTemplates templates, CreateTextIndex textIndex) {
        this.resourceWatching = resourceWatching;
        this.restore = restore;
        this.scheduler = scheduler;
        this.templates = templates;
        this.textIndex = textIndex;
    }

    // run this on start-up
    @PostConstruct
    public void updateDB() {
        String rootPath = resourceWatching.getDatabasesPath();

        Map<String, List<ResourceWatchingCollection>> databases = resourceWatching.getDatabases();

        this.scheduler.scheduleAtFixedRate(
                // rootFolder/dbName
                () -> {
                    LOGGER_UPDATE_DB.info("Registered {} for watching", rootPath);
                    this.watchDatabases(rootPath, databases);
                },
                Duration.ofSeconds(30)
        );
    }


    private void watchDatabases(String rootFolderPathStr, Map<String, List<ResourceWatchingCollection>> databases) {
        final Collection<String> databasesName = databases.keySet();

        try {
            Path rootPath = Path.of(rootFolderPathStr);

            // try restore all on start
            for (var databaseName : databasesName) {
                // restore data
                LOGGER_UPDATE_DB.info("Startup task: Sync {}.", databaseName);
                restore.restoreDatabase(databaseName, rootFolderPathStr + File.separator + databaseName, databases.get(databaseName));
                // ensure textIndex for each database, each collection
                for (Map.Entry<String, MongoTemplate> templateEntries : templates.entrySet()) {
                    for (ResourceWatchingCollection collection : resourceWatching.getDatabases().get(templateEntries.getKey())) {
                        textIndex.createTextIndex(templateEntries.getValue(), collection.getCollectionName(), collection.getTextFields());
                    }
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

                    List<String> collectionsName = databases.get(changedDatabaseName).stream().map(ResourceWatchingCollection::getCollectionName).toList();

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
                                        .findFirst().map(ResourceWatchingCollection::getTextFields)
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

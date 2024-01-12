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
import special.org.endpoints.search.fulltext.TextIndexMap;
import special.org.endpoints.search.fulltext.TextMarker;
import special.org.endpoints.search.fulltext.TextSearchRepo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@SuppressWarnings("unused")
public class UpdateDb {
    private static final Logger LOGGER_UPDATE_DB = LoggerFactory.getLogger(UpdateDb.class);

    private final TaskScheduler scheduler;
    private final MongodbTemplateMap templates;
    private final MongodbDetailMap details;
    private final TextSearchRepo repo;
    private final SyncDb syncDb;


    @Autowired
    public UpdateDb(TaskScheduler scheduler, MongodbTemplateMap templates, MongodbDetailMap details, TextSearchRepo repo, SyncDb syncDb) {
        this.scheduler = scheduler;
        this.templates = templates;
        this.details = details;
        this.repo = repo;
        this.syncDb = syncDb;
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
                            LOGGER_UPDATE_DB.info("Registering |{}|-|{}| for watching", dbName, collection.getCollectionName());
                            this.watchCollection(template, collection);
                        },
                        Duration.ofSeconds(30)
                );
            }
        }
        LOGGER_UPDATE_DB.info("Sync existing data");
        syncDb.syncDb();
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
            Document document = changeEvent.getFullDocument();
            if (document == null) continue;
            switch (changeEvent.getOperationType()) {
                case INSERT -> {
                    LOGGER_UPDATE_DB.info("{} has new collection", collectionConfig.getCollectionName());
                    documentInserted(document, mongoTemplate.getDb().getName(), collectionConfig);
                }
                case UPDATE -> {
                    LOGGER_UPDATE_DB.info("{} has modified collection", collectionConfig.getCollectionName());
                    documentModified(document, mongoTemplate.getDb().getName(), collectionConfig);
                }
                case DELETE -> {
                    LOGGER_UPDATE_DB.info("{} has removed collection", collectionConfig.getCollectionName());
                    documentRemoved(document, mongoTemplate.getDb().getName(), collectionConfig.getCollectionName());
                }
            }
        }
    }

    private void documentInserted(@NonNull Document insertedDocument, String dbName, WatchingCollectionConfig collectionConfig) {
        String refId = insertedDocument.getObjectId("_id").toString();
        TextIndexMap textIndexes = new TextIndexMap();

        for (String fieldName : collectionConfig.getTextFields()) {
            String value;
            try {
                value = insertedDocument.getString(fieldName);
            } catch (ClassCastException e) {
                value = "";
            }

            textIndexes.put(fieldName, value);
        }

        // Create a new TextMarker instance
        TextMarker textMarker = new TextMarker();
        textMarker.setDbName(dbName);
        textMarker.setCollectionName(collectionConfig.getCollectionName());
        textMarker.setRefId(refId);
        textMarker.setTextIndexes(textIndexes);

        repo.insert(textMarker);
    }

    private void documentModified(@NonNull Document modifiedDocument, String dbName, WatchingCollectionConfig collectionConfig) {
        String refId = modifiedDocument.getObjectId("_id").toString();
        TextIndexMap textIndexes = new TextIndexMap();

        for (String fieldName : collectionConfig.getTextFields()) {
            String value;
            try {
                value = modifiedDocument.getString(fieldName);
            } catch (ClassCastException e) {
                value = "";
            }

            textIndexes.put(fieldName, value);
        }

        // Create a new TextMarker instance
        TextMarker textMarker;
        var existing = repo.findByDbNameAndCollectionNameAndRefId(dbName, collectionConfig.getCollectionName(), refId);
        if (existing.isPresent()) {
            textMarker = existing.get();
        } else {
            textMarker = new TextMarker();
            textMarker.setDbName(dbName);
            textMarker.setCollectionName(collectionConfig.getCollectionName());
            textMarker.setRefId(refId);
        }
        textMarker.setTextIndexes(textIndexes);

        repo.save(textMarker);
    }

    private void documentRemoved(@NonNull Document removedDocument, String dbName, String collectionName) {
        String refId = removedDocument.getObjectId("_id").toString();
        repo.deleteByDbNameAndCollectionNameAndRefId(dbName, collectionName, refId);
    }
}

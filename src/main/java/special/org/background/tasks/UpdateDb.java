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
import special.org.background.services.CudTextMarker;
import special.org.background.services.SyncDb;
import special.org.beans.MongodbDetailMap;
import special.org.beans.MongodbTemplateMap;
import special.org.configs.ResourceWatching;
import special.org.configs.subconfig.WatchingCollectionConfig;

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
    private final SyncDb syncDb;
    private final ResourceWatching resourceWatching;
    private final CudTextMarker cudTextMarker;


    @Autowired
    public UpdateDb(
            TaskScheduler scheduler,
            MongodbTemplateMap templates,
            MongodbDetailMap details,
            SyncDb syncDb,
            ResourceWatching resourceWatching,
            CudTextMarker cudTextMarker
    ) {
        this.scheduler = scheduler;
        this.templates = templates;
        this.details = details;
        this.syncDb = syncDb;
        this.resourceWatching = resourceWatching;
        this.cudTextMarker = cudTextMarker;
    }

    // run this on start-up
    @PostConstruct
    public void updateDB() {
        for (var dbName : this.templates.keySet()) {
            var collections = details.get(dbName).getCollections();
            var template = templates.get(dbName);
            for (var collection : collections) {
                LOGGER_UPDATE_DB.info("Registering |{}|->|{}| for watching", dbName, collection.getName());
                this.scheduler.scheduleWithFixedDelay(
                        () -> this.watchCollection(template, collection),
                        Duration.ofSeconds(30)
                );
            }
        }

        // make sure mydb is up-to-date with source at start-up
        switch (resourceWatching.getSyncMode()) {
            case NO -> LOGGER_UPDATE_DB.info("Sync data at startup is disabled");

            case STARTUP -> {
                LOGGER_UPDATE_DB.info("Sync existing data");
                syncDb.syncDb();
            }

            case INTERVAL -> {
                LOGGER_UPDATE_DB.info("Sync data every {} seconds", resourceWatching.getSyncInterval());
                this.scheduler.scheduleWithFixedDelay(
                        () -> {
                            LOGGER_UPDATE_DB.info("Sync existing data");
                            syncDb.syncDb();
                        },
                        Duration.ofSeconds(resourceWatching.getSyncInterval())
                );
            }
        }
    }

    private void watchCollection(MongoTemplate mongoTemplate, WatchingCollectionConfig collectionConfig) {
        // Select the collection to query
        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionConfig.getName());

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
                    LOGGER_UPDATE_DB.info("{} has new document", collectionConfig.getName());
                    documentInserted(document, mongoTemplate.getDb().getName(), collectionConfig);
                }
                case UPDATE -> {
                    LOGGER_UPDATE_DB.info("{} has modified document", collectionConfig.getName());
                    documentModified(document, mongoTemplate.getDb().getName(), collectionConfig);
                }
                case DELETE -> {
                    LOGGER_UPDATE_DB.info("{} has removed document", collectionConfig.getName());
                    documentRemoved(document, mongoTemplate.getDb().getName(), collectionConfig);
                }
                default -> LOGGER_UPDATE_DB.info("{} has {}", collectionConfig.getName(), changeEvent.getOperationType());
            }
        }
    }

    private void documentInserted(@NonNull Document insertedDocument, String dbName, WatchingCollectionConfig collectionConfig) {
        cudTextMarker.createDocument(insertedDocument, dbName, collectionConfig);
    }

    private void documentModified(@NonNull Document modifiedDocument, String dbName, WatchingCollectionConfig collectionConfig) {
        cudTextMarker.upsertDocument(modifiedDocument, dbName, collectionConfig);
    }

    private void documentRemoved(@NonNull Document removedDocument, String dbName, WatchingCollectionConfig collectionConfig) {
        cudTextMarker.deleteDocument(removedDocument, dbName, collectionConfig);
    }
}

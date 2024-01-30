package special.org.background.tasks;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import special.org.background.services.CollectionWatcher;
import special.org.background.services.CudTextMarker;
import special.org.background.services.SyncDb;
import special.org.beans.MongodbDetailMap;
import special.org.beans.MongodbTemplateMap;
import special.org.configs.ResourceWatching;
import special.org.configs.subconfig.WatchingCollectionConfig;

import java.time.Duration;

@Component
public class UpdateDb {
    private static final Logger LOGGER_UPDATE_DB = LoggerFactory.getLogger(UpdateDb.class);

    private final TaskScheduler scheduler;
    private final MongodbTemplateMap templates;
    private final MongodbDetailMap details;
    private final SyncDb syncDb;
    private final ResourceWatching resourceWatching;
    private final CudTextMarker cudTextMarker;
    private final CollectionWatcher collectionWatcher;


    @Autowired
    public UpdateDb(
            TaskScheduler scheduler,
            MongodbTemplateMap templates,
            MongodbDetailMap details,
            SyncDb syncDb,
            ResourceWatching resourceWatching,
            CudTextMarker cudTextMarker,
            CollectionWatcher collectionWatcher
    ) {
        this.scheduler = scheduler;
        this.templates = templates;
        this.details = details;
        this.syncDb = syncDb;
        this.resourceWatching = resourceWatching;
        this.cudTextMarker = cudTextMarker;
        this.collectionWatcher = collectionWatcher;
    }

    // run this on start-up
    @PostConstruct
    public void updateDB() {
        for (var dbName : this.templates.keySet()) {
            var collections = details.get(dbName).getCollections();
            for (var collection : collections) {
                LOGGER_UPDATE_DB.info("Registering |{}|->|{}| for watching", dbName, collection.getName());
                this.scheduler.scheduleWithFixedDelay(
                        () -> this.watchCollection(dbName, collection),
                        Duration.ofSeconds(30)
                );
            }
        }

        // make sure mydb is up-to-date with source at start-up
        switch (resourceWatching.getSyncMode()) {
            case FALSE -> LOGGER_UPDATE_DB.info("Sync data at startup is disabled");

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

    private void watchCollection(String dbName, WatchingCollectionConfig collectionConfig) {
        collectionWatcher.watchCollection(
                dbName,
                collectionConfig,
                (changeEvent) -> {
                    if (changeEvent == null) {
                        LOGGER_UPDATE_DB.info("Empty change event");
                        return;
                    }
                    if (changeEvent.getOperationType() == null) {
                        LOGGER_UPDATE_DB.info("Unknown OperationType");
                        return;
                    }

                    // Process the change event here
                    Document document = changeEvent.getFullDocument();
                    switch (changeEvent.getOperationType()) {
                        case INSERT -> {
                            LOGGER_UPDATE_DB.info("{} has new document", collectionConfig.getName());
                            assert document != null;
                            documentInserted(document, dbName, collectionConfig);
                        }
                        case UPDATE -> {
                            LOGGER_UPDATE_DB.info("{} has modified document", collectionConfig.getName());
                            assert document != null;
                            documentModified(document, dbName, collectionConfig);
                        }
                        case DELETE -> {
                            LOGGER_UPDATE_DB.info("{} has removed document", collectionConfig.getName());
                            assert changeEvent.getDocumentKey() != null;
                            documentRemoved(Document.parse(changeEvent.getDocumentKey().toJson()), dbName, collectionConfig);
                        }
                        default -> LOGGER_UPDATE_DB.info("{} has {}", collectionConfig.getName(), changeEvent.getOperationType());
                    }
                }
        );
    }

    private void documentInserted(@NonNull Document insertedDocument, String dbName, WatchingCollectionConfig collectionConfig) {
        cudTextMarker.createDocument(insertedDocument, dbName, collectionConfig);
    }

    private void documentModified(@NonNull Document modifiedDocument, String dbName, WatchingCollectionConfig collectionConfig) {
        cudTextMarker.upsertDocument(modifiedDocument, dbName, collectionConfig);
    }

    private void documentRemoved(Document removedDocument, String dbName, WatchingCollectionConfig collectionConfig) {
        if (removedDocument == null) {
            LOGGER_UPDATE_DB.error("Cant get Document before change");
            return;
        }
        cudTextMarker.deleteDocument(removedDocument, dbName, collectionConfig);
    }
}

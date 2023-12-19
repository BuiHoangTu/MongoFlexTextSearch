package special.org.background.tasks;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import special.org.bash.mongo.MongoRestore;
import special.org.configs.ResourceWatching;
import special.org.configs.ResourceWatchingCollection;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;

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


    @Autowired
    public UpdateDb(ResourceWatching resourceWatching, MongoRestore restore, TaskScheduler scheduler) {
        this.resourceWatching = resourceWatching;
        this.restore = restore;
        this.scheduler = scheduler;
    }

    // run this on start-up
    @PostConstruct
    public void updateDB() {
        var rootPath = resourceWatching.getDatabasesPath();

        Map<String, List<ResourceWatchingCollection>> databases = resourceWatching.getDatabases();

        for (var database : databases.entrySet()) {
            this.scheduler.scheduleAtFixedRate(
                    // rootFolder/dbName
                    () -> {this.watchDatabase(Path.of(rootPath, database.getKey()), database.getValue());},
                    Duration.ofSeconds(30)
            );
        }

//        this.watchFolder(folderPath);
    }

    private void watchDatabase(Path dbFolderPath, List<ResourceWatchingCollection> collectionsEntry) {
        final List<String> collectionsName = collectionsEntry.stream().map(ResourceWatchingCollection::getCollectionName).toList();

        try {
            // try restore all on start
            restore.restoreDatabase(dbFolderPath.toFile().getName(), dbFolderPath.toAbsolutePath().toString(), collectionsEntry );

            // Register the folder to be watched for create, modify, and delete events
            dbFolderPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY); //, StandardWatchEventKinds.ENTRY_DELETE);

            while (true) {
                // this will block until there are changes so while true is feasible
                WatchKey watchKey = watchService.take();

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // data lost somehow
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    Path changedPath = (Path) event.context();
                    File collectionFile = dbFolderPath.resolve(changedPath).toFile();

                    var collectionName = extractCollectionNameFromBsonFile(collectionFile);
                    if (collectionName == null) continue;

                    // not the collection we are watching for
                    if (!collectionsName.contains(collectionName)) {
                        LOGGER_UPDATE_DB.info("File {} has been {}, but it is not in collections list. Ignored", collectionFile, kind);
                        continue;
                    }

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {

                    } else {
                        // modify
                    }

                    restore.restoreCollection(dbFolderPath.toFile().getName(), collectionName, collectionFile.getPath());
                }

                boolean reset = watchKey.reset();
                if (!reset) {
                    LOGGER_UPDATE_DB.error("Error resetting WatchKey");
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER_UPDATE_DB.error("Error watching folder " + dbFolderPath, e);
        }
    }

    private String extractCollectionNameFromBsonFile(File collectionBson) {
        String collectionFileName = collectionBson.getName();
        final int lastPos = collectionFileName.lastIndexOf(".bson");
        if (lastPos <= 0) {
            LOGGER_UPDATE_DB.error("File {} is not format-able to collection name. Need .bson file", collectionFileName);
            return null;
        }

        return collectionFileName.substring(0, lastPos);
    }
}

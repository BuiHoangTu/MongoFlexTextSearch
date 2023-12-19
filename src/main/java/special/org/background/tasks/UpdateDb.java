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
import java.util.Collection;
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
            var rootPath = Path.of(rootFolderPathStr);

            // try restore all on start
            for (var databaseName : databasesName) {
                LOGGER_UPDATE_DB.info("Startup task: Sync {}.", databaseName);
                restore.restoreDatabase(databaseName, rootFolderPathStr + File.separator + databaseName, databases.get(databaseName));
            }

            // Register the folder to be watched for create, modify, and delete events
            rootPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY); //, StandardWatchEventKinds.ENTRY_DELETE);

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
                    File collectionFile = rootPath.resolve(changedCollectionPath).toFile();
                    var changedDatabaseName = collectionFile.getParent();

                    if (!databasesName.contains(changedDatabaseName)) {
                        LOGGER_UPDATE_DB.info("File {} has been {}, but it's parent `{}` is not in databases list", collectionFile, kind, changedDatabaseName);
                        continue;
                    }

                    var collectionsName = databases.get(changedDatabaseName).stream().map(ResourceWatchingCollection::getCollectionName).toList();

                    var collectionName = extractCollectionNameFromBsonFile(collectionFile);
                    if (collectionName == null) continue;

                    // not the collection we are watching for
                    if (!collectionsName.contains(collectionName)) {
                        LOGGER_UPDATE_DB.info("File {} has been {}, but it is not in collections list. Ignored", collectionFile, kind);
                        continue;
                    }

                    LOGGER_UPDATE_DB.info("File {} has been {}. Updating", collectionFile, kind);
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {

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
        if (lastPos <= 0) lastPos = collectionFileName.lastIndexOf(".metadata.json");
        if (lastPos <= 0) {
            LOGGER_UPDATE_DB.error("File {} is not format-able to collection name. Need .bson or .metadata.json file", collectionFileName);
            return null;
        }

        return collectionFileName.substring(0, lastPos);
    }
}

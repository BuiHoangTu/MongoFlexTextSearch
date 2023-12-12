package special.org.background.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import special.org.configs.ResourceWatching;

import java.io.IOException;
import java.nio.file.*;

@Service
public class UpdateDb {
    private static final Logger LOGGER_UPDATE_DB = LoggerFactory.getLogger(UpdateDb.class);
    private final ResourceWatching resourceWatching;
    private static final WatchService watchService;

    static {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (UnsupportedOperationException | IOException e) {
            LOGGER_UPDATE_DB.error("OS does not support WatchService");
            throw new RuntimeException(e);
        }
    }

    @Autowired
    public UpdateDb(ResourceWatching resourceWatching) {
        this.resourceWatching = resourceWatching;
    }

    @Scheduled(fixedRate = 30_000) // 30s after last run finish
    public void updateDB() {
        var folderPath = resourceWatching.getDatabasePath();

        this.watchFolder(folderPath);
    }

    public void watchFolder(String folderPath) {
        try {
            Path path = Paths.get(folderPath);

            // Register the folder to be watched for create, modify, and delete events
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            while (true) {
                // this will block until there are changes so while true is feasible
                WatchKey watchKey = watchService.take();

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    Path changedPath = (Path) event.context();
                    String fullPath = path.resolve(changedPath).toString();

                    LOGGER_UPDATE_DB.info("File {} has been {}", fullPath, kind);

                    // Perform your action based on the file change (e.g., print to console)
                    LOGGER_UPDATE_DB.info(fullPath + kind);
                }

                boolean reset = watchKey.reset();
                if (!reset) {
                    LOGGER_UPDATE_DB.error("Error resetting WatchKey");
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER_UPDATE_DB.error("Error watching folder", e);
        }
    }
}

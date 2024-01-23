package special.org.configs;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.context.annotation.Configuration;
import special.org.configs.subconfig.WatchingDatabaseConfig;

import java.util.List;

/**
 * Shouldn't use this directly
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "special.org.watching")
public class ResourceWatching {
    private static final Logger LOGGER_RESOURCE_WATCHER = LoggerFactory.getLogger(ResourceWatching.class);

    private SyncMode syncMode = SyncMode.INTERVAL;
    private int syncInterval = 60;
    private List<WatchingDatabaseConfig> databases;

    @ConstructorBinding
    public ResourceWatching(
            List<WatchingDatabaseConfig> databases,
            SyncMode syncMode,
            int syncInterval
    ) {
        this.databases = databases;
        this.syncMode = syncMode;
        this.syncInterval = syncInterval;
    }

    @PostConstruct
    private void postConstruct() {
        LOGGER_RESOURCE_WATCHER.info("Application's configuration is watching databases {}", databases);
    }

    public enum SyncMode {
        NO,
        STARTUP,
        INTERVAL
    }
}

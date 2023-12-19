package special.org.configs;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Configuration
@ConfigurationProperties(prefix = "special.org.watching")
public class ResourceWatching {
    private static final Logger LOGGER_RESOURCE_WATCHER = LoggerFactory.getLogger(ResourceWatching.class);

    // ------------db_name----collection_name--text
    private final Map<String, List<ResourceWatchingCollection>> databases;
    @Value("${special.org.watching.database-path}")
    private String databasesPath;

    @ConstructorBinding
    public ResourceWatching(Map<String, List<ResourceWatchingCollection>> databases) {
        this.databases = databases;
        LOGGER_RESOURCE_WATCHER.info("Application is watching {}", databases);
    }


//    public static class
}

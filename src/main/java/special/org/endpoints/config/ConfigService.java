package special.org.endpoints.config;


import org.springframework.stereotype.Service;
import special.org.configs.ResourceWatching;

@Service
public class ConfigService {
    private final ResourceWatching resourceWatching;

    public ConfigService(ResourceWatching resourceWatching) {
        this.resourceWatching = resourceWatching;
    }

    public ResourceWatching getResourceWatching() {
        return resourceWatching;
    }


    public Object setResourceWatching() {
        this.resourceWatching.setSyncMode(resourceWatching.getSyncMode());
        this.resourceWatching.setSyncInterval(resourceWatching.getSyncInterval());
        var dbs = this.resourceWatching.getDatabases();
        try (var x = dbs.startBatchChanges()) {
            dbs.clear();
            dbs.addAll(resourceWatching.getDatabases());
        }

        return true;
    }
}

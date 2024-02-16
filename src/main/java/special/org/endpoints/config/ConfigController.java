package special.org.endpoints.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import special.org.configs.ResourceWatching;

@RestController
@RequestMapping(path = "api/config")
public class ConfigController {
    private final ResourceWatching resourceWatching;

    public ConfigController(ResourceWatching resourceWatching) {
        this.resourceWatching = resourceWatching;
    }

    @GetMapping(path = "watching/get")
    public ResponseEntity<ResourceWatching> getResourceWatching() {
        return ResponseEntity.ok(resourceWatching);
    }

    @PostMapping(path = "watching/get")
    public ResponseEntity<?> setResourceWatching(ResourceWatching resourceWatching) {
        this.resourceWatching.setSyncMode(resourceWatching.getSyncMode());
        this.resourceWatching.setSyncInterval(resourceWatching.getSyncInterval());
        this.resourceWatching.setDatabases().getDatabases()(resourceWatching.getDatabases());
    }
}

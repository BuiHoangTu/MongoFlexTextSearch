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
    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping(path = "watching/get")
    public ResponseEntity<ResourceWatching> getResourceWatching() {
        return ResponseEntity.ok(configService.getResourceWatching());
    }
    
    @PostMapping(path = "watching/get")
    public ResponseEntity<?> setResourceWatching(ResourceWatching resourceWatching) {
        return ResponseEntity.accepted().body(configService.setResourceWatching());
    }
}

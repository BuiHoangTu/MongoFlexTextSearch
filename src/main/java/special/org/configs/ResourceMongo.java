package special.org.configs;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@ConfigurationProperties(prefix = "spring.data.mongodb")
@Getter
public class ResourceMongo {
    private final String host;
    private final String username;
    private final String password;
    private final String port;
    private final String database;
    private final String authenticationDatabase;


    public ResourceMongo(Optional<String> host, Optional<String> username, Optional<String> password, Optional<String> port, Optional<String> database, Optional<String> authenticationDatabase) {
        this.host = host.orElse(null);
        this.username = username.orElse(null);
        this.password = password.orElse(null);
        this.port = port.orElse(null);
        this.database = database.orElse(null);
        this.authenticationDatabase = authenticationDatabase.orElse(null);
    }
}

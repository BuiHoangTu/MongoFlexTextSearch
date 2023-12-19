package special.org.configs;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@Getter
public class ResourceMongo {
    @Value("${spring.data.mongodb.host}")
    private String host;
    @Value("${spring.data.mongodb.username}")
    private String username;
    @Value("${spring.data.mongodb.password}")
    private String password;
    @Value("${spring.data.mongodb.port}")
    private String port;
    @Value("${spring.data.mongodb.database}")
    private String database;
    @Value("${spring.data.mongodb.authentication-database}")
    private String authenticationDatabase;

//    @ConstructorBinding
//    public ResourceMongo(Optional<String> host, Optional<String> username, Optional<String> password, Optional<String> port, Optional<String> database) {
//        this.host = host.orElse(null);
//        this.username = username.orElse(null);
//        this.password = password.orElse(null);
//        this.port = port.orElse(null);
//        this.database = database.orElse(null);
//    }
}

package special.org.configs.subconfig;

import lombok.Data;

import java.util.List;

@Data
public class WatchingDatabaseConfig {
    private String database;
    private String host;
    private String username;
    private String password;
    private int port;
    private String authenticationDatabase;
    private List<WatchingCollectionConfig> collections;
}

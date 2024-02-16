package special.org.configs.subconfig;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WatchingDatabaseConfig {
    @EqualsAndHashCode.Include
    private String database;
    @EqualsAndHashCode.Include
    private String host;
    private String username;
    private String password;
    private int port;
    private String authenticationDatabase;
    private List<WatchingCollectionConfig> collections;
}

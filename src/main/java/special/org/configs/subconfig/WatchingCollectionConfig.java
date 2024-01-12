package special.org.configs.subconfig;

import lombok.Data;

import java.util.List;

@Data
public class WatchingCollectionConfig {
    private String name;
    private List<String> textFields;
}

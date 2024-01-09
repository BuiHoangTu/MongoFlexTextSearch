package special.org.configs.subconfig;

import lombok.Data;

import java.util.List;

@Data
public class WatchingCollectionConfig {
    private String collectionName;
    private List<String> textFields;
}

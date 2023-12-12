package special.org.configs;

import lombok.Data;

import java.util.List;

@Data
public class ResourceWatchingCollection {
    private String collectionName;
    private List<String> textFields;
}

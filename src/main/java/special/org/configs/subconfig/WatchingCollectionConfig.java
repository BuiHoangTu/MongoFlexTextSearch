package special.org.configs.subconfig;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class WatchingCollectionConfig {
    private static final String DEFAULT_ID_NAME = "_id";

    private String name;
    private String idName = DEFAULT_ID_NAME;
    private List<String> textFields;

    @NonNull
    public String getIdName() {
        if (idName == null) return DEFAULT_ID_NAME;
        else return idName;
    }
}

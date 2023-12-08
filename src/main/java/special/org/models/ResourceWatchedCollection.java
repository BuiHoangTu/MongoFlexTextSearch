package special.org.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.List;

@Data
public class ResourceWatchedCollection {
    private String name;
    private List<String> textFields;

    @ConstructorBinding
    public ResourceWatchedCollection(String name, List<String> textFields) {
        this.name = name;
        this.textFields = textFields;
    }
}

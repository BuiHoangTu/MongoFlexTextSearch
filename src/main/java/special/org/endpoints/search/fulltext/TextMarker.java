package special.org.endpoints.search.fulltext;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document
@NoArgsConstructor
@AllArgsConstructor
public class TextMarker {
    // where is this text
    private String dbName;
    private String collectionName;
    // main Id of text
    private String refId;
    @TextIndexed
    private Map<String, String> textIndexes;
}

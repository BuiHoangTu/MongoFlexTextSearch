package special.org.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.List;

@Getter
@Document
@NoArgsConstructor
@CompoundIndexes({
        // make sure the ref is unique
        @CompoundIndex(
                name = "unique_ref",
                def = "{" +
                        "'dbName' : 1," +
                        "'collectionName' : 1," +
                        "'refId' : 1" +
                        "}",
                unique = true
        )
})
public class TextMarker {
    @Id
    @Setter
    private String id;
    // where is this text
    @Setter
    private String dbName;

    @Setter
    private String collectionName;

    // main Id of text
    @Setter
    private String refId;

    // store data here
    private TextIndexMap textIndexes;
    @TextIndexed // convert textIndexes to string array
    private List<String> textIndexesAsList;


    public TextMarker(String dbName, String collectionName, String refId, TextIndexMap textIndexes, Collection<String> textIndexesAsList) {
        this.dbName = dbName;
        this.collectionName = collectionName;
        this.refId = refId;
        this.setTextIndexes(textIndexes);
    }

    public void setTextIndexes(TextIndexMap textIndexes) {
        this.textIndexes = textIndexes;
        this.textIndexesAsList = textIndexes.values().stream().toList();
    }

    /**
     * DO NOT USE. Use setTextIndexes
     * @param textIndexesAsList N/A
     */
    public void setTextIndexesAsList(Collection<String> textIndexesAsList) {
        // do nothing because real value is from textIndexes
    }
}

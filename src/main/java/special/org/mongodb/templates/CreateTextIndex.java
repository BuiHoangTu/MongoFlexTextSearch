package special.org.mongodb.templates;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class CreateTextIndex {
    public void createTextIndex(MongoTemplate template, String collectionName, Collection<String> textFields) {
        IndexOperations indexOps = template.indexOps(collectionName);

        TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onFields(textFields.toArray(new String[0]))
                .build();

        indexOps.ensureIndex(textIndex);
    }
}

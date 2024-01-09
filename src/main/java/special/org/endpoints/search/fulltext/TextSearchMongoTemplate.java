package special.org.endpoints.search.fulltext;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@SuppressWarnings("unused")
public class TextSearchMongoTemplate {
    @Autowired
    public TextSearchMongoTemplate() {
    }

    public List<Document> search(MongoTemplate template, String collectionName, String searchPhrase, int limit) {
        Query textSearch = TextQuery
                .queryText(new TextCriteria().matching(searchPhrase))
                .sortByScore() // sorted by decreasing order
                .limit(limit);

        return template.find(textSearch, Document.class, collectionName);
    }

    public BsonArray getWordsCount(MongoTemplate template, String collectionName) {
        Query query = new Query();

        query.addCriteria(Criteria.where("wordCounts")
                // field exist
                .exists(true)
                // field is not null
                .ne(null)
                // field is array
                .type(JsonSchemaObject.Type.ARRAY)
                // size != 0
                .not().size(0)
        );

        query.getFieldsObject();

        var res = template.findOne(query, BsonDocument.class, collectionName);

        if (res != null) return res.getArray("wordCounts");
        else return new BsonArray();
    }
}

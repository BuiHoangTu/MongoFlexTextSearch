package special.org.controllers;

import java.awt.desktop.QuitResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.EmptyBSONCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoCollection;
import org.w3c.dom.Text;

@Repository
public class TextSearchMongoTemplate {
    private final MongoTemplate template;
    private static final String collectionName = "";

    @Autowired
    public TextSearchMongoTemplate(MongoTemplate template) {
        this.template = template;
    }

    public List<Document> search(String collectionName, String searchPhrase, int limit) {
        Query textSearch = TextQuery
                .queryText(new TextCriteria().matching(searchPhrase))
                .sortByScore()
                .limit(limit);

        return template.find(textSearch, Document.class, collectionName);
    }

    public BsonArray getWordsCount() {
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

    public void insert() {

    }
}

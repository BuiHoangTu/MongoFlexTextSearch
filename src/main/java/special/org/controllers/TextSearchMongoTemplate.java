package special.org.controllers;

import java.util.Collections;
import java.util.List;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.EmptyBSONCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoCollection;

@Repository
public class TextSearchMongoTemplate {
    private final MongoTemplate template;
    private final MongoCollection<Document> collection;
    private static final String collectionName = "";

    @Autowired
    public TextSearchMongoTemplate(MongoTemplate template) {
        this.template = template;
        this.collection = template.getCollection(collectionName);
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

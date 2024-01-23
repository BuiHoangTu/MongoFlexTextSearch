package special.org.background.services;

import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import special.org.configs.subconfig.WatchingCollectionConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Primary
@Service
public class CollectionWatcher1 implements CollectionWatcher {

    public void watchCollection(
            MongoTemplate mongoTemplate,
            WatchingCollectionConfig collectionConfig,
            Consumer<ChangeStreamDocument<Document>> changeStreamDocumentConsumer
    ) {
        // Select the collection to query
        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionConfig.getName());

        // Create pipeline for operationType filter
        List<Bson> pipeline = new ArrayList<>();
        pipeline.add(Aggregates.match(
                        Filters.in(
                                "operationType",
                                Arrays.asList("insert", "update", "delete")
                        )
                )
        );

        // watch only text field
        List<Bson> fieldFilters = collectionConfig.getTextFields().stream()
                .map(fieldStr -> Filters.exists(fieldStr, true))
                .toList();

        pipeline.add(Aggregates.match(Filters.or(fieldFilters)));

        // Create the Change Stream
        ChangeStreamIterable<Document> changeStream = collection.watch(pipeline)
                .fullDocument(FullDocument.UPDATE_LOOKUP);

        // Iterate over the Change Stream
        for (ChangeStreamDocument<Document> changeEvent : changeStream) {
            changeStreamDocumentConsumer.accept(changeEvent);
        }
    }
}

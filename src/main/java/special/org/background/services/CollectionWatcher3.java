package special.org.background.services;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.messaging.*;
import reactor.core.publisher.Flux;
import special.org.configs.subconfig.WatchingCollectionConfig;

import java.util.function.Consumer;

public class CollectionWatcher3 {
    public void watchCollection(
            ReactiveMongoTemplate mongoTemplate,
            WatchingCollectionConfig collectionConfig,
            Consumer<ChangeStreamDocument<Document>> changeStreamDocumentConsumer
    ) {
        ReactiveMongoTemplate reactiveMongoTemplate = new ReactiveMongoTemplate(mongoTemplate.getMongoDatabaseFactory());
        Flux<ChangeStreamEvent<Document>> flux = reactiveMongoTemplate
                .changeStream(Document.class)
                .watchCollection(collectionConfig.getName())
//                .filter((Aggregation) null)
                .listen();
        flux.doOnNext(x -> System.out.println(x)).subscribe();
    }
}

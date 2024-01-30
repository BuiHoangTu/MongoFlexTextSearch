package special.org.background.services;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import special.org.beans.MongodbReactiveTemplateMap;
import special.org.configs.subconfig.WatchingCollectionConfig;

import java.util.function.Consumer;

@Primary
@Service
public class CollectionWatcher3 implements CollectionWatcher {
    private static final Logger LOGGER_COLLECTION_WATCHER_3 = LoggerFactory.getLogger(CollectionWatcher3.class);

    private final MongodbReactiveTemplateMap mongoTemplateMap;

    public CollectionWatcher3(MongodbReactiveTemplateMap mongoTemplateMap) {
        this.mongoTemplateMap = mongoTemplateMap;
    }

    public void watchCollection(
            String dbName,
            WatchingCollectionConfig collectionConfig,
            Consumer<ChangeStreamDocument<Document>> changeStreamDocumentConsumer
    ) {
        var mongoTemplate = mongoTemplateMap.get(dbName);
        if (mongoTemplate == null) {
            LOGGER_COLLECTION_WATCHER_3.error("The database {} does not exist in bean {}", dbName, mongoTemplateMap.getClass().getName());
            return;
        }

        ReactiveMongoTemplate reactiveMongoTemplate = new ReactiveMongoTemplate(mongoTemplate.getMongoDatabaseFactory());
        Flux<ChangeStreamEvent<Document>> flux = reactiveMongoTemplate
                .changeStream(Document.class)
                .watchCollection(collectionConfig.getName())
//                .filter((Aggregation) null)
                .listen();
        flux.doOnNext(event -> {
            changeStreamDocumentConsumer.accept(event.getRaw());
        }).subscribe();
//        flux.
    }
}

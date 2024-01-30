package special.org.background.services;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mongo.ReactiveMongoClientFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.messaging.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import special.org.beans.MongodbTemplateMap;
import special.org.configs.subconfig.WatchingCollectionConfig;

import java.util.Map;
import java.util.function.Consumer;

@Service
public class CollectionWatcher2 implements CollectionWatcher {
    private static final Logger LOGGER_COLLECTION_WATCHER_2 = LoggerFactory.getLogger(CollectionWatcher2.class);

    private final MongodbTemplateMap mongoTemplateMap;

    public CollectionWatcher2(MongodbTemplateMap mongoTemplateMap) {
        this.mongoTemplateMap = mongoTemplateMap;
    }

    public void watchCollection(
            String dbName,
            WatchingCollectionConfig collectionConfig,
            Consumer<ChangeStreamDocument<Document>> changeStreamDocumentConsumer
    ) {
        var mongoTemplate = mongoTemplateMap.get(dbName);
        if (mongoTemplate == null) {
            LOGGER_COLLECTION_WATCHER_2.error("The database {} does not exist in bean {}", dbName, mongoTemplateMap.getClass().getName());
            return;
        }

        MessageListenerContainer container = new DefaultMessageListenerContainer(mongoTemplate);
        container.start();

        MessageListener<ChangeStreamDocument<Document>, Document> listener = x -> System.out.println(x.getBodyBeforeChange().toString() + x.getRaw().getOperationType());
        ChangeStreamRequest.ChangeStreamRequestOptions options = new ChangeStreamRequest.ChangeStreamRequestOptions(
                mongoTemplate.getDb().getName(),
                collectionConfig.getName(),
                ChangeStreamOptions.builder()
                        .returnFullDocumentOnUpdate()
                        .returnFullDocumentBeforeChange()
                        .build()
        );

        Subscription subscription = container.register(new ChangeStreamRequest<>(listener, options), Document.class);

//        container.stop();
    }
}

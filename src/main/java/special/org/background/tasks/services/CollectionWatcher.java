package special.org.background.tasks.services;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import special.org.configs.subconfig.WatchingCollectionConfig;

import java.util.function.Consumer;

public interface CollectionWatcher {
    void watchCollection(
            String dbName,
            WatchingCollectionConfig collectionConfig,
            Consumer<ChangeStreamDocument<Document>> changeStreamDocumentConsumer
    );
}

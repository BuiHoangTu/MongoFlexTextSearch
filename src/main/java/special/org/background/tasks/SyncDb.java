package special.org.background.tasks;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import special.org.beans.MongodbDetailMap;
import special.org.beans.MongodbTemplateMap;
import special.org.configs.subconfig.WatchingCollectionConfig;
import special.org.endpoints.search.fulltext.TextIndexMap;
import special.org.endpoints.search.fulltext.TextMarker;
import special.org.endpoints.search.fulltext.TextSearchRepo;

import java.util.List;

/**
 * Make sure text db is up-to-date when start-up
 */
@Service
public class SyncDb {
    private final MongodbTemplateMap templates;
    private final MongodbDetailMap details;
    private final TextSearchRepo repo;

    @Autowired
    public SyncDb(MongodbTemplateMap templates, MongodbDetailMap details, TextSearchRepo repo) {
        this.templates = templates;
        this.details = details;
        this.repo = repo;
    }

    public void syncDb() {
        for (var dbName : this.templates.keySet()) {
            var collections = details.get(dbName).getCollections();
            var template = templates.get(dbName);
            for (var collection : collections) {
                this.syncCollection(template, collection);
            }
        }
    }

    private void syncCollection(MongoTemplate template, WatchingCollectionConfig collectionConfig) {
        Query query = new Query();
        query.fields().include("_id");

        collectionConfig.getTextFields().forEach(fieldName -> {
            query.fields().include(fieldName);
        });

        List<Document> res = template.find(query, Document.class, collectionConfig.getCollectionName());

        for (var document : res) {
            String refId = document.getObjectId("_id").toString();
            TextIndexMap textIndexMap = new TextIndexMap();
            for (String key : collectionConfig.getTextFields()) {
                try {
                    textIndexMap.put(key, document.getString(key));
                } catch (Exception e) {
                    textIndexMap.put(key, "");
                }
            }

            var existing = repo.findByDbNameAndCollectionNameAndRefId(template.getDb().getName(), collectionConfig.getCollectionName(), refId);
            if (existing.isPresent()) {
                var updating = existing.get();
                updating.setTextIndexes(textIndexMap);
                repo.save(updating);
            } else {
                TextMarker textMarker = new TextMarker();
                textMarker.setDbName(template.getDb().getName());
                textMarker.setCollectionName(collectionConfig.getCollectionName());
                textMarker.setRefId(refId);
                textMarker.setTextIndexes(textIndexMap);

                repo.insert(textMarker);
            }
        }
    }
}

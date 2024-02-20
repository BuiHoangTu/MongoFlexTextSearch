package special.org.background.tasks.services;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import special.org.beans.MongodbDetailMap;
import special.org.beans.MongodbTemplateMap;
import special.org.configs.subconfig.WatchingCollectionConfig;

import java.util.List;

/**
 * Make sure text db is up-to-date when start-up
 */
@Service
public class SyncDb {
    private final MongodbTemplateMap templates;
    private final MongodbDetailMap details;
    private final CudTextMarker cudTextMarker;

    @Autowired
    public SyncDb(MongodbTemplateMap templates, MongodbDetailMap details, CudTextMarker cudTextMarker) {
        this.templates = templates;
        this.details = details;
        this.cudTextMarker = cudTextMarker;
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
        Field fields = query.fields();

        fields.include(collectionConfig.getIdName());

        collectionConfig.getTextFields().forEach(fields::include);

        List<Document> res = template.find(query, Document.class, collectionConfig.getName());

        for (var document : res) {
            cudTextMarker.upsertDocument(document, template.getDb().getName(), collectionConfig);
        }
    }
}

package special.org.background.services;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import special.org.beans.MongodbDetailMap;
import special.org.beans.MongodbTemplateMap;
import special.org.configs.subconfig.WatchingCollectionConfig;
import special.org.endpoints.search.fulltext.TextIndexMap;
import special.org.endpoints.search.fulltext.TextMarker;
import special.org.endpoints.search.fulltext.TextSearchRepo;

import java.util.Arrays;
import java.util.List;

/**
 * Make sure text db is up-to-date when start-up
 */
@Service
public class SyncDb {
    private final MongodbTemplateMap templates;
    private final MongodbDetailMap details;
    private final TextSearchRepo repo;
    private final IdService mainService;

    @Autowired
    public SyncDb(MongodbTemplateMap templates, MongodbDetailMap details, TextSearchRepo repo, IdService mainService) {
        this.templates = templates;
        this.details = details;
        this.repo = repo;
        this.mainService = mainService;
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
            String refId = mainService.getId(document, collectionConfig.getIdName());
            TextIndexMap textIndexMap = new TextIndexMap();
            for (String key : collectionConfig.getTextFields()) {
                try {
                    String[] keyAsParts = key.split("\\.");
                    String lastKey = keyAsParts[keyAsParts.length - 1];

                    var middleKeys = Arrays.stream(keyAsParts).limit(keyAsParts.length - 1).toList();

                    Document currentDocument = document;
                    for (var part : middleKeys) {
                        currentDocument = (Document) currentDocument.get(part);
                    }

                    textIndexMap.put(key, currentDocument.getString(lastKey));
                } catch (Exception e) {
                    textIndexMap.put(key, "");
                }
            }

            var existing = repo.findByDbNameAndCollectionNameAndRefId(template.getDb().getName(), collectionConfig.getName(), refId);
            if (existing.isPresent()) {
                var updating = existing.get();
                updating.setTextIndexes(textIndexMap);
                repo.save(updating);
            } else {
                TextMarker textMarker = new TextMarker();
                textMarker.setDbName(template.getDb().getName());
                textMarker.setCollectionName(collectionConfig.getName());
                textMarker.setRefId(refId);
                textMarker.setTextIndexes(textIndexMap);

                repo.insert(textMarker);
            }
        }
    }
}

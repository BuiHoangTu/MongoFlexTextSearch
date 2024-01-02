package special.org.endpoints.search.fulltext;

import com.mongodb.MongoQueryException;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import special.org.configs.MongodbTemplates;
import special.org.configs.ResourceWatching;
import special.org.configs.ResourceWatchingCollection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class TextSearchService {
    private static final Logger LOGGER_TEXT_SEARCH_SERVICE = LoggerFactory.getLogger(TextSearchService.class);

    private final TextSearchMongoTemplate database;
    private final ResourceWatching resourceWatching;
    private final MongodbTemplates templatesMap;


    @Autowired
    public TextSearchService(TextSearchMongoTemplate database, ResourceWatching resourceWatching, MongodbTemplates templatesMap) {
        this.database = database;
        this.resourceWatching = resourceWatching;
        this.templatesMap = templatesMap;
    }


    public List<Document> searchTextWithAllWordCount(String searchPhrase, int limit) {
//        var dbWords = this.database.getWordsCount();
//        for (var wordJson : dbWords) {
//            var word = wordJson.asDocument().getString("word");
//            var count = wordJson.asDocument().getString("count");
//        }

        List<Document> result = new ArrayList<>();

        for (Map.Entry<String, MongoTemplate> templateEntries : templatesMap.entrySet()) {
            String databaseName = templateEntries.getKey();
            LOGGER_TEXT_SEARCH_SERVICE.info("Search for `{}` in db `{}`", searchPhrase, databaseName);
            // search each collection
            for (ResourceWatchingCollection collection : resourceWatching.getDatabases().get(templateEntries.getKey())) {
                try {
                    result.addAll(database.search(templateEntries.getValue(), collection.getCollectionName(), searchPhrase, limit));
                } catch (MongoQueryException e) {
                    LOGGER_TEXT_SEARCH_SERVICE.error("Can't search in collection `" + collection.getCollectionName() + "` of db `" + databaseName + "`: ", e);
                }
            }
        }

        result.sort(Comparator.comparingDouble(doc -> doc.getDouble("score")));

        return result.stream().limit(limit).toList();
    }

}

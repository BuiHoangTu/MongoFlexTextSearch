package special.org.endpoints.search.fulltext;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import special.org.beans.MongodbTemplateMap;
import special.org.models.TextMarker;

import java.util.List;

@Service
public class TextSearchService {
    private static final Logger LOGGER_TEXT_SEARCH_SERVICE = LoggerFactory.getLogger(TextSearchService.class);

    private final TextSearchRepo searchRepo;
    private final MongodbTemplateMap templateMap;


    @Autowired
    public TextSearchService(TextSearchRepo searchRepo, MongodbTemplateMap templateMap) {
        this.searchRepo = searchRepo;
        this.templateMap = templateMap;
    }


    public List<TextMarker> searchTextWithAllWordCount(String searchPhrase, int limit) {
//        var dbWords = this.database.getWordsCount();
//        for (var wordJson : dbWords) {
//            var word = wordJson.asDocument().getString("word");
//            var count = wordJson.asDocument().getString("count");
//        }

        return searchRepo.searchFullText(searchPhrase, limit);
    }

    public Document getDocumentFromLocation(TextMarker location) {
        var targetTemplate = templateMap.get(location.getDbName());

        return targetTemplate.findById(location.getId(), Document.class, location.getCollectionName());
    }

}

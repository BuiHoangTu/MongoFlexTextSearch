package special.org.endpoints.search.fulltext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import special.org.beans.MongodbTemplateMap;

import java.util.List;

@Service
public class TextSearchService {
    private static final Logger LOGGER_TEXT_SEARCH_SERVICE = LoggerFactory.getLogger(TextSearchService.class);

    private final TextSearchRepo searchRepo;
    private final MongodbTemplateMap templatesMap;


    @Autowired
    public TextSearchService(TextSearchRepo searchRepo, MongodbTemplateMap templatesMap) {
        this.searchRepo = searchRepo;
        this.templatesMap = templatesMap;
    }


    public List<TextMarker> searchTextWithAllWordCount(String searchPhrase, int limit) {
//        var dbWords = this.database.getWordsCount();
//        for (var wordJson : dbWords) {
//            var word = wordJson.asDocument().getString("word");
//            var count = wordJson.asDocument().getString("count");
//        }

        return searchRepo.searchFullText(searchPhrase, limit);
    }

}

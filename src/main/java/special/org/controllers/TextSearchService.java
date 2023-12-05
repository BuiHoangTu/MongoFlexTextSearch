package special.org.controllers;

import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TextSearchService {
    private final TextSearchMongoTemplate database;

    @Autowired
    public TextSearchService(TextSearchMongoTemplate database) {
        this.database = database;
    }


    public JsonObject searchTextWithAllWordCount(String searchPhrase) {
        var dbWords = this.database.getWordsCount();
        for (var wordJson : dbWords) {
            var word = wordJson.asDocument().getString("word");
            var count = wordJson.asDocument().getString("count");
        }

        return null;
    }

}

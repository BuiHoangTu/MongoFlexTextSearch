package special.org.endpoints.search.fulltext;

import org.bson.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping(path = "api/full-text-search")
public class TextSearchController {
    private static final Logger OPEN_CONTROLLER_LOG = LoggerFactory.getLogger(TextSearchController.class);

    private final TextSearchService service;

    @Autowired
    public TextSearchController(TextSearchService service) {
        this.service = service;
    }

    /** */
    @GetMapping(path = "precise")  
    public ResponseEntity<JsonObject> searchFullTextPrecise(@RequestParam(value = "searchPhrase") String searchPhrase) {
        JsonObject res = service.searchTextWithAllWordCount(searchPhrase, 3);
        OPEN_CONTROLLER_LOG.info("combine-reduced-search got keyText:" + searchPhrase);
        OPEN_CONTROLLER_LOG.info(res.toString());
        return ResponseEntity.ok(res);
    }

}

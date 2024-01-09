package special.org.endpoints.search.fulltext;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(path = "api/full-text-search")
public class TextSearchController {
    private static final Logger OPEN_CONTROLLER_LOG = LoggerFactory.getLogger(TextSearchController.class);

    private final TextSearchService service;

    @Autowired
    public TextSearchController(TextSearchService service) {
        this.service = service;
    }

    /**
     * Get locations of best search
     * @param searchPhrase What you want to search
     * @return max amount of values
     */
    @GetMapping(path = "location")
    public ResponseEntity<List<TextMarker>> searchFullTextPrecise(
            @RequestParam(value = "searchPhrase") String searchPhrase,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        List<TextMarker> res = service.searchTextWithAllWordCount(searchPhrase, limit);
        OPEN_CONTROLLER_LOG.info("combine-reduced-search got keyText:" + searchPhrase);
        OPEN_CONTROLLER_LOG.info(res.toString());
        return ResponseEntity.ok(res);
    }

}

package special.org.endpoints.search.fulltext;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TextSearchRepo extends MongoRepository<TextMarker, String> {

    /**
     * Perform a full-text search on document associate with TextWithAllWordCount.
     *
     * @param text  search phrase
     * @param limit maximum result expected
     * @return sorted of best suited texts with the search phrase in descending order.
     * @apiNote This method always return results with `keywordCounts = null` to
     * speed up search and reduce memory. To access `keywordCounts`, please use
     * `findFirstWithKeywordCount`
     */
    @Aggregation(pipeline = {
            "{ $match: {$text: {$search: ?0 } } }",
            // this field is not necessary in this step
            "{ $unset: 'wordCounts'}",
            "{ $sort: { score: { $meta: 'textScore' } } }",
            "{ $limit: ?1 }"
    })
    List<TextMarker> searchFullText(String text, long limit);

    Optional<TextMarker> findByDbNameAndCollectionNameAndRefId(String dbName, String collectionName, String refId);

    long deleteTextMarkerByDbNameAndCollectionNameAndRefId(String dbName, String collectionName, String refId);
}

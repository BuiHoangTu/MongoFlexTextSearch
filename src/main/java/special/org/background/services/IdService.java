package special.org.background.services;

import org.bson.Document;
import org.springframework.stereotype.Service;

@Service
public class IdService {
    /**
     * Extract id from document as String
     * @param document full data
     * @param idName name of id field
     * @return String of id
     * @throws ClassCastException if type of id is not String or mongodb.ObjectId
     */
    public String getId(Document document, String idName) throws ClassCastException {
        String refId;
        try {
            refId = document.getObjectId(idName).toString();
        } catch (ClassCastException exception) {
            refId = document.getString(idName);
        }

        return refId;
    }
}

package special.org.background.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import special.org.models.ResourceWatcher;

@Service
public class UpdateDb {
    private static final Logger LOGGER_UPDATE_DB = LoggerFactory.getLogger(UpdateDb.class);
    private final ResourceWatcher resourceWatcher;

    @Autowired
    public UpdateDb(ResourceWatcher resourceWatcher) {
        this.resourceWatcher = resourceWatcher;
    }

    @Scheduled(fixedRate = 30_000) // ms
    public void updateDB() {
        var rs = resourceWatcher.get();
        final String key = "north";

        LOGGER_UPDATE_DB.error("Printing database " + key + " :" + rs.get(key).toString());
//        for (var dbEntry : rs.entrySet()) {
//            LOGGER_UPDATE_DB.error("Database {}: ", dbEntry.getKey());
//
//            for (var collectionEntry : dbEntry.getValue().entrySet()) {
//                LOGGER_UPDATE_DB.error("\tName: {}\n\tText Field: {}", collectionEntry.getKey(), collectionEntry.getValue().get("text_fields").toString());
//            }
//
//        }
    }
}

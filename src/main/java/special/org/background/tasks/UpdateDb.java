package special.org.background.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import special.org.configs.ResourceWatching;

@Service
public class UpdateDb {
    private static final Logger LOGGER_UPDATE_DB = LoggerFactory.getLogger(UpdateDb.class);
    private final ResourceWatching resourceWatching;

    @Autowired
    public UpdateDb(ResourceWatching resourceWatching) {
        this.resourceWatching = resourceWatching;
    }

    @Scheduled(fixedRate = 30_000) // ms
    public void updateDB() {
        var rs = resourceWatching.getDatabases();

//        LOGGER_UPDATE_DB.error("Printing database " + rs.entrySet());
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

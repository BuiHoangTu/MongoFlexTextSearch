package special.org.bash.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import special.org.configs.ResourceMongo;
import special.org.configs.ResourceWatchingCollection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

@Service
public class MongoRestore {
    private static final Logger LOGGER_MONGO_RESTORE = LoggerFactory.getLogger(MongoRestore.class);
    private final String exePath;
    private final ResourceMongo resourceMongo;

    @Autowired
    public MongoRestore(ResourceMongo resourceMongo) throws IOException {
        this.resourceMongo = resourceMongo;
        // TODO: select based on OS
        var resource = new ClassPathResource("/lib/mongodb-database-tools-windows-x86_64-100.9.4/bin/mongorestore.exe");
        try {
            this.exePath = resource.getFile().getAbsolutePath();

        } catch (IOException e) {
            LOGGER_MONGO_RESTORE.error("Exe not found: {}", resource.getPath());
            throw e;
        }
    }

    public void restoreDatabase(String dbName, String dbFolderStr, List<ResourceWatchingCollection> collectionsEntry) {
        try {
            // Create a process builder with the executable path
            ProcessBuilder processBuilder = new ProcessBuilder(exePath);

            // add params
            var command = processBuilder.command();
            this.addAuthParams(command);
            // add restored db
            command.add("--db");
            command.add(dbName);
            // add path
            command.add("--dir");
            command.add(dbFolderStr);

            // redirect error to output to log
            processBuilder.redirectErrorStream(true);


            // Start the process
            Process process = processBuilder.start();

            // log output
            try (InputStream inputStream = process.getInputStream();) {
                LOGGER_MONGO_RESTORE.info(new String(inputStream.readAllBytes()));
            }

        } catch (IOException e) {
            LOGGER_MONGO_RESTORE.error("Can't execute restore", e);
        }
    }

    public void restoreCollection(String dbName, String collectionName, String collectionBsonPathStr) {
        try {
            // Create a process builder with the executable path
            ProcessBuilder processBuilder = new ProcessBuilder(exePath);

            // add params
            var command = processBuilder.command();
            this.addAuthParams(command);
            // add restored db
            command.add("--db");
            command.add(dbName);
            // add collection name
            command.add("--collection");
            command.add(collectionName);
            // add path
            command.add("--dir");
            command.add(collectionBsonPathStr);

            // redirect error to output to log
            processBuilder.redirectErrorStream(true);


            // Start the process
            Process process = processBuilder.start();

            // log output
            try (InputStream inputStream = process.getInputStream();) {
                LOGGER_MONGO_RESTORE.info(new String(inputStream.readAllBytes()));
            }

        } catch (IOException e) {
            LOGGER_MONGO_RESTORE.error("Can't execute restore", e);
        }
    }


    private void addAuthParams(List<String> command) {
        // host
        command.add("--host");
        command.add(resourceMongo.getHost());
        // username
        command.add("-u");
        command.add(resourceMongo.getUsername());
        // password
        command.add("-p");
        command.add(resourceMongo.getPassword());
        // auth database
        command.add("--authenticationDatabase");
        command.add(resourceMongo.getAuthenticationDatabase());
    }
}

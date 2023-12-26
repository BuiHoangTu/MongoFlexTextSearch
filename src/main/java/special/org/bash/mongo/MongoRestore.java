package special.org.bash.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import special.org.bash.ExeNotFoundException;
import special.org.configs.ResourceMongo;
import special.org.configs.ResourceWatchingCollection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.System.getenv;

@Service
public class MongoRestore {
    private static final Logger LOGGER_MONGO_RESTORE = LoggerFactory.getLogger(MongoRestore.class);
    private final String exePath;
    private final ResourceMongo resourceMongo;

    @Autowired
    public MongoRestore(ResourceMongo resourceMongo) throws ExeNotFoundException {
        this.resourceMongo = resourceMongo;

        // find exe from path
        String exeName = "mongorestore";
        Optional<Path> execPath = Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                .map(Paths::get)
                .filter(path -> Files.exists(path.resolve(exeName))).findFirst();

        // if exe is in path
        if (execPath.isPresent()) {
            this.exePath = exeName;
            return;
        }
        LOGGER_MONGO_RESTORE.error("Exe not found: {} in PATH", exeName);

        // try windows exe
        // TODO: select based on OSes
        var resource = new ClassPathResource("/lib/mongodb-database-tools-windows-x86_64-100.9.4/bin/mongorestore.exe");
        try {
            this.exePath = resource.getFile().getAbsolutePath();
            return;
        } catch (IOException e) {
            LOGGER_MONGO_RESTORE.error("Exe not found: {}", resource.getPath());
        }

        LOGGER_MONGO_RESTORE.error("Can't find any exe for mongorestore");
        throw new ExeNotFoundException(exeName);
    }

    public void restoreDatabase(String dbName, String dbFolderStr, List<ResourceWatchingCollection> collectionsEntry) {
        try {
            // Create a process builder with the executable path
            ProcessBuilder processBuilder = new ProcessBuilder(exePath);

            // add params
            var command = processBuilder.command();
            this.addDefaultParams(command);
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
            this.addDefaultParams(command);
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


    private void addDefaultParams(List<String> command) {
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
        // less messages
        command.add("--quiet");
    }
}

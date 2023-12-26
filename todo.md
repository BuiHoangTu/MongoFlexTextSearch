# search
foreach db - foreach collection
- [ ] reduce search phrase by db data
- [x] db.textSearch()
- [ ] sort search by score 
        




# fetch data
## watcher
- [x] On start-up try to restore all (also try creating index)
- [x] Only looking at specified bson file 
- [x] Only call collection update if file change
- [x] Also call create index if file create 
```java
public class FileWatcher {

    public static void main(String[] args) {
        try {
            // Define the directory to be watched
            Path directoryPath = Paths.get("/path/to/your/folder");

            // Create a WatchService
            WatchService watchService = FileSystems.getDefault().newWatchService();

            // Register the directory for watch events for create and modify
            directoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

            System.out.println("Watching directory: " + directoryPath);

            // Infinite loop to wait for events
            while (true) {
                // Wait for a key event
                WatchKey key = watchService.take();

                // Process all events in the key
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path createdFilePath = (Path) event.context();
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE && createdFilePath.toString().equals("target_file")) {
                        // A new file "target_file" was created
                        System.out.println("Target file created: " + createdFilePath);
                        // Perform your first action here
                        performAction1();
                    } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY && createdFilePath.toString().equals("target_file")) {
                        // The file "target_file" was modified
                        System.out.println("Target file modified: " + createdFilePath);
                        // Perform your second action here
                        performAction2();
                    }
                }

                // Reset the key to receive further events
                key.reset();
            }

        } catch (Exception e) {
        }
    }
}
```

- [ ] Consider multiple changes 
Implement ActionSet
```java
class LastChange {
    Map<Path, Long> lastModifiedMap = new HashMap<>();

    void function() {
        // Inside the loop
        for (WatchEvent<?> event : key.pollEvents()) {
            Path changedPath = (Path) event.context();
            Path fullPath = mainFolderPath.resolve(changedPath);

            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                long currentTime = System.currentTimeMillis();
                long lastModified = lastModifiedMap.getOrDefault(fullPath, 0L);

                if (currentTime - lastModified > 1000) {
                    // Consider this as a separate change
                    lastModifiedMap.put(fullPath, currentTime);
                    System.out.println("File changed: " + fullPath);
                    // Perform your action here
                    performAction(fullPath);
                }
            }
        }

        // Reset the key to receive further events
        key.reset();

    }

}
```

## mongorestore
- [x] ~~restore~~ reload only changed collection 
```shell
mongorestore --db db_1 dump_folder/db_1/collection_1.bson
mongorestore --db db_1 dump_folder/db_1/collection_2.bson
```

## Indexes
- [x] create text index if not existed 
```java
public class Sample {
    @Autowired
    private MongoTemplate mongoTemplate;

    public void createTextIndexIfNotExist() {
        IndexOperations indexOps = mongoTemplate.indexOps(YourEntity.class);
        
        // check if index exist 
        if (!indexOps.getIndexInfo().stream().anyMatch(indexInfo -> indexInfo.getName().equals("multi_text_index"))) {
            TextIndexDefinition textIndex = new TextIndexDefinitionBuilder()
                    .onField("text_col1")
                    .onField("text_col2")
                    .named("multi_text_index")
                    .build();

            indexOps.ensureIndex(textIndex);
        }
    }
}
```

- [ ] check empty textIndex
- [ ] fix loop in updatedb if error
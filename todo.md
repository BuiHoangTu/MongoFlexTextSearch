# search
foreach db - foreach collection
- [ ] reduce search phrase by db data
- [x] db.textSearch()
- [x] sort search by score 
        




# fetch data
## watcher
- [x] On start-up try to restore all (also try creating index)
- [x] Only looking at specified bson file 
- [x] Only call collection update if file change
- [x] Also call create index if file create 



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

- [ ] fix loop in updatedb if error
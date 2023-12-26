package special.org.configs;

import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
public class MongodbTemplates implements Map<String, MongoTemplate> {
    private final HashMap<String, MongoTemplate> map = new HashMap<>();

    @Autowired
    public MongodbTemplates(ResourceWatching databases, MongoClient client) {
        for (var dbName : databases.getDatabases().keySet()) {
            var factory = new SimpleMongoClientDatabaseFactory(client, dbName);
            var template = new MongoTemplate(factory);

            map.put(dbName, template);
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public MongoTemplate get(Object key) {
        return map.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public MongoTemplate put(String key, MongoTemplate value) {
        return map.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends MongoTemplate> m) {
        map.putAll(m);
    }

    @Override
    public MongoTemplate remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<MongoTemplate> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, MongoTemplate>> entrySet() {
        return map.entrySet();
    }
}

package special.org.beans;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import special.org.configs.ResourceWatching;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Pre-build templates for each database.
 * Map from db name -> its MongoTemplate
 */
@Component
public class MongodbTemplateMap implements Map<String, MongoTemplate> {
    private final HashMap<String, MongoTemplate> map = new HashMap<>();

    @Autowired
    public MongodbTemplateMap(ResourceWatching resourceWatching) {
        for (var databaseConfig : resourceWatching.getDatabases()) {

            String connectionString = "mongodb://" + databaseConfig.getUsername() +
                    ":" + databaseConfig.getPassword() +
                    "@" + databaseConfig.getHost() +
                    ":" + databaseConfig.getPort() +
                    "/" + databaseConfig.getDatabase() +
                    "?authSource=" + databaseConfig.getAuthenticationDatabase();
            MongoClient client = MongoClients.create(connectionString);
            MongoTemplate template = new MongoTemplate(client, databaseConfig.getDatabase());

            map.put(databaseConfig.getDatabase(), template);
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

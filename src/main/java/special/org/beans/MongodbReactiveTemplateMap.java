package special.org.beans;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import special.org.configs.ResourceWatching;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class MongodbReactiveTemplateMap implements Map<String, ReactiveMongoTemplate> {
    private final HashMap<String, ReactiveMongoTemplate> map = new HashMap<>();

    @Autowired
    public MongodbReactiveTemplateMap(ResourceWatching resourceWatching) {
        for (var databaseConfig : resourceWatching.getDatabases()) {

            String connectionString = "mongodb://" + databaseConfig.getUsername() +
                    ":" + databaseConfig.getPassword() +
                    "@" + databaseConfig.getHost() +
                    ":" + databaseConfig.getPort() +
                    "/" + databaseConfig.getDatabase() +
                    "?authSource=" + databaseConfig.getAuthenticationDatabase();
            MongoClient client = MongoClients.create(connectionString);
            ReactiveMongoTemplate template = new ReactiveMongoTemplate(client, databaseConfig.getDatabase());

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
    public ReactiveMongoTemplate get(Object key) {
        return map.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public ReactiveMongoTemplate put(String key, ReactiveMongoTemplate value) {
        return map.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends ReactiveMongoTemplate> m) {
        map.putAll(m);
    }

    @Override
    public ReactiveMongoTemplate remove(Object key) {
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
    public Collection<ReactiveMongoTemplate> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, ReactiveMongoTemplate>> entrySet() {
        return map.entrySet();
    }

}

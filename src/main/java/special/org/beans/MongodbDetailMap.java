package special.org.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import special.org.configs.ResourceWatching;
import special.org.configs.subconfig.WatchingDatabaseConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class MongodbDetailMap implements Map<String, WatchingDatabaseConfig> {
    private final HashMap<String, WatchingDatabaseConfig> map = new HashMap<>();


    @Autowired
    public MongodbDetailMap(ResourceWatching resourceWatching) {
        for (var db : resourceWatching.getDatabases()) {
            map.put(db.getDatabase(), db);
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
    public WatchingDatabaseConfig get(Object key) {
        return map.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public WatchingDatabaseConfig put(String key, WatchingDatabaseConfig value) {
        return map.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends WatchingDatabaseConfig> m) {
        map.putAll(m);
    }

    @Override
    public WatchingDatabaseConfig remove(Object key) {
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
    public Collection<WatchingDatabaseConfig> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, WatchingDatabaseConfig>> entrySet() {
        return map.entrySet();
    }
}

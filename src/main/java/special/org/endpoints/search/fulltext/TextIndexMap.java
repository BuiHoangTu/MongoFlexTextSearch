package special.org.endpoints.search.fulltext;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class TextIndexMap implements Map<String, String> {
    private final LinkedHashMap<String, String> content = new LinkedHashMap<>();


    @Override
    public int size() {
        return content.size();
    }

    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return content.containsKey(key);
    }

    @Override
    public String put(String key, String value) {
        return content.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        content.putAll(m);
    }

    @Override
    public String remove(Object key) {
        return content.remove(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return content.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return content.get(key);
    }

    @Override
    public String getOrDefault(Object key, String defaultValue) {
        return content.getOrDefault(key, defaultValue);
    }

    @Override
    public void clear() {
        content.clear();
    }

    @Override
    public Set<String> keySet() {
        return content.keySet();
    }

    @Override
    public Collection<String> values() {
        return content.values();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return content.entrySet();
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super String> action) {
        content.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super String, ? extends String> function) {
        content.replaceAll(function);
    }
}

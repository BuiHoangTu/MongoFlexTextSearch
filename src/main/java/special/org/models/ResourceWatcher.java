package special.org.models;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "special.org.watching")
public class ResourceWatcher {
    // ------------db_name----collection_name--text
    private final Map<String, Map<String, Map<String, List<String>>>> databases;

    @ConstructorBinding
    public ResourceWatcher(Map<String, Map<String, Map<String, List<String>>>> databases) {
        this.databases = databases;
    }

    public Map<String, Map<String, Map<String, List<String>>>> get() {
        return this.databases;
    }


    public static class
}

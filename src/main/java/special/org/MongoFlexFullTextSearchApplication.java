package special.org;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import special.org.configs.ResourceWatching;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class MongoFlexFullTextSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongoFlexFullTextSearchApplication.class, args);
    }

}

package special.org;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MongoFlexFullTextSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongoFlexFullTextSearchApplication.class, args);
    }

}

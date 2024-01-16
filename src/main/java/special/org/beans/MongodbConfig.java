package special.org.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import special.org.configs.ResourceMongo;

@Configuration
public class MongodbConfig extends MappingMongoConverter {
    private static final String DEFAULT_DOT_REPLACEMENT = "-DOT-";

    private static DbRefResolver configResolver(MongoDatabaseFactory mongoFactory) {
        return new DefaultDbRefResolver(mongoFactory);
    }

    @Autowired
    public MongodbConfig(
            MongoDatabaseFactory mongoFactory,
            MongoMappingContext mongoMappingContext,
            ResourceMongo myMongodb
    ) {
        super(configResolver(mongoFactory), mongoMappingContext);

        String replacement = myMongodb.getMapKeyDotReplacement();
        if (replacement == null || replacement.equals(".")) {
            replacement = DEFAULT_DOT_REPLACEMENT;
        }

        this.setMapKeyDotReplacement(replacement);
    }
}

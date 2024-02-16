package special.org;

public class MongodbUtils {
    public static String buildMongodbConnectionString(
            String username,
            String password,
            String host,
            String port,
            String database,
            String authDatabase
    ) {

        return "mongodb://" + username +
                ":" + password +
                "@" + host +
                ":" + port +
                "/" + database +
                "?authSource=" + authDatabase;
    }
}

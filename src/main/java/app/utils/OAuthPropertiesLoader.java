package app.utils;


import java.io.InputStream;
import java.util.Properties;

public class OAuthPropertiesLoader {
    private static final String PROPERTIES_FILE = "oAuth.properties";

    public static Properties loadProperties() throws Exception {
        Properties properties = new Properties();

        try (InputStream inputStream = OAuthPropertiesLoader.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            properties.load(inputStream);
        }

        return properties;
    }
}

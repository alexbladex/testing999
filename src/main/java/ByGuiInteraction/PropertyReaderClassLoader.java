package ByGuiInteraction;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReaderClassLoader {
    private static Properties properties = new Properties();

    static {
        try (InputStream input = PropertyReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                throw new RuntimeException("Property file not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static boolean getPropertyBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public static String[] getPropertyArray(String key) {
        String property = getProperty(key);
        if (property != null) {
            return property.split(",");
        }
        return new String[0];
    }

    public static void main(String[] args) {
        String[] envUrls = PropertyReaderClassLoader.getPropertyArray("envUrls");
        for (String url : envUrls) {
            System.out.println(url);
        }
    }
}
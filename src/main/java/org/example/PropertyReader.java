package org.example;
import java.io.FileInputStream;
import java.util.Properties;
public class PropertyReader {
    private static Properties properties = new Properties();

    static {
        try (FileInputStream fileInput = new FileInputStream("config.properties")) {
            properties.load(fileInput); // try-with-resources
        } catch (Exception e) {
            e.printStackTrace(); // В продакшн доработать исключение
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
        String[] envUrls = PropertyReader.getPropertyArray("envUrls");
        for (String url : envUrls) {
            System.out.println(url);
        }
    }
}
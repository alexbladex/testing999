package org.example;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JSONReader {
    private static JSONObject jsonObject;

    static {
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get("config.json")), "UTF-8");
            jsonObject = new JSONObject(jsonContent);
        } catch (Exception e) {
            e.printStackTrace(); // В продакшн доработать исключение
        }
    }

    public static String getProperty(String key) {
        return jsonObject.optString(key, null);
    }

    public static String[] getPropertyArray(String key) {
        JSONArray jsonArray = jsonObject.optJSONArray(key);
        if (jsonArray != null) {
            String[] result = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                result[i] = jsonArray.optString(i);
            }
            return result;
        }
        return new String[0];
    }

    public static JSONArray getPropertyJSONArray(String key) {
        return jsonObject.optJSONArray(key);
    }

    public static void main(String[] args) {
        String[] envUrls = JSONReader.getPropertyArray("ads__title");
        for (String url : envUrls) {
            System.out.println(url);
        }
        JSONArray itemsArray = JSONReader.getPropertyJSONArray("items_for_sale");
        if (itemsArray != null) {
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                System.out.println(item.toString(2));
            }
        }
    }
}
package gui.interaction;
import org.json.JSONObject;
public class AdTemplate {
    private String json = "{\n" +
            "  \"url\": \"https://999.md/add?category=construction-and-repair&subcategory=construction-and-repair/finishing-and-facing-materials\",\n" +
            "  \"price\": 200,\n" +
            "  \"price_type\": \"UNIT_MDL\",\n" +
            "  \"title\": \"плитка размер 15*15 белая turkey\",\n" +
            "  \"desc\": \"Настенная плитка кафель 15*15 цвет белая турция. 200 л/м2. Минимальный заказ 2 метра\",\n" +
            "  \"img\": [\n" +
            "    \"defaultAd/plitka1515.png\"\n" +
            "  ],\n" +
            "  \"c\": {\n" +
            "    \"#7.value\": 12900,\n" +
            "    \"#686.value\": 21099\n" +
            "  }\n" +
            "}";
    public JSONObject getAd() {
        return new JSONObject(json);
    }
}

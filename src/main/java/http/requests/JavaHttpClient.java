package http.requests;

import gui.interaction.PropertyReader;
import org.brotli.dec.BrotliInputStream;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.io.*;
import java.net.URI;

public class JavaHttpClient {

    private static HttpClient client;
    private static CookieManager cookieManager;

    public static void main(String[] args) throws Exception {
        String uri, user, pswd, decompressedBody, xsrfValue, formIdValue, imageFileName;
        uri = PropertyReader.getProperty("999Login");
        user = PropertyReader.getProperty("user");
        pswd = PropertyReader.getProperty("pswd");
        HttpResponse<InputStream> httpResponse;
        Map<String, String> cookies;
        Map<String, String> params;
        Document doc;

        // Настройка CookieManager для автоматической обработки куков
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        // Создание HttpClient с поддержкой редиректов и куков
        client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .cookieHandler(cookieManager)
                .connectTimeout(Duration.ofSeconds(20))
                .version(Version.HTTP_1_1)
                .build();

        // 1. Open Login page
        httpResponse = httpGet(uri, uri);
        System.out.println("Response (GET): " + httpResponse.statusCode());

        // 2. Login POST Entity
        cookies = extractCookies();
        params = Map.of(
                "_xsrf", cookies.getOrDefault("_xsrf", ""),
                "redirect_url", cookies.getOrDefault("redirect_url", ""),
                "login", user,
                "password", pswd
        );
        httpResponse = httpPost(uri, uri, params, false);
        System.out.println("\nResponse Headers (GET): " + httpResponse.statusCode());
        httpResponse.headers().map().forEach((key, values) -> {
            System.out.println(key + ": " + String.join(", ", values));
        });
        decompressedBody = decompressResponse(httpResponse);
        System.out.println(decompressedBody);

        // 3. Open Ad Pages
        String pageAd = "https://999.md/add?category=construction-and-repair&subcategory=construction-and-repair/finishing-and-facing-materials";
        String page999 = "https://999.md";
        httpResponse = httpGet(pageAd, page999);
        System.out.println("Response (GET): " + httpResponse.statusCode());
        //System.out.println("Response Body (GET): " + httpResponse.body());
        decompressedBody = decompressResponse(httpResponse);

        // Парсинг HTML с помощью JSoup
        doc = Jsoup.parse(decompressedBody);
        xsrfValue = doc.select("input[name=_xsrf]").attr("value"); // один из куков как input xsrf
        formIdValue = doc.select("input[name=form_id]").attr("value");

        // Выводим тело
        System.out.println("XSRF Value: " + xsrfValue);
        System.out.println("Form ID Value: " + formIdValue);
        System.out.println(decompressedBody);

        // 4. Upload image
        String uploadUrl = "https://i.simpalsmedia.com/upload//?template=f2a36d5fa9db98cd70d83a1dee629306b1b683153cfcca3c6a7ed1004afd4bf0eDviK8XVZ3RiU95wJ9OLdQLWiRP5ZG1ldBn9Yc2wcvVxvlE5U6qYqEzn3WU6bQhx8R%2FCv046tzYZpIi9KNw0keOpppQr4%2F%2FAZR6NADhwfDeeEy8%2BUyXZkVP11yaDiflf2VdrvFIlC3jmt4LzTMENEj%2F0DzKA60gZq5BDqT%2FsvBk%3DgOXiQQLBUCeKJY279OJVZOI33S8jxjfmlNhzOwXt38nkGhjyWheoIAEzyLDa%2BGh4ilDxmzYp9X1Ckt8UoSEdsxX7quT84O%2ByKeKCgmBotyftkJfNAEMHLAkg7tMYW7Hvif5GZgxfl2E2zMhw6xrSDf62xzOipWGTNb5ah8HIpXI%3DUmuAAux21jkmCTB%2B9ykJQIZ%2F90Sb2nuJ6vh8e6aDTXsoNyNUBg9fZ%2ByfWfxZ1hJniR31ZNh32gEzJlyvoZ1hArRE5Vye5%2FJeROFcdC04aIlVyWud2%2FHV5ffPVIkdydL3%2BgYqllgE7ICZfv%2By88Tjz43W%2F%2Fb%2FnB0KfubkV%2BuAC3o%3DEJUJ2gnkrdt4Wxd1J09OveTEZB%2Fk7eKRJN6SuEIJpw8JVlNMp9B%2Br1ObSe6H%2F7EzPwvs%2FqHx%2B82zH8U2p580CtgJBO%2ByfPFxaV2F%2Fgfvu1WwGGSacQ%2FT7KVgOcd806NYNTdJb1pHnUUZAxrdxw5KfSg03LDxSP11LiPk2pB1Xug%3D&base64=thumb";
        Path filePath = Path.of("C:/Temp/plitka10x10.jpg");
        httpResponse = httpPost(uploadUrl, page999, filePath);
        System.out.println("\nImage Response Headers (GET): " + httpResponse.statusCode());
        decompressedBody = decompressResponse(httpResponse);
        JSONObject jsonResponse = new JSONObject(decompressedBody);
        imageFileName = jsonResponse.getJSONObject("output_metadata").getString("filename");
        System.out.println("Image name: " + imageFileName);

        // Create Params
        params = createParams(xsrfValue, formIdValue, imageFileName);
        Map<String, String> paramsWithoutAgree = new HashMap<>(params);
        paramsWithoutAgree.remove("agree");

        // 5. Agree checkbox
        String agreeUrl = "https://999.md/ad_price?subcategory=1238&ad_id=&has_delivery=&package_name=basic&phone=37379169100";
        httpResponse = httpGet(agreeUrl, pageAd);
        System.out.println("Response (GET): " + httpResponse.statusCode());

        // premoderate
        String premoderateUrl = "https://999.md/premoderate";
        httpResponse = httpPost(premoderateUrl, pageAd, params, true);
        System.out.println("\nResponse Headers (GET): " + httpResponse.statusCode());

        // 6. Send Ad Form
        httpResponse = httpPost(pageAd, pageAd, params, false);
        System.out.println("\nResponse Headers (GET): " + httpResponse.statusCode());
        httpResponse.headers().map().forEach((key, values) -> {
            System.out.println(key + ": " + String.join(", ", values));
        });
        decompressedBody = decompressResponse(httpResponse);
        System.out.println(decompressedBody);
        doc = Jsoup.parse(decompressedBody);
        if (!doc.select("div[class*='success'] > h2 > i[class*='success']").isEmpty()) {
            String hrefValue = doc.select("link[rel='alternate']").attr("href");
            String itemId = hrefValue.replaceAll("/(\\d+)/", "$1"); //^.*?md.*?(\d+).*$
            System.out.println("Success ID: " + itemId);
        }
        if (!doc.select("section[class*='error']").isEmpty()) {
            String hrefValue = doc.select("section[class*='error'] > ul > li").text();
            System.out.println("Error ID: " + hrefValue);
        }
        if (!doc.select("form#js-product-payment > h1").isEmpty()) {
            String hrefValue = doc.select("link[rel='alternate']").attr("href");
            String itemId = hrefValue.replaceAll("/(\\d+)/", "$1");
            System.out.println("Payment required ID: " + itemId);
        }
    }

    // Метод для извлечения кук из CookieManager
    private static Map<String, String> extractCookies() {
        Map<String, String> cookieMap = new HashMap<>();
        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();

        for (HttpCookie cookie : cookies) cookieMap.put(cookie.getName(), cookie.getValue());
        return cookieMap;
    }

    // Метод для проверки, является ли статус код редиректом
    private static boolean isRedirect(int statusCode) {
        return statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 307 || statusCode == 308;
    }

    // Метод для разжатия данных
    private static <T> String decompressResponse(HttpResponse<T> response) throws IOException {
        String body;

        if (response.body() instanceof InputStream inputStream) {
            String contentEncoding = response.headers().firstValue("content-encoding").orElse("");

            if (contentEncoding.contains("gzip")) {
                try (GZIPInputStream gzipStream = new GZIPInputStream(inputStream)) {
                    body = new String(gzipStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            } else if (contentEncoding.contains("br")) {
                try (BrotliInputStream brotliStream = new BrotliInputStream(inputStream)) {
                    body = new String(brotliStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            } else {
                body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } else if (response.body() instanceof String) {
            body = (String) response.body();
        } else {
            throw new IllegalArgumentException("Unsupported response type: " + response.body().getClass());
        }
        return body;
    }

    // Стандартный заголовок
    private static HttpRequest.Builder getDefaultHeaders(String referer) {
        return HttpRequest.newBuilder()
                .timeout(Duration.ofMinutes(2))
                .headers("accept", "*/*",
                        "accept-language", "en-US,en;q=0.5",
                        "accept-encoding", "gzip, deflate",
                        "cache-control", "no-cache",
                        "keep-alive", "true",
                        "referer", referer,
                        "user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, Gecko) Chrome/109.0.0.0 Safari/537.36");
    }

    // Метод для выполнения GET запроса
    private static HttpResponse<InputStream> httpGet(String url, String referer) throws Exception {
        HttpRequest getRequest = getDefaultHeaders(referer)
                .uri(new URI(url))
                .build();
        return client.send(getRequest, HttpResponse.BodyHandlers.ofInputStream());
    }

    // Метод для выполнения POST запроса
    private static HttpResponse<InputStream> httpPost(String url, String referer, Map<String, String> params, boolean isMultipart) throws Exception {
        URI uri = new URI(url);
        HttpRequest.Builder postBuilder = getDefaultHeaders(referer)
                .uri(uri);

        if (isMultipart) {
            postBuilder.header("Content-Type", "multipart/form-data; boundary=---boundary");
            String multipartBody = buildMultipartFormData(params);
            postBuilder.POST(BodyPublishers.ofString(multipartBody));
        } else {
            postBuilder.header("Content-Type", "application/x-www-form-urlencoded");
            String formData = buildFormData(params);
            postBuilder.POST(BodyPublishers.ofString(formData));
        }
        HttpRequest postRequest = postBuilder.build();
        HttpResponse<InputStream> response = client.send(postRequest, HttpResponse.BodyHandlers.ofInputStream());

        while (isRedirect(response.statusCode())) {
            String location = response.headers().firstValue("location").orElse(null);
            if (location == null) break;

            location = location.replace("|", "%7C").replace(" ", "%20");
            if (location.startsWith("/")) location = uri.resolve(location).toString();

            uri = new URI(location);
            /*String query = uri.getQuery();
            if (query != null) {
                String[] paramsArray = query.split("&");
                StringBuilder encodedQuery = new StringBuilder();
                for (String param : paramsArray) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length != 2) continue;
                    if (encodedQuery.length() > 0) encodedQuery.append("&");
                    String key = keyValue[0];
                    String value = keyValue[1];
                    encodedQuery.append(keyValue[0]).append("=").append(URLEncoder.encode(keyValue[1], StandardCharsets.UTF_8));
                }
                location = uri.getPath() + "?" + encodedQuery;
                uri = URI.create(location);
            }*/
            HttpRequest request = getDefaultHeaders(referer)
                    .uri(uri)
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        }
        return response;
    }

    // Метод для построения тела запроса из параметров (application/x-www-form-urlencoded)
    private static String buildFormData(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    // Метод для построения тела запроса из параметров (multipart/form-data)
    private static String buildMultipartFormData(Map<String, String> params) {
        String boundary = "---boundary";
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.append("--").append(boundary).append("\r\n");
            builder.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n\r\n");
            builder.append(entry.getValue()).append("\r\n");
        }
        builder.append("--").append(boundary).append("--");
        return builder.toString();
    }

    // Метод для выполнения POST запроса с файловым вложением
    private static HttpResponse<InputStream> httpPost(String url, String referer, Path filePath) throws Exception {
        String boundary = "---boundary";
        String mimeType = Files.probeContentType(filePath);
        if (mimeType == null) {
            mimeType = "image/jpeg";
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, "UTF-8"), true);
        // Начало формируемого тела
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(filePath.getFileName().toString()).append("\"\r\n");
        writer.append("Content-Type: ").append(mimeType).append("\r\n\r\n");
        writer.flush(); // Принудительный сброс заголовков
        // Читаем файл и записываем его в поток
        Files.copy(filePath, byteArrayOutputStream);
        writer.append("\r\n");
        writer.append("--").append(boundary).append("--").append("\r\n");
        writer.flush();

        HttpRequest postRequest = getDefaultHeaders(referer)
                .uri(new URI(url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(BodyPublishers.ofByteArray(byteArrayOutputStream.toByteArray()))
                .build();

        return client.send(postRequest, HttpResponse.BodyHandlers.ofInputStream());
    }

    // Метод для создания списка параметров
    private static Map<String, String> createParams(String xsrfValue, String formIdValue, String imageFileName) {
        // No duplicate, each key in the Map must be unique.
        Map<String, String> params = new HashMap<>();
        params.put("_xsrf", xsrfValue);
        params.put("form_id", formIdValue);
        params.put("category_url", "construction-and-repair");
        params.put("subcategory_url", "construction-and-repair/finishing-and-facing-materials");
        params.put("offer_type", "776");
        params.put("12", "Настенная плитка 10 x 10 белая");
        params.put("13", "Кафель 10*10 цвет белая турция. 200 л/м2. Минимальный заказ 2 метра");
        params.put("1404", "");
        params.put("7", "12900");
        params.put("2", "200");
        params.put("2_unit", "mdl");
        params.put("1640", "");
        params.put("686", "21099");
        params.put("5", "12869");
        params.put("14", imageFileName);
        params.put("file", "");
        params.put("video", "");
        params.put("16", "37379169100");
        params.put("country_prefix", "373");
        params.put("number", "");
        params.put("package_name", "basic");
        params.put("agree", "1");
        return params;
    }
}
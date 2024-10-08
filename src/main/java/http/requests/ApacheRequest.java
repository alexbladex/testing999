package http.requests;

import gui.interaction.PropertyReader;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApacheRequest {
    public static void main(String[] args) throws Exception {
        String uri, user, pswd;
        uri = PropertyReader.getProperty("simpalsUriLogin"); //https://simpalsid.com/user/login
        user = PropertyReader.getProperty("user68");
        pswd = PropertyReader.getProperty("pswd68");
        HashMap<String, String> map = null;

        CloseableHttpClient client = HttpClients.createDefault();

        // Первый GET запрос
        HttpGet getRequest = new HttpGet(new URI(uri));
        setCommonHeaders(getRequest);
        CloseableHttpResponse getResponse = client.execute(getRequest);

        // Выводим в консоль заголовок ответа на первый запрос
        System.out.println("Отправленный URI: " + getRequest.getURI());
        System.out.println("Response Status (GET): " + getResponse.getStatusLine());
        Arrays.stream(getResponse.getAllHeaders()).forEach(header ->
                System.out.println(header.getName() + ": " + header.getValue())
        );

        // Извлекаем значения _xsrf и redirect_url из заголовков Set-Cookie
        map = extractCookies(getResponse);
        String _xsrf = map.get("_xsrf");
        String redirect_url = map.get("redirect_url");

        System.out.println("XSRF Token: " + _xsrf);
        System.out.println("Redirect URL: " + redirect_url);
        getResponse.close();

        // Второй POST запрос
        String postData = String.format("_xsrf=%s&redirect_url=%s&login=%s&password=%s", _xsrf, redirect_url, user, pswd);

        HttpPost postRequest = new HttpPost(new URI(uri));
        setPostHeaders(postRequest, _xsrf, redirect_url);
        postRequest.setEntity(new StringEntity(postData));
        CloseableHttpResponse postResponse = client.execute(postRequest);

        // Выводим в консоль заголовок ответа на второй запрос
        System.out.println("Отправленный URI: " + postRequest.getURI());
        System.out.println("\nResponse Status (POST): " + postResponse.getStatusLine());
        Arrays.stream(postResponse.getAllHeaders()).forEach(header ->
                System.out.println(header.getName() + ": " + header.getValue())
        );

        // Извлекаем значения auth и location из заголовков
        map = extractCookies(postResponse);
        String auth = map.get("auth");

        // Извлекаем значения location из заголовка
        String location = null, token = null, token2 = null;
        Header locationHeader = postResponse.getFirstHeader("Location");
        if (locationHeader != null) {
            location = locationHeader.getValue();
        }
        System.out.println("AUTH Token: " + auth);
        System.out.println("Location URL: " + location);

        Pattern pattern = Pattern.compile("token=([^&]*)&token2=([^&]*)");
        Matcher matcher = pattern.matcher(location);
        if (matcher.find()) {
            token = matcher.group(1);
            token2 = matcher.group(2);
            System.out.println("Token: " + token);
            System.out.println("Token2: " + token2);
        } else {
            System.out.println("Токены не найдены");
        }

        location = String.format("https://999.md/external/auth/log_in?token=%s&token2=%s&redirect_url=%s",
                URLEncoder.encode(token, StandardCharsets.UTF_8),
                URLEncoder.encode(token2, StandardCharsets.UTF_8),
                URLEncoder.encode(redirect_url, StandardCharsets.UTF_8));
        System.out.println("Сформированный URL: " + location);
        postResponse.close();

        // Третий GET запрос
        HttpGet redirectRequest = new HttpGet(new URI(location));
//        setCommonHeaders(redirectRequest);
        redirectRequest.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        redirectRequest.setHeader("accept-language", "en-US,en;q=0.9");
        redirectRequest.setHeader("accept-encoding", "gzip, deflate, br");
        redirectRequest.setHeader("cache-control", "max-age=0");
        redirectRequest.setHeader("connection", "keep-alive");
        redirectRequest.setHeader("host", "999.md");
        redirectRequest.setHeader("referer", "https://simpalsid.com/user/login");
        redirectRequest.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");

        CloseableHttpResponse redirectResponse = client.execute(redirectRequest);

        // Выводим в консоль заголовок ответа на первый запрос
        System.out.println("Отправленный URI: " + redirectRequest.getURI());
        System.out.println("\nResponse Status (GET): " + redirectResponse.getStatusLine());
        Arrays.stream(redirectResponse.getAllHeaders()).forEach(header ->
                System.out.println(header.getName() + ": " + header.getValue())
        );

        // Извлекаем значения auth и simpalsid.auth из заголовков Set-Cookie
        map = extractCookies(redirectResponse);
        String simpalsid_auth = map.get("simpalsid.auth");

        // Извлекаем значения location из заголовка
        location = null;
        locationHeader = redirectResponse.getFirstHeader("Location");
        if (locationHeader != null) {
            location = locationHeader.getValue();
        }

        System.out.println("AUTH Token: " + auth);
        System.out.println("SimpalsAuth Token: " + simpalsid_auth);
        System.out.println("Redirect URL: " + location);
        redirectResponse.close();

        // Четвертый GET запрос
//        HttpGet finalRequest = new HttpGet(new URI(location));
//        setCommonHeaders(finalRequest);
//        finalRequest.setHeader("cookie", String.format("simpalsid.auth=%s; auth=%s", simpalsid_auth, auth));
//        CloseableHttpResponse finalResponse = client.execute(finalRequest);
//
//        System.out.println("4:Response Status (GET): " + finalResponse.getStatusLine());
//        Arrays.stream(finalResponse.getAllHeaders()).forEach(header ->
//                System.out.println(header.getName() + ": " + header.getValue())
//        );
//
//        // Извлекаем utid из ответа
//        map = extractCookies(finalResponse);
//
//        String utid = map.get("utid");
//
//        System.out.println("UTID: " + utid);
//
//        // Выводим тело ответа на четвертый запрос
//        String getResponseBody = EntityUtils.toString(finalResponse.getEntity());
//        System.out.println("Response Body (GET):");
//        System.out.println(getResponseBody);
//        finalResponse.close();

        // Закрываем соединения
        client.close();

    }
    public static HashMap<String, String> extractCookies(CloseableHttpResponse response) {
        HashMap<String, String> cookiesMap = new HashMap<>();

        Header[] headers = response.getHeaders("Set-Cookie");

        for (Header header : headers) {
            String cookie = header.getValue();

            // Разделяем на ключ и значение
            String[] cookieParts = cookie.split(";", 2);
            String[] keyValue = cookieParts[0].split("=", 2);

            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                // Удаляем кавычки, если они есть
                value = value.replace("\"", "");
                cookiesMap.put(key, value);
            }
        }
        return cookiesMap;
    }
    // Метод для установки общих заголовков GET и POST запросов
    private static void setCommonHeaders(HttpRequestBase request) {
        request.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        request.setHeader("accept-language", "en-US,en;q=0.9");
        request.setHeader("accept-encoding", "gzip, deflate, br");
        request.setHeader("cache-control", "max-age=0");
        request.setHeader("connection", "keep-alive");
        request.setHeader("referer", "https://simpalsid.com/user/login");
        request.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
    }

    private static void setPostHeaders(HttpPost postRequest, String xsrf, String redirect_url) {
        postRequest.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        postRequest.setHeader("accept-language", "en-US,en;q=0.9");
        postRequest.setHeader("accept-encoding", "gzip, deflate, br");
        postRequest.setHeader("cache-control", "max-age=0");
        postRequest.setHeader("connection", "keep-alive");
        postRequest.setHeader("content-type", "application/x-www-form-urlencoded");
        postRequest.setHeader("cookie", String.format("_xsrf=%s; redirect_url=%s", xsrf, redirect_url));
        postRequest.setHeader("host", "simpalsid.com");
        postRequest.setHeader("origin", "https://simpalsid.com");
        postRequest.setHeader("referer", "https://simpalsid.com/user/login");
        postRequest.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
    }
}
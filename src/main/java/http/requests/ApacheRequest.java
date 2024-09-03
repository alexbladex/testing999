package http.requests;

import gui.interaction.PropertyReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.Arrays;

public class ApacheRequest {
    public static void main(String[] args) throws Exception {
        String uri, user, pswd;
        uri = PropertyReader.getProperty("simpalsUriLogin");
        user = PropertyReader.getProperty("user");
        pswd = PropertyReader.getProperty("pswd");
        CloseableHttpClient client = HttpClients.createDefault();

        // Первый GET запрос
        HttpGet getRequest = new HttpGet(new URI(uri));
        getRequest.setHeader("authority", "simpalsid.com");
        getRequest.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        getRequest.setHeader("accept-language", "en-US,en;q=0.9");
        getRequest.setHeader("cache-control", "max-age=0");
        getRequest.setHeader("dnt", "1");
        getRequest.setHeader("sec-fetch-dest", "document");
        getRequest.setHeader("sec-fetch-mode", "navigate");
        getRequest.setHeader("sec-fetch-site", "none");
        getRequest.setHeader("sec-fetch-user", "?1");
        getRequest.setHeader("upgrade-insecure-requests", "1");
        getRequest.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");

        CloseableHttpResponse getResponse = client.execute(getRequest);

        // Выводим в консоль заголовок ответа на первый запрос
        System.out.println("Response Status (GET): " + getResponse.getStatusLine());
        Arrays.stream(getResponse.getAllHeaders()).forEach(header ->
                System.out.println(header.getName() + ": " + header.getValue())
        );

        // Выводим тело ответа на первый запрос
        //String getResponseBody = EntityUtils.toString(getResponse.getEntity());
        //System.out.println("Response Body (GET):");
        //System.out.println(getResponseBody);

        // Извлекаем значения _xsrf и redirect_url из заголовков Set-Cookie
        String xsrf = null;
        String redirectUrl = null;
        for (var header : getResponse.getHeaders("Set-Cookie")) {
            if (header.getValue().startsWith("_xsrf")) {
                xsrf = header.getValue().split("=")[1].split(";")[0];
            } else if (header.getValue().contains("redirect_url")) {
                redirectUrl = header.getValue().split("=")[1].split(";")[0].replace("\"", "");
            }
        }

        if (xsrf == null || redirectUrl == null) {
            System.out.println("Не удалось извлечь _xsrf или redirect_url.");
            return;
        }

        // Второй POST запрос
        String postData = String.format("_xsrf=%s&redirect_url=%s&login=%s&password=%s", xsrf, redirectUrl, user, pswd);

        HttpPost postRequest = new HttpPost(new URI("https://simpalsid.com/user/login"));
        postRequest.setHeader("authority", "simpalsid.com");
        postRequest.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        postRequest.setHeader("accept-language", "en-US,en;q=0.9");
        postRequest.setHeader("cache-control", "max-age=0");
        postRequest.setHeader("content-type", "application/x-www-form-urlencoded");
        postRequest.setHeader("cookie", String.format("_xsrf=%s; redirect_url=\"%s\"", xsrf, redirectUrl));
        postRequest.setHeader("dnt", "1");
        postRequest.setHeader("origin", "https://simpalsid.com");
        postRequest.setHeader("referer", "https://simpalsid.com/user/login");
        postRequest.setHeader("sec-fetch-dest", "document");
        postRequest.setHeader("sec-fetch-mode", "navigate");
        postRequest.setHeader("sec-fetch-site", "same-origin");
        postRequest.setHeader("sec-fetch-user", "?1");
        postRequest.setHeader("upgrade-insecure-requests", "1");
        postRequest.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
        postRequest.setEntity(new StringEntity(postData));

        CloseableHttpResponse postResponse = client.execute(postRequest);

        // Выводим в консоль заголовок ответа на второй запрос
        System.out.println("\nResponse Status (POST): " + postResponse.getStatusLine());
        Arrays.stream(postResponse.getAllHeaders()).forEach(header ->
                System.out.println(header.getName() + ": " + header.getValue())
        );

        // Выводим тело ответа на второй запрос
        //String postResponseBody = EntityUtils.toString(postResponse.getEntity());
        //System.out.println("Response Body (POST):");
        //System.out.println(postResponseBody);

        // Закрываем соединения
        getResponse.close();
        postResponse.close();
        client.close();

    }
}
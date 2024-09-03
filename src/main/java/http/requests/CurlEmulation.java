package http.requests;

import gui.interaction.PropertyReader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class CurlEmulation {
    public static void main(String[] args) throws Exception {
        String uri, user, pswd;
        uri = PropertyReader.getProperty("simpalsUriLogin");
        user = PropertyReader.getProperty("user");
        pswd = PropertyReader.getProperty("pswd");
        HttpClient client = HttpClient.newHttpClient();

        // Первый GET запрос
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI(uri))
                .header("authority", "simpalsid.com")
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("accept-language", "en-US,en;q=0.9")
                .header("cache-control", "max-age=0")
                .header("dnt", "1")
                .header("sec-fetch-dest", "document")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-site", "none")
                .header("sec-fetch-user", "?1")
                .header("upgrade-insecure-requests", "1")
                .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        // Выводим в консоль весь заголовок ответа на первый запрос
        System.out.println("Response Headers (GET):");
        getResponse.headers().map().forEach((key, values) -> {
            System.out.println(key + ": " + String.join(", ", values));
        });

        // Извлекаем значения _xsrf и redirect_url из заголовков Set-Cookie
        String xsrf = null;
        String redirectUrl = null;
        Map<String, List<String>> headers = getResponse.headers().map();
        if (headers.containsKey("set-cookie")) {
            for (String cookie : headers.get("set-cookie")) {
                if (cookie.startsWith("_xsrf")) {
                    xsrf = cookie.split("=")[1].split(";")[0];
                } else if (cookie.contains("redirect_url")) {
                    redirectUrl = cookie.split("=")[1].split(";")[0].replace("\"", "");
                }
            }
        }

        if (xsrf == null || redirectUrl == null) {
            System.out.println("Не удалось извлечь _xsrf или redirect_url.");
            return;
        }

        // Второй POST запрос
        String postData = String.format("_xsrf=%s&redirect_url=%s&login=%s&password=%s", xsrf, redirectUrl, user, pswd);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("https://simpalsid.com/user/login"))
                .header("authority", "simpalsid.com")
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("accept-language", "en-US,en;q=0.9")
                .header("cache-control", "max-age=0")
                .header("content-type", "application/x-www-form-urlencoded")
                .header("cookie", String.format("_xsrf=%s; redirect_url=\"%s\"", xsrf, redirectUrl))
                .header("dnt", "1")
                .header("origin", "https://simpalsid.com")
                .header("referer", "https://simpalsid.com/user/login")
                .header("sec-fetch-dest", "document")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-site", "same-origin")
                .header("sec-fetch-user", "?1")
                .header("upgrade-insecure-requests", "1")
                .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
                .POST(HttpRequest.BodyPublishers.ofString(postData))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        // Выводим в консоль весь заголовок ответа на второй запрос
        System.out.println("\nResponse Headers (POST):");
        postResponse.headers().map().forEach((key, values) -> {
            System.out.println(key + ": " + String.join(", ", values));
        });

    }
}

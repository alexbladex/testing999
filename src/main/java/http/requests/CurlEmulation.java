package http.requests;

import gui.interaction.PropertyReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class CurlEmulation {
    public static void main(String[] args) throws Exception {
        String uri, user, pswd;
        uri = PropertyReader.getProperty("simpalsUriLogin");
        user = PropertyReader.getProperty("user68");
        pswd = PropertyReader.getProperty("pswd68");

        HttpClient client = HttpClient.newHttpClient();

        // Первый GET запрос
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI(uri))
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("accept-language", "en-US,en;q=0.9")
                .header("accept-encoding", "gzip, deflate, br")
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
        String _xsrf = null;
        String redirect_url = null;
        Map<String, List<String>> headers = getResponse.headers().map();
        if (headers.containsKey("set-cookie")) {
            for (String cookie : headers.get("set-cookie")) {
                if (cookie.startsWith("_xsrf")) {
                    _xsrf = cookie.split("=")[1].split(";")[0];
                } else if (cookie.startsWith("redirect_url")) {
                    redirect_url = cookie.split("=")[1].split(";")[0].replace("\"", "");
                }
            }
        }

        System.out.println("XSRF Token: " + _xsrf);
        System.out.println("Redirect URL: " + redirect_url);

        // Второй POST запрос
//        String postData = "_xsrf=" + URLEncoder.encode(_xsrf, StandardCharsets.UTF_8)
//                + "&redirect_url=" + URLEncoder.encode(redirect_url, StandardCharsets.UTF_8)
//                + "&login=" + URLEncoder.encode(user, StandardCharsets.UTF_8)
//                + "&password=" + URLEncoder.encode(pswd, StandardCharsets.UTF_8);
        String postData = String.format("_xsrf=%s&redirect_url=%s&login=%s&password=%s", _xsrf, redirect_url, user, pswd);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(uri))
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("accept-language", "en-US,en;q=0.9")
                .header("accept-encoding", "gzip, deflate, br")
                .header("cache-control", "max-age=0")
                .header("content-type", "application/x-www-form-urlencoded")
                .header("cookie", String.format("_xsrf=%s; redirect_url=\"%s\"", _xsrf, redirect_url))
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

        // Извлекаем значения auth и location из заголовков Set-Cookie
        String auth = null, simpalsid_auth = null;
        String location = postResponse.headers().firstValue("location").orElse(null);
        headers = postResponse.headers().map();
        if (headers.containsKey("set-cookie")) {
            for (String cookie : headers.get("set-cookie")) {
                if (cookie.startsWith("auth")) {
                    auth = cookie.split("=")[1].split(";")[0];
                }
            }
        }

        System.out.println("AUTH Token: " + auth);
        System.out.println("Redirect URL: " + location);

        String token = null, token2 = null;
        Pattern pattern = Pattern.compile("token=([^&]*)&token2=([^&]*)");
        Matcher matcher = pattern.matcher(location);
        if (matcher.find()) {
            token = matcher.group(1);
            token2 = matcher.group(2);
            token = URLEncoder.encode(token, StandardCharsets.UTF_8);
            token2 = URLEncoder.encode(token2, StandardCharsets.UTF_8);
        } else {
            System.out.println("Token not found");
        }

        location = String.format("https://999.md/external/auth/log_in?token=%s&token2=%s&redirect_url=%s",
                token, token2, URLEncoder.encode(redirect_url, StandardCharsets.UTF_8));

        // Третий GET запрос
        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(new URI(location))
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("accept-language", "en-US,en;q=0.9")
                .header("accept-encoding", "gzip, deflate, br")
                .header("cache-control", "max-age=0")
                .header("dnt", "1")
                .header("referer", "https://simpalsid.com/user/login")
                .header("sec-fetch-dest", "document")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-site", "cross-site")
                .header("sec-fetch-user", "?1")
                .header("upgrade-insecure-requests", "1")
                .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
                .build();

        HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());

        // Выводим в консоль весь заголовок ответа на третий запрос
        System.out.println("\nResponse Headers (GET):");
        tokenResponse.headers().map().forEach((key, values) -> {
            System.out.println(key + ": " + String.join(", ", values));
        });

        // Извлекаем значения auth и simpalsid.auth из заголовков Set-Cookie
        headers = tokenResponse.headers().map();
        if (headers.containsKey("set-cookie")) {
            for (String cookie : headers.get("set-cookie")) {
                if (cookie.startsWith("simpalsid.auth")) {
                    simpalsid_auth = cookie.split("=")[1].split(";")[0];
                } else if (cookie.contains("auth")) {
                    auth = cookie.split("=")[1].split(";")[0];
                }
            }
        }

        location = tokenResponse.headers().firstValue("location").orElse(null);

        System.out.println("AUTH Token: " + auth);
        System.out.println("SimpalsAuth Token: " + simpalsid_auth);
        System.out.println("Redirect URL: " + location);

        // Четвертый GET запрос
        HttpRequest siteRequest = HttpRequest.newBuilder()
                .uri(new URI(location))
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("accept-language", "en-US,en;q=0.9")
                .header("accept-encoding", "gzip, deflate, br")
                .header("cache-control", "max-age=0")
                .header("cookie", String.format("simpalsid.auth=%s; auth=%s", simpalsid_auth, auth))
                .header("dnt", "1")
                .header("sec-fetch-dest", "document")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-site", "cross-site")
                .header("sec-fetch-user", "?1")
                .header("upgrade-insecure-requests", "1")
                .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
                .build();

        // Отправляем запрос и получаем ответ в виде массива байт
        HttpResponse<byte[]> siteResponse = client.send(siteRequest, HttpResponse.BodyHandlers.ofByteArray());

        // Выводим в консоль весь заголовок ответа на четвертый запрос
        System.out.println("\nResponse Headers (GET):");
        siteResponse.headers().map().forEach((key, values) -> {
            System.out.println(key + ": " + String.join(", ", values));
        });

        // Проверяем, сжат ли ответ
        if (siteResponse.headers().firstValue("content-encoding").orElse("").contains("gzip")) {
            // Распаковка GZIP
            InputStream inputStream = new ByteArrayInputStream(siteResponse.body());
            GZIPInputStream gzipStream = new GZIPInputStream(inputStream);

            // Чтение распакованного ответа
            String decompressedBody = new String(gzipStream.readAllBytes(), StandardCharsets.UTF_8);

            // Выводим распакованное тело ответа
            System.out.println("\nDecompressed Response Body (GET):");
            System.out.println(decompressedBody);
        } else {
            // Если ответ не сжат, просто выводим его как есть
            String responseBody = new String(siteResponse.body(), StandardCharsets.UTF_8);
            System.out.println("\nResponse Body (GET):");
            System.out.println(responseBody);
        }

    }
}

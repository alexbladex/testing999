package http.requests;
import gui.interaction.PropertyReader;
import okhttp3.*;
import okio.BufferedSource;
import okio.GzipSource;
import okio.InflaterSource;
import okio.Okio;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.zip.Inflater;

public class OkHttpClientExample {

    private static final OkHttpClient client;
    private static final CookieManager cookieManager;

    static {
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .addInterceptor(new RedirectInterceptor())
                .build();
    }

    public static void main(String[] args) throws IOException {
        String uri, user, pswd;
        uri = PropertyReader.getProperty("999Login");
        user = PropertyReader.getProperty("user");
        pswd = PropertyReader.getProperty("pswd");
        Request request;

        // 1. Open Login page
        request = new Request.Builder()
                .url(uri)
                .headers(getCommonHeaders(uri))
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Login Page is opened: " + response.code());
        }

        // 2. Login POST Entity
        FormBody formBody = new FormBody.Builder()
                .add("_xsrf", getCookieByName("_xsrf"))
                .add("redirect_url", getCookieByName("redirect_url"))
                .add("login", user)
                .add("password", pswd)
                .build();

        request = new Request.Builder()
                .url(uri)
                .post(formBody)
                .headers(getCommonHeaders(uri))
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = decompressResponse(response);
            System.out.println("Final Response: " + responseBody);
        }
    }

    private static Headers getCommonHeaders(String referer) {
        return new Headers.Builder()
                .add("accept", "*/*")
                .add("accept-language", "en-US,en;q=0.5")
                .add("accept-encoding", "gzip, deflate")
                .add("cache-control", "no-cache")
                .add("connection", "keep-alive")
                .add("referer", referer)
                .add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
                .build();
    }

    private static String getCookieByName(String name) {
        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        return cookies.stream().filter(cookie -> cookie.getName().equals(name))
                .map(HttpCookie::getValue)
                .findFirst()
                .orElse(null);
    }

    // Метод для декомпрессии ответа
    private static String decompressResponse(Response response) throws IOException {
        String contentEncoding = response.header("Content-Encoding");
        if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
            try (BufferedSource source = Okio.buffer(new GzipSource(response.body().source()))) {
                return source.readUtf8();
            }
        } else if (contentEncoding != null && contentEncoding.equalsIgnoreCase("deflate")) {
            try (BufferedSource source = Okio.buffer(new InflaterSource(response.body().source(), new Inflater(true)))) {
                return source.readUtf8();
            }
        } else {
            return response.body().string();
        }
    }

    // Логирование запросов и ответов
    static class RedirectInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);

            String location = response.header("Location");
            if (location != null) {
                location = location.replace("|", "%7C").replace(" ", "%20");
                request = request.newBuilder()
                        .url(location)
                        .build();
                response = chain.proceed(request);
            }
            return response;
        }
    }
}

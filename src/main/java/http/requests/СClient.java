package http.requests;
import gui.interaction.PropertyReader;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
public class СClient {
    public static void main(String[] args) throws Exception {
        String uri, user, pswd;
        uri = PropertyReader.getProperty("simpalsUriLogin");
        user = PropertyReader.getProperty("user");
        pswd = PropertyReader.getProperty("pswd");
        HashMap<String, String> map = null;

        // Create a custom request logging interceptor
        HttpRequestInterceptor requestLogger = (request, context) -> {
            System.out.println("Request Line: " + request.getRequestLine());
            System.out.println("Headers:");
            for (var header : request.getAllHeaders()) {
                System.out.println(header.getName() + ": " + header.getValue());
            }

            if (request instanceof HttpPost) {
                HttpPost postRequest = (HttpPost) request;
                if (postRequest.getEntity() != null) {
                    System.out.println("Body:");
                    var entity = postRequest.getEntity();
                    var body = entity.getContent().readAllBytes(); // Считываем тело запроса
                    System.out.println(new String(body)); // Логируем тело запроса
                }
            }
        };

        // Create a custom response logging interceptor
        HttpResponseInterceptor responseLogger = (response, context) -> {
            System.out.println("Response: " + response.getStatusLine());
            if (response.getEntity() != null) {
                System.out.println("Response Body: " + EntityUtils.toString(response.getEntity()));
            }
        };

        CookieStore cookieStore = new LenientCookieStore();

        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD) // Менее строгая спецификация
                .setRedirectsEnabled(true)
                .build();

        CloseableHttpClient client = HttpClients.custom()
                .addInterceptorFirst(requestLogger)  // Logs requests
                //.addInterceptorFirst(responseLogger) // Logs responses
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(requestConfig)
                .setRedirectStrategy(new CustomRedirectStrategy())
                .build();

//        HttpClientContext context = HttpClientContext.create();
//        context.setCookieStore(cookieStore);

//        CloseableHttpClient client = HttpClients.createMinimal();

        // 1. Login запрос
        HttpGet getLogin = new HttpGet(new URI(uri));
        setCommonHeaders(getLogin);
        CloseableHttpResponse getLoginResponse = client.execute(getLogin);

        // Выводим в консоль ответ
        System.out.println("Отправленный URI: " + getLogin.getURI());
        System.out.println("Response Status (GET): " + getLoginResponse.getStatusLine());
        String _xsrf = null;
        String redirect_url = null;
        for (Cookie cookie : cookieStore.getCookies()) {
            System.out.println(cookie.getName() + ": " + cookie.getValue());
            if (cookie.getName().equals("_xsrf")) _xsrf = cookie.getValue();
            if (cookie.getName().equals("redirect_url")) redirect_url = cookie.getValue();
        }
        /*String xsrf = cookieStore.getCookies().stream()
                .filter(cookie -> cookie.getName().equals("_xsrf"))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);*/

        getLoginResponse.close();

        /*Arrays.stream(getLoginResponse.getAllHeaders()).forEach(header ->
                System.out.println(header.getName() + ": " + header.getValue())
        );

        // Извлекаем значения _xsrf и redirect_url из заголовков Set-Cookie
        map = extractCookies(getLoginResponse);
        String _xsrf = map.get("_xsrf");
        String redirect_url = map.get("redirect_url");

        System.out.println("XSRF Token: " + _xsrf);
        System.out.println("Redirect URL: " + redirect_url);*/

        // 2. POST запрос
        HttpPost postLogin = new HttpPost(new URI(uri));

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("_xsrf", _xsrf));
        params.add(new BasicNameValuePair("redirect_url", redirect_url));
        params.add(new BasicNameValuePair("login", user));
        params.add(new BasicNameValuePair("password", pswd));
        postLogin.setEntity(new UrlEncodedFormEntity(params)); //automatically set (content-type: application/x-www-form-urlencoded)

        setCommonHeaders(postLogin);
        CloseableHttpResponse postLoginResponse = client.execute(postLogin);

        // Выводим в консоль ответ
        System.out.println("Отправленный URI: " + getLogin.getURI());
        System.out.println("Response Status (POST): " + postLoginResponse.getStatusLine());
        String auth = null;
        String simpalsid_auth = null;
        String utid = null;
        String tid = null;
        for (Cookie cookie : cookieStore.getCookies()) {
            System.out.println(cookie.getName() + ": " + cookie.getValue());
            if (cookie.getName().equals("auth")) auth = cookie.getValue();
            if (cookie.getName().equals("simpalsid.auth")) simpalsid_auth = cookie.getValue();
            if (cookie.getName().equals("utid")) utid = cookie.getValue();
            if (cookie.getName().equals("tid")) tid = cookie.getValue();
        }

        // Выводим ответ
        String getResponseBody = EntityUtils.toString(postLoginResponse.getEntity());
        System.out.println("GET Response Body: " + getResponseBody);

        postLoginResponse.close();
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
    private static void setCommonHeaders(HttpRequestBase request) {
        request.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        request.setHeader("accept-language", "en-US,en;q=0.9");
        request.setHeader("accept-encoding", "gzip, deflate, br");
        request.setHeader("cache-control", "max-age=0");
        request.setHeader("connection", "keep-alive");
        request.setHeader("referer", "https://simpalsid.com/user/login");
        request.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
    }
}

package http.requests;
import gui.interaction.PropertyReader;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CClient {
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

        CookieStore cookieStore = new BasicCookieStore(); //LenientCookieStore

        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD) // Менее строгая спецификация
                .setRedirectsEnabled(true)
                .setMaxRedirects(8)
                .build();

        CloseableHttpClient client = HttpClients.custom()
                .addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                    HttpHost targetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
                    if (targetHost != null) {
                        String fullUrl = targetHost.toURI() + request.getRequestLine().getUri();
                        System.out.println(request.getRequestLine().getMethod() + " " + fullUrl + " " + request.getRequestLine().getProtocolVersion());
                    } else {
                        System.out.println(request.getRequestLine());
                    }

                    for (Header header : request.getAllHeaders()) {
                        System.out.println(header.getName() + ": " + header.getValue());
                    }
                    System.out.println();
                })
                .addInterceptorLast((HttpResponseInterceptor) (response, context) -> {
                    System.out.println(response.getStatusLine());
                    for (Header header : response.getAllHeaders()) {
                        System.out.println(header.getName() + ": " + header.getValue());
                    }
                    System.out.println();
                })
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(requestConfig)
                .setRedirectStrategy(new LaxRedirectStrategy() {
                    @Override
                    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
                        String location = response.getFirstHeader("Location").getValue();
                        response.setHeader("Location", location.replace("|", "%7C").replace(" ", "%20"));
                        return super.getRedirect(request, response, context);
                    }
                })
                .build();

        // 1. Login запрос
        HttpGet getLogin = new HttpGet(new URI(uri));
        setCommonHeaders(getLogin);
        CloseableHttpResponse getLoginResponse = client.execute(getLogin);

        // Выводим в консоль ответ
        String _xsrf = null;
        String redirect_url = null;
        for (Cookie cookie : cookieStore.getCookies()) {
            System.out.println(cookie.getName() + ": " + cookie.getValue());
            if (cookie.getName().equals("_xsrf")) _xsrf = cookie.getValue();
            if (cookie.getName().equals("redirect_url")) redirect_url = cookie.getValue();
        }

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

        String auth = null;
        String simpalsid_auth = null;
        String utid = null;
        String tid = null;
        /*for (Cookie cookie : cookieStore.getCookies()) {
            System.out.println(cookie.getName() + ": " + cookie.getValue());
            if (cookie.getName().equals("auth")) auth = cookie.getValue();
            if (cookie.getName().equals("simpalsid.auth")) simpalsid_auth = cookie.getValue();
            if (cookie.getName().equals("utid")) utid = cookie.getValue();
            if (cookie.getName().equals("tid")) tid = cookie.getValue();
        }*/

        // Выводим тело
        String getResponseBody = EntityUtils.toString(postLoginResponse.getEntity());
        System.out.println("GET Response Body: " + getResponseBody);

        /////////////////////////
        getLoginResponse.close();
        postLoginResponse.close();
        client.close();
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

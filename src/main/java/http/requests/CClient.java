package http.requests;

import gui.interaction.PropertyReader;
import org.apache.http.*;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class CClient {
    public static void main(String[] args) throws Exception {
        String uri, user, pswd;
        uri = PropertyReader.getProperty("999Login");
        user = PropertyReader.getProperty("user");
        pswd = PropertyReader.getProperty("pswd");

        // Custom request logging interceptor
        HttpRequestInterceptor requestLogger = (request, context) -> {
            HttpHost targetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
            if (targetHost != null) {
                String fullUrl = targetHost.toURI() + request.getRequestLine().getUri();
                System.out.println(request.getRequestLine().getMethod() + " " + fullUrl);
            } else {
                System.out.println(request.getRequestLine());
            }
            for (Header header : request.getAllHeaders()) {
                System.out.println(header.getName() + ": " + header.getValue());
            }
            if (request instanceof HttpEntityEnclosingRequest entityRequest) {
                HttpEntity originalEntity = entityRequest.getEntity();
                if (originalEntity != null) {
                    final byte[] bodyBytes;
                    try (InputStream content = originalEntity.getContent()) {
                        bodyBytes = content.readAllBytes();
                    }
                    System.out.println("Body:");
                    System.out.println(new String(bodyBytes));
                    // ByteArrayEntity replaces the entity. If you need to log the body while preserving the original request entity, use HttpEntityWrapper.
                    entityRequest.setEntity(new ByteArrayEntity(bodyBytes, ContentType.get(originalEntity)));
                }
            }
            System.out.println();
        };

        // Custom response logging interceptor
        HttpResponseInterceptor responseLogger = (response, context) -> {
            System.out.println(response.getStatusLine());
            for (Header header : response.getAllHeaders()) {
                System.out.println(header.getName() + ": " + header.getValue());
            }
            System.out.println();
        };

        //CookieStore cookieStore = new BasicCookieStore(); //LenientCookieStore
        CookieStorage cookieStore = new CookieStorage();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD) // Менее строгая спецификация
                .setRedirectsEnabled(true)
                .setMaxRedirects(8)
                .build();

        CloseableHttpClient client = HttpClients.custom()
                .addInterceptorFirst(requestLogger)
                .addInterceptorLast(responseLogger)
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
        getLoginResponse.close();

        // 2. Login POST Entity
        HttpPost postLogin = new HttpPost(new URI(uri));

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("_xsrf", cookieStore.getCookieByName("_xsrf")));
        params.add(new BasicNameValuePair("redirect_url", cookieStore.getCookieByName("redirect_url")));
        params.add(new BasicNameValuePair("login", user));
        params.add(new BasicNameValuePair("password", pswd));
        postLogin.setEntity(new UrlEncodedFormEntity(params)); //automatically set (content-type: application/x-www-form-urlencoded)
        setCommonHeaders(postLogin);
        CloseableHttpResponse postLoginResponse = client.execute(postLogin);

        // Выводим тело
        String getResponseBody = EntityUtils.toString(postLoginResponse.getEntity());
        System.out.println("GET Response Body: " + getResponseBody);
        postLoginResponse.close();


        // 3. Open Ad Pages
        String adPages = "https://999.md/add?category=construction-and-repair&subcategory=construction-and-repair/finishing-and-facing-materials";
        HttpGet get999 = new HttpGet(new URI(adPages));
        setCommonHeaders(get999);
        get999.setHeader("Referer", "https://999.md");
        CloseableHttpResponse get999Response = client.execute(get999);

        String auth = null, simpalsid_auth = null, utid = null, tid = null;

        for (Cookie cookie : cookieStore.getCookies()) {
            System.out.println(cookie.getName() + ": " + cookie.getValue());
        }

        getResponseBody = EntityUtils.toString(get999Response.getEntity());
        // Парсинг HTML с помощью JSoup
        Document doc = Jsoup.parse(getResponseBody);
        // один из куков как input xsrf
        String xsrfValue = doc.select("input[name=_xsrf]").attr("value");
        String formIdValue = doc.select("input[name=form_id]").attr("value");

        System.out.println("XSRF Value: " + xsrfValue);
        System.out.println("Form ID Value: " + formIdValue);

        System.out.println("GET Response Body: " + getResponseBody);

        get999Response.close();

        /////////////////////////
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
package http.requests;

import gui.interaction.PropertyReader;
import org.apache.http.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CClient {
    private static CloseableHttpClient client;

    public static void main(String[] args) throws Exception {
        String uri, user, pswd;
        uri = PropertyReader.getProperty("999Login");
        user = PropertyReader.getProperty("user");
        pswd = PropertyReader.getProperty("pswd");
        CloseableHttpResponse httpResponse;
        String responseBody = null;
        HttpPost postRequest;
        Document doc;
        HttpEntity entity;
        String premoderateUrl = "https://999.md/premoderate";
        List<NameValuePair> params;

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
            /*if (request instanceof HttpEntityEnclosingRequest entityRequest) {
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
            }*/
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

        //CookieStore cookieStore = new BasicCookieStore();
        CookieStorage cookieStore = new CookieStorage();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD) // Менее строгая спецификация
                .setRedirectsEnabled(true)
                .setMaxRedirects(8)
                .build();

        client = HttpClients.custom()
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
        httpResponse = httpGet(uri, uri);
        httpResponse.close();

        // 2. Login POST Entity
        postRequest = new HttpPost(new URI(uri));
        setCommonHeaders(postRequest, uri);
        params = new ArrayList<>();
        params.add(new BasicNameValuePair("_xsrf", cookieStore.getCookieByName("_xsrf")));
        params.add(new BasicNameValuePair("redirect_url", cookieStore.getCookieByName("redirect_url")));
        params.add(new BasicNameValuePair("login", user));
        params.add(new BasicNameValuePair("password", pswd));
        postRequest.setEntity(new UrlEncodedFormEntity(params)); //automatically set (content-type: application/x-www-form-urlencoded)
        httpResponse = client.execute(postRequest);

        // Выводим тело
        responseBody = EntityUtils.toString(httpResponse.getEntity());
        System.out.printf("999 Response Body: %s", responseBody);
        httpResponse.close();


        // 3. Open Ad Pages
        String pageAd = "https://999.md/add?category=construction-and-repair&subcategory=construction-and-repair/finishing-and-facing-materials";
        String page999 = "https://999.md";
        httpResponse = httpGet(pageAd, page999);
        responseBody = EntityUtils.toString(httpResponse.getEntity());

        /*for (Cookie cookie : cookieStore.getCookies()) {
            System.out.println(cookie.getName() + ": " + cookie.getValue() + ", " + cookie.getDomain());
        }*/

        // Парсинг HTML с помощью JSoup
        doc = Jsoup.parse(responseBody);
        String xsrfValue = doc.select("input[name=_xsrf]").attr("value"); // один из куков как input xsrf
        String formIdValue = doc.select("input[name=form_id]").attr("value");

        // Выводим тело
        System.out.println("XSRF Value: " + xsrfValue);
        System.out.println("Form ID Value: " + formIdValue);
        System.out.printf("Form Response Body: %s", responseBody);
        httpResponse.close();

        // 4. Upload image
        String uploadUrl = "https://i.simpalsmedia.com/upload//?template=f2a36d5fa9db98cd70d83a1dee629306b1b683153cfcca3c6a7ed1004afd4bf0eDviK8XVZ3RiU95wJ9OLdQLWiRP5ZG1ldBn9Yc2wcvVxvlE5U6qYqEzn3WU6bQhx8R%2FCv046tzYZpIi9KNw0keOpppQr4%2F%2FAZR6NADhwfDeeEy8%2BUyXZkVP11yaDiflf2VdrvFIlC3jmt4LzTMENEj%2F0DzKA60gZq5BDqT%2FsvBk%3DgOXiQQLBUCeKJY279OJVZOI33S8jxjfmlNhzOwXt38nkGhjyWheoIAEzyLDa%2BGh4ilDxmzYp9X1Ckt8UoSEdsxX7quT84O%2ByKeKCgmBotyftkJfNAEMHLAkg7tMYW7Hvif5GZgxfl2E2zMhw6xrSDf62xzOipWGTNb5ah8HIpXI%3DUmuAAux21jkmCTB%2B9ykJQIZ%2F90Sb2nuJ6vh8e6aDTXsoNyNUBg9fZ%2ByfWfxZ1hJniR31ZNh32gEzJlyvoZ1hArRE5Vye5%2FJeROFcdC04aIlVyWud2%2FHV5ffPVIkdydL3%2BgYqllgE7ICZfv%2By88Tjz43W%2F%2Fb%2FnB0KfubkV%2BuAC3o%3DEJUJ2gnkrdt4Wxd1J09OveTEZB%2Fk7eKRJN6SuEIJpw8JVlNMp9B%2Br1ObSe6H%2F7EzPwvs%2FqHx%2B82zH8U2p580CtgJBO%2ByfPFxaV2F%2Fgfvu1WwGGSacQ%2FT7KVgOcd806NYNTdJb1pHnUUZAxrdxw5KfSg03LDxSP11LiPk2pB1Xug%3D&base64=thumb";
        postRequest = new HttpPost(new URI(uploadUrl));
        setCommonHeaders(postRequest, page999);
        FileBody fileBody = new FileBody(new File("/C:/Temp/plitka.jpg"));
        entity = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .build();
        postRequest.setEntity(entity);
        httpResponse = client.execute(postRequest);
        responseBody = EntityUtils.toString(httpResponse.getEntity());
        JSONObject jsonResponse = new JSONObject(responseBody);
        String imageFileName = jsonResponse.getJSONObject("output_metadata").getString("filename");

        // Выводим тело
        System.out.println("Image name: " + imageFileName);
        System.out.printf("Image Response Body: %s", responseBody);
        //EntityUtils.consume(postImageResponse.getEntity());
        httpResponse.close();

        // premoderate
        postRequest = new HttpPost(new URI(premoderateUrl));
        setCommonHeaders(postRequest, pageAd);
        entity = MultipartEntityBuilder.create()
                .addTextBody("_xsrf", xsrfValue, ContentType.TEXT_PLAIN )
                .addTextBody("form_id", formIdValue, ContentType.TEXT_PLAIN )
                .addTextBody("category_url", "construction-and-repair", ContentType.TEXT_PLAIN )
                .addTextBody("subcategory_url", "construction-and-repair/finishing-and-facing-materials", ContentType.TEXT_PLAIN )
                .addTextBody("offer_type", "776", ContentType.TEXT_PLAIN )
                .addTextBody("12", "плитка размер 15*15 белая turkey", ContentType.TEXT_PLAIN.withCharset("UTF-8") )
                .addTextBody("13", "Настенная плитка кафель 15*15 цвет белая турция. 200 л/м2. Минимальный заказ 2 метра", ContentType.TEXT_PLAIN.withCharset("UTF-8") )
                .addTextBody("1404", "", ContentType.TEXT_PLAIN )
                .addTextBody("7", "12900", ContentType.TEXT_PLAIN )
                .addTextBody("2", "200", ContentType.TEXT_PLAIN )
                .addTextBody("2_unit", "mdl", ContentType.TEXT_PLAIN )
                .addTextBody("1640", "", ContentType.TEXT_PLAIN )
                .addTextBody("686", "21099", ContentType.TEXT_PLAIN )
                .addTextBody("5", "12869", ContentType.TEXT_PLAIN )
                .addTextBody("14", imageFileName, ContentType.TEXT_PLAIN )
                .addTextBody("video", "", ContentType.TEXT_PLAIN )
                .addTextBody("16", "37379169100", ContentType.TEXT_PLAIN )
                .addTextBody("16", "37379544975", ContentType.TEXT_PLAIN )
                .addTextBody("country_prefix", "373", ContentType.TEXT_PLAIN )
                .addTextBody("number", "", ContentType.TEXT_PLAIN )
                .addTextBody("package_name", "basic", ContentType.TEXT_PLAIN )
                .build();
        postRequest.setEntity(entity);
        httpResponse = client.execute(postRequest);

        // Выводим тело
        responseBody = EntityUtils.toString(httpResponse.getEntity());
        System.out.printf("premoderate Response Body: %s", responseBody);
        httpResponse.close();

        // 5. Agree checkbox
        String agreeUrl = "https://999.md/ad_price?subcategory=1238&ad_id=&has_delivery=&package_name=basic&phone=37379169100";
        httpResponse = httpGet(agreeUrl, pageAd);
        httpResponse.close();

        // premoderate
        postRequest = new HttpPost(new URI(premoderateUrl));
        setCommonHeaders(postRequest, pageAd);
        entity = MultipartEntityBuilder.create()
                .addTextBody("_xsrf", xsrfValue, ContentType.TEXT_PLAIN )
                .addTextBody("form_id", formIdValue, ContentType.TEXT_PLAIN )
                .addTextBody("category_url", "construction-and-repair", ContentType.TEXT_PLAIN )
                .addTextBody("subcategory_url", "construction-and-repair/finishing-and-facing-materials", ContentType.TEXT_PLAIN )
                .addTextBody("offer_type", "776", ContentType.TEXT_PLAIN )
                .addTextBody("12", "плитка размер 15*15 белая turkey", ContentType.TEXT_PLAIN.withCharset("UTF-8") )
                .addTextBody("13", "Настенная плитка кафель 15*15 цвет белая турция. 200 л/м2. Минимальный заказ 2 метра", ContentType.TEXT_PLAIN.withCharset("UTF-8") )
                .addTextBody("1404", "", ContentType.TEXT_PLAIN )
                .addTextBody("7", "12900", ContentType.TEXT_PLAIN )
                .addTextBody("2", "200", ContentType.TEXT_PLAIN )
                .addTextBody("2_unit", "mdl", ContentType.TEXT_PLAIN )
                .addTextBody("1640", "", ContentType.TEXT_PLAIN )
                .addTextBody("686", "21099", ContentType.TEXT_PLAIN )
                .addTextBody("5", "12869", ContentType.TEXT_PLAIN )
                .addTextBody("14", imageFileName, ContentType.TEXT_PLAIN )
                .addTextBody("video", "", ContentType.TEXT_PLAIN )
                .addTextBody("16", "37379169100", ContentType.TEXT_PLAIN )
                .addTextBody("country_prefix", "373", ContentType.TEXT_PLAIN )
                .addTextBody("number", "", ContentType.TEXT_PLAIN )
                .addTextBody("package_name", "basic", ContentType.TEXT_PLAIN )
                .addTextBody("agree", "1", ContentType.TEXT_PLAIN)
                .build();
        postRequest.setEntity(entity);
        httpResponse = client.execute(postRequest);

        // Выводим тело
        responseBody = EntityUtils.toString(httpResponse.getEntity());
        System.out.printf("premoderate Response Body: %s", responseBody);
        httpResponse.close();

        // 6. Send Ad Form
        postRequest = new HttpPost(new URI(pageAd));
        setCommonHeaders(postRequest, pageAd);
        params = new ArrayList<>();
        params.add(new BasicNameValuePair("_xsrf", xsrfValue));
        params.add(new BasicNameValuePair("form_id", formIdValue));
        params.add(new BasicNameValuePair("category_url", "construction-and-repair"));
        params.add(new BasicNameValuePair("subcategory_url", "construction-and-repair/finishing-and-facing-materials"));
        params.add(new BasicNameValuePair("offer_type", "776"));
        params.add(new BasicNameValuePair("12", "плитка размер 15*15 белая turkey"));
        params.add(new BasicNameValuePair("13", "Настенная плитка кафель 15*15 цвет белая турция. 200 л/м2. Минимальный заказ 2 метра"));
        params.add(new BasicNameValuePair("1404", ""));
        params.add(new BasicNameValuePair("7", "12900"));
        params.add(new BasicNameValuePair("2", "200"));
        params.add(new BasicNameValuePair("2_unit", "mdl"));
        params.add(new BasicNameValuePair("1640", ""));
        params.add(new BasicNameValuePair("686", "21099"));
        params.add(new BasicNameValuePair("5", "12869"));
        params.add(new BasicNameValuePair("14", imageFileName));
        params.add(new BasicNameValuePair("file", ""));
        params.add(new BasicNameValuePair("video", ""));
        params.add(new BasicNameValuePair("16", "37379169100"));
        params.add(new BasicNameValuePair("country_prefix", "373"));
        params.add(new BasicNameValuePair("number", ""));
        params.add(new BasicNameValuePair("package_name", "basic"));
        params.add(new BasicNameValuePair("agree", "1"));
        postRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        httpResponse = client.execute(postRequest);

        // Выводим тело
        responseBody = EntityUtils.toString(httpResponse.getEntity());
        System.out.printf("Response Body: %s", responseBody);
        httpResponse.close();

        /////////////////////////
        client.close();
    }
    public static CloseableHttpResponse httpGet(String url, String referer) throws URISyntaxException, IOException {
        HttpGet getRequest = new HttpGet(new URI(url));
        setCommonHeaders(getRequest, referer);

        return client.execute(getRequest);
    }
    private static void setCommonHeaders(HttpRequestBase request, String referer) {
        request.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        request.setHeader("accept-language", "en-US,en;q=0.9");
        request.setHeader("accept-encoding", "gzip, deflate, br");
        request.setHeader("cache-control", "no-cache");
        request.setHeader("connection", "keep-alive");
        request.setHeader("referer", referer);
        request.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
    }
}
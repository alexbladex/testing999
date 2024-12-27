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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CClient {
    private static CloseableHttpClient client;

    public static void main(String[] args) throws Exception {
        String uri, user, pswd, responseBody, xsrfValue, formIdValue, imageFileName;
        uri = PropertyReader.getProperty("999Login");
        user = PropertyReader.getProperty("user");
        pswd = PropertyReader.getProperty("pswd");
        Document doc;

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

        // 1. Open Login page
        httpGet(uri, uri);

        // 2. Login POST Entity
        List<NameValuePair> loginBody = List.of(
                new BasicNameValuePair("_xsrf", cookieStore.getCookieByName("_xsrf")),
                new BasicNameValuePair("redirect_url", cookieStore.getCookieByName("redirect_url")),
                new BasicNameValuePair("login", user),
                new BasicNameValuePair("password", pswd)
        );
        httpPost(uri, uri, loginBody, false);

        // 3. Open Ad Pages
        String pageAd = "https://999.md/add?category=construction-and-repair&subcategory=construction-and-repair/finishing-and-facing-materials";
        String page999 = "https://999.md";
        responseBody = httpGet(pageAd, page999);

        // Парсинг HTML с помощью JSoup
        doc = Jsoup.parse(responseBody);
        xsrfValue = doc.select("input[name=_xsrf]").attr("value"); // один из куков как input xsrf
        formIdValue = doc.select("input[name=form_id]").attr("value");
        System.out.println("XSRF Value: " + xsrfValue);
        System.out.println("Form ID Value: " + formIdValue);

        // 4. Upload image
        String uploadUrl = "https://i.simpalsmedia.com/upload//?template=f2a36d5fa9db98cd70d83a1dee629306b1b683153cfcca3c6a7ed1004afd4bf0eDviK8XVZ3RiU95wJ9OLdQLWiRP5ZG1ldBn9Yc2wcvVxvlE5U6qYqEzn3WU6bQhx8R%2FCv046tzYZpIi9KNw0keOpppQr4%2F%2FAZR6NADhwfDeeEy8%2BUyXZkVP11yaDiflf2VdrvFIlC3jmt4LzTMENEj%2F0DzKA60gZq5BDqT%2FsvBk%3DgOXiQQLBUCeKJY279OJVZOI33S8jxjfmlNhzOwXt38nkGhjyWheoIAEzyLDa%2BGh4ilDxmzYp9X1Ckt8UoSEdsxX7quT84O%2ByKeKCgmBotyftkJfNAEMHLAkg7tMYW7Hvif5GZgxfl2E2zMhw6xrSDf62xzOipWGTNb5ah8HIpXI%3DUmuAAux21jkmCTB%2B9ykJQIZ%2F90Sb2nuJ6vh8e6aDTXsoNyNUBg9fZ%2ByfWfxZ1hJniR31ZNh32gEzJlyvoZ1hArRE5Vye5%2FJeROFcdC04aIlVyWud2%2FHV5ffPVIkdydL3%2BgYqllgE7ICZfv%2By88Tjz43W%2F%2Fb%2FnB0KfubkV%2BuAC3o%3DEJUJ2gnkrdt4Wxd1J09OveTEZB%2Fk7eKRJN6SuEIJpw8JVlNMp9B%2Br1ObSe6H%2F7EzPwvs%2FqHx%2B82zH8U2p580CtgJBO%2ByfPFxaV2F%2Fgfvu1WwGGSacQ%2FT7KVgOcd806NYNTdJb1pHnUUZAxrdxw5KfSg03LDxSP11LiPk2pB1Xug%3D&base64=thumb";
        responseBody = httpPost(uploadUrl, page999, "C:/Temp/plitka.jpg");

        // Парсинг JSON
        JSONObject jsonResponse = new JSONObject(responseBody);
        imageFileName = jsonResponse.getJSONObject("output_metadata").getString("filename");
        System.out.println("Image name: " + imageFileName);

        // Create Params
        List<NameValuePair> params = createParams(xsrfValue, formIdValue, imageFileName);
        List<NameValuePair> paramsWithoutAgree = new ArrayList<>(params);
        paramsWithoutAgree.removeIf(param -> param.getName().equals("agree"));

        // premoderate
        /*postRequest = new HttpPost(new URI(premoderateUrl));
        setCommonHeaders(postRequest, pageAd);
        postRequest.setEntity(buildEntity(paramsWithoutAgree, true));
        httpResponse = client.execute(postRequest);
        EntityUtils.consume(httpResponse.getEntity());
        httpResponse.close();*/

        // 5. Agree checkbox
        String agreeUrl = "https://999.md/ad_price?subcategory=1238&ad_id=&has_delivery=&package_name=basic&phone=37379169100";
        httpGet(agreeUrl, pageAd);

        // premoderate
        String premoderateUrl = "https://999.md/premoderate";
        httpPost(premoderateUrl, pageAd, params, true);


        // 6. Send Ad Form
        responseBody = httpPost(pageAd, pageAd, params, false);
        doc = Jsoup.parse(responseBody);
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

        /////////////////////////
        client.close();
    }
    private static String httpGet(String url, String referer) throws URISyntaxException, IOException {
        HttpGet getRequest = new HttpGet(new URI(url));
        setCommonHeaders(getRequest, referer);
        return executeRequest(getRequest);
    }
    private static String httpPost(String url, String referer, List<NameValuePair> params, boolean isMultipart) throws URISyntaxException, IOException {
        HttpPost postRequest = new HttpPost(new URI(url));
        setCommonHeaders(postRequest, referer);
        postRequest.setEntity(buildEntity(params, isMultipart)); //automatically set (content-type: application/x-www-form-urlencoded)
        return executeRequest(postRequest);
    }
    private static String httpPost(String url, String referer, String path) throws URISyntaxException, IOException {
        HttpPost postRequest = new HttpPost(new URI(url));
        setCommonHeaders(postRequest, referer);
        FileBody fileBody = new FileBody(new File(path));
        HttpEntity entity = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .build();
        postRequest.setEntity(entity);
        return executeRequest(postRequest);
    }
    private static String executeRequest(HttpRequestBase request) {
        try (CloseableHttpResponse httpResponse = client.execute(request)) {
            String responseBody = EntityUtils.toString(httpResponse.getEntity());
            System.out.printf("Response Body: %s%n", responseBody);
            return responseBody;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    private static void setCommonHeaders(HttpRequestBase request, String referer) {
        request.setHeader("accept", "*/*");
        request.setHeader("accept-language", "en-US,en;q=0.5");
        request.setHeader("accept-encoding", "gzip, deflate");
        request.setHeader("cache-control", "no-cache");
        request.setHeader("connection", "keep-alive");
        request.setHeader("referer", referer);
        request.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
    }
    private static HttpEntity buildEntity(List<NameValuePair> params, boolean isMultipart) throws UnsupportedEncodingException {
        if (isMultipart) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (NameValuePair param : params) {
                builder.addTextBody(param.getName(), param.getValue(), ContentType.TEXT_PLAIN.withCharset("UTF-8"));
            }
            return builder.build();
        } else {
            return new UrlEncodedFormEntity(params, "UTF-8");
        }
    }
    private static List<NameValuePair> createParams(String xsrfValue, String formIdValue, String imageFileName) {
        return List.of(
                new BasicNameValuePair("_xsrf", xsrfValue),
                new BasicNameValuePair("form_id", formIdValue),
                new BasicNameValuePair("category_url", "construction-and-repair"),
                new BasicNameValuePair("subcategory_url", "construction-and-repair/finishing-and-facing-materials"),
                new BasicNameValuePair("offer_type", "776"),
                new BasicNameValuePair("12", "плитка размер 15*15 белая turkey"),
                new BasicNameValuePair("13", "Настенная плитка кафель 15*15 цвет белая турция. 200 л/м2. Минимальный заказ 2 метра"),
                new BasicNameValuePair("1404", ""),
                new BasicNameValuePair("7", "12900"),
                new BasicNameValuePair("2", "200"),
                new BasicNameValuePair("2_unit", "mdl"),
                new BasicNameValuePair("1640", ""),
                new BasicNameValuePair("686", "21099"),
                new BasicNameValuePair("5", "12869"),
                new BasicNameValuePair("14", imageFileName),
                new BasicNameValuePair("file", ""),
                new BasicNameValuePair("video", ""),
                new BasicNameValuePair("16", "37379169100"),
                new BasicNameValuePair("country_prefix", "373"),
                new BasicNameValuePair("number", ""),
                new BasicNameValuePair("package_name", "basic"),
                new BasicNameValuePair("agree", "1")
        );
    }
}
class CookieStorage extends BasicCookieStore {

    public String getCookieByName(String cookieName) {
        return this.getCookies().stream()
                .filter(cookie -> cookie.getName().equals(cookieName))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public String getCookieByDomainAndName(String domain, String cookieName) {
        return this.getCookies().stream()
                .filter(cookie -> cookie.getDomain().equals(domain) && cookie.getName().equals(cookieName))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public List<Cookie> getCookiesByDomain(String domain) {
        return this.getCookies().stream()
                .filter(cookie -> cookie.getDomain().equals(domain))
                .collect(Collectors.toList());
    }
}
package http.requests;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import gui.interaction.PropertyReader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HtmlUnitExample {
    public static void main(String[] args) {
        String uri, user, pswd, responseBody, imageFileName;
        uri = PropertyReader.getProperty("999Login");
        user = PropertyReader.getProperty("user");
        pswd = PropertyReader.getProperty("pswd");

        Logger logger = Logger.getLogger("com.gargoylesoftware.htmlunit");
        logger.setLevel(Level.SEVERE);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.SEVERE); // Устанавливаем уровень выводимых сообщений
        logger.addHandler(consoleHandler);

        // Настройка клиента (WebClient синхронный клиент)
        WebClient client = new WebClient(BrowserVersion.BEST_SUPPORTED);
        client.getOptions().setRedirectEnabled(true);
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());

        // Установка куков
        CookieManager cookieManager = client.getCookieManager();
        cookieManager.setCookiesEnabled(true);

        // Кастомный WebConnection для логирования запросов и ответов
        client.setWebConnection(new WebConnectionWrapper(client) {
            @Override
            public WebResponse getResponse(WebRequest request) throws IOException {
                String requestUrl = request.getUrl().toString();
                if (!requestUrl.contains("999.md") && !requestUrl.contains("simpalsid.com")) {
                    System.out.println("Blocked Request: " + request.getUrl());
                    return new WebResponse(
                            new WebResponseData(
                                    "Blocked".getBytes(), 403, "Forbidden", new ArrayList<>()
                            ), request, 0
                    );
                }
                System.out.println("Request: " + request.getUrl());
                WebResponse response = super.getResponse(request);
                int statusCode = response.getStatusCode();
                System.out.println("Response Code: " + statusCode);

                // Обработка редиректов
                while (isRedirect(statusCode)) {
                    String location = response.getResponseHeaderValue("Location");
                    if (location == null) break;
                    location = location.replace("|", "%7C").replace(" ", "%20");
                    URL newUrl = new URL(request.getUrl(), location);

                    request = new WebRequest(newUrl, HttpMethod.GET);
                    setCommonHeaders(request, uri);
                    System.out.println("Redirected to: " + newUrl);

                    response = super.getResponse(request);
                    System.out.println("Redirected Code: " + response.getStatusCode());

                    boolean logRequest = false;
                    if (logRequest) {
                        logHeaders("Request Headers", mapToList(request.getAdditionalHeaders()));
                        logHeaders("Response Headers", response.getResponseHeaders());
                        //System.out.println("Response Content: " + response.getContentAsString());
                    }
                }
                return response;
            }
        });

        // 1. Open Login page
        try {
            HtmlPage page = client.getPage(uri);
            System.out.println("Login Page is opened: " + page.getTitleText());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Login POST Entity
        try {
            WebRequest request = new WebRequest(new URL(uri), HttpMethod.POST);
            request.setRequestParameters(List.of(
                    new NameValuePair("_xsrf", getCookieByName(cookieManager, "_xsrf")),
                    new NameValuePair("redirect_url", getCookieByName(cookieManager, "redirect_url")),
                    new NameValuePair("login", user),
                    new NameValuePair("password", pswd)
            ));
            request.setAdditionalHeader("Content-Type", "application/x-www-form-urlencoded");
            setCommonHeaders(request, uri);

            HtmlPage page = client.getPage(request);
            System.out.println("Final Response: " + page.getWebResponse().getContentAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Open Main page
        try {
            HtmlPage page = client.getPage(uri);
            // Извлечение значений с помощью XPath
            HtmlInput xsrfInput = (HtmlInput) page.getFirstByXPath("//input[@name='_xsrf']");
            HtmlInput formIdInput = (HtmlInput) page.getFirstByXPath("//input[@name='form_id']");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Закрываем клиент
        client.close();
    }
    private static void setCommonHeaders(WebRequest request, String referer) {
        request.setAdditionalHeader("accept", "*/*");
        request.setAdditionalHeader("accept-language", "en-US,en;q=0.5");
        request.setAdditionalHeader("accept-encoding", "gzip, deflate");
        request.setAdditionalHeader("cache-control", "no-cache");
        request.setAdditionalHeader("connection", "keep-alive");
        request.setAdditionalHeader("referer", referer);
        request.setAdditionalHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
    }
    private static String getCookieByName(CookieManager cookieManager, String name) {
        for (Cookie cookie : cookieManager.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }
    private static boolean isRedirect(int statusCode) {
        return statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 307 || statusCode == 308;
    }
    private static void logHeaders(String title, List<NameValuePair> headers) {
        System.out.println(title + ":");
        for (NameValuePair header : headers) {
            System.out.println(header.getName() + ": " + header.getValue());
        }
        System.out.println();
    }
    private static List<NameValuePair> mapToList(Map<String, String> map) {
        List<NameValuePair> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            list.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        return list;
    }
}

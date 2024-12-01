package http.requests;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.*;

import java.util.List;
import java.util.stream.Collectors;

public class CookieStorage extends BasicCookieStore {

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

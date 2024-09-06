package gui.interaction;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class DriverFactory {
    public static WebDriver init(String uri) {
        //multithreaded calling is available at class level. cos we are using getClassName()
        String id = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getName(); //+ System.currentTimeMillis();
        WebDriver driver = createDriver(id);
        //driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        //driver.manage().deleteAllCookies();
        //driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        driver.get(uri);
        return driver;
    }
    public static WebDriver init() {
        //multithreaded calling is available at class level. cos we are using getClassName()
        String id = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getName(); //+ System.currentTimeMillis();
        return createDriver(id);
    }
    private static WebDriver createDriver(String id) {
        //portable chrome name should be chrome.exe otherwise is need setBinary
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-data-dir=d:/Program Files/ChromePortable/Data/profile." + id);
        options.addArguments("--no-default-browser-check");
        options.addArguments("--no-first-run");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-default-apps");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-features=WebBluetooth");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--headless");
        return new ChromeDriver(options);
    }
}

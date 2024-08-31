package gui.interaction;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class DriverFactory {
    public static WebDriver init(String uri) {
        WebDriver driver = createDriver();
        //driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        //driver.manage().deleteAllCookies();
        //driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        driver.get(uri);
        return driver;
    }
    public static WebDriver init() {
        return createDriver();
    }
    private static WebDriver createDriver() {
        //portable chrome name should be chrome.exe otherwise is need setBinary
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-data-dir=d:/Program Files/ChromePortable/Data/profile/");
        options.addArguments("--disable-features=WebBluetooth");
        options.addArguments("--no-default-browser-check");
        options.addArguments("--no-first-run");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-default-apps");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--headless");
        return new ChromeDriver(options);
    }
}

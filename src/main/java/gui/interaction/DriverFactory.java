package gui.interaction;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class DriverFactory {
    private static final String tempProfile = createProfileDir();
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final ConcurrentHashMap<Thread, WebDriver> drivers = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);
    public static WebDriver getDriver(int port) throws MalformedURLException {
        if (driver.get() == null) {
            String hubUrl = "http://192.168.100.9:" + port + "/wd/hub";
            ChromeOptions options = new ChromeOptions();
//            DesiredCapabilities options = new DesiredCapabilities();
            options.addArguments("--headless");
            driver.set(new RemoteWebDriver(new URL(hubUrl), options));
        }
        return driver.get();
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }
    public static String createProfileDir() {
        String chromePath = Arrays.stream(System.getenv("PATH").split(";"))
                .filter(path -> path.contains("Chrome"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Chrome is missing from PATH!"));
        return chromePath += "Data/TempProfile/";
    }
    public static synchronized WebDriver init() {
        return init(null);
    }
    public static synchronized WebDriver init(String uri) {
        logger.info("Initializing WebDriver for thread: {}", Thread.currentThread().getId());
        //String id = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getName();
        String id = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames -> frames
                        .map(StackWalker.StackFrame::getClassName)
                        .filter(className -> !className.equals(DriverFactory.class.getName())) // Finding the external caller Class
                        .map(className -> className.substring(className.lastIndexOf('.') + 1)) // Extracting the class name
                        .findFirst()
                        .orElse("UnknownCaller"));
        WebDriver driver = createDriver(tempProfile + id);
        //driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        if (uri != null) driver.get(uri);
        drivers.put(Thread.currentThread(), driver);
        return driver;
    }
    private static WebDriver createDriver(String profilePath) {
        //portable chrome name should be chrome.exe otherwise is need setBinary
        //https://support.google.com/chrome/answer/114662

        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-data-dir=" + profilePath);
        options.addArguments("--ignore-certificate-errors");
//        options.addArguments("--no-default-browser-check");
//        options.addArguments("--no-first-run");
//        options.addArguments("--enable-precise-memory-info");
//        options.addArguments("--disable-extensions");
//        options.addArguments("--disable-default-apps");
//        options.addArguments("--disable-search-engine-choice-screen");
//        options.addArguments("--disable-features=WebBluetooth,ThirdPartyCookies");
//        options.addArguments("--disable-features=OptimizationGuideModelDownloading,OptimizationHintsFetching,OptimizationTargetPrediction,OptimizationHints");
//        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu"); // Отключение GPU (рекомендуется для headless)
        options.addArguments("--headless");
        options.addArguments("--remote-debugging-port=9222"); //если в user-data-dir использовать родную папку профиля а не временную то non-headless режим будет работать и без debugging-port

        return new ChromeDriver(options);
    }
    public static synchronized void close(){
        WebDriver driver = drivers.remove(Thread.currentThread());
        if (driver != null) {
            driver.quit();
            logger.info("Closing WebDriver for thread: {}", Thread.currentThread().getId());
        }
        if (drivers.isEmpty()) {
            File path = new File(tempProfile);
            delProfile(path);
            logger.info("Deleting contents of directory: {}", tempProfile);
        }
    }
    private static void delProfile(File directory) {
        if (directory.exists()) {
            File[] allContents = directory.listFiles();
            if (allContents != null) {
                for (File file : allContents) {
                    if (file.isDirectory()) {
                        delProfile(file);
                        if (!file.delete()) logger.warn("Failed to delete directory: {}", file.getAbsolutePath());
                    } else {
                        //if (!file.delete()) logger.warn("Failed to delete file: {}", file.getAbsolutePath(), new Exception("Failed to delete file"));
                        file.delete();
                    }
                }
            }
        } else logger.warn("Directory does not exist: {}", directory.getAbsolutePath());
    }
}

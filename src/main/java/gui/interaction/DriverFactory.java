package gui.interaction;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class DriverFactory {
    private static final File tempDir = new File("d:/Program Files/ChromePortable/Data/tempProfile");
    private static final ConcurrentHashMap<Thread, WebDriver> drivers = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);
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
        WebDriver driver = createDriver(id);
        //driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        if (uri != null) driver.get(uri);
        drivers.put(Thread.currentThread(), driver);
        return driver;
    }
    public static synchronized WebDriver init() {
        return init(null);
    }
    private static WebDriver createDriver(String id) {
        //portable chrome name should be chrome.exe otherwise is need setBinary
        //https://support.google.com/chrome/answer/114662

        String profilePath = createProfileDir(tempDir + File.separator + id);
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-data-dir=" + profilePath);
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--no-default-browser-check");
        options.addArguments("--no-first-run");
//        options.addArguments("--enable-precise-memory-info");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-default-apps");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-search-engine-choice-screen");
        options.addArguments("--disable-features=WebBluetooth,ThirdPartyCookies");
//        options.addArguments("--disable-features=OptimizationGuideModelDownloading,OptimizationHintsFetching,OptimizationTargetPrediction,OptimizationHints");
//        options.addArguments("--disable-dev-shm-usage");
        if (false) options.addArguments("--headless");
        else options.addArguments("--remote-debugging-port=9222"); //если в user-data-dir использовать родную папку профиля а не временную то non-headless режим будет работать и без debugging-port

        return new ChromeDriver(options);
    }
    private static String createProfileDir(String path) {
        File profileDir = new File(path);

        if (!profileDir.getParentFile().exists()) {
            if (!profileDir.getParentFile().mkdirs()) throw new RuntimeException("Failed to create parent folder: " + profileDir.getParentFile());
        }

        if (!profileDir.exists()) {
            if (!profileDir.mkdirs()) throw new RuntimeException("Failed to create profile folder: " + profileDir);
            try {
                Runtime.getRuntime().exec("icacls \"" + profileDir.getAbsolutePath() + "\" /grant Everyone:(OI)(CI)F");
                logger.info("Granted full permissions to: {}", profileDir);
            } catch (IOException e) {
                logger.warn("Failed to grant full permissions to: {}", profileDir, e);
            }
        }
        return profileDir.getAbsolutePath();
    }
    public static synchronized void close(){
        WebDriver driver = drivers.remove(Thread.currentThread());
        if (driver != null) {
            driver.quit();
            logger.info("Closing WebDriver for thread: {}", Thread.currentThread().getId());
        }
        if (drivers.isEmpty()) {
            delProfile(tempDir);
            logger.info("Deleting contents of directory: {}", tempDir);
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

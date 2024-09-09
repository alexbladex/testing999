package gui.interaction;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentDriverFactory {
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, WebDriver> drivers = new ConcurrentHashMap<>();
    private static final AtomicInteger activeDriversCount = new AtomicInteger(0);
    private static final String profilePath = "path/to/your/profile"; // Замените на реальный путь

    public static WebDriver init() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");
            ChromeOptions options = new ChromeOptions();
            // ... ваши опции
            driver = new ChromeDriver(options);
            driverThreadLocal.set(driver);
            drivers.put(Thread.currentThread().getName(), driver);
            activeDriversCount.incrementAndGet();
        }
        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
            drivers.remove(Thread.currentThread().getName());
            if (activeDriversCount.decrementAndGet() == 0) {
                // Удалить временную папку профиля
                deleteProfile(profilePath);
            }
        }
    }

    private static void deleteProfile(String profilePath) {
        // Логика удаления профиля
    }
}
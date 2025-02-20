package gui.interaction;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.UUID;

public class ChromeHeadlessTest {

    public static void main(String[] args) {
        // Указываем путь к ChromeDriver (если он не добавлен в PATH)
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");

        // Получаем путь к Chrome из PATH
        String chromePath = Arrays.stream(System.getenv("PATH").split(";"))
                .filter(path -> path.contains("Chrome"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Chrome is missing from PATH!"));
        chromePath += "Data/TempProfile/";

        // Генерируем уникальный UUID для имени папки профиля
        String profilePath = chromePath + UUID.randomUUID().toString();

        // Настройка ChromeOptions
        ChromeOptions options = new ChromeOptions();
//        options.setBinary("C:/path/to/chrome.exe");
        options.addArguments("--headless=new");
//        options.addArguments("--disable-gpu"); // Отключение GPU (рекомендуется для headless)
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--user-data-dir=" + profilePath);

        // Инициализация WebDriver с настройками
        WebDriver driver = new ChromeDriver(options);

        try {
            // Получаем Capabilities для доступа к информации о браузере
            if (driver instanceof RemoteWebDriver) {
                String browserVersion = ((RemoteWebDriver) driver).getCapabilities().getBrowserVersion();
                System.out.println("Версия Chrome: " + browserVersion);
            }
            // Открываем страницу Google
            driver.get("https://www.google.com");

            // Получаем заголовок страницы
            String pageTitle = driver.getTitle();

            // Выводим заголовок в консоль
            System.out.println("Заголовок страницы: " + pageTitle);
            System.out.println("Папка профиля: " + profilePath);
        } finally {
            // Закрываем браузер
            driver.quit();
            Path profileDir = Paths.get(profilePath);
            try {
                Files.walk(profileDir)
                        .sorted((a, b) -> b.compareTo(a)) // Удаляем вложенные файлы и папки
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (Exception e) {
                                System.err.println("Ошибка при удалении: " + path); // if (Files.isDirectory(path))
                            }
                        });
                System.out.println("Папка профиля удалена: " + profilePath);
            } catch (Exception e) {
                System.err.println("Ошибка при удалении профиля: " + e.getMessage());
            }
        }
    }
}
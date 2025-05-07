package gui.interaction;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Actions actions;
    protected Select select;
    protected static BasePage currentPage;
    By anchor = By.xpath("(//a[@data-test-id='recommended-item']//img)[1]");
    By frame = By.xpath("");
    By script_topbar = By.xpath("");
    By buttonLang = By.xpath("//div[@data-sentry-component='ChangeLangButton']/div[1]/div[1]");
    By buttonRu = By.xpath("//button[@data-lang='ru']");
    By buttonRo = By.xpath("//button[@data-lang='ro']");
    By activeLang = By.xpath("/html[@data-sentry-component]");
    By notActiveLang = By.xpath("//div[@data-sentry-component='ChangeLangButton']/div[2]/span/button");
    By categories = By.xpath("//button[@id='js-categories-toggle']");
    By add_ad = By.xpath("//*[@id='anchor--items']/div[2]/div/div/div[3]/a");
    By cabinet = By.xpath("//*[@id='anchor--items']/div[1]/div/div[2]/div/div/div/img");
    By login = By.xpath("//div[@data-sentry-component='HeaderTopSection']//button[@data-test-id='login']");
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.actions = new Actions(driver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        currentPage = this;
    }
    public enum AdType {
        SELL, BUY
    }
    public By getAnchor() { return anchor; }
    public void setAnchor(By anchor) { this.anchor = anchor; }
    protected void mouseOver(By element) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(element));
        actions.moveToElement(driver.findElement(element)).perform();
    }
    protected void scrollTo(By element) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(element));
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", driver.findElement(element));
    }
    protected void scrollTo(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
        ((JavascriptExecutor)driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'auto', block: 'center', inline: 'center'});", element
        );
    }
    protected void clickTo(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", element);
    }
    public boolean isCabinetDisplayed(){
        boolean b = isElementVisible(cabinet, 2);
        if (Config.debug) System.out.println("Personal Cabinet is visible: " + b);
        return b;
    }
    public boolean isLoginDisplayed(){
        boolean b = isElementVisible(login);
        if (Config.debug) System.out.println("Login button is visible: " + b);
        return b;
    }
    public boolean isLanguageDisplayed(){
//        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
        boolean b = isElementVisible(buttonLang);
        if (Config.debug) System.out.println("Language button is visible: " + b);
//        driver.switchTo().defaultContent();
        return b;
    }
    public String currentLang(){
//        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
//        String lang = driver.findElement(activeLang).getAttribute("data-lang");
//        driver.switchTo().defaultContent();
//        return lang == LangCode.RO.getCode() ? LangCode.RU.getCode() : lang;
        return driver.findElement(activeLang).getAttribute("lang");
    }
    public void changeLang(LangCode newLang) {
//        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
        WebElement button = driver.findElement(buttonLang);
        WebElement secondButton = driver.findElement(notActiveLang);
        String lang = currentLang();
        if (Config.debug) System.out.println("Current lang: " + lang);
        if (!newLang.getCode().equals(lang)) {
//            mouseOver(buttonLang);
            scrollTo(button);
            button.click();
            scrollTo(secondButton);
            secondButton.click();
            wait.until(ExpectedConditions.invisibilityOf(secondButton));
            wait.until(ExpectedConditions.attributeToBe(activeLang, "lang", newLang.getCode()));
            lang = currentLang();
            if (Config.debug) System.out.println("Lang is changed: " + lang);
        }
//        driver.switchTo().defaultContent();
    }
    protected boolean isElementPresent(By element) {
        try {
            Thread.sleep(200);
            return driver.findElement(element).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException | InterruptedException e) {
            return false;
        }
    }
    protected boolean isElementVisible(By element) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(element));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    protected boolean isElementVisible(By element, int sec_timeout) {
        try {
            WebDriverWait waitT = new WebDriverWait(driver, Duration.ofSeconds(sec_timeout));
            waitT.until(ExpectedConditions.visibilityOfElementLocated(element));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    protected void initializeSelect(WebElement element) {
        this.select = new Select(element);
    }
    protected void selectByValue(String val) {
        if (this.select != null) {
            this.select.selectByValue(val);
        } else {
            throw new IllegalStateException("Select not initialized");
        }
    }
    public void openLoginPage() {
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(login));
        if (Config.debug) System.out.println("Login: " + loginButton.getText());
        loginButton.click();
        wait.until(ExpectedConditions.invisibilityOf(loginButton));
    }
    public boolean tryHttpRequest(String currentUrl) {
        if (!currentUrl.startsWith("http")) return true; // Skip non-HTTP URLs
        try {
            URL url = new URL(currentUrl);
            if (!url.getHost().endsWith("999.md")) return true; // Skipping non-999.md URL
            if (url.getPath().equals("/") || url.getPath().isEmpty()) return true; // URL is just the domain with a trailing slash

            System.out.println("Starting HTTP request for URL: " + currentUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();

            // Handle redirects
            if (responseCode >= 300 && responseCode < 400) {
                String location = connection.getHeaderField("Location");
                System.out.println("Redirect to: " + location);
                // Resolve relative URL
                if (location != null) {
                    location = location.startsWith("http") ? location : new URL(new URL(currentUrl), location).toString();
                    return tryHttpRequest(location); // Retry with the new URL
                }
            }

            boolean success = responseCode == HttpURLConnection.HTTP_OK;
            System.out.println(success ? "SUCCESS" : "FAILURE");
            return success;

        } catch (IOException e) {
            System.out.println("Exception encountered for URL: " + currentUrl);
            e.printStackTrace();
            return false;
        }
    }
    public void takeScreenshot() {
        // Get the calling class and method name
        //StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getName();
        /* final String[] callerInfo = new String[2];
        StackWalker.getInstance().walk(frames -> {
                    frames.skip(1)
                            .findFirst()
                            .ifPresent(frame -> {
                                callerInfo[0] = frame.getClassName();
                                callerInfo[1] = frame.getMethodName();
                            });
                    return null;
                }); */
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[2];
        String callerClass = stackTrace.getClassName();
        String callerMethod = stackTrace.getMethodName();
        callerClass = callerClass.substring(callerClass.lastIndexOf('.') + 1);
        if (takeScreenshot(callerClass, callerMethod) == null) System.out.println("Failed to create screenshot.");;
    }
    protected String takeScreenshot(String callerClass, String callerMethod) {
        // Generate timestamp
        String timestamp = new SimpleDateFormat("_yyyyMMdd_HHmmss").format(new Date());
        // Set the file path and name using the class, method, and timestamp
        String filePath = "screenshot/" + callerClass + "_" + callerMethod + timestamp + ".png";
        // Create screenshot directory if it doesn't exist
        File screenshotDir = new File("screenshot");
        if (!screenshotDir.exists()) {
            screenshotDir.mkdir();
        }
        // Take 'n' Save
        return ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.FILE)
                .renameTo(new File(filePath)) ? filePath : null;
    }
    public static BasePage getCurrentPage() {
        return currentPage;
    }
    public WebDriver getDriver() {
        return driver;
    }
}
enum LangCode {
    RU("ru"),
    RO("ro");
    private final String code;
    LangCode(String code) { this.code = code; }
    public String getCode() { return code; }
}

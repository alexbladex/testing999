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
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Actions actions;
    protected Select select;
    protected static BasePage currentPage;
    By anchor = By.xpath("//aside//img");
    By frame = By.xpath("//iframe[@id='topbar-panel']");
    By script_topbar = By.xpath("//script [@id='topbar']");
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
    public boolean isCabinetDisplayed(){
        boolean b = isElementPresent(cabinet);
        if (Config.debug) System.out.println("Personal Cabinet is visible: " + b);
        return b;
    }
    public boolean isLoginDisplayed(){
        boolean b = isElementPresent(login);
        if (Config.debug) System.out.println("Login button is visible: " + b);
        return b;
    }
    public boolean isLanguageDisplayed(){
//        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
        boolean b = isElementPresent(buttonLang);
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
            button.click();
            wait.until(ExpectedConditions.visibilityOf(secondButton));
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
            Thread.sleep(100);
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
        WebElement loginButton = driver.findElement(login);
        wait.until(ExpectedConditions.visibilityOf(loginButton));
        if (Config.debug) System.out.println("Login: " + loginButton.getText());
        loginButton.click();
        wait.until(ExpectedConditions.invisibilityOf(loginButton));
    }
    public boolean tryHttpRequest(String href) {
        if (!href.startsWith("http")) return true;  // Skip the check for non-HTTP links
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(href).openConnection();
            connection.setRequestMethod("GET"); // Use HEAD to get only the response code
            connection.connect();
            int responseCode = connection.getResponseCode();
//            System.out.println("URL: " + href + " Response Code: " + responseCode);
            return responseCode == 200;
        } catch (IOException e) {
            if (Config.debug) System.out.println("URL: " + href + " encountered an exception: " + e.getMessage());
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
        takeScreenshot(callerClass, callerMethod);
    }
    protected void takeScreenshot(String callerClass, String callerMethod) {
        // Generate timestamp
        String timestamp = new SimpleDateFormat("_yyyyMMdd_HHmmss").format(new Date());
        // Set the file path and name using the class, method, and timestamp
        String filePath = "screenshot/" + callerClass + "_" + callerMethod + timestamp + ".png";
        // Take 'n' Save
        ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.FILE)
                .renameTo(new File(filePath));
    }
    public static BasePage getCurrentPage() {
        return currentPage;
    }
}
enum LangCode {
    RU("ru"),
    RO("ro");
    private final String code;
    LangCode(String code) { this.code = code; }
    public String getCode() { return code; }
}

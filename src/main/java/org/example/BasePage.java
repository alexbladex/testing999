package org.example;

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
    By anchor = By.xpath("//h1[@class='mainPage__withDeliveryBlock__title']");
    By frame = By.xpath("//iframe[@id='topbar-panel']");
    By script_topbar = By.xpath("//script [@id='topbar']");
    By buttonLang = By.xpath("//li[@id='user-language']/button[@class='user-item-btn']");
    By buttonRu = By.xpath("//button[@data-lang='ru']");
    By buttonRo = By.xpath("//button[@data-lang='ro']");
    By activeLang = By.xpath("//li[@class='is-active']/button");
    By categories = By.xpath("//button[@id='js-categories-toggle']");
    By add_ad = By.xpath("//a[@data-autotest='add_ad']");
    By cabinet = By.xpath("//div[@data-autotest='cabinet']");
    By login = By.xpath("//a[@data-autotest='login']");
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.actions = new Actions(driver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }
    public enum AddType {
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
    public boolean isLoginDisplayed(){
        boolean b = isElementPresent(login);
        if (Config.debug) System.out.println("Login button is visible: " + b);
        return b;
    }
    public boolean isLanguageDisplayed(){
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
        boolean b = isElementPresent(buttonLang);
        if (Config.debug) System.out.println("Language button is visible: " + b);
        driver.switchTo().defaultContent();
        return b;
    }
    public String currentLang(){
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
        String lang = driver.findElement(activeLang).getAttribute("data-lang");
        driver.switchTo().defaultContent();
        return lang;
    }
    public void changeLang(String newLang) {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
        String lang = driver.findElement(activeLang).getAttribute("data-lang");
        if (newLang.equals("ro") && lang.equals("ru")) {
            if (Config.debug) System.out.println("Current: RU");
            mouseOver(buttonLang);
            WebElement button = driver.findElement(buttonRo);
            wait.until(ExpectedConditions.visibilityOf(button));
            lang = button.getText();
            button.click();
            wait.until(ExpectedConditions.stalenessOf(button));
        }
        if (newLang.equals("ru") && lang.equals("ro")) {
            if (Config.debug) System.out.println("Current: RO");
            mouseOver(buttonLang);
            WebElement button = driver.findElement(buttonRu);
            wait.until(ExpectedConditions.visibilityOf(button));
            lang = button.getText();
            button.click();
            wait.until(ExpectedConditions.stalenessOf(button));
        }
        if (Config.debug) System.out.println("Lang is changed: " + lang);
        driver.switchTo().defaultContent();
    }
    public boolean isElementPresent(By element) {
        try {
            driver.findElement(element);
            return true;
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }
    public boolean isElementVisible(By element) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(element));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public void initializeSelect(WebElement element) {
        this.select = new Select(element);
    }
    public void selectByValue(String val) {
        if (this.select != null) {
            this.select.selectByValue(val);
        } else {
            throw new IllegalStateException("Select not initialized");
        }
    }
    public void openLoginPage() {
        WebElement loginButton = driver.findElement(login);
        wait.until(ExpectedConditions.visibilityOf(loginButton));
        if (Config.debug) System.out.println("login: " + loginButton.getText());
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
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String callingClass = stackTrace[2].getClassName();
        String callingMethod = stackTrace[2].getMethodName();
        callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);

        // Generate timestamp
        String timestamp = new SimpleDateFormat("_yyyyMMdd_HHmmss").format(new Date());

        // Set the file path and name using the class, method, and timestamp
        String filePath = "screenshot/" + callingClass + "_" + callingMethod + timestamp + ".png";

        ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.FILE)
                .renameTo(new File(filePath));
    }
}
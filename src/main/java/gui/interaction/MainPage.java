package gui.interaction;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.URL;

public class MainPage extends BasePage{

    public MainPage(WebDriver driver) {
        super(driver);  // Call MainPage constructor
        wait.until(ExpectedConditions.visibilityOfElementLocated(anchor)); // The locators are resolved only for those elements that have loaded at the moment the class is initialized
        if (this.getClass() == MainPage.class) { if (Config.debug) System.out.println("Main page is opened"); }
    }
    public MainPage(WebDriver driver, String url) {
        super(driver);  // Call MainPage constructor
        driver.get(url);
        waitDomainMatch(driver, url); //wait.until(ExpectedConditions.urlToBe(url));
        if (this.getClass() == MainPage.class) { if (Config.debug) System.out.println(driver.getCurrentUrl() + " is opened"); }
    }
    public MainPage(WebDriver driver, String url, By locator) {
        super(driver);  // Call MainPage constructor
        driver.get(url);
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator)); // The locators are resolved only for those elements that have loaded at the moment the class is initialized
        if (this.getClass() == MainPage.class) { if (Config.debug) System.out.println(driver.getCurrentUrl() + " is opened"); }
    }
    private void waitDomainMatch(WebDriver driver, final String expectedUrl) {
        wait.until((ExpectedCondition<Boolean>) d -> {
            try {
                String currentDomain = new URL(driver.getCurrentUrl()).getHost(); //^(https?:\/\/)?([\w.-]+)
                String expectedDomain = new URL(expectedUrl).getHost();

                return currentDomain.equals(expectedDomain);
            } catch (Exception e) {
                System.out.println("URL does not match");
                return false;
            }
        });
    }
    private void waitUrlMatch(WebDriver driver, final String expectedUrl) {
        wait.until((ExpectedCondition<Boolean>) d -> {
            try {
                String currentUrl = driver.getCurrentUrl();
                if (currentUrl.endsWith("/")) currentUrl = currentUrl.substring(0, currentUrl.length() - 1);
                String Url = expectedUrl.endsWith("/") ?
                        expectedUrl.substring(0, expectedUrl.length() - 1) : expectedUrl;

                return currentUrl.equals(Url);
            } catch (Exception e) {
                System.out.println("URL does not match");
                return false;
            }
        });
    }
}
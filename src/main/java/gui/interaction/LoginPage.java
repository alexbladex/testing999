package gui.interaction;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {
    By loginWin = By.xpath("//form//button[@type='submit']");
    By logContinue = By.xpath("//a/button[@data-sentry-element='Button']");
    By loginLogo = By.xpath("//*[@data-sentry-component='Header']//img[@data-sentry-element='Image']");
    By logoutWin = By.xpath("//*[@data-sentry-component='Header']//button[@data-sentry-component='ExitButton']");
    By user = By.xpath("//form/div[1]/div/input[@data-sentry-element='Input']");
    By pswd = By.xpath("//form/div[2]/div/input[@data-sentry-element='Input']");
    By p_error = By.xpath("//div[@data-sentry-component='LoginView']/p[@data-sentry-component='Caption']");
    public LoginPage(WebDriver driver) {
        super(driver);  // Call MainPage constructor
        wait.until(ExpectedConditions.visibilityOfElementLocated(add_ad));
        if (Config.debug) System.out.println("Prepare to login.");
    }
    public boolean isUserLoggedIn(){
        boolean b = isElementPresent(cabinet);
        if (Config.debug) System.out.println("User logged in: " + b);
        return b;
    }
    public boolean performLogin(String user, String pswd) {
        if (isElementPresent(cabinet)) {if (Config.debug) System.out.println("Already logged in"); return true;}
        do {
            if (!isElementPresent(loginLogo)) openLoginPage();
        } while (!loginWin(user, pswd));
        if (isElementPresent(logContinue)) driver.findElement(logContinue).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(cabinet));
        if (Config.debug) System.out.println("Login completed");
        return true;
    }
    public boolean loginWin(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(loginLogo));
        if (isElementPresent(logContinue)) {
            driver.findElement(logoutWin).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(logoutWin));
            if (Config.debug) System.out.println("Old session");
            return false;
        }
        driver.findElement(user).sendKeys(username);
        driver.findElement(pswd).sendKeys(password);
        wait.until(ExpectedConditions.visibilityOfElementLocated(loginWin));
        driver.findElement(loginWin).click();
        if (isElementPresent(p_error)) driver.findElement(loginWin).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loginWin));
        return true;
    }
}

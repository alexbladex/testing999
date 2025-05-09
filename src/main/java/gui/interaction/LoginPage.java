package gui.interaction;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {
    By loginWin = By.xpath("//form//button[@type='submit']");
    By loginContinue = By.xpath("//a/button[@data-sentry-element='Button']");
    By loginLogo = By.xpath("//*[@data-sentry-component='Header']//img[@data-sentry-element='Image']");
    By logout = By.xpath("//*[@data-sentry-component='Header']//button[@data-sentry-component='ExitButton']");
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
        wait.until(driver -> {
            Object isLoaded = ((JavascriptExecutor) driver).executeScript(
                    "return arguments[0].complete && arguments[0].naturalWidth > 0;", driver.findElement(anchor)
            );
            return isLoaded != null && (boolean) isLoaded;
        });
        if (isElementPresent(cabinet)) {if (Config.debug) System.out.println("Already logged in"); return true;}
        do {
            if (!isElementPresent(loginLogo)) openLoginPage();
        } while (!loginWin(user, pswd));
        if (isElementPresent(loginContinue)) driver.findElement(loginContinue).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(cabinet));
        if (Config.debug) System.out.println("Login completed");
        return true;
    }
    public boolean loginWin(String username, String password) {
        try{
            wait.until(ExpectedConditions.visibilityOfElementLocated(loginLogo));
            if (isElementPresent(loginContinue)) {
                driver.findElement(logout).click();
                wait.until(ExpectedConditions.invisibilityOfElementLocated(logout));
                if (Config.debug) System.out.println("Old session");
                return false;
            }
            driver.findElement(user).sendKeys(username);
            driver.findElement(pswd).sendKeys(password);
            Thread.sleep(500);
            driver.findElement(loginWin).click();
//        if (isElementPresent(p_error)) driver.findElement(loginWin).click();
            try {
                wait.until(ExpectedConditions.invisibilityOfElementLocated(loginWin));
                return true;
            } catch (TimeoutException e) {
                return false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

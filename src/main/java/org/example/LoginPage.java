package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {
    By loginWin = By.xpath("//button[@class='login__form__footer__submit']");
    By logContinue = By.xpath("//a[@class='login__user__buttons__redirect']");
    By loginLogo = By.xpath("//div[@class='login__logo']");
    By userLogwin = By.xpath("//div[@class='login__user__name']");////button[@id='user-username-btn']
    By titleLogwin = By.xpath("//div[@class='login__title']");
    By logoutWin = By.xpath("//a[@class='login__user__buttons__logout']");
    By user = By.xpath("//input[@name=\"login\"]");
    By pswd = By.xpath("//input[@name=\"password\"]");
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
            wait.until(ExpectedConditions.visibilityOfElementLocated(loginLogo));
        } while (!loginWin(user, pswd));
        if (Config.debug) System.out.println("Login completed");
        return true;
    }
    public boolean loginWin(String username, String password) {
        boolean continueLogin = isElementPresent(logContinue);
        if (continueLogin) {
            driver.findElement(logoutWin).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(logoutWin));
            if (Config.debug) System.out.println("Old session");
            return false;
        }
        driver.findElement(user).sendKeys(username);
        driver.findElement(pswd).sendKeys(password);
        driver.findElement(loginWin).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loginWin));
        return true;
    }
}

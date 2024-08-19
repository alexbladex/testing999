package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoggedInPage extends MainPage {
    By cabinet = By.xpath("//div[@data-autotest='cabinet']");
    By items = By.xpath("//a[@href='/cabinet/items']");
    By userButton = By.xpath("//button[@id='user-username-btn']");
    By logoutTop = By.xpath("//button[@data-autotest='logout']");
    By logoutWin = By.xpath("//a[@class='login__user__buttons__logout']");
    public LoggedInPage(WebDriver driver) {
        super(driver);  // Call MainPage constructor
        if (Config.debug) System.out.println(driver.getTitle() + " is opened");
    }
    public void openItems(){
        mouseOver(cabinet);
        wait.until(ExpectedConditions.visibilityOfElementLocated(items)).click();
        //wait.until(ExpectedConditions.invisibilityOfElementLocated(items));
        if (Config.debug) System.out.println("Переход на items");
    }
    public void logoutPage() {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
        mouseOver(userButton);
        wait.until(ExpectedConditions.visibilityOfElementLocated(logoutTop)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(logoutTop));
        driver.switchTo().defaultContent();
    }
}

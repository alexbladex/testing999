package gui.interaction;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoggedInPage extends MainPage {
    By cabinet = By.xpath("//div[@data-autotest='cabinet']");
    By items = By.xpath("//a[@href='/cabinet/items']");
    By userButton = By.xpath("//button[@id='user-username-btn']");
    By logoutTop = By.xpath("//button[@data-autotest='logout']");
    By logoutWin = By.xpath("//a[@class='login__user__buttons__logout']");
    public LoggedInPage(WebDriver driver) {
        super(driver);  // Call MainPage constructor
        if (this.getClass() == LoggedInPage.class) { if (Config.debug) System.out.println(driver.getCurrentUrl() + " is opened"); }
    }
    public void openItems(){
        mouseOver(cabinet);
        wait.until(ExpectedConditions.visibilityOfElementLocated(items)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(items));
        if (Config.debug) System.out.println("Open items");
    }
    public void logoutPage() {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
        mouseOver(userButton);
        wait.until(ExpectedConditions.visibilityOfElementLocated(logoutTop)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(logoutTop));
        driver.switchTo().defaultContent();
    }
}

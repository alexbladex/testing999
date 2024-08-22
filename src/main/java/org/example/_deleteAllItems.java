package org.example;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

import org.openqa.selenium.*;

public class _deleteAllItems {
    public static void main(String[] args) {
        String uri = PropertyReader.getProperty("uri");
        String user = PropertyReader.getProperty("user");
        String pswd = PropertyReader.getProperty("pswd");
        String[] itemsSummary = PropertyReader.getPropertyArray("ads_title_for_delete");
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");
        WebDriver driver = DriverFactory.init(uri);
        if (Config.rmCookies) {
            driver.manage().deleteCookieNamed("auth");
            driver.manage().deleteCookieNamed("utid");
            driver.manage().deleteCookieNamed("simpalsid.auth");
        }
        MainPage mainpage = new MainPage(driver);
        mainpage.changeLang("ru");
        LoginPage loginpage = new LoginPage(driver);
        loginpage.performLogin(user,pswd);
        UserItemsPage itempage = new UserItemsPage(driver);
        itempage.delAllItems(itemsSummary);
        driver.quit();
    }
}

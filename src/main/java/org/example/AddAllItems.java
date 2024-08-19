package org.example;

import org.json.JSONArray;
import org.openqa.selenium.WebDriver;

public class AddAllItems {
    public static void main(String[] args) {
        String uri = PropertyReader.getProperty("uri");
        String user = PropertyReader.getProperty("user");
        String pswd = PropertyReader.getProperty("pswd");
        JSONArray itemsArray = JSONReader.getPropertyJSONArray("items_for_webform");
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");
        WebDriver driver = DriverFactory.init(uri);
        MainPage mainpage = new MainPage(driver);
        mainpage.changeLang("ru");
        LoginPage loginpage = new LoginPage(driver);
        loginpage.performLogin(user,pswd);
        AddItemsPage itemspage = new AddItemsPage(driver);
        itemspage.addItems(itemsArray);
        driver.quit();
    }
}

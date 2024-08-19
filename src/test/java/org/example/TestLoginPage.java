package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;

public class TestLoginPage {
    private WebDriver driver;
    private MainPage mainpage;
    String uri, user, pswd;

    @BeforeClass
    public void setupTest() {
        uri = PropertyReader.getProperty("uri");
        user = PropertyReader.getProperty("user");
        pswd = PropertyReader.getProperty("pswd");
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");
        driver = DriverFactory.init();
        mainpage = new MainPage(driver, uri);
    }
    @AfterClass
    public void closeTest(){
        driver.quit();
    }
    @BeforeMethod
    public void initializePageObjects() {
        driver.get(uri);
    }
    @AfterMethod
    public void clearCookies(){
        driver.manage().deleteAllCookies();
    }

    @Test(enabled=true)
    public void testLoginButton() {
        Assert.assertTrue(mainpage.isLoginDisplayed(), "Login button is not visible");
    }

}

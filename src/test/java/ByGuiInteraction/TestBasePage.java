package ByGuiInteraction;

import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TestBasePage {
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
    @Test
    public void testLangButton() {
        Assert.assertTrue(mainpage.isLanguageDisplayed(), "Language button is not visible");
    }
    @Test(enabled=true)
    public void testLoginButton() {
        Assert.assertTrue(mainpage.isLoginDisplayed(), "Login button is not visible");
    }
    @Test
    public void testChangeLang() {
        String activeLang = mainpage.currentLang();
        mainpage.takeScreenshot();
        if (activeLang.equals("ro")) mainpage.changeLang("ru");
        else mainpage.changeLang("ro");
        Assert.assertNotEquals(mainpage.currentLang(), activeLang, "Language is not changed");
    }
    @Test(enabled=false)
    public void testAllHref() {
        if (!mainpage.isElementVisible(mainpage.getAnchor())) Assert.fail("Anchor element is not visible, failing the test.");

        List<String> failedLinks = new ArrayList<>();
        List<WebElement> tagsA = driver.findElements(By.tagName("a"));
        for (WebElement a:tagsA){
            String href = a.getAttribute("href");
            boolean httpResponse = mainpage.tryHttpRequest(href);
            if (!httpResponse) failedLinks.add(href);
        }

        if (failedLinks.isEmpty()) {
            System.out.println("All links have status: OK");
        } else {
            System.out.println("Failed links:");
            for (String link : failedLinks) {
                System.out.println(link);
            }
            Assert.fail("Some links did not return a 200 response code.");
        }
    }
}

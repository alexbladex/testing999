package gui.interaction;

import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

//@Listeners(gui.interaction.EventListener.class)
public class TestBasePage {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private WebDriver driver;
    private MainPage mainpage;
    String uri, user, pswd;

    @Parameters({"driverProfile", "port"})
    @BeforeClass
    public void setupTest(@Optional("local") String driverProfile, @Optional("8080") int port) throws MalformedURLException {
        uri = PropertyReader.getProperty("uri");
        user = PropertyReader.getProperty("user");
        pswd = PropertyReader.getProperty("pswd");
        if ("remote".equals(driverProfile)) {
            driver = DriverFactory.remoteInit(port);
        } else {
            driver = DriverFactory.localInit();
        }
        mainpage = new MainPage(driver, uri);
        logger.info("Test setup complete");
    }
    @AfterClass
    public void closeTest(){
        DriverFactory.close();
//        DriverFactory.quitDriver();
        logger.info("Test Class closed");
    }
    //@Parameters("baseURL")
    @BeforeMethod
    public void initMainPage() {
        driver.get(uri);
        logger.info("Navigated to URL: {}", uri);
    }
    @AfterMethod
    public void clearCookies(){
        driver.manage().deleteAllCookies();
        logger.info("Cookies cleared");
    }
    @Test
    public void testLangButton() {
        boolean isLanguageDisplayed = mainpage.isLanguageDisplayed();
        logger.info("Language button visibility: {}", isLanguageDisplayed);
        Assert.assertTrue(isLanguageDisplayed, "Language button is not visible");
    }
    @Test(enabled=true)
    public void testLoginButton() {
        boolean isLoginDisplayed = mainpage.isLoginDisplayed();
        logger.info("Login button visibility: {}", isLoginDisplayed);
        Assert.assertTrue(isLoginDisplayed, "Login button is not visible");
    }
    @Test
    public void testChangeLang() {
        String activeLang = mainpage.currentLang();
        //mainpage.takeScreenshot();
        logger.info("Current language: {}", activeLang);

        if (activeLang.equals(LangCode.RO.getCode())) mainpage.changeLang(LangCode.RU);
        else mainpage.changeLang(LangCode.RO);

        String newLang = mainpage.currentLang();
        logger.info("Language after change: {}", newLang);
        Assert.assertNotEquals(newLang, activeLang, "Language is not changed");
    }
    @Test(enabled=false, retryAnalyzer = RetryAnalyzer.class)
    public void testAllHref() {
        if (!mainpage.isElementPresent(mainpage.getAnchor())) {
            logger.error("Anchor element is not visible, failing the test.");
            Assert.fail("Anchor element is not visible, failing the test.");
        }

        List<String> failedLinks = new ArrayList<>();
        List<WebElement> tagsA = driver.findElements(By.tagName("a"));
        mainpage.takeScreenshot();
        for (WebElement a:tagsA){
            String href = a.getAttribute("href");
            boolean httpResponse = mainpage.tryHttpRequest(href);
            if (!httpResponse) {
                failedLinks.add(href);
                logger.warn("Failed link: {}", href);
            }
        }

        if (failedLinks.isEmpty()) {
            System.out.println("All links have status: OK");
            logger.info("All links have status: OK");
        } else {
            System.out.println("Failed links:");
            logger.error("Failed links:");
            for (String link : failedLinks) {
                System.out.println(link);
                logger.error("Failed link: {}", link);
            }
            logger.error("Some links did not return a 200 response code.");
            Assert.fail("Some links did not return a 200 response code.");
        }
    }
}

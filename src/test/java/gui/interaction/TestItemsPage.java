package gui.interaction;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

public class TestItemsPage {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private WebDriver driver;
    String uri, user, pswd;

    @BeforeClass
    public void setupTest() {
        uri = PropertyReader.getProperty("uri");
        user = PropertyReader.getProperty("user");
        pswd = PropertyReader.getProperty("pswd");
        driver = DriverFactory.init();
        logger.info("Test setup complete");
    }
    @AfterClass
    public void closeTest(){
        DriverFactory.close();
        logger.info("Test Class closed");
    }
    //@Parameters("baseURL")
    @BeforeMethod
    public void initMainPage() {
        driver.get(uri);
        logger.info("Navigated to URL: {}", uri);
    }
    @BeforeMethod(onlyForGroups = "requiresLogin", dependsOnMethods = "initMainPage")
    //Methods with the same annotation, for ex. @BeforeMethod, are executed in the alphabetical order.
    //But attribute dependsOnMethods or Priority can change order
    public void performLogin() {
        LoginPage loginpage = new LoginPage(driver);
        Assert.assertTrue(loginpage.performLogin(user,pswd), "Login is not completed");
    }
    @AfterMethod
    public void clearCookies(){
        driver.manage().deleteAllCookies();
        logger.info("Cookies cleared");
    }
    @Test(enabled = false, groups = "requiresLogin", retryAnalyzer = RetryAnalyzer.class)
    public void testDelLastItem() {
        CabinetItemsPage itempage = new CabinetItemsPage(driver);
        AdItem ad = itempage.addDefaultAd();
        Assert.assertTrue(itempage.delLastItemById(ad.getId()), "Last Item was not deleted");
    }
    @Test(enabled = false, groups = "requiresLogin", retryAnalyzer = RetryAnalyzer.class)
    public void testGetIdByTitle() {
        CabinetItemsPage itempage = new CabinetItemsPage(driver);
        AdItem ad = itempage.addDefaultAd();
        Integer id = itempage.getIdByTitle(ad.getTitle());
        Assert.assertEquals(id, ad.getId(), "Ad Id does not match");
    }
    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void testLogin() {
        LoginPage loginpage = new LoginPage(driver);
        Assert.assertTrue(loginpage.performLogin(user,pswd), "Login is not completed");
    }
    @Test
    public void tesCabinetButton() {
        MainPage mainpage = new MainPage(driver);
        Assert.assertFalse(mainpage.isCabinetDisplayed(), "Personal Cabinet is visible for not logged in user");
    }
    @Test(enabled = true)
    public void testFailed() {
        throw new RuntimeException("This is an uncaught exception!");
    }
}

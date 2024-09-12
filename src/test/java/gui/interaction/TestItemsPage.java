package gui.interaction;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.*;

public class TestItemsPage {
    private WebDriver driver;
    private MainPage mainpage;
    String uri, user, pswd;

    @BeforeClass
    public void setupTest() {
        uri = PropertyReader.getProperty("uri");
        user = PropertyReader.getProperty("user");
        pswd = PropertyReader.getProperty("pswd");
        driver = DriverFactory.init();
    }
    @AfterClass
    public void closeTest(){
        DriverFactory.close();
    }
    //@Parameters("baseURL")
    @BeforeMethod
    public void initMainPage() {
        driver.get(uri);
    }
    @BeforeMethod(onlyForGroups = "requiresLogin", dependsOnMethods = "initMainPage")
    //Methods with the same annotation, for ex. @BeforeMethod, are executed in the alphabetical order.
    //But attribute dependsOnMethods or Priority can change order
    public void performLoginGroup() {
        performLogin();
    }
    public void performLogin() {
        LoginPage loginpage = new LoginPage(driver);
        Assert.assertTrue(loginpage.performLogin(user,pswd), "Login is not completed");
    }
    @AfterMethod
    public void clearCookies(){
        driver.manage().deleteAllCookies();
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
    @Test
    public void testLogin() {
        LoginPage loginpage = new LoginPage(driver);
        Assert.assertTrue(loginpage.performLogin(user,pswd), "Login is not completed");
    }
    @Test
    public void testFailed() {
        throw new RuntimeException("Тест не пройден!");
    }
}

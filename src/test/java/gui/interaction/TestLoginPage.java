package gui.interaction;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.MalformedURLException;

public class TestLoginPage {
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
    }
    @AfterClass
    public void closeTest(){
        DriverFactory.close();
    }
    @BeforeMethod
    public void initMainPage() {
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
    @Test
    public void testPerformLogin() {
        LoginPage loginpage = new LoginPage(driver);
        Assert.assertTrue(loginpage.performLogin(user,pswd), "Login is not completed");
    }
    @Test
    public void testIsUserIn() {
        LoginPage loginpage = new LoginPage(driver);
        loginpage.performLogin(user,pswd);
        Assert.assertTrue(loginpage.isUserLoggedIn(), "User is not logged in");
    }
}

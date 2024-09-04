package gui.interaction;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.*;

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
    public void initiMainPage() {
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

package com.example;

import gui.interaction.DriverFactory;
import gui.interaction.LoginPage;
import gui.interaction.MainPage;
import gui.interaction.PropertyReader;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestSomePage {
    //@PageObject not applicable to local variable, это не всегда подходит. Но такое внедрение юзерской аннотации, и её последующая обработка, позволяет вызвать соответсвующий метод от желаемого экземпляра объекта.
    @PageObject
    private LoginPage loginpage;
    private WebDriver driver;
    String uri, user, pswd;
    @BeforeClass
    public void setupTest() {
        uri = PropertyReader.getProperty("uri");
        user = PropertyReader.getProperty("user");
        pswd = PropertyReader.getProperty("pswd");
        driver = DriverFactory.localInit();
    }
    @Test
    public void testLogin() {
        Assert.assertTrue(loginpage.performLogin(user,pswd), "Login is not completed");
    }
}

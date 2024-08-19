package org.example;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class DeleteAllItems {

    public static void main(String[] args) throws InterruptedException {
        String uri = PropertyReader.getProperty("uri");
        String user = PropertyReader.getProperty("user");
        String pswd = PropertyReader.getProperty("pswd");
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");
        WebDriver driver = DriverFactory.init(uri);
//        driver.manage().deleteCookieNamed("auth");
//        driver.manage().deleteCookieNamed("utid");
//        driver.manage().deleteCookieNamed("simpalsid.auth");
        MainPage mainpage = new MainPage(driver);
        mainpage.changeLang("ru");
        LoginPage loginpage = new LoginPage(driver);
        loginpage.performLogin(user,pswd);
        LoggedInPage userpage = new LoggedInPage(driver);
        userpage.openItems();
//        if (true) {
//            throw new RuntimeException("Stopping execution");
//        }
        String[] itemsSummary = PropertyReader.getPropertyArray("ads_title_for_delete");
        for (String itemSummary : itemsSummary) {
            System.out.println("Search: " + itemSummary);
            int currentPage = 0;
            List<WebElement> paginators = driver.findElements(By.xpath("//nav[@class='paginator cf']/ul/li/a"));
            do {
                paginators.get(currentPage).click();
                new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.stalenessOf(paginators.get(currentPage)));
                System.out.println("Opening next page");
                WebElement iframeElement = driver.findElement(By.xpath("//iframe[@id='topbar-panel']"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('style', 'display:none');", iframeElement);
                iframeElement = driver.findElement(By.xpath("//script [@id='topbar']"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('style', 'display: none;');", iframeElement);

                do {
                    List<WebElement> itemsForSale = driver.findElements(By.xpath("//a[contains(text(), '" + itemSummary + "')]"));
                    for (WebElement itemForSale : itemsForSale) {
                        String itemNumber = itemForSale.getAttribute("href").replaceAll("^.*?/ru/", "").replaceAll("\\D+", ""); // https://999.md/ru/87800316
                        System.out.println(itemNumber);
                        new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@value='" + itemNumber + "']"))).click();
                        System.out.println("первй клик?");
                        Thread.sleep(5000);
                        new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@value='" + itemNumber + "']"))).click();
                        System.out.println("второй клик?");
                        Thread.sleep(5000);
                    }
                    WebElement buttonDelete = driver.findElement(By.xpath("//a[contains(@class, 'js-multi-delete')]"));
                    if (buttonDelete.isDisplayed()) {
                        buttonDelete.click();
                        WebElement agreeDelete = driver.findElement(By.xpath("//button[contains(@class, 'js-form-submit') and contains(@data-form-action, 'multiple_remove')]"));
                        agreeDelete.click();
                        new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.invisibilityOf(buttonDelete));
                        System.out.println("items deleted");
                    }
                } while (!driver.findElements(By.xpath("//a[contains(text(), '" + itemSummary + "')]")).isEmpty());

                currentPage++;
                paginators = driver.findElements(By.xpath("//nav[@class='paginator cf']/ul/li/a"));
            } while (currentPage < paginators.size());
        }
        System.out.println("Завершили");
        driver.quit();
    }
}

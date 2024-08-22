package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class UserItemsPage extends LoggedInPage {
    String summaryTemplate = "//a[contains(text(), '%s')]";
    String itemTemplate = "//input[@value='%s']";
    By page_qty = By.xpath("//nav[@class='paginator cf']/ul/li/a");
    By delete = By.xpath("//a[contains(@class, 'js-multi-delete')]");
    By agree = By.xpath("//button[contains(@data-form-action, 'multiple_remove')]");
    By anchor = By.xpath("//form/table/tbody[@id='js-cabinet-items-list']");
    String uri = "https://999.md/cabinet/items";

    public UserItemsPage(WebDriver driver) {
        super(driver);  // Call MainPage constructor
        wait.until(ExpectedConditions.visibilityOfElementLocated(add_ad));
    }
    private void delSelectedItems(String[] itemsArray, int item_qty){
        driver.get(uri);
        wait.until(ExpectedConditions.visibilityOfElementLocated(anchor));
        for (String itemSummary : itemsArray) {
            System.out.println("Search: " + itemSummary);
            int currentPage = 0;
            List<WebElement> paginators = driver.findElements(page_qty);
            do {
                if (item_qty == 0) break;
                paginators.get(currentPage).click();
                new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.stalenessOf(paginators.get(currentPage)));
                System.out.println("Opening next page");
                WebElement frameElement = driver.findElement(frame);
                ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('style', 'display:none');", frameElement);
                frameElement = driver.findElement(script_topbar);
                ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('style', 'display: none;');", frameElement);

                do {
                    if (item_qty == 0) break;
                    List<WebElement> itemsForSale = driver.findElements(getSummary(itemSummary));
                    for (WebElement itemForSale : itemsForSale) {
                        if (item_qty == 0) break;
                        String itemNumber = itemForSale.getAttribute("href").replaceAll("^.*?/ru/", "").replaceAll("\\D+", ""); // https://999.md/ru/87800316
                        System.out.println(itemNumber);
                        new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.visibilityOfElementLocated(getItem(itemNumber))).click();
                        item_qty--;
                    }
                    WebElement buttonDelete = driver.findElement(delete);
                    if (buttonDelete.isDisplayed()) {
                        buttonDelete.click();
                        WebElement agreeDelete = driver.findElement(agree);
                        agreeDelete.click();
                        new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.invisibilityOf(buttonDelete));
                        System.out.println("items deleted");
                    }
                } while (!driver.findElements(getSummary(itemSummary)).isEmpty());

                currentPage++;
                paginators = driver.findElements(page_qty);
            } while (currentPage < paginators.size());
        }
        System.out.println("Completed");
    }
    public void delAllItems(String[] itemsArray){
        delSelectedItems(itemsArray, -1); // -1: all
    }
    public void delLastItem(String[] itemsArray){
        delSelectedItems(itemsArray, 1);
    }
    private By getSummary(String itemSummary) {
        return By.xpath(String.format(summaryTemplate, itemSummary));
    }
    private By getItem(String itemNumber) {
        return By.xpath(String.format(itemTemplate, itemNumber));
    }
    public void selectDropdown(WebElement dropdown, String value) {
        initializeSelect(dropdown);
        selectByValue(value);
    }
    @Override
    public By getAnchor() { return anchor; }
    @Override
    public void setAnchor(By anchor) {
        this.anchor = anchor;
    }
}

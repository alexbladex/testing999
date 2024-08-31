package gui.interaction;

import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class CabinetItemsPage extends LoggedInPage {
    String summaryTemplate = "//a[contains(text(), '%s')]";
    String itemTemplate = "//input[@value='%s']";
    By page_qty = By.xpath("//nav[@class='paginator cf']/ul/li/a");
    By delete = By.xpath("//a[contains(@class, 'js-multi-delete')]");
    By agree = By.xpath("//button[contains(@data-form-action, 'multiple_remove')]");
    By anchor = By.xpath("//form/table/tbody[@id='js-cabinet-items-list']");
    String uri = "https://999.md/cabinet/items";
    public CabinetItemsPage(WebDriver driver) {
        super(driver);  // Call MainPage constructor
        wait.until(ExpectedConditions.visibilityOfElementLocated(add_ad));
    }
    private void delSelectedItems(String[] itemsArray, int item_qty){
        if (!driver.getCurrentUrl().contains("items")) driver.get(uri);
        wait.until(ExpectedConditions.visibilityOfElementLocated(anchor));
        for (String itemSummary : itemsArray) {
            System.out.println("Search: " + itemSummary);
            int currentPage = 0;
            List<WebElement> paginators = driver.findElements(page_qty);
            do {
                if (item_qty == 0) break;
                paginators.get(currentPage).click();
                wait.until(ExpectedConditions.stalenessOf(paginators.get(currentPage)));
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
                        //String itemNumber = itemForSale.getAttribute("href").replaceAll("^.*?/ru/", "").replaceAll("\\D+", ""); // https://999.md/ru/87800316
                        String itemNumber = itemForSale.getAttribute("href").replaceAll("^.*?md.*?(\\d+).*$", "$1");
                        System.out.println(itemNumber);
                        wait.until(ExpectedConditions.visibilityOfElementLocated(getItem(itemNumber))).click();
                        item_qty--;
                    }
                    performDelete();
                } while (!driver.findElements(getSummary(itemSummary)).isEmpty());

                currentPage++;
                paginators = driver.findElements(page_qty);
            } while (currentPage < paginators.size());
        }
        System.out.println("Completed");
    }
    private boolean performDelete() {
        List<WebElement> elements = driver.findElements(delete);
        if (elements.isEmpty()) return false;
        WebElement buttonDelete = elements.get(0);
        if (buttonDelete.isDisplayed()) {
            buttonDelete.click();
            WebElement agreeDelete = driver.findElement(agree);
            agreeDelete.click();
            wait.until(ExpectedConditions.invisibilityOf(buttonDelete));
            System.out.println("deleted.");
            return true;
        }
        System.out.println("are not deleted.");
        return false;
    }
    public void delAllItems(String[] titleArray){
        delSelectedItems(titleArray, -1); // -1: all
    }
    public void delLastItems(String[] titles){
        delSelectedItems(titles, 1); // last of each
    }
    public void delAllItems(String title){
        delSelectedItems(new String[]{title}, -1); // -1: all
    }
    public void delLastItemByTitle(String title){
        delSelectedItems(new String[]{title}, 1); // last
    }
    public boolean delLastItemById(Integer id){
        if (!driver.getCurrentUrl().contains("items")) driver.get(uri);
        System.out.println(id);
        wait.until(ExpectedConditions.visibilityOfElementLocated(getItem(id.toString()))).click();
        return performDelete();
    }
    private By getSummary(String itemSummary) {
        return By.xpath(String.format(summaryTemplate, itemSummary));
    }
    private By getItem(String itemNumber) {
        return By.xpath(String.format(itemTemplate, itemNumber));
    }
    public Integer getIdByTitle(String title) {
        if (!driver.getCurrentUrl().contains("items")) driver.get(uri);
        List<WebElement> elements = driver.findElements(getSummary(title));
        if (elements.isEmpty()) return null;
        WebElement itemForSale = elements.get(0);
        return Integer.parseInt(itemForSale.getAttribute("href")
                .replaceAll("^.*?md.*?(\\d+).*$", "$1"));
    }
    public AdItem addDefaultAd() {
        String json = "{\n" +
                "  \"url\": \"https://999.md/add?category=construction-and-repair&subcategory=construction-and-repair/finishing-and-facing-materials\",\n" +
                "  \"price\": 200,\n" +
                "  \"price_type\": \"mdl\",\n" +
                "  \"title\": \"плитка кафель 15*15 белая turkey\",\n" +
                "  \"desc\": \"Настенная плитка кафель 15*15 цвет белая турция. 200 л/м2. Минимальный заказ 2 метра\",\n" +
                "  \"img\": [\n" +
                "    \"defaultAd/plitka1515.png\"\n" +
                "  ],\n" +
                "  \"c\": {\n" +
                "    \"control_686\": 21099\n" +
                "  }\n" +
                "}";
        JSONObject jsonObject = new JSONObject(json);
        AddAdPage itempage = new AddAdPage(driver);
        int id = itempage.submittingForm(jsonObject);
        return (id > 0) ? new AdItem(id, jsonObject.getString("title")) : new AdItem(null, null);
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

package gui.interaction;

import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class DelAdPage extends LoggedInPage {
    String summaryTemplate = "//a[contains(text(), '%s')]";
    String itemTemplate = "//input[@value='%s']";
    By page_qty = By.xpath("//nav[@class='paginator cf']/ul/li/a");
    By delete = By.xpath("//a[contains(@class, 'js-multi-delete')]");
    By agree = By.xpath("//button[contains(@data-form-action, 'multiple_remove')]");
    By anchor = By.xpath("//form/table/tbody[@id='js-cabinet-items-list']");
    String uri = "https://999.md/cabinet/items";
    int deletedAds = 0;
    public DelAdPage(WebDriver driver) {
        super(driver);  // Call MainPage constructor
        wait.until(ExpectedConditions.visibilityOfElementLocated(add_ad));
    }
    private int delSelectedItems(String[] itemsArray, int item_qty, final DataAdItemState state){
        if (!driver.getCurrentUrl().contains("items")) driver.get(uri);
        wait.until(ExpectedConditions.visibilityOfElementLocated(anchor));
        for (String itemSummary : itemsArray) {
            System.out.println("Search: " + itemSummary);
            int currentPage = 0;
            List<WebElement> paginators = driver.findElements(page_qty);
            do {
                if (item_qty == 0) break;
                if (paginators.size() > 0) {
                    paginators.get(currentPage).click();
                    wait.until(ExpectedConditions.stalenessOf(paginators.get(currentPage)));
                    System.out.println("Opening next page");
                }
                WebElement frameElement = driver.findElement(frame);
                ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('style', 'display:none');", frameElement);
                frameElement = driver.findElement(script_topbar);
                ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('style', 'display: none;');", frameElement);

                do {
                    if (item_qty == 0) break;
                    int checked = 0;
                    List<WebElement> itemsForSale = driver.findElements(getSummary(itemSummary));
                    for (WebElement itemForSale : itemsForSale) {
                        if (item_qty == 0) break;
                        WebElement trElement = itemForSale.findElement(By.xpath("./ancestor::tr"));
                        String itemState = trElement.getAttribute("data-test-item-state");
                        System.out.println(itemState);
                        String itemNumber = itemForSale.getAttribute("href").replaceAll("^.*?md.*?(\\d+).*$", "$1"); // https://999.md/ru/87800316
                        System.out.println(itemNumber);
                        WebElement inputElement = trElement.findElement(By.xpath(".//td//input"));

                        boolean shouldClick = true;
                        switch (state) {
                            case DISABLED:
                                if (itemState.equals("public")) {
                                    shouldClick = false;
                                }
                                break;
                            case ACTIVE:
                                if (itemState.matches("need_pay|expired|blocked")) {
                                    shouldClick = false;
                                }
                                break;
                        }
                        if (shouldClick) {
                            wait.until(ExpectedConditions.visibilityOf(inputElement)).click(); //visibilityOfElementLocated(getItem(itemNumber))
                            checked++;
                        }
                        item_qty--;
                    }
                    if (performDelete()) deletedAds += checked;;
                } while (isFoundDesired(itemSummary, state));

                currentPage++;
                paginators = driver.findElements(page_qty);
            } while (currentPage < paginators.size());
        }
        System.out.println("Completed. Deleted ads: " + deletedAds);
        return deletedAds;
    }
    private boolean isFoundDesired(String itemSummary, DataAdItemState state) {
        List<WebElement> itemsForSale = driver.findElements(getSummary(itemSummary));
        if (state.equals(DataAdItemState.ALL) && !itemsForSale.isEmpty()) {
            return true;
        }
        for (WebElement itemForSale : itemsForSale) {
            WebElement trElement = itemForSale.findElement(By.xpath("./ancestor::tr"));
            String itemState = trElement.getAttribute("data-test-item-state");
            switch (state) {
                case DISABLED:
                    if (itemState.matches("need_pay|expired|blocked")) return true;
                    break;
                case ACTIVE:
                    if (itemState.equals("public")) return true;
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected state: " + state);
            }
        }
        return false;
    }
    private boolean performDelete() {
        System.out.println("trying delete");
        WebElement delButton = driver.findElement(delete);
        if (!delButton.isDisplayed()) return true;
        try {
            delButton.click();
            Thread.sleep(100);
            driver.findElement(agree).click();
            wait.until(ExpectedConditions.invisibilityOf(delButton));
            Thread.sleep(1000);
//            driver.navigate().refresh();
            System.out.println("deleted.");
            return true;
        } catch (Exception e) {
            System.out.println("not deleted: " + e.getMessage());
            return false;
        }
    }
    public int delAllItems(String[] titleArray){
        return delSelectedItems(titleArray, -1, DataAdItemState.ALL); // -1: all
    }
    public int delAllInactiveItems(String[] titleArray){
        return delSelectedItems(titleArray, -1, DataAdItemState.DISABLED); // -1: all
    }
    public int delAllActiveItems(String[] titleArray){
        return delSelectedItems(titleArray, -1, DataAdItemState.ACTIVE); // -1: all
    }
    public int delItems(String title){
        return delSelectedItems(new String[]{title}, -1, DataAdItemState.ALL); // -1: all
    }
    public int delInactiveItems(String title){
        return delSelectedItems(new String[]{title}, -1, DataAdItemState.DISABLED); // -1: all
    }
    public int delActiveItems(String title){
        return delSelectedItems(new String[]{title}, -1, DataAdItemState.ACTIVE); // -1: all
    }
    public int delLastItems(String[] title){
        return delSelectedItems(title, 1, DataAdItemState.ALL); // last(newest) of each different title
    }
    public int delLastItemByTitle(String title){
        return delSelectedItems(new String[]{title}, 1, DataAdItemState.ALL); // last(newest)
    }
    public boolean delLastItemById(Integer id){
        if (!driver.getCurrentUrl().contains("items")) driver.get(uri);
        System.out.print(id + " ");
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
        int i = Integer.parseInt(itemForSale.getAttribute("href")
                .replaceAll("^.*?md.*?(\\d+).*$", "$1"));
        System.out.println("getIdByTitle return: " + i);
        return i;
    }
    public AdItem addDefaultAd() {
        AdTemplate temp = new AdTemplate();
        JSONObject jsonObject = new JSONObject(temp.getAd());
        AddAdPage itempage = new AddAdPage(driver);
        return itempage.submittingForm(jsonObject);
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

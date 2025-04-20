package gui.interaction;

import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class DelAdPage extends AddAdPage {
    String summaryTemplate = "//a[contains(text(), '%s')]";
    String itemTemplate = "//a[contains(@href, '%s')]";
    By page_qty = By.xpath("//a[contains(@class, 'pagination')]//button[@data-test-page-value]");
    By delete = By.xpath("//i[contains(@data-test-id, 'multiactions-delete')]");
    By agree = By.xpath("(//footer/button[contains(@data-sentry-element, 'Button')])[2]");
    By anchor = By.xpath("//div[@data-sentry-component='CabinetItemsViewDesktop']//table");
    String uri = "https://999.md/cabinet/items";
    int deletedAds = 0;
    public DelAdPage(WebDriver driver) {
        super(driver);  // Call MainPage constructor
        wait.until(ExpectedConditions.visibilityOfElementLocated(add_ad));
    }
    private int delSelectedItems(String[] itemsArray, int item_qty, final DataAdItemState state){
        if (!driver.getCurrentUrl().contains("items")) driver.get(uri);
        wait.until(ExpectedConditions.visibilityOfElementLocated(page_qty));
        for (String itemSummary : itemsArray) {
            System.out.println("Search: " + itemSummary);
            int currentPage = 0;
            List<WebElement> paginators = driver.findElements(page_qty);
            do {
                if (item_qty == 0) break;
                if (paginators.size() > 0) {
                    clickTo(paginators.get(currentPage));
                    wait.until(ExpectedConditions.attributeContains(
                            paginators.get(currentPage), "class", "active"
                    ));
                    System.out.println("Opening next page");
                }
//                WebElement frameElement = driver.findElement(frame);
//                ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('style', 'display:none');", frameElement);
//                frameElement = driver.findElement(script_topbar);
//                ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('style', 'display: none;');", frameElement);

                do {
                    if (item_qty == 0) break;
                    int checked = 0;
                    List<WebElement> itemsForSale = driver.findElements(getSummary(itemSummary));
                    for (WebElement itemForSale : itemsForSale) {
                        if (item_qty == 0) break;
                        AdInfo adInfo = getItemInfo(itemForSale);
                        boolean itemBlocked = adInfo.isBlocked;
                        System.out.println(adInfo.itemNumber + " is: " + (itemBlocked ? "disabled" : "active"));

                        boolean shouldClick = state == DataAdItemState.ALL || (state == DataAdItemState.DISABLED) == itemBlocked;
                        if (shouldClick) {
                            clickTo(adInfo.checkBox);
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
            AdInfo adInfo = getItemInfo(itemForSale);
            boolean itemBlocked = adInfo.isBlocked;
            if (state == DataAdItemState.DISABLED) {
                return itemBlocked;
            }
            if (state == DataAdItemState.ACTIVE) {
                return !itemBlocked;
            }
            throw new IllegalArgumentException("Unexpected state: " + state);
        }
        return false;
    }
    private boolean performDelete() {
        System.out.println("trying delete");
        List<WebElement> delButtons = driver.findElements(delete);
        if (delButtons.isEmpty()) return true;
        try {
            scrollTo(delButtons.get(0));
            clickTo(delButtons.get(0));
            Thread.sleep(100);
            driver.findElement(agree).click();
            wait.until(ExpectedConditions.invisibilityOf(delButtons.get(0)));
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
    public int delAllInactiveItems(String[] titleArray, Integer qty){
        if (qty == null) qty = -1;
        return delSelectedItems(titleArray, qty, DataAdItemState.DISABLED); // -1: all
    }
    public int delAllActiveItems(String[] titleArray){
        return delSelectedItems(titleArray, -1, DataAdItemState.ACTIVE); // -1: all
    }
    public int delItems(String title){
        return delSelectedItems(new String[]{title}, -1, DataAdItemState.ALL); // -1: all
    }
    public int delInactiveItems(String title, Integer qty){
        return delSelectedItems(new String[]{title}, qty, DataAdItemState.DISABLED); // -1: all
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
        WebElement itemForSale = wait.until(ExpectedConditions.visibilityOfElementLocated(getItem(id.toString())));
        AdInfo adInfo = getItemInfo(itemForSale);
        clickTo(adInfo.checkBox);
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
        wait.until(ExpectedConditions.visibilityOfElementLocated(page_qty));
        List<WebElement> elements = driver.findElements(getSummary(title));
        if (elements.isEmpty()) return null;
        AdInfo adInfo = getItemInfo(elements.get(0));
        System.out.println("getIdByTitle return: " + adInfo.itemNumber);
        return adInfo.itemNumber;
    }
    public AdInfo getItemInfo(WebElement itemForSale) {
        int itemNumber = Integer.parseInt(itemForSale.getAttribute("href")
                .replaceAll("^.*\\/(\\d+)\\/?$", "$1"));
        WebElement trElement = itemForSale.findElement(By.xpath("./ancestor::tr"));
        boolean isBlocked = trElement.getAttribute("class").contains("styles_blocked");
        WebElement checkBox = trElement.findElement(By.xpath("./td[1]//input[@type='checkbox']"));
        return new AdInfo(isBlocked, itemNumber, checkBox);
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
class AdInfo {
    public boolean isBlocked;
    public int itemNumber;
    public WebElement checkBox;

    public AdInfo(boolean isBlocked, int itemNumber, WebElement checkBox) {
        this.isBlocked = isBlocked;
        this.itemNumber = itemNumber;
        this.checkBox = checkBox;
    }
}
enum DataAdItemState {
    DISABLED,
    ACTIVE,
    ALL
}

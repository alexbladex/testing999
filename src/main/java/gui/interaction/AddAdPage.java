package gui.interaction;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.util.List;

public class AddAdPage extends LoggedInPage {
    By items = By.xpath("//a[@href='/cabinet/items']");
    By offer_sell = By.xpath("//input[@value='776' and @type='radio']");
    By offer_buy = By.xpath("//input[@value='777' and @type='radio']");
    By title_id = By.xpath("//input[@id='control_12']");
    By desc_id = By.xpath("//textarea[@id='control_13']");
    By price_id = By.xpath("//input[@id='control_2']");
    By price_type = By.xpath("//select[@name='2_unit']");
    By my_phone = By.xpath("//input[@id='phone_37379169100']");
    By other_phone = By.xpath("//input[@id='phone_37379544975']");
    By img_id = By.xpath("//section[@id='filupload-media-container']/figure/a/img");
    By agree = By.xpath("//input[@id='agree']");
    By submit = By.xpath("//div[@class='grid_11']/button[@type='submit']");
    By error_hint_h = By.xpath("//*[contains(@id, 'error') or contains(@class, 'error')]");
    By payment_h = By.xpath("//form[@id='js-product-payment']/h1");
    By payment_id = By.xpath("//link[@rel='alternate']"); ////meta[@property='og:url']
    By success_h = By.xpath("//h2/*[contains(@class, 'success')]");
    By success_id = By.xpath("//p/a[contains(@href, 'success')]");

    public AddAdPage(WebDriver driver) {
        super(driver);  // Call MainPage constructor
        wait.until(ExpectedConditions.visibilityOfElementLocated(add_ad));
    }
    public boolean addItems(JSONArray itemsArray){
        if (itemsArray == null) return true;
        wait.until(ExpectedConditions.visibilityOfElementLocated(cabinet));
        System.out.print("Submitting items ");
        boolean result = true;
        for (int i = 0; i < itemsArray.length(); i++) {
            JSONObject item = itemsArray.getJSONObject(i);
            if (submittingForm(item) < 1) result = false;
        }
        return result;
    }
    public Integer submittingForm(JSONObject data) {
        String uri = data.getString("url");
        String price = String.valueOf(data.getInt("price"));
        String price_value = data.getString("price_type");
        String offer = data.optString("offer", "sell");
        String title = data.getString("title");
        String desc = data.getString("desc");
        boolean skip_item = data.optBoolean("skip", false);
        boolean myads = data.optBoolean("my", false);
        JSONArray imgArray = data.optJSONArray("img");
        JSONObject controls = data.optJSONObject("c");

        if (skip_item) return 1;
        driver.get(uri);
        wait.until(ExpectedConditions.visibilityOfElementLocated(agree));
        System.out.println(title);
        if (offer.equals("sell")) { driver.findElement(offer_sell).click(); }
        else { driver.findElement(offer_buy).click(); }
        WebElement agreeCheckbox = wait.until(ExpectedConditions.visibilityOfElementLocated(agree));
        driver.findElement(title_id).sendKeys(title);
        driver.findElement(desc_id).sendKeys(desc);
        driver.findElement(price_id).sendKeys(price);
        selectDropdown(driver.findElement(price_type), price_value);

        if (controls != null) {
            for (String key : controls.keySet()) {
                Object value = controls.opt(key);
                if (value instanceof String) {
                    control(key, (String) value);
                } else if (value instanceof Number) {
                    control(key, ((Number) value).toString());
                } else {
                    System.out.println("Unsupported type for key: " + key);
                }
            }
        }

        if (imgArray != null) {
            WebElement fileUpload = driver.findElement(By.id("fileupload-file-input"));
            for (int i = 0; i < imgArray.length(); i++) {
                String relativePath = "images/" + imgArray.getString(i);
                String absolutePath = new File(relativePath).getAbsolutePath();
                fileUpload.sendKeys(absolutePath);
                try {
                    String imgXpath = img_id.toString().replace("By.xpath: ", "");
                    String xpath = String.format("(%s)[%d]", imgXpath, i + 1);
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
                    System.out.println("Image " + (i + 1) + " loaded");
                } catch (Exception e) {
                    System.out.println("Image not loaded");
                }
            }
        }

        if (myads) {
            WebElement other = driver.findElement(other_phone);
            if (other.isSelected()) other.click();
        } else {
            WebElement my = driver.findElement(my_phone);
            if (my.isSelected()) my.click();
        }

        agreeCheckbox.click();
        WebElement buttonSubmit = driver.findElement(submit);
        wait.until(d -> buttonSubmit.getAttribute("disabled") == null);
        buttonSubmit.click();
        System.out.println("Submitted");
        wait.until(ExpectedConditions.stalenessOf(buttonSubmit));
        Integer id = null;
        if (isElementPresent(success_h)) {
            id = getItemIdBy(success_id);
            System.out.println(id + " Success");
            return id;
        }
        if (isElementPresent(payment_h)) {
            id = getItemIdBy(payment_id);
            System.out.println(id + " not active. Payment required.");
            return id;
        }
        List<WebElement> errorElements = driver.findElements(error_hint_h);
        if (!errorElements.isEmpty()) {
            for (WebElement errorElement : errorElements) {
                String elementText = errorElement.getText();
                if (!elementText.trim().isEmpty()) {
                    System.out.println("Warning content: " + elementText);
                }
                List<WebElement> childElements = errorElement.findElements(By.xpath(".//*"));
                for (WebElement childElement : childElements) {
                    String childText = childElement.getText();
                    if (!childText.trim().isEmpty()) {
                        System.out.println("Warning content: " + childText);
                    }
                }
            }
            return -1; //Error or Warning
        }
        return 1;
//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if (true) {
//            throw new RuntimeException("Stopping execution");
//        }

//            if (errorSection.isDisplayed()) {
//                errorSection.findElements(By.tagName("li")).forEach(li -> System.out.println(li.getText()));
//            } else {
//                System.out.println("FALSE !!!");
//            }
    }
    private Integer getItemIdBy(By element) {
        return Integer.parseInt(driver.findElement(element)
                .getAttribute("href")
                .replaceAll("^.*?md.*?(\\d+).*$", "$1"));
    }
    private void control(String id, String val) {
        WebElement element = driver.findElement(By.id(id));
        if (element.getTagName().equals("select")) {
            selectDropdown(element, val);
        } else if (element.getTagName().equals("input") && element.getAttribute("type").equals("text")) {
            element.sendKeys(val);
        } else {
            System.out.println(id + " not present");
        }
    }
    private void selectDropdown(WebElement dropdown, String value) {
        initializeSelect(dropdown);
        selectByValue(value);
    }

}

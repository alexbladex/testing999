package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;

public class AddItemsPage extends LoggedInPage {
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
    By payment_h = By.xpath("//form[@id='js-product-payment']/h1");
    By success_h = By.xpath("//a[contains(@href, 'success') and normalize-space()='Пропустить']");
    //переделать чтоб небыло русского или румынского текста

    public AddItemsPage(WebDriver driver) {
        super(driver);  // Call MainPage constructor
        wait.until(ExpectedConditions.visibilityOfElementLocated(add_ad));
    }
    public void addItems(JSONArray itemsArray){
        if (itemsArray == null) return;
        wait.until(ExpectedConditions.visibilityOfElementLocated(cabinet));
        System.out.println("Submitting items");
        for (int i = 0; i < itemsArray.length(); i++) {
            JSONObject item = itemsArray.getJSONObject(i);
            fillingForm(item);
        }
    }
    private void fillingForm(JSONObject data) {
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

        if (skip_item) return;
        driver.get(uri);
        wait.until(ExpectedConditions.visibilityOfElementLocated(agree));
        if (offer.equals("sell")) {driver.findElement(offer_sell).click();}
        else {driver.findElement(offer_buy).click();}
        WebElement agreeCheckbox = wait.until(ExpectedConditions.visibilityOfElementLocated(agree));
        driver.findElement(title_id).sendKeys(title);
        driver.findElement(desc_id).sendKeys(desc);
        driver.findElement(price_id).sendKeys(price);
        selectDropdown(driver.findElement(price_type), price_value);
        System.out.println("Filling in the controls");

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
            driver.findElement(other_phone).click();
        } else {
            driver.findElement(my_phone).click();
        }

        agreeCheckbox.click();
        System.out.println("Agree");
        WebElement buttonSubmit = driver.findElement(submit);
        wait.until(d -> buttonSubmit.getAttribute("disabled") == null);
        buttonSubmit.click();
        System.out.println("Submit");
        wait.until(ExpectedConditions.visibilityOfElementLocated(add_ad));
        boolean success = isElementPresent(success_h);
        if (success) {
            System.out.println("Success");
            return;
        }
        boolean payment = isElementPresent(payment_h);
        if (payment) {
            System.out.println("Item is not placed. Payment canceled");
            driver.findElement(payment_h).click();
            return;
        }
//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        if (true) {
            throw new RuntimeException("Stopping execution");
        }

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-add__text")));
            System.out.println("Success");
        } catch (Exception e) {
            WebElement errorSection = driver.findElement(By.className("board__content__errors grid_18"));
            WebElement payInfo = driver.findElement(By.className("product-payment__info__value"));
            if (payInfo.isDisplayed()) {
                System.out.println("Limit is reached");
            } else if (errorSection.isDisplayed()) {
                errorSection.findElements(By.tagName("li")).forEach(li -> System.out.println(li.getText()));
            } else {
                System.out.println("FALSE !!!");
            }
        }
    }
    public void control(String id, String val) {
        WebElement element = driver.findElement(By.id(id));
        if (element.getTagName().equals("select")) {
            selectDropdown(element, val);
        } else if (element.getTagName().equals("input") && element.getAttribute("type").equals("text")) {
            element.sendKeys(val);
        } else {
            System.out.println(id + " not present");
        }
    }
    public void selectDropdown(WebElement dropdown, String value) {
        initializeSelect(dropdown);
        selectByValue(value);
    }

}

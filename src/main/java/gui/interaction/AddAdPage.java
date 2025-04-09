package gui.interaction;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.util.*;

public class AddAdPage extends LoggedInPage {
    By items = By.xpath("//a[@href='/cabinet/items']");
    By offer_sell = By.xpath("//input[@value='776' and @type='radio']");
    By offer_buy = By.xpath("//input[@value='777' and @type='radio']");
    By title_id = By.xpath("//input[contains(@name, '#12.value')]");
    By desc_id = By.xpath("//textarea[contains(@name, '#13.value')]");
    By price_id = By.xpath("//input[contains(@name, '#2.value')]");
    By price_type = By.xpath("//select[contains(@name, '#2.value')]");
    By my_phone = By.xpath("(//input[contains(@id, 'phone')])[1]");
    By other_phone = By.xpath("(//input[contains(@id, 'phone')])[2]");
    By img_id = By.xpath("//div[@data-fancybox='gallery']/img");
    By agree = By.xpath("//input[@id='agreement']");
    By submit = By.xpath("(//form//button[@type='submit'])[2]");
    By error_hint_h = By.xpath("//*[contains(@id, 'error') or contains(@class, 'error')]");
    By payment_h = By.xpath("//h1[contains(@class, 'payment')]");
    By payment_id = By.xpath("//span/a[contains(@class, 'header')]"); ////meta[@property='og:url']
    By success_h = By.xpath("//div[contains(@class, 'success')]/h2/i[contains(@class, 'success')]");
    By success_id = By.xpath("//div[contains(@class, 'success')]/a[contains(@href, 'success')]");
    By limba_tooltip = By.xpath("//div[contains(@class, 'introjs')]//a[contains(@class, 'skipbutton')]");
    By overlay = By.xpath("(//div[contains(@class, 'tooltip')])[3]");
    By agree_error = By.xpath("//p[contains(@class, 'styles_agreement')]");

    public AdItem addDefaultAd() {
        AdTemplate temp = new AdTemplate();
        JSONObject jsonObject = temp.getAd();
        return submittingForm(jsonObject);
    }
    public AddAdPage(WebDriver driver) {
        super(driver);  // Call MainPage constructor
        wait.until(ExpectedConditions.visibilityOfElementLocated(add_ad));
    }
    public boolean addItems(JSONArray itemsArray){
        Integer[] userAdsList = null; // null = to use all ads from json
        return addItems(itemsArray, userAdsList);
    }
    public boolean addUserItems(JSONArray itemsArray){
        Integer[] userAdsList = getAdsList(PropertyReader.getProperty("userAdsList"));
        return addItems(itemsArray, userAdsList);
    }
    private boolean addItems(JSONArray itemsArray, Integer[] userList){
        if (itemsArray == null) return true;
        wait.until(ExpectedConditions.visibilityOfElementLocated(cabinet));
        System.out.print("Submitting items ");
        boolean result = true;
        Set<Integer> userSet = (userList != null) ? new HashSet<>(Arrays.asList(userList)) : null;
        for (int i = 0; i < itemsArray.length(); i++) {
            if (userSet == null || userSet.contains(i)) {
                JSONObject item = itemsArray.getJSONObject(i);
                AdItem ad = submittingForm(item);
                if (ad.getId() < 0) result = false;
            }
        }
        return result;
    }
    private Integer[] getAdsList(String userAdsList) {
        if (userAdsList == null) return null;
        Set<Integer> resultSet = new TreeSet<>(); //автоматически удаляет дубликаты, сохраняет элементы в отсортированном порядке
        String[] parts = userAdsList.split(",");

        for (String part : parts) {
            // Если часть содержит диапазон (например, "3-6")
            if (part.contains("-")) {
                String[] range = part.split("-");
                int start = Integer.parseInt(range[0]);
                int end = Integer.parseInt(range[1]);
                for (int i = start; i <= end; i++) resultSet.add(i);
            } else {
                resultSet.add(Integer.parseInt(part));
            }
        }
        //int[] resultArray = resultSet.stream().mapToInt(Integer::intValue).toArray();
        Integer[] resultArray = resultSet.stream().toArray(Integer[]::new);
        return resultArray;
    }
    protected AdItem submittingForm(JSONObject data) {
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
//        imgArray.toList().forEach(System.out::println);
//        controls.keySet().forEach(System.out::println);

        if (skip_item) return new AdItem(null, null, null, null);
        driver.get(uri);
        WebElement agreeCheckbox = wait.until(ExpectedConditions.visibilityOfElementLocated(agree));
        if (isElementVisible(limba_tooltip,3)) {
            driver.findElement(limba_tooltip).click(); // Попытка кликнуть по элементу
        }
        System.out.println(title);
        wait.until(ExpectedConditions.elementToBeClickable(
                offer.equals("sell") ? offer_sell : offer_buy
        )).click();

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
            WebElement fileUpload = driver.findElement(By.id("upload-photo"));
            for (int i = 0; i < imgArray.length(); i++) {
                String relativePath = "images/" + imgArray.getString(i);
                String absolutePath = new File(relativePath).getAbsolutePath();
                fileUpload.sendKeys(absolutePath);
                String imgXpath = img_id.toString().replace("By.xpath: ", "");
                String xpath = String.format("(%s)[%d]", imgXpath, i + 1);
                boolean isImageLoaded = false;
                for (int attempt = 0; attempt < 2; attempt++) {
                    try {
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
                        System.out.println("Image " + (i + 1) + " loaded");
                        isImageLoaded = true;
                        break;
                    } catch (Exception e) {
                        System.out.println("Error: Image " + (i + 1) + " not loaded");
                    }
                }
                if (!isImageLoaded) {
                    return new AdItem(-1, title, null, "Image not loaded");
                }
            }
        }

        if (myads) {
            WebElement other = driver.findElement(other_phone);
            if (other.isSelected()) clickTo(other);
        } else {
            WebElement my = driver.findElement(my_phone);
            if (my.isSelected()) clickTo(my);
        }

        scrollTo(agree);
        clickTo(agreeCheckbox);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(agree_error));
        WebElement buttonSubmit = driver.findElement(submit);
        clickTo(buttonSubmit);
        System.out.println("Submitted");
        wait.until(ExpectedConditions.stalenessOf(buttonSubmit));
        Integer id = null;
        if (isElementPresent(success_h)) {
            id = getItemIdBy(success_id);
            System.out.println(id + " Success");
            return new AdItem(id, title, "public", null);
        }
        if (isElementPresent(payment_h)) {
            id = getItemIdBy(payment_id);
            System.out.println(id + " not active. Payment required.");
            return new AdItem(id, title, "need_pay", null);
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
                        System.out.println("Warning child content: " + childText);
                    }
                }
            }
            return new AdItem(-1, title, null, "Warning content");
        }
        return new AdItem(null, null, null, null);
    }
//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if (true) {
//            throw new RuntimeException("Stopping execution");
//        }
    private Integer getItemIdBy(By element) {
        return Integer.parseInt(driver.findElement(element)
                .getAttribute("href")
                .replaceAll("^.*?\\/(\\d+)\\/.*$", "$1"));
    }
    private void control(String id, String val) {
        WebElement element = driver.findElement(By.name(id));
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

package org.example;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class Main {

    public static void main(String[] args) {
        String inputSummaryString = "Toyota CVT FE 08886-02505";
        String inputDescriptionString = "90$ A rămas după înlocuire, un borcan sigilat\n" +
                "\n" +
                "08886-02505\n" +
                "0888602505";
        String inputPriceString = "1600";

        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        //portable chrome name should be chrome.exe otherwise is need setBinary
        //options.setBinary("D:\\Program Files\\ChromePortable\\ChromePortable.exe");
        options.addArguments("--user-data-dir=d:\\Program Files\\ChromePortable\\Data\\profile\\");
        options.addArguments("--disable-features=WebBluetooth");
        options.addArguments("--no-default-browser-check");
        options.addArguments("--no-first-run");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-default-apps");
        WebDriver driver = new ChromeDriver(options);
        //driver.manage().deleteAllCookies();
        //driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(2));
        //driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));// если не поставить то след страница фейлится

        try {
            driver.get("https://999.md/");

            //WebElement buttonLang = driver.findElement(By.xpath("//li[@class='is-active']/button[@id='user-language-btn']"));
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            driver.switchTo().frame("topbar-panel");
            WebElement lang = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//li[@id='user-language']/button[@class='user-item-btn']")));

            //это когда проверяем видимость но сам элемент нам не нужен
            //new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.visibilityOf(registrationPage.registerButton()));

            if (lang.getText().equals("română")) {
                System.out.println("Текст RO: ");
                Actions actions = new Actions(driver);
                actions.moveToElement(lang).perform();

                lang = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@id='user-language-btn' and @data-lang='ru']")));
                System.out.println("Текст элемента: " + lang.getText());
                lang.click();
            } else {
                System.out.println("Текст RU: ");
            }
            driver.switchTo().defaultContent();

            boolean notLoggedIn = !driver.findElements(By.xpath("//a[@id='user-login' or @data-autotest='login']")).isEmpty();
            if (notLoggedIn) {
                WebElement linkLogin = driver.findElement(By.xpath("//a[@id='user-login' or @data-autotest='login']"));
                System.out.println("логин: " + linkLogin.getText());
                linkLogin.click();
                //wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("topbar-panel")));

                boolean loginContinue = !driver.findElements(By.xpath("//a[@class='login__user__buttons__logout']")).isEmpty();
                if (loginContinue) {
                    WebElement buttonLogin = driver.findElement(By.xpath("//a[@class='login__user__buttons__logout']"));
                    buttonLogin.click();
                    linkLogin = driver.findElement(By.xpath("//a[@id='user-login' or @data-autotest='login']"));
                    System.out.println("логин: " + linkLogin.getText());
                    linkLogin.click();
                }
                WebElement buttonLogin = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@class='login__form__footer__submit']")));
                WebElement username = driver.findElement(By.xpath("//input[@name=\"login\"]"));
                WebElement password = driver.findElement(By.xpath("//input[@name=\"password\"]"));
                username.sendKeys("");
                password.sendKeys("");
                System.out.println("");
                buttonLogin.click();
            }

            WebElement buttonCabinet = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='header_menu_nav__dropdown-wrapper']")));
            Actions actions = new Actions(driver);
            actions.moveToElement(buttonCabinet).perform();
            buttonCabinet = driver.findElement(By.xpath("//a[@href='/cabinet/items']"));
            buttonCabinet.click();

            boolean isItemForSale = !driver.findElements(By.xpath("//a[text()='" + inputSummaryString + "']")).isEmpty();
            /*if(isItemForSale) {
                WebElement itemForSale = driver.findElement(By.xpath("//a[text()='" + inputSummaryString + "']"));
                String itemNumber = itemForSale.getAttribute("href").replaceAll("^.*?/ru/", "").replaceAll("\\D+", ""); // https://999.md/ru/87800316
                System.out.println(itemNumber);
                WebElement checkboxItemForSale = driver.findElement(By.xpath("//input[@value='" + itemNumber + "']"));
                checkboxItemForSale.click();
                WebElement buttonDelete = driver.findElement(By.xpath("//a[@class='js-modal js-select-multiple-ads js-multi-control js-multi-delete tooltip tooltipstered']"));
                buttonDelete.click();
                WebElement agreeDelete = driver.findElement(By.xpath("//button[@class='js-form-submit button button--small']"));
                agreeDelete.click();
                System.out.println("itemNumber deleted");
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//a[text()='" + inputSummaryString + "']")));
            }*/

            WebElement buttonAddAd = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='js-add-ad' or @data-autotest='add_ad']")));
            buttonAddAd.click();
            System.out.println("Add");
            WebElement buttonTransport = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@data-category-id='658']")));
            buttonTransport.click();
            System.out.println("Transport");
            WebElement buttonOilAdd = driver.findElement(By.xpath("//a[@data-subcategory-id='6688']"));
            buttonOilAdd.click();
            System.out.println("buttonOilAdd");
            WebElement inputSummary = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='control_12']")));
            inputSummary.sendKeys(inputSummaryString);
            System.out.println("inputSummary");
            WebElement inputDescription = driver.findElement(By.xpath("//textarea[@id='control_13']"));
            inputDescription.sendKeys(inputDescriptionString);
            System.out.println("inputDescription");
            WebElement inputPrice = driver.findElement(By.xpath("//input[@id='control_2']"));
            inputPrice.sendKeys(inputPriceString);
            System.out.println("inputPrice");

            Select selectItem;
            selectItem = new Select(driver.findElement(By.xpath("//select[@name='2_unit']")));
            selectItem.selectByValue("mdl");
            selectItem = new Select(driver.findElement(By.xpath("//select[@name='1432']")));
            selectItem.selectByValue("24905");
            selectItem = new Select(driver.findElement(By.xpath("//select[@name='1439']")));
            selectItem.selectByValue("24877");
            selectItem = new Select(driver.findElement(By.xpath("//select[@name='1433']")));
            selectItem.selectByValue("24871");
            selectItem = new Select(driver.findElement(By.xpath("//select[@name='1434']")));
            selectItem.selectByValue("24972");
            selectItem = new Select(driver.findElement(By.xpath("//select[@name='1438']")));
            selectItem.selectByValue("24942");
            selectItem = new Select(driver.findElement(By.xpath("//select[@name='1437']")));
            selectItem.selectByValue("24860");
            System.out.println("Select passed");

            WebElement inputCheckbox = driver.findElement(By.xpath("//input[@id='phone_37379544975']"));
            inputCheckbox.click();
            WebElement fileInput = driver.findElement(By.id("fileupload-file-input"));
            fileInput.sendKeys("D:\\Desktop\\08886-02505.png");
            System.out.println("Picture");
            fileInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//section[@id='filupload-media-container']/figure/a")));
            System.out.println("Loaded");
            WebElement inputAgree = driver.findElement(By.xpath("//input[@id='agree']"));
            inputAgree.click();
            System.out.println("Agree");
            WebElement buttonSubmit = driver.findElement(By.xpath("//div[@class='grid_11']/button[@type='submit']"));
            wait.until(d -> buttonSubmit.getAttribute("disabled") == null);
/*
            boolean isSubmitDisabled = buttonSubmit.getAttribute("disabled") == null;
            if (isSubmitDisabled) {
                // The button does not have the 'disabled' attribute
                System.out.println("The button is enabled.");
            } else {
                // The button has the 'disabled' attribute
                System.out.println("The button is disabled.");
            }
*/
            buttonSubmit.click();
            System.out.println("Submit");
            WebElement buttonBoost = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(@href, 'success') and normalize-space()='Пропустить']")));
            buttonBoost.click();
            buttonBoost = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[normalize-space()='Перейти к объявлению']")));
            buttonBoost.click();
            System.out.println("Завершили.");
        } catch (org.openqa.selenium.NoSuchElementException e) {
            System.out.println("Элемент не найден на странице.");
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}

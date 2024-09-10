package com.example;

import gui.interaction.BasePage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface PageObject {}
public class TestListener implements ITestListener {
    private final Logger logger = LoggerFactory.getLogger(TestListener.class);
    @Override
    public void onTestFailure(ITestResult result) {
        Throwable exception = result.getThrowable();
        logger.error("Test failed.");
        if (exception != null) {
            logger.error("Error message: {}", exception.getMessage());
            logger.error("Stack trace: ", exception);
        }
        Object testInstance = result.getInstance();
        Field[] fields = testInstance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(PageObject.class)) {
                try {
                    field.setAccessible(true);
                    Object pageObject = field.get(testInstance);
                    if (pageObject instanceof BasePage) {
                        ((BasePage) pageObject).takeScreenshot();
                        return;
                    }
                } catch (IllegalAccessException e) {
                    logger.error("Error accessing field: {}", e.getMessage());
                }
            }
        }
    }
}

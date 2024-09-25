package gui.interaction;

import org.slf4j.MDC;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class EventListener implements ITestListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onTestStart(ITestResult result) {
        MDC.put("testMethod", result.getName());
        MDC.put("testClass", result.getTestClass().getRealClass().getSimpleName());
        logger.info("Test started with parameters: {}", Arrays.toString(result.getParameters()));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test passed.");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        BasePage currentPage = BasePage.getCurrentPage();
        if (currentPage != null) {
            logger.error("Test failed on page: {} !!!", currentPage.getClass().getSimpleName());
            currentPage.takeScreenshot(result.getTestClass().getRealClass().getSimpleName(),result.getName());
        } else logger.error("Test failed.");

        Throwable exception = result.getThrowable();
        if (exception != null) {
            logger.error("{}: {}", exception.getClass().getSimpleName(), exception.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test skipped.");
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        logger.warn("Test partially failed.");
    }

    @Override
    public void onStart(ITestContext context) {
        logger.info("Test suite started: {}", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("Test suite finished: {}\n", context.getName());
        MDC.remove("testMethod");
        MDC.remove("testClass");
//        DriverFactory.close();
    }
}

package gui.interaction;

import org.slf4j.MDC;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;

public class TestListener implements ITestListener {
    private final Logger logger = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        MDC.put("testName", result.getName());
        logger.info("Test started with parameters: {}", Arrays.toString(result.getParameters()));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test passed.");
        MDC.remove("testName");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable exception = result.getThrowable();
        logger.error("Test failed.");
        if (exception != null) {
            logger.error("Error message: {}", exception.getMessage());
            logger.error("Stack trace: ", exception);
        }
        MDC.remove("testName");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test skipped.");
        MDC.remove("testName");
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        logger.warn("Test partially failed.");
        MDC.remove("testName");
    }

    @Override
    public void onStart(ITestContext context) {
        logger.info("Test suite started: {}", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("Test suite finished: {}", context.getName());
//        DriverFactory.close();
    }
}

package gui.interaction;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    // Used for test methods
    private int count = 0;
    private static final int MAX_RETRY = 1;
    @Override
    public boolean retry(ITestResult result) {
        if (count < MAX_RETRY) {
            count++;
            return true; // Retry
        }
        return false; // Quit
    }
}
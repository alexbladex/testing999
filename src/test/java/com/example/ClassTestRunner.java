package com.example;
import org.testng.TestNG;

public class ClassTestRunner {
    public static void main(String[] args) {
        //Direct class call. When you need to run a specific classes of tests without complex setup.
        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[] { ExampleTest.class });
        testng.run();
    }
}

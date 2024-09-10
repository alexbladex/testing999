package com.example;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;

import java.util.Collections;

public class XmlTestRunner {
    public static void main(String[] args) {
        TestNG testng = new TestNG();
        testng.setXmlSuites(Collections.singletonList(new XmlSuite() {{
            setSuiteFiles(Collections.singletonList("src/test/java/org/example/testng-suite.xml"));
        }}));
        testng.run();
    }
}

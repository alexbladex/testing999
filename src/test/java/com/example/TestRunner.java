package com.example;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.List;

public class TestRunner {
    public static void main(String[] args) {
        TestNG testng = new TestNG();

        // Создание XML Suite программно
        XmlSuite suite = new XmlSuite();
        suite.setName("Suite");

        // Опционально Настройка параллелизма на уровне тестов или классов
        suite.setParallel(XmlSuite.ParallelMode.TESTS);
        //XmlSuite.ParallelMode.CLASSES
        suite.setThreadCount(4);

        // Создание теста
        XmlTest test = new XmlTest(suite);
        test.setName("Test");

        // Опционально Передача параметров
        test.addParameter("param1", "value1");
        test.addParameter("param2", "value2");

        // Добавление классов для тестирования
        List<XmlClass> classes = new ArrayList<>();
        classes.add(new XmlClass("org.example.ExampleTest"));
        test.setXmlClasses(classes);

        // Добавление Suite в TestNG
        List<XmlSuite> suites = new ArrayList<>();
        suites.add(suite);
        testng.setXmlSuites(suites);

        // Опционально Добавление listener'ов
        testng.addListener(new TestListenerAdapter());

        // Запуск тестов
        testng.run();
    }
}

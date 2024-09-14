package com.example;

import ch.qos.logback.classic.PatternLayout;
import gui.interaction.MethodConverter;

public class CustomPatternLayout extends PatternLayout {
    /* in the logback.xml
        <encoder>
            <pattern>%d{HH:mm:ss} [%thread] %-15logger{0} %-5level %method - %msg%n</pattern>
            <layout class="com.example.CustomPatternLayout" />
        </encoder> */
    static {
        defaultConverterMap.put("method", MethodConverter.class.getName());
    }
}
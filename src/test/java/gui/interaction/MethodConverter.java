package gui.interaction;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.MDC;

public class MethodConverter extends ClassicConverter {
    // Used for logback
    @Override
    public String convert(ILoggingEvent event) {
        String className = event.getLoggerName();
        String methodName = event.getCallerData()[0].getMethodName();
        if (methodName.startsWith("onTest")) return String.format("%s.%s", MDC.get("testClass"), MDC.get("testMethod")); //MDC.put in the Listener
        return String.format("%s.%s", className.substring(className.lastIndexOf(".") + 1), methodName);
    }
}
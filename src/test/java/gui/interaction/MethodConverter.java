package gui.interaction;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.MDC;

public class MethodConverter extends ClassicConverter {
    // Used for logback
    @Override
    public String convert(ILoggingEvent event) {
        String methodName = event.getCallerData()[0].getMethodName();
        if (methodName.startsWith("onTest")) return MDC.get("testName"); //MDC.put in the Listener
        return methodName;
    }
}
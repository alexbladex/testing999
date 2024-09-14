package gui.interaction;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.MDC;

public class ClassNameAppender extends AppenderBase<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent eventObject) {
        String callerClass = eventObject.getLoggerName();
        callerClass = callerClass.substring(callerClass.lastIndexOf('.') + 1);
        MDC.put("testClass", callerClass);
    }
}

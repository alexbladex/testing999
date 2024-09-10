package gui.interaction;

import ch.qos.logback.classic.PatternLayout;

public class CustomPatternLayout extends PatternLayout {
    //<layout class="gui.interaction.CustomPatternLayout" />
    static {
        defaultConverterMap.put("method", MethodConverter.class.getName());
    }
}
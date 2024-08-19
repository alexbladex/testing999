package org.example;

public class Config {
    public static boolean debug;

    static {
        debug = PropertyReader.getPropertyBoolean("EnablePrintLog");
    }
}

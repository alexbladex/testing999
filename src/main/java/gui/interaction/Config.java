package gui.interaction;

public class Config {
    public static boolean debug, rmCookies;

    static {
        debug = PropertyReader.getPropertyBoolean("EnablePrintLog");
        rmCookies = PropertyReader.getPropertyBoolean("DeletePrevSession");
    }
}

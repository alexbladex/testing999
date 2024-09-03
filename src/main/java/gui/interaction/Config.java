package gui.interaction;

public class Config {
    public static boolean debug, rmCookies;

    static {
        debug = PropertyReader.getPropertyBoolean("printLog");
        rmCookies = PropertyReader.getPropertyBoolean("deleteSession");
    }
}

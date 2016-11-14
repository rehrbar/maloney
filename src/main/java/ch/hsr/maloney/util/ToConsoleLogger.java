package ch.hsr.maloney.util;

/**
 * Created by olive_000 on 14.11.2016.
 */
public class ToConsoleLogger implements Logger {
    @Override
    public void logError(String msg, Exception ex) {
        toConsole("ERROR: " + msg + " Exception: " + ex.getMessage());
    }

    @Override
    public void logInfo(String msg) {
        toConsole(msg);
    }

    @Override
    public void logTrace(String msg) {
        toConsole(msg);
    }

    @Override
    public void logDebug(String msg) {
        toConsole("DEBUG: " + msg);
    }

    @Override
    public void logWarn(String msg, Exception ex) {
        toConsole(msg + ex.getMessage());
    }

    @Override
    public void logFatal(String msg, Exception ex) {
        toConsole(msg + ex.getMessage());
    }

    private void toConsole(String msg){
        System.out.println(msg);
    }
}

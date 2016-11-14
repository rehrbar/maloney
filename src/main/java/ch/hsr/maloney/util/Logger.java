package ch.hsr.maloney.util;

/**
 * Created by olive_000 on 08.11.2016.
 */
public interface Logger {
    /**
     * Log an Error
     *
     * @param msg   Message for context
     * @param ex    Exception that was created
     */
    void logError(String msg,Exception ex);

    /**
     * Log Info
     *
     * @param msg   Information to be logged
     */
    void logInfo(String msg);

    /**
     * Log Trace
     *
     * @param msg   Trace to be logged
     */
    void logTrace(String msg);

    /**
     * Log a debug message.
     * @param msg   Debug message to be logged.
     */
    void logDebug(String msg);

    /**
     * Log a Warning
     *
     * @param msg   Message for context
     * @param ex    Exception that was created
     */
    void logWarn(String msg, Exception ex);

    /**
     * Log a Fatal Error
     *
     * @param msg   Message for context
     * @param ex    Exception that was created
     */
    void logFatal(String msg, Exception ex);
}

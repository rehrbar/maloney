package ch.hsr.maloney.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by r1ehrbar on 14.11.2016.
 */
public class Log4jLogger implements ch.hsr.maloney.util.Logger {
    private Logger logger;

    public Log4jLogger(){
        logger = LogManager.getLogger();
    }

    @Override
    public void logError(String msg, Exception ex) {
        logger.error(msg, ex);
    }

    @Override
    public void logInfo(String msg) {
        logger.info(msg);
    }

    @Override
    public void logTrace(String msg) {
        logger.trace(msg);
    }

    @Override
    public void logDebug(String msg) {
        logger.debug(msg);
    }

    @Override
    public void logWarn(String msg, Exception ex) {
        logger.warn(msg, ex);
    }

    @Override
    public void logFatal(String msg, Exception ex) {
        logger.fatal(msg, ex);
    }
}

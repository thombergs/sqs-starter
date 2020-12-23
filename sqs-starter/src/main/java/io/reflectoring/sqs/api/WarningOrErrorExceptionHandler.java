package io.reflectoring.sqs.api;

import com.amazonaws.services.sqs.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An ExceptionHandler logs an error with stacktrace on unknown errors, and a warning without stacktrace on
 * certain known errors.
 */
public abstract class WarningOrErrorExceptionHandler implements ExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(WarningOrErrorExceptionHandler.class);

    @Override
    public ExceptionHandlerDecision handleException(Message message, Exception e) {
        if (isKnownError(e)) {
            logger.warn("Known exception when processing message {} (reason: {}). Message has not been deleted from SQS and will be retried.", message.getMessageId(), getWarnMessageForKnownError(e));
        } else {
            logger.warn("error while processing message {} - message has not been deleted from SQS and will be retried:", message.getMessageId(), e);
        }
        return ExceptionHandlerDecision.RETRY;
    }

    /**
     * Decides if a message is a known error. For a known error, we'll only log a warning with a message (see {@link #getWarnMessageForKnownError(Throwable)}).
     * For an unknown error, we'll log a complete stack trace.
     *
     * Implement this method to handle all errors that you think should not pollute the logs with stack traces.
     */
    protected abstract boolean isKnownError(Throwable e);

    /**
     * The message to be logged when a known error occurs. By default, it will just return e.getMessage(), but you
     * may want to show the message of a certain other exception in the stacktrace. In this case, just override this
     * method.
     */
    protected String getWarnMessageForKnownError(Throwable e) {
        return e.getMessage();
    }
}

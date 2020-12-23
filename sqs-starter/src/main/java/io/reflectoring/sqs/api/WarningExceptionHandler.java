package io.reflectoring.sqs.api;

import com.amazonaws.services.sqs.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An ExceptionHandler that logs a warning without a stack trace every time an SQS message fails to be processed.
 */
public class WarningExceptionHandler implements ExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(WarningExceptionHandler.class);

    @Override
    public ExceptionHandlerDecision handleException(Message message, Exception e) {
        logger.warn("Exception when processing message {} (reason: {}). Message has not been deleted from SQS and will be retried.", message.getMessageId(), e.getMessage());
        return ExceptionHandlerDecision.RETRY;
    }
}

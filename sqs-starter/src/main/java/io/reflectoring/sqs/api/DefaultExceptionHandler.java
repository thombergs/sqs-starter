package io.reflectoring.sqs.api;

import com.amazonaws.services.sqs.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An ExceptionHandler that logs an error with a stack trace every time an SQS message failed to be processed correctly.
 */
public class DefaultExceptionHandler implements ExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @Override
    public ExceptionHandlerDecision handleException(Message message, Exception e) {
        logger.warn("error while processing message {} - message has not been deleted from SQS and will be retried:", message.getMessageId(), e);
        return ExceptionHandlerDecision.RETRY;
    }
}

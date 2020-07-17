package io.reflectoring.sqs.api;

import com.amazonaws.services.sqs.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultExceptionHandler implements ExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @Override
    public void handleException(Message message, Exception e) {
        logger.warn("error while processing message {} - message will be retried according to SQS properties:", message.getMessageId(), e);
    }
}

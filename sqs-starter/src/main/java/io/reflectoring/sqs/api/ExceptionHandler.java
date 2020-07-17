package io.reflectoring.sqs.api;

import com.amazonaws.services.sqs.model.Message;

public interface ExceptionHandler {

    /**
     * Handles any exception that is thrown during message processing by an {@link SqsMessageHandler}.
     */
    void handleException(Message message, Exception e);

    static ExceptionHandler defaultExceptionHandler() {
        return new DefaultExceptionHandler();
    }
}

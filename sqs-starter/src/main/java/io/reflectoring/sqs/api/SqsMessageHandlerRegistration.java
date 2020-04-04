package io.reflectoring.sqs.api;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Include a bean of this type in the Spring application context to register a poller that polls messages
 * from an SQS queue and passes them to an {@link SqsMessageHandler}.
 *
 * @param <T> the type of the messages to process.
 */
public interface SqsMessageHandlerRegistration<T> {

    /**
     * The message handler that shall process the messages polled from SQS.
     */
    SqsMessageHandler<T> messageHandler();

    /**
     * A human-readable name for the message handler. This is used to name the message handler threads.
     */
    String name();

    /**
     * Configuration properties for the message handler.
     */
    SqsMessageHandlerProperties messageHandlerProperties();

    /**
     * Configuration properties for the message poller.
     */
    SqsMessagePollerProperties messagePollerProperties();

    /**
     * The SQS client to use for polling messages from SQS.
     */
    AmazonSQS sqsClient();

    /**
     * The {@link ObjectMapper} to use for deserializing messages from SQS.
     */
    ObjectMapper objectMapper();
}

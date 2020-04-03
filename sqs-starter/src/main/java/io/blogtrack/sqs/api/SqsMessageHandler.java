package io.blogtrack.sqs.api;

/**
 * Interface for handlers of SQS messages.
 *
 * @param <T> the type of messages this message handler can process.
 */
public interface SqsMessageHandler<T> {

    /**
     * Processes a message.
     */
    void handle(T message);

    /**
     * Returns the type of messages this message handler can process.
     */
    Class<T> messageType();

}

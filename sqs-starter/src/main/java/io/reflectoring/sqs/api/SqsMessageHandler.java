package io.reflectoring.sqs.api;

/**
 * Interface for handlers of SQS messages.
 *
 * @param <T> the type of messages this message handler can process.
 */
public interface SqsMessageHandler<T> {

    /**
     * Called just before the handle() method is being called.
     * You can implement this method to initialize the thread handling the message with {@link ThreadLocal}s or add
     * an MDC context for logging or something similar. Just make sure that you clean up after yourself in the onAfterHandle()
     * method.
     * <p>
     * The default implementation does nothing.
     */
    default void onBeforeHandle(T message) {

    }

    /**
     * Called after a message has been handled, irrespective of the success. In case of an exception during the
     * invocation of handle(), onAfterHandle() is called AFTER the exception has been handled by an {@link ExceptionHandler}
     * so that the exception handler still has any context that might have been set in onBeforeHandle().
     * <p>
     * The default implementation does nothing.
     */
    default void onAfterHandle(T message) {

    }

    /**
     * Processes a message.
     */
    void handle(T message);

    /**
     * Returns the type of messages this message handler can process.
     */
    Class<T> messageType();

}

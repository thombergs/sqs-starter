package io.reflectoring.sqs.api;

import lombok.Data;

@Data
public class SqsMessageHandlerProperties {

    /**
     * The size of the thread pool of {@link SqsMessageHandler}s.
     */
    private int handlerThreadPoolSize = 10;

    /**
     * The size of the in-memory queue of the thread pool of {@link SqsMessageHandler}s.
     */
    private int handlerQueueSize = 1000;
}

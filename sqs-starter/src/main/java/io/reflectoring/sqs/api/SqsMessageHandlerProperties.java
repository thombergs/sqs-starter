package io.reflectoring.sqs.api;

import lombok.Data;

@Data
public class SqsMessageHandlerProperties {

    private int handlerThreadPoolSize = 10;

    private int handlerQueueSize = 1000;

    /**
     * The size of the thread pool of {@link SqsMessageHandler}s.
     */
    SqsMessageHandlerProperties withHandlerThreadPoolSize(int handlerThreadPoolSize){
        this.handlerThreadPoolSize = handlerThreadPoolSize;
        return this;
    }

    /**
     * The size of the in-memory queue of the thread pool of {@link SqsMessageHandler}s.
     */
    SqsMessageHandlerProperties withHandlerQueueSize(int handlerQueueSize){
        this.handlerQueueSize = handlerQueueSize;
        return this;
    }
}

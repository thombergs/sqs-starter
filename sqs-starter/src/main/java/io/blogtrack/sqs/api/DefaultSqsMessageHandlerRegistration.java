package io.blogtrack.sqs.api;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class DefaultSqsMessageHandlerRegistration<T> implements SqsMessageHandlerRegistration<T> {

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public SqsMessageHandlerProperties messageHandlerProperties() {
        return new SqsMessageHandlerProperties();
    }

    @Override
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

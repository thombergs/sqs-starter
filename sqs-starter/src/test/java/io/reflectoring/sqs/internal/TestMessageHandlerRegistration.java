package io.reflectoring.sqs.internal;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reflectoring.sqs.api.*;

class TestMessageHandlerRegistration implements SqsMessageHandlerRegistration<TestMessage> {

    private final AmazonSQS client;
    private final ObjectMapper objectMapper;
    private final TestMessageHandler messageHandler;

    public TestMessageHandlerRegistration(
            AmazonSQS client,
            ObjectMapper objectMapper,
            TestMessageHandler messageHandler) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.messageHandler = messageHandler;
    }

    @Override
    public SqsMessageHandler<TestMessage> messageHandler() {
        return this.messageHandler;
    }

    @Override
    public String name() {
        return "testMessageHandler";
    }

    @Override
    public SqsMessageHandlerProperties messageHandlerProperties() {
        return new SqsMessageHandlerProperties();
    }

    @Override
    public SqsMessagePollerProperties messagePollerProperties() {
        return new SqsMessagePollerProperties("http://localhost:4576/queue/testMessages");
    }

    @Override
    public AmazonSQS sqsClient() {
        return this.client;
    }

    @Override
    public ObjectMapper objectMapper() {
        return this.objectMapper;
    }
}

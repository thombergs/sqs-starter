package io.reflectoring.sqs.internal;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reflectoring.sqs.api.SqsMessagePublisher;

class TestMessagePublisher extends SqsMessagePublisher<TestMessage> {

    TestMessagePublisher(AmazonSQS sqsClient, ObjectMapper objectMapper) {
        super("http://localhost:4576/queue/testMessages", sqsClient, objectMapper);
    }

}

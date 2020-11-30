package co.cargoai.sqs.internal;

import co.cargoai.sqs.api.SqsMessagePublisher;
import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;

class TestMessagePublisher extends SqsMessagePublisher<TestMessage> {

    TestMessagePublisher(AmazonSQS sqsClient, ObjectMapper objectMapper) {
        super("http://localhost:4576/queue/testMessages", sqsClient, objectMapper);
    }

}

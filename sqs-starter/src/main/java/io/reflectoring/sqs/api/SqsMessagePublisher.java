package io.reflectoring.sqs.api;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SqsMessagePublisher<T> {

    private static final Logger logger = LoggerFactory.getLogger(SqsMessagePublisher.class);
    private final String sqsQueueUrl;
    private final AmazonSQS sqsClient;
    private final ObjectMapper objectMapper;
    private final RetryRegistry retryRegistry;

    public SqsMessagePublisher(
            String sqsQueueUrl,
            AmazonSQS sqsClient,
            ObjectMapper objectMapper) {
        this.sqsQueueUrl = sqsQueueUrl;
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.retryRegistry = defaultRetryRegistry();
    }

    public SqsMessagePublisher(
            String sqsQueueUrl,
            AmazonSQS sqsClient,
            ObjectMapper objectMapper,
            RetryRegistry retryRegistry) {
        this.sqsQueueUrl = sqsQueueUrl;
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.retryRegistry = retryRegistry;
    }

    private RetryRegistry defaultRetryRegistry() {
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(IntervalFunction.ofExponentialBackoff())
                .build();
        return RetryRegistry.of(retryConfig);
    }

    public void publish(T message) {
        publish(message, new SendMessageRequest());
    }

    /**
     * Publishes a message with a pre-configured {@link SendMessageRequest} which gives you all the options you may need
     * from the underlying SQS client. Note that the `queueUrl` and `messageBody` must not be set because they will be set
     * by the this publisher.
     */
    public void publish(T message, SendMessageRequest preConfiguredRequest) {
        if (preConfiguredRequest.getQueueUrl() != null) {
            throw new IllegalArgumentException("attribute queueUrl of pre-configured request must not be set!");
        }
        if (preConfiguredRequest.getMessageBody() != null) {
            throw new IllegalArgumentException("message body of pre-configured request must not be set!");
        }

        Retry retry = retryRegistry.retry("publish");
        retry.getEventPublisher()
                .onError(event -> logger.warn("error publishing message to queue {}", this.sqsQueueUrl));
        retry.executeRunnable(() -> doPublish(message, preConfiguredRequest));
    }

    private void doPublish(T message, SendMessageRequest preConfiguredRequest) {
        try {
            logger.debug("sending message to SQS queue {}", sqsQueueUrl);
            SendMessageRequest request = preConfiguredRequest
                    .withQueueUrl(sqsQueueUrl)
                    .withMessageBody(objectMapper.writeValueAsString(message));
            SendMessageResult result = sqsClient.sendMessage(request);

            if (result.getSdkHttpMetadata().getHttpStatusCode() != 200) {
                throw new RuntimeException(String.format("got error response from SQS queue %s: %s",
                        sqsQueueUrl,
                        result.getSdkHttpMetadata()));
            }

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("error sending message to SQS: ", e);
        }
    }
}

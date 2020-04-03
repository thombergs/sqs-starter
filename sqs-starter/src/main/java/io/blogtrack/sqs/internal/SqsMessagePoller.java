package io.blogtrack.sqs.internal;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.blogtrack.sqs.api.SqsMessageHandler;
import io.blogtrack.sqs.api.SqsMessagePollerProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Polls messages from an SQS queue in potentially multiple threads at regular intervals.
 *
 * @param <T> the type of message.
 */
@RequiredArgsConstructor
class SqsMessagePoller<T> {

    private static final Logger logger = LoggerFactory.getLogger(SqsMessagePoller.class);
    private final SqsMessageHandler<T> messageHandler;
    private final SqsMessagePollerProperties pollingProperties;
    private final AmazonSQS sqsClient;
    private final ObjectMapper objectMapper;
    private final ScheduledThreadPoolExecutor pollerThreadPool;
    private final ThreadPoolExecutor handlerThreadPool;

    void start() {
        logger.info("starting SqsMessagePoller");
        for (int i = 0; i < pollerThreadPool.getCorePoolSize(); i++) {
            logger.info("starting SqsMessagePoller - thread {}", i);
            pollerThreadPool.scheduleWithFixedDelay(
                    this::poll,
                    1,
                    pollingProperties.getPollDelay().toSeconds(),
                    TimeUnit.SECONDS);
        }
    }

    void stop() {
        logger.info("stopping SqsMessagePoller");
        pollerThreadPool.shutdownNow();
        handlerThreadPool.shutdownNow();
    }

    private void poll() {

        List<Message> messages = fetchMessages(this.sqsClient, this.pollingProperties);

        for (Message sqsMessage : messages) {
            try {
                final T message = objectMapper.readValue(sqsMessage.getBody(), messageHandler.messageType());
                handlerThreadPool.submit(() -> {
                    messageHandler.handle(message);
                    acknowledgeMessage(sqsMessage);
                });
            } catch (JsonProcessingException e) {
                logger.warn("error parsing message: ", e);
            }

            // we let other exceptions bubble up to trigger a retry
        }
    }

    private List<Message> fetchMessages(
            AmazonSQS sqsClient,
            SqsMessagePollerProperties pollingProperties) {

        logger.debug("polling messages from SQS queue {}", pollingProperties.getQueueUrl());

        ReceiveMessageRequest request = new ReceiveMessageRequest()
                .withMaxNumberOfMessages(pollingProperties.getBatchSize())
                .withQueueUrl(pollingProperties.getQueueUrl())
                .withWaitTimeSeconds((int) pollingProperties.getWaitTime().toSeconds());

        ReceiveMessageResult result = sqsClient.receiveMessage(request);

        if (result.getSdkHttpMetadata().getHttpStatusCode() != 200) {
            logger.error("got error response from SQS queue {}: {}",
                    pollingProperties.getQueueUrl(),
                    result.getSdkHttpMetadata());
            return Collections.emptyList();
        }

        if (result.getMessages().isEmpty()) {
            logger.debug("empty polling result from SQS queue {}", pollingProperties.getQueueUrl());
            return Collections.emptyList();
        }

        logger.debug("polled {} messages from SQS queue {}",
                result.getMessages().size(),
                pollingProperties.getQueueUrl());

        return result.getMessages();
    }

    private void acknowledgeMessage(Message message) {
        sqsClient.deleteMessage(pollingProperties.getQueueUrl(), message.getReceiptHandle());
    }

}

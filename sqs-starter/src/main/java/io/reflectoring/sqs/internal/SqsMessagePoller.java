package io.reflectoring.sqs.internal;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reflectoring.sqs.api.ExceptionHandler;
import io.reflectoring.sqs.api.SqsMessageHandler;
import io.reflectoring.sqs.api.SqsMessagePollerProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final String name;
    private final SqsMessageHandler<T> messageHandler;
    private final SqsMessageFetcher messageFetcher;
    private final SqsMessagePollerProperties pollingProperties;
    private final AmazonSQS sqsClient;
    private final ObjectMapper objectMapper;
    private final ScheduledThreadPoolExecutor pollerThreadPool;
    private final ThreadPoolExecutor handlerThreadPool;
    private final ExceptionHandler exceptionHandler;


    void start() {
        logger.info("starting SqsMessagePoller");
        for (int i = 0; i < pollerThreadPool.getCorePoolSize(); i++) {
            logger.info("starting SqsMessagePoller ({}) - thread {}", this.name, i);
            pollerThreadPool.scheduleWithFixedDelay(
                    this::pollMessages,
                    pollingProperties.getPollDelay().getSeconds(),
                    pollingProperties.getPollDelay().getSeconds(),
                    TimeUnit.SECONDS);
        }
    }

    void stop() {
        logger.info("stopping SqsMessagePoller");
        pollerThreadPool.shutdownNow();
        handlerThreadPool.shutdownNow();
    }

    void pollMessages() {
        try {
            List<Message> messages = messageFetcher.fetchMessages();
            for (Message sqsMessage : messages) {
                handleMessage(sqsMessage);
            }
        } catch (Exception e) {
            logger.error("error fetching messages from queue {}:", pollingProperties.getQueueUrl(), e);
        }
    }

    private void handleMessage(Message sqsMessage) {
        try {
            final T message = objectMapper.readValue(sqsMessage.getBody(), messageHandler.messageType());
            handlerThreadPool.submit(() -> {
                try {
                    messageHandler.onBeforeHandle(message);
                    messageHandler.handle(message);
                    acknowledgeMessage(sqsMessage);
                    logger.debug("message {} processed successfully - message has been deleted from SQS", sqsMessage.getMessageId());
                } catch (Exception e) {
                    ExceptionHandler.ExceptionHandlerDecision result = exceptionHandler.handleException(sqsMessage, e);
                    switch (result) {
                        case RETRY:
                            // do nothing ... the message hasn't been deleted from SQS yet, so it will be retried
                            break;
                        case DELETE:
                            acknowledgeMessage(sqsMessage);
                            break;
                    }
                } finally {
                    messageHandler.onAfterHandle(message);
                }

            });
        } catch (JsonProcessingException e) {
            logger.warn("error parsing message {} - deleting message from SQS because it's not recoverable: ", sqsMessage.getMessageId(), e);
        }
    }

    private void acknowledgeMessage(Message message) {
        sqsClient.deleteMessage(pollingProperties.getQueueUrl(), message.getReceiptHandle());
    }

}

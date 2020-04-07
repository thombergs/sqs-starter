package io.reflectoring.sqs.internal;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reflectoring.sqs.api.SqsMessageHandler;
import io.reflectoring.sqs.api.SqsMessagePollerProperties;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polls messages from an SQS queue in potentially multiple threads at regular intervals.
 *
 * @param <T> the type of message.
 */
@RequiredArgsConstructor
class SqsMessagePoller<T> {

  private static final Logger logger = LoggerFactory.getLogger(SqsMessagePoller.class);
  private final SqsMessageHandler<T> messageHandler;
  private final SqsMessageFetcher messageFetcher;
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

  void poll() {

    List<Message> messages = messageFetcher.fetchMessages();

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

  private void acknowledgeMessage(Message message) {
    sqsClient.deleteMessage(pollingProperties.getQueueUrl(), message.getReceiptHandle());
  }

}

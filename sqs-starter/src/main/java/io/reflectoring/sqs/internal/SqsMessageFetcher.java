package io.reflectoring.sqs.internal;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import io.reflectoring.sqs.api.SqsMessagePollerProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Fetches a batch of messages from SQS.
 */
@RequiredArgsConstructor
class SqsMessageFetcher {

  private static final Logger logger = LoggerFactory.getLogger(SqsMessageFetcher.class);
  private final AmazonSQS sqsClient;
  private final SqsMessagePollerProperties properties;

  List<Message> fetchMessages() {

    logger.debug("fetching messages from SQS queue {}", properties.getQueueUrl());

    ReceiveMessageRequest request = new ReceiveMessageRequest()
        .withMaxNumberOfMessages(properties.getBatchSize())
        .withQueueUrl(properties.getQueueUrl())
        .withWaitTimeSeconds((int) properties.getWaitTime().getSeconds());

    ReceiveMessageResult result = sqsClient.receiveMessage(request);

    if (result.getSdkHttpMetadata() == null) {
      logger.error("cannot determine success from response for SQS queue {}: {}",
              properties.getQueueUrl(),
              result.getSdkResponseMetadata());
      return Collections.emptyList();
    }

    if (result.getSdkHttpMetadata().getHttpStatusCode() != 200) {
      logger.error("got error response from SQS queue {}: {}",
          properties.getQueueUrl(),
          result.getSdkHttpMetadata());
      return Collections.emptyList();
    }

    logger.debug("polled {} messages from SQS queue {}",
        result.getMessages().size(),
        properties.getQueueUrl());

    return result.getMessages();
  }

}

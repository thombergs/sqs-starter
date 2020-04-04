package io.reflectoring.sqs.api;

import lombok.Data;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Data
public class SqsMessagePollerProperties {

    /**
     * The URL to the SQS queue.
     */
    private final String queueUrl;

    /**
     * The delay the poller should wait for the next poll after the previous poll has finished.
     */
    private Duration pollDelay = Duration.of(1, ChronoUnit.SECONDS);

    /**
     * The duration the SQS client should wait for messages before closing the connection.
     */
    private Duration waitTime = Duration.ofSeconds(1);

    /**
     * The maximum number of messages to pull from SQS with each poll.
     */
    private int batchSize = 10;

    public SqsMessagePollerProperties(String queueUrl) {
        this.queueUrl = queueUrl;
    }
}

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

    private Duration pollDelay = Duration.of(1, ChronoUnit.SECONDS);

    private Duration waitTime = Duration.ofSeconds(1);

    private int batchSize = 10;

    public SqsMessagePollerProperties(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    /**
     * The delay the poller should wait for the next poll after the previous poll has finished.
     */
    public SqsMessagePollerProperties withPollDelay(Duration pollDelay){
        this.pollDelay = pollDelay;
        return this;
    }

    /**
     * The duration the SQS client should wait for messages before closing the connection.
     */
    public SqsMessagePollerProperties withWaitTime(Duration waitTime){
        this.waitTime = waitTime;
        return this;
    }

    /**
     * The maximum number of messages to pull from SQS with each poll.
     */
    public SqsMessagePollerProperties withBatchSize(int batchSize){
        this.batchSize = batchSize;
        return this;
    }

}

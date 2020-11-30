package co.cargoai.sqs.api;

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

    private int pollingThreads = 1;

    private ExceptionHandler exceptionHandler = ExceptionHandler.defaultExceptionHandler();

    public SqsMessagePollerProperties(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    /**
     * The delay the poller should wait for the next poll after the previous poll has finished.
     */
    public SqsMessagePollerProperties withPollDelay(Duration pollDelay) {
        this.pollDelay = pollDelay;
        return this;
    }

    /**
     * The duration the SQS client should wait for messages before closing the connection.
     *
     * The default is 1 second.
     */
    public SqsMessagePollerProperties withWaitTime(Duration waitTime) {
        this.waitTime = waitTime;
        return this;
    }

    /**
     * The maximum number of messages to pull from SQS with each poll (10 maximum SQS allows is 10).
     *
     * The default is 10.
     */
    public SqsMessagePollerProperties withBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public SqsMessagePollerProperties withExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    /**
     * The number of threads that should poll SQS for new messages. Each of those threads will poll a batch of
     * batchSize messages and then wait for the pollDelay interval until polling the next batch.
     *
     * The default is 1.
     */
    public SqsMessagePollerProperties withPollingThreads(int pollingThreads) {
        this.pollingThreads = pollingThreads;
        return this;
    }

}

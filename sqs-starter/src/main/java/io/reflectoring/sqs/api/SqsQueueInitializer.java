package io.reflectoring.sqs.api;

import com.amazonaws.services.sqs.AmazonSQS;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;

/**
 * Creates an SQS queue on startup. Useful in tests.
 */
@RequiredArgsConstructor
public class SqsQueueInitializer implements InitializingBean {

    private final AmazonSQS sqsClient;
    private final String queueName;

    private void initializeQueue(AmazonSQS sqsClient, String queueName) {
        sqsClient.createQueue(queueName);
    }

    @Override
    public void afterPropertiesSet() {
        initializeQueue(sqsClient, queueName);
    }
}

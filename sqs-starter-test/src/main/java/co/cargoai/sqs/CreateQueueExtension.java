package co.cargoai.sqs;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class CreateQueueExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        SqsTest annotation = extensionContext.getRequiredTestClass().getAnnotation(SqsTest.class);
        for (String queueName : annotation.queueNames()) {
            createQueue(queueName);
        }
    }

    private void createQueue(String queueName) {
        CreateQueueResult result = sqsClient().createQueue(queueName);
        if (result.getSdkHttpMetadata().getHttpStatusCode() != 200) {
            throw new IllegalStateException(String.format("error creating queue %s: %s", queueName, result.getSdkHttpMetadata()));
        }
    }

    private AmazonSQS sqsClient() {
        return AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        "http://localhost:4576",
                        "us-east"
                )).build();
    }
}

package io.reflectoring.sqs;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SqsTest(queueNames = {"queue1", "queue2"})
@Tag("IgnoreInCi")
class SqsTestAnnotationTest {

    @Test
    void canInteractWithQueues() {
        AmazonSQS sqsClient = sqsClient();

        assertSendAndReceive(sqsClient, "queue1");
        assertSendAndReceive(sqsClient, "queue2");
    }

    private void assertSendAndReceive(AmazonSQS sqsClient, String queueName) {
        String queueUrl = String.format("http://localhost:4576/queue/%s", queueName);
        SendMessageResult sendMessageResult = sqsClient.sendMessage(new SendMessageRequest(
                queueUrl,
                "message"));
        assertThat(sendMessageResult.getMessageId()).isNotNull();

        ReceiveMessageResult receiveMessageResult = sqsClient.receiveMessage(queueUrl);
        assertThat(receiveMessageResult.getSdkHttpMetadata().getHttpStatusCode()).isEqualTo(200);
        assertThat(receiveMessageResult.getMessages()).hasSize(1);
    }

    private AmazonSQS sqsClient() {
        return AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        "http://localhost:4576",
                        "us-east"
                )).build();
    }

}

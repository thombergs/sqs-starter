package io.reflectoring.sqs.internal;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TestMessageHandlerConfiguration {

    @Bean
    AmazonSQS sqsClient() {
        AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard();
        builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                "http://localhost:4576",
                "us-east"
        ));
        return builder.build();
    }

    @Bean
    TestMessageHandler messageHandler(){
        return new TestMessageHandler();
    }

    @Bean
    TestMessageHandlerRegistration testMessageHandlerRegistration(AmazonSQS sqsClient, ObjectMapper objectMapper, TestMessageHandler messageHandler) {
        return new TestMessageHandlerRegistration(sqsClient, objectMapper, messageHandler);
    }

    @Bean
    TestMessagePublisher testMessagePublisher(AmazonSQS sqsClient, ObjectMapper objectMapper) {
        return new TestMessagePublisher(sqsClient, objectMapper);
    }

    @Bean
    ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

}

package io.blogtrack.sqs.internal;

import io.blogtrack.sqs.api.SqsMessageHandlerRegistration;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SqsAutoConfiguration {

  @Bean
  SqsMessageHandlerRegistry sqsMessageHandlerRegistry(List<SqsMessageHandlerRegistration<?>> registrations){
    return new SqsMessageHandlerRegistry(registrations);
  }



}

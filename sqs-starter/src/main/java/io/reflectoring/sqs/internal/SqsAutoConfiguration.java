package io.reflectoring.sqs.internal;

import io.reflectoring.sqs.api.SqsMessageHandlerRegistration;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SqsAutoConfiguration {

  @Bean
  SqsMessageHandlerRegistry sqsMessageHandlerRegistry(List<SqsMessageHandlerRegistration<?>> registrations) {
    return new SqsMessageHandlerRegistry(registrations);
  }

  @Bean
  SqsAutoConfigurationLifecycle sqsLifecycle(SqsMessageHandlerRegistry registry) {
    return new SqsAutoConfigurationLifecycle(registry);
  }

}

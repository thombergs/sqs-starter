package co.cargoai.sqs.internal;

import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

@RequiredArgsConstructor
class SqsAutoConfigurationLifecycle implements ApplicationListener<ApplicationReadyEvent> {

  private final SqsMessageHandlerRegistry registry;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    registry.start();
  }

  @PreDestroy
  public void destroy() {
    registry.stop();
  }

}

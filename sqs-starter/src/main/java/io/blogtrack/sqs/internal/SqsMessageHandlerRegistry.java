package io.blogtrack.sqs.internal;

import io.blogtrack.sqs.api.SqsMessageHandlerRegistration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

class SqsMessageHandlerRegistry implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SqsMessageHandlerRegistry.class);

    private final Set<SqsMessagePoller<?>> pollers;

    public SqsMessageHandlerRegistry(List<SqsMessageHandlerRegistration<?>> messageHandlerRegistrations) {
        this.pollers = initializePollers(messageHandlerRegistrations);
    }

    private Set<SqsMessagePoller<?>> initializePollers(List<SqsMessageHandlerRegistration<?>> registrations) {
        Set<SqsMessagePoller<?>> pollers = new HashSet<>();
        for (SqsMessageHandlerRegistration<?> registration : registrations) {
            pollers.add(createPollerForHandler(registration));
            logger.info("initialized SqsMessagePoller '{}'", registration.name());
        }
        return pollers;
    }

    private SqsMessagePoller<?> createPollerForHandler(SqsMessageHandlerRegistration<?> registration) {
        return new SqsMessagePoller<>(
                registration.messageHandler(),
                registration.messagePollerProperties(),
                registration.sqsClient(),
                registration.objectMapper(),
                createPollingThreadPool(registration),
                createHandlerThreadPool(registration));
    }

    private ScheduledThreadPoolExecutor createPollingThreadPool(SqsMessageHandlerRegistration<?> registration) {
        return ThreadPools.blockingScheduledThreadPool(
                1,
                String.format("%s-poller", registration.name()));
    }

    private ThreadPoolExecutor createHandlerThreadPool(SqsMessageHandlerRegistration<?> registration) {
        return ThreadPools.blockingThreadPool(
                registration.messageHandlerProperties().getHandlerThreadPoolSize(),
                registration.messageHandlerProperties().getHandlerQueueSize(),
                String.format("%s-handler", registration.name()));
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        for (SqsMessagePoller<?> poller : this.pollers) {
            poller.start();
        }
    }

    @PreDestroy
    public void stop() {
        for (SqsMessagePoller<?> poller : this.pollers) {
            poller.stop();
        }
    }
}

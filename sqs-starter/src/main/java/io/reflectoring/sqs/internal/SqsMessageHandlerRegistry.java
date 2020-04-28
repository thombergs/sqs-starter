package io.reflectoring.sqs.internal;

import io.reflectoring.sqs.api.SqsMessageHandlerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

class SqsMessageHandlerRegistry {

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
                registration.name(),
                registration.messageHandler(),
                createFetcherForHandler(registration),
                registration.messagePollerProperties(),
                registration.sqsClient(),
                registration.objectMapper(),
                createPollingThreadPool(registration),
                createHandlerThreadPool(registration));
    }

    private SqsMessageFetcher createFetcherForHandler(SqsMessageHandlerRegistration<?> registration) {
        return new SqsMessageFetcher(
                registration.sqsClient(),
                registration.messagePollerProperties());
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

    public void start() {
        for (SqsMessagePoller<?> poller : this.pollers) {
            poller.start();
        }
    }

    public void stop() {
        for (SqsMessagePoller<?> poller : this.pollers) {
            poller.stop();
        }
    }
}

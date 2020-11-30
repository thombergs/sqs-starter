package co.cargoai.sqs.internal;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;

/**
 * A thread factory that produces {@link Thread}s with a given name prefix and a sequential number.
 */
@AllArgsConstructor
class NamedThreadFactory implements ThreadFactory {

  private final AtomicInteger currentThreadCount = new AtomicInteger(0);
  private final String threadNamePrefix;

  @Override
  public Thread newThread(Runnable runnable) {
    Integer threadNumber = currentThreadCount.incrementAndGet();
    String threadName = String.format("%s-%d", threadNamePrefix, threadNumber);
    return new Thread(runnable, threadName);
  }

}

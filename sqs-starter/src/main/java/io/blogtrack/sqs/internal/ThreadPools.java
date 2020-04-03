package io.blogtrack.sqs.internal;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class ThreadPools {

    public static ThreadPoolExecutor blockingThreadPool(int threads, int queueSize, String poolName) {
        return new ThreadPoolExecutor(
                threads,
                threads,
                0L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize),
                new NamedThreadFactory(poolName),
                retryPolicy()
        );
    }

    public static ScheduledThreadPoolExecutor blockingScheduledThreadPool(int threads, String poolName) {
        return new ScheduledThreadPoolExecutor(
                threads,
                new NamedThreadFactory(poolName),
                retryPolicy());
    }

    /**
     * Re-Queues a rejected {@link Runnable} into the thread pool's blocking queue, making the submitting thread wait
     * until the threadpool has capacity again.
     */
    static RejectedExecutionHandler retryPolicy() {
        return (r, executor) -> {
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

}

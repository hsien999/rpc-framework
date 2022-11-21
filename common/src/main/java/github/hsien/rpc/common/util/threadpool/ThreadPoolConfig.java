package github.hsien.rpc.common.util.threadpool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Config values for thread pool
 *
 * @author hsien
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public final class ThreadPoolConfig {
    /**
     * Default config values
     */
    private static final int DEFAULT_CORE_POOL_SIZE = 5;
    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;
    private static final int DEFAULT_KEEP_ALIVE_TIME = 60;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    private static final int DEFAULT_BLOCKING_QUEUE_CAPACITY = 10;
    /**
     * Custom config values
     */
    @Builder.Default
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    @Builder.Default
    private int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;
    @Builder.Default
    private long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
    @Builder.Default
    private TimeUnit unit = DEFAULT_TIME_UNIT;
    @Builder.Default
    private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(DEFAULT_BLOCKING_QUEUE_CAPACITY);
}

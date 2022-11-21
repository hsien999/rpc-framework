package github.hsien.rpc.common.util.threadpool;


import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * A common util for creating thread pool
 *
 * @author hsien
 */
public abstract class ThreadPoolFactoryUtils {
    public static ExecutorService createThreadPool(ThreadPoolConfig config, String prefixName, Boolean daemon) {
        return new ThreadPoolExecutor(config.getCorePoolSize(), config.getMaximumPoolSize(), config.getKeepAliveTime(),
            config.getUnit(), config.getWorkQueue(), createThreadFactory(prefixName, daemon));
    }

    public static ThreadFactory createThreadFactory(String prefixName, Boolean daemon) {
        if (prefixName != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder().setNameFormat(prefixName + "-%d").setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(prefixName + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }
}

/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.auditlog.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author xujs@mamcharge.com
 * @date 2019/12/5 9:17
 **/
public class ThreadPoolUtil {

    /**线程池*/
    private final ExecutorService executorService;

    private static ThreadPoolUtil instance = new ThreadPoolUtil();

    private ThreadPoolUtil() {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }


    public static ThreadPoolUtil getInstance() {
        return instance;
    }

    public static <T> Future<T> execute(final Callable<T> runnable) {
        return getInstance().executorService.submit(runnable);
    }

    public static Future<?> execute(final Runnable runnable) {
        return getInstance().executorService.submit(runnable);
    }

}

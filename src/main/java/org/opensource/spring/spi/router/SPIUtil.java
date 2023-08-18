package org.opensource.spring.spi.router;

import java.util.function.Supplier;

/**
 * SPI执行工具
 * 
 * 用来简化ThreadLocal堆栈模式的使用。
 * 
 * 用户应仅配合ThreadLocalSPIRouter使用，用来简化嵌套路由场景的使用。
 * 
 * @author wutianbiao
 * @date 2021-12-13
 */
public class SPIUtil {

    /**
     * 使用prefix配置，执行supplier函数，并返回结果
     * 
     * @param <T>
     * @param prefix
     * @param supplier
     * @return
     */
    public static <T> T withPrefix(String prefix, Supplier<T> supplier) {
        ThreadLocalSPIRouter.pushPrefix(prefix);
        try {
            T result = supplier.get();
            return result;
        } finally {
            ThreadLocalSPIRouter.popPrefix();
        }
    }

    /**
     * 使用prefix配置，执行runnable函数(非多线程)
     * 
     * @param prefix
     * @param runnable
     */
    public static void withPrefix(String prefix, Runnable runnable) {
        ThreadLocalSPIRouter.pushPrefix(prefix);
        try {
            runnable.run();
        } finally {
            ThreadLocalSPIRouter.popPrefix();
        }
    }
}

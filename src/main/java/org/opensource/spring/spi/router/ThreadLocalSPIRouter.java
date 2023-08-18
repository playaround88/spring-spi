package org.opensource.spring.spi.router;

import java.util.EmptyStackException;
import java.util.Stack;

import com.alibaba.ttl.TransmittableThreadLocal;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 默认路由实现，基于ThreadLocal的SPI路由实现。
 * 
 * 使用ThreadLocal存取路由配置。
 * 1. 借助阿里的TTL支持子线程、stream并发流线程和TransmittableThreadLocal线程池路由传递。
 * 2. 借助栈结构实现路由的嵌套处理，避免底层修改污染上层空间。
 * 
 * 默认路由实现，当{@link SPI}为指定路由器时，该路由器生效。也可以显示指定。
 * 
 * @author wutianbiao
 * @date 2021-11-24
 */
@Component("threadLocalSPIRouter")
public class ThreadLocalSPIRouter implements SPIRouter {
    private static Logger log = LoggerFactory.getLogger(ThreadLocalSPIRouter.class);

    private static String DEFAULT_PREFIX_STRING = "";

    // 线程变量，业务路由栈
    private static TransmittableThreadLocal<Stack<String>> PREFIX = new TransmittableThreadLocal<Stack<String>>() {
        @Override
        @SuppressWarnings("unchecked")
        protected Stack<String> childValue(Stack<String> parentValue) {
            return (Stack<String>) parentValue.clone();
        }

        @Override
        protected Stack<String> initialValue() {
            Stack<String> stack = new Stack<>();
            stack.push(DEFAULT_PREFIX_STRING);
            return stack;
        }
    };

    /**
     * 推入SPI路由栈顶prefix
     * 
     * @param prefix
     */
    public static void pushPrefix(String prefix) {
        if (prefix == null) {
            return;
        }
        log.info("SPI stack:{} pushPrefix:{}", System.identityHashCode(ThreadLocalSPIRouter.PREFIX.get()), prefix);
        ThreadLocalSPIRouter.PREFIX.get().push(prefix);
    }

    /**
     * 弹出SPI路由栈顶prefix
     * 
     * @return
     */
    public static String popPrefix() {
        String result = null;
        try {
            result = ThreadLocalSPIRouter.PREFIX.get().pop();
        } catch (EmptyStackException e) {
            log.warn("pop spi route prefix with empty stack! return default");
            result = DEFAULT_PREFIX_STRING;
        }
        log.info("SPI stack:{} pushPrefix:{}", System.identityHashCode(ThreadLocalSPIRouter.PREFIX.get()), result);
        return result;
    }

    /**
     * 查看当前路由栈顶Prefix
     * 
     * @return
     */
    public static String peekPrefix() {
        return ThreadLocalSPIRouter.PREFIX.get().peek();
    }

    /**
     * 清理当前线程的SPI路由的prefix
     */
    public static void clear() {
        log.info("SPI clear stack: {}", System.identityHashCode(ThreadLocalSPIRouter.PREFIX.get()));
        ThreadLocalSPIRouter.PREFIX.get().clear();
    }

    @Override
    public String route(MethodInvocation invocation) {
        return ThreadLocalSPIRouter.PREFIX.get().peek();
    }

}

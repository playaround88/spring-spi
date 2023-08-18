package org.opensource.spring.spi.router;

import org.aopalliance.intercept.MethodInvocation;

/**
 * 策略路由接口
 * 
 * 用来抽取隔离业务路由策略
 * 
 * 使用方可以实现该接口，自定义适合自身业务的路由实现。
 *
 * @author wutianbiao
 * @date 2021-11-21
 */
@FunctionalInterface
public interface SPIRouter {
    /**
     * 会传入整个方法调用，业务方获取请求对象，
     * 需要返回一个从spring取bean的prefix，扩展机制会通过 prefix + 接口名称取bean。
     *
     * @param invocation
     * @return
     */
    String route(MethodInvocation invocation);
}

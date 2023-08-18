package org.opensource.spring.spi.annotation;

import org.opensource.spring.spi.router.ThreadLocalSPIRouter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SPRING-SPI插件接口注解
 * 
 * 用来标识一个接口有多个扩展实现
 * 
 * 使用时只需要在对应的接口上添加对应的注解标识该接口有多个SPI扩展实现。
 * route参数是用来指定具体的路由实现类的，值为路由实现类在spring中的beanName。
 * 如果未指定默认的路由为{@link ThreadLocalSPIRouter}。
 *
 * @author wutianbiao
 * @date 2021-11-19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
    /**
     * spi route在spring中的beanName
     */
    String route() default "threadLocalSPIRouter";
}

package org.opensource.spring.spi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * spel路由表达式注解
 * 
 * spel路由实现的，用来指定路由spel表达式的注解。
 * 
 * 在{@link SPI}使用spel路由实现时，在方法签名中指定具体的spel表达式。
 * 路由计算时会使用对应的参数计算指定的spel表达式来算出路由。
 *
 * @author wutianbiao
 * @date 2021-11-22
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElParam {
    /**
     * 指定路由的el表达式
     *
     * @return
     */
    String value();
}

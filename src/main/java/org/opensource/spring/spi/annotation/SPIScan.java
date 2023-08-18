package org.opensource.spring.spi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.opensource.spring.spi.SPIScanRegister;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * SPI扩展扫描路径注解
 * 
 * 用来配置项目启动时，要扫描@SPI注解的包路径。
 * 
 * 通常在spring-boot的启动类，或者@Configuration配置类上添加该注解。
 * basePackages参数用来指定一个或者多个扫描路径，支持模糊匹配预发与{@link ComponentScan}一致。
 *
 * @author wutianbiao
 * @date 2021-11-19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ SPIScanRegister.class })
public @interface SPIScan {

    /**
     * 扫描@SPI注解接口的路径配置
     * 
     * @return
     */
    String[] basePackages() default {};

}

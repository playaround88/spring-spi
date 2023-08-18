package org.opensource.spring.spi.router;

import java.lang.annotation.Annotation;

import org.opensource.spring.spi.annotation.ElParam;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * SPEL策略路由实现
 * 
 * 1. 获取接口方法中第一个注解了@ElParam的参数; <br />
 * 2. 获取ElParam配置的el表达式；<br />
 * 3. 在对应的参数上执行el表达式，并返回String格式的结果；
 *
 * 当{@link SPI}注解中配置"spelSPIRouter"时，使用该路由类。
 * 
 * @author wutianbiao
 * @date 2021-11-22
 */
@Component("spelSPIRouter")
public class SpelSPIRouter implements SPIRouter {
    private static final Logger log = LoggerFactory.getLogger(SpelSPIRouter.class);

    @Override
    public String route(MethodInvocation invocation) {
        // 获取el表达式
        String el = null;
        Object param = null;
        Annotation[][] parameterAnnotations = invocation.getMethod().getParameterAnnotations();
        out: for (int i = 0; i < parameterAnnotations.length; i++) {
            for (int j = 0; j < parameterAnnotations[i].length; j++) {
                if (parameterAnnotations[i][j].annotationType().equals(ElParam.class)) {
                    param = invocation.getArguments()[i];
                    if (parameterAnnotations[i][j] instanceof ElParam) {
                        el = ((ElParam) parameterAnnotations[i][j]).value();
                    }
                    break out;
                }
            }
        }
        log.debug("获取到的el表达式:{}", el);
        if (!StringUtils.hasText(el)) {
            throw new RuntimeException("SPI未获取到SPIParam的el配置");
        }

        // 计算el表达式
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(el);
        String prefix = exp.getValue(param, String.class);
        log.debug("计算的SPI路由prefix:{}", prefix);

        return prefix;
    }

}

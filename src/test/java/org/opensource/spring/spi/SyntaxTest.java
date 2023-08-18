package org.opensource.spring.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opensource.spring.spi.annotation.ElParam;
import org.opensource.spring.spi.router.ThreadLocalSPIRouter;
import org.opensource.spring.spi.ttl.TestService;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class SyntaxTest {
    private static final Logger log = LoggerFactory.getLogger(SyntaxTest.class);

    @Test
    public void testSpel() {
        BaseParam param = new BaseParam();
        param.setChannel("b");
        // 计算el表达式
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression("channel");
        String prefix = exp.getValue(param, String.class);
        log.debug("计算的SPI路由prefix:{}", prefix);
    }

    @Test
    public void testParamAnnotation() throws NoSuchMethodException, SecurityException {
        Method method = TestService.class.getDeclaredMethod("sayHello", BaseParam.class);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        String el = null;
        out: for (int i = 0; i < parameterAnnotations.length; i++) {
            for (int j = 0; j < parameterAnnotations[i].length; j++) {
                if (parameterAnnotations[i][j].annotationType().equals(ElParam.class)) {
                    el = ((ElParam) parameterAnnotations[i][j]).value();
                    break out;
                }
            }
        }
        log.debug("获取到的el表达式:{}", el);
    }

    @Test
    public void testInheritableThreadLocal() throws InterruptedException {

        ThreadLocalSPIRouter.pushPrefix("wutb");

        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final int j = i;
            Thread t = new Thread(() -> {
                System.out.println(j + ":" + ThreadLocalSPIRouter.peekPrefix());
            });
            list.add(t);
            t.start();
        }

        list.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        System.out.println("test end...");
    }

    @Test
    public void testInheritableThreadLocalStream() {
        ThreadLocalSPIRouter.pushPrefix("wutb");

        List<Integer> list = Arrays.asList(1, 2, 3);
        list.parallelStream().forEach(i -> {
            System.out.println(i + ":" + ThreadLocalSPIRouter.peekPrefix());
        });

        System.out.println("test end...");
    }
}

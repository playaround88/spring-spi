package org.opensource.spring.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.opensource.spring.spi.spel.TestSpelService;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * 测试入口，测试spel路由实现
 */
@SpringJUnitConfig(SPIConfig.class)
public class TestSpelServiceTest {
    private static final Logger log = LoggerFactory.getLogger(TestServiceTest.class);

    /**
     * 自动注入
     */
    @Autowired
    private TestSpelService service;

    @Test
    void testSayHello() {
        BaseParam param = new BaseParam();

        /**
         * 通过入参路由，spel表达式在接口定义中
         */
        param.setChannel("a");
        String response = service.sayHello(param);
        System.out.println("a测试结果：" + response);
        assertEquals("a: hello world!", response);

        /**
         * 通过入参路由，spel表达式在接口定义中
         */
        param.setChannel("b");
        response = service.sayHello(param);
        System.out.println("b测试结果：" + response);
        assertEquals("b: hello world!", response);

        log.info("测试结束");
    }

}

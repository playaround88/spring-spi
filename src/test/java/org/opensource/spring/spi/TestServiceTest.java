package org.opensource.spring.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.opensource.spring.spi.router.SPIUtil.withPrefix;
import org.opensource.spring.spi.router.ThreadLocalSPIRouter;
import org.opensource.spring.spi.ttl.TestService;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * 单测入口，ThreadLocal路由器测试
 *
 * @author wutianbiao
 * @date 2021-11-20
 */
@SpringJUnitConfig(SPIConfig.class)
public class TestServiceTest {
    private static final Logger log = LoggerFactory.getLogger(TestServiceTest.class);

    /**
     * 自动注入
     */
    @Autowired
    private TestService testService;

    /**
     * 压栈出栈测试
     */
    @Test
    void testSayHello() {
        ThreadLocalSPIRouter.pushPrefix("a");
        BaseParam param = new BaseParam();
        String response = testService.sayHello(param);

        System.out.println("测试结果：" + response);
        assertEquals("a: hello world!", response);
        ThreadLocalSPIRouter.popPrefix();

        // 通过参数切换扩展
        ThreadLocalSPIRouter.pushPrefix("b");
        response = testService.sayHello(param);

        System.out.println("测试结果：" + response);
        assertEquals("b: hello world!", response);
        ThreadLocalSPIRouter.popPrefix();

        log.debug("单测结束");
    }

    /**
     * 工具类测试
     */
    @Test
    void testSpiUtil() {
        BaseParam param = new BaseParam();

        String response = null;
        response = withPrefix("a", () -> testService.sayHello(param));
        System.out.println("a测试结果：" + response);
        assertEquals("a: hello world!", response);

        response = withPrefix("b", () -> testService.sayHello(param));
        System.out.println("b测试结果：" + response);
        assertEquals("b: hello world!", response);

        log.debug("spiUtil单测结束");
    }
}

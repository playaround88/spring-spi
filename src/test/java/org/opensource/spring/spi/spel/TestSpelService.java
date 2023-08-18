package org.opensource.spring.spi.spel;

import org.opensource.spring.spi.annotation.ElParam;
import org.opensource.spring.spi.annotation.SPI;
import org.opensource.spring.spi.BaseParam;

/**
 * 测试服务
 * 
 * 采用spel作为路由器
 * 
 * @author wutianbiao
 * @date 2021-11-20
 */
@SPI(route = "spelSPIRouter")
public interface TestSpelService {

    /**
     * 注意这里的{@link ElParam}注解，值为spel表达式
     * 
     * @param param
     * @return
     */
    String sayHello(@ElParam("channel") BaseParam param);
}

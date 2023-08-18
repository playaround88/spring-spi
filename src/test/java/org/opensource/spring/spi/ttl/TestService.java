package org.opensource.spring.spi.ttl;

import org.opensource.spring.spi.annotation.SPI;
import org.opensource.spring.spi.BaseParam;

/**
 * 测试服务
 * 
 * 默认路由为ThreadLocal实现
 * 
 * @author wutianbiao
 * @date 2021-11-20
 */
@SPI
public interface TestService {

    String sayHello(BaseParam param);
}

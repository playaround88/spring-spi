package org.opensource.spring.spi;

import org.opensource.spring.spi.router.SPIRouter;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.stereotype.Component;

/**
 * 测试自定义路由
 * 
 * @author wutianbiao
 */
@Component("channelRoute")
public class ChannelRoute implements SPIRouter {

    @Override
    public String route(MethodInvocation invocation) {
        BaseParam baseParam = (BaseParam) invocation.getArguments()[0];
        return baseParam.getChannel();
    }

}

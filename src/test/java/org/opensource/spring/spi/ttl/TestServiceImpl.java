package org.opensource.spring.spi.ttl;

import org.opensource.spring.spi.BaseParam;

import org.springframework.stereotype.Service;

@Service("aTestService")
public class TestServiceImpl implements TestService {

    @Override
    public String sayHello(BaseParam param) {
        return "a: hello world!";
    }

}

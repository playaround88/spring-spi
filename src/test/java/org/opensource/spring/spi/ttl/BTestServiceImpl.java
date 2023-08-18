package org.opensource.spring.spi.ttl;

import org.opensource.spring.spi.BaseParam;

import org.springframework.stereotype.Service;

@Service("bTestService")
public class BTestServiceImpl implements TestService {

    @Override
    public String sayHello(BaseParam param) {
        return "b: hello world!";
    }

}

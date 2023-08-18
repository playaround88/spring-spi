package org.opensource.spring.spi.spel;

import org.opensource.spring.spi.BaseParam;

import org.springframework.stereotype.Service;

@Service("aTestSpelService")
public class TestServiceImpl implements TestSpelService {

    @Override
    public String sayHello(BaseParam param) {
        return "a: hello world!";
    }

}

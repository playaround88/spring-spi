package org.opensource.spring.spi.spel;

import org.opensource.spring.spi.BaseParam;

import org.springframework.stereotype.Service;

@Service("bTestSpelService")
public class BTestServiceImpl implements TestSpelService {

    @Override
    public String sayHello(BaseParam param) {
        return "b: hello world!";
    }

}

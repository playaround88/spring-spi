package org.opensource.spring.spi;

import org.opensource.spring.spi.annotation.SPIScan;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "org.opensource.spring.spi")
@SPIScan(basePackages = { "org.opensource.spring.spi" })
public class SPIConfig {

}

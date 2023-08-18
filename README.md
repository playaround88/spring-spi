# SPRING-SPI插件扩展机制

> 随着业务的稳步发展，不断有新的业务模式接入。多种商品来源，多种订单渠道，多个业务线，更甚者客户各种定制版。  
> 最初，我们可能在代码中添加一些开关来适配多种不同的实现。但这种方式好比给一个水管打洞，最终会发现到处”漏水“。  
> 原有的系统在演进过程中，面临着诸多挑战。

SPRING-SPI插件，核心聚焦与上述描述的业务场景，即一个业务动作可能会多种实现的场景。  
扩展机制可以根据不同的业务参数，动态的选择具体的业务实现。  
这个扩展机制，在分离业务实现的同时，给了业务系统无限的扩展能力，同时大大较低了代码维护的复杂性和人力成本。

## 1. HOWTO
从开发者角度来看，使用该插件非常简单，下面是全部的集成和使用的步骤，如果是仅仅开发，只需关注2、3两部分。    
集成使用步骤描述如下：
1. 引入maven包
```xml
<dependency>
    <groupId>org.opensource</groupId>
	<artifactId>spring-spi</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>
```

2. 在你需要扩展的接口类上添加注解
```java
@SPI(route = "channelRoute")  // 这里的channelRoute先忽略，最后一步
public interface TestService {
    String sayHello(BaseParam param);
}
```

3. 随便写自己的多个实现，但beanName需要按要求的格式(prefix + 接口名称)
```java
@Service("aTestService")
public class TestServiceImpl implements TestService {
    @Override
    public String sayHello(BaseParam param) {
        return "hello world:" + param.getChannel();
    }
}
```
```java
@Service("bTestService")
public class BTestServiceImpl implements TestService {
    @Override
    public String sayHello(BaseParam param) {
        return "hello world:" + param.getChannel();
    }
}
```

4. 在启动类，或者Configuration类上加上扫描注解，即要到哪些目录扫描@SPI注解的扩展点接口
```java
@Configuration
@ComponentScan(basePackages = {"org.opensource.spring.spi"}) // 加载spi相关的默认实现
@SPIScan(basePackages = {"org.opensource.spring.test"}) // 这里需要定制自己的扫描路径
public class SPIConfig {
    // pass
}
```

5. 最后这一步，只需项目架构师人员做一次开发，即定制自己系统的路由策略  
简单来说，路由策略就是扩展机，如何从spring中获取到业务代码多种实现的prefix。  
这个路由会把接口方法调用作为参数传进去，并且要求返回一个String(不可为空)。  

下面是一个最简单的路由实现：
```java
@Component("channelRoute")
public class ChannelRoute implements SPIRouter {
    @Override
    public String route(MethodInvocation invocation) {
        BaseParam baseParam = (BaseParam)invocation.getArguments()[0];
        return baseParam.getChannel();
    }
}
```
> 这里的**channelRoute**，就是第一步，扩展插件@SPI里面写的值，即一个系统可能有多种路由机制，可以在扩展点自定义。

*That's All!*

## 2. 默认路由

## 2.1 ThreadLocal默认路由
扩展插件本身提供了一个基于ThreadLocal的默认路由，当@SPI未指定路由方式的时候，默认走该路由。  
借助阿里的TransmittableThreadLocal库，可以实现子线程、stream并行流、TransmittableThreadLocal线程池方式的参数传递。  
测试代码如下：
```java
@SPI //(route = "channelRoute") 这里注掉了路由配置
public interface TestService {
    String sayHello(BaseParam param);
}
```
```java
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


    // 工具类执行
    response = withPrefix("a", () -> testService.sayHello(param));
    System.out.println("测试结果：" + response);

    log.debug("单测结束");
}
```
> 这里的线程池变量使用的是阿里的TransmittableThreadLocal，支持子线程、stream并发流线程和TransmittableThreadLocal线程池方式的参数传递。

### 2.2 SPIUtil
对应ThreadLocal的默认路由，为了简化路由嵌套调用，提供了工具类，可以降低嵌套调用的代码”音噪“。  
测试代码如下：
```java
@Test
void testSayHello() {
    // 工具类执行
    response = withPrefix("a", () -> testService.sayHello(param));
    System.out.println("测试结果：" + response);

    log.debug("单测结束");
}
```

### 2.3 SPEL路由
扩展库还提供了一个支持Spel注解的默认路由实现，该路由默认取接口方法的第一个注解@ElParam("")的参数，并根据配置的el表达式计算路由，测试代码如下：
```java
@SPI(route = "spelSPIRouter") // 这里指定使用spel路由
public interface TestService {
    String sayHello(@ElParam("channel") BaseParam param); // 这里参数加了@ElParam的注解
}
```
```java
@Test
void testSayHello() {
    BaseParam param = new BaseParam();
    param.setChannel("a");
    String response = testService.sayHello(param);

    System.out.println("测试结果：" + response);
    assertEquals("a: hello world!", response);

    log.debug("单测结束");
}
```

## 3. 原理简述
![spi结构图](/assets/spi.png)  

为了更好的使用，这里简单描述一下整个的实现机制。有兴趣的同学可以翻一下对应的源码。

首先，系统启动时，会去指定目录下，扫描所有的@SPI注解的接口。  
然后，就会给对应的接口生成一个”接口名称 + #Proxy“的代理类，设置为primary，并注入到spring容器中。  
最后，就是代理逻辑的实现了，它会调用SPI定义的route，取到prefix，拼接上service的接口名称，去spring找到对应的bean，并调用对应的方法。  

> ## route的特殊说明
> 实现路由，只需实现*SPIRouter*接口，上面的例子已经演示，每个系统都需要定义一个自己的路由策略。  
> 但一个系统也可能实现多个路由策略，比如不同的业务层，可能路由策略不一样。  
> 路由方法的入参，是接口方法的第一个入参，可以强制转换为自己模块的参数父类。这样就能根据入参定制路由策略了。  
> 但路由策略也可以完全不使用入参，比如也可能直接使用ThreadLocal中的变量。  


## 4. Contribut

> 致谢！参与有你

## 5. release note
2021-12-13 1.1.0版本，非兼容性变更！ThreadLocal存放的prefix改为栈结构，可以支持方法嵌套的prefix设置。(可有效避免下层变更污染上层调用)

2021-12-07 反射调用会包装原始异常，封装为InvocationTargetException，造成业务无法捕获原始异常，改正！finder @陈鑫

2021-12-06 proxyfactory会持有route对象，把proxyfactory改为scope=prototype. 

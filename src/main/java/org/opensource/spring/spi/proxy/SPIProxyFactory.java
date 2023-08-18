package org.opensource.spring.spi.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.opensource.spring.spi.router.SPIRouter;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * SPI策略代理工厂
 *
 * 动态代理实现，业务路由逻辑的封装类。
 * 拦截调用，根据配置的路由器计算出路由。
 * 根据路由在spring容器中找到对应的bean，分发请求到目标bean。
 * 
 * 该类在{@link SPIScanRegister}中引用，用来为{@link SPI}注解的接口生成动态代理。
 * 
 * @author wutianbiao
 * @date 2021-11-19
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SPIProxyFactory
        implements ApplicationContextAware, BeanClassLoaderAware, MethodInterceptor, FactoryBean<Object> {
    private static final Logger log = LoggerFactory.getLogger(SPIProxyFactory.class);

    /**
     * 应用上下文
     */
    private ApplicationContext applicationContext;
    /**
     * 类加载器
     */
    private ClassLoader classLoader;
    /**
     * 扩展服务接口
     */
    private Class<?> serviceInterface;
    /**
     * 代理bean
     */
    private Object serviceProxy;

    /**
     * 策略路由
     */
    private SPIRouter spiRouter;

    public SPIRouter getSpiRouter() {
        return spiRouter;
    }

    public void setSpiRouter(SPIRouter spiRouter) {
        this.spiRouter = spiRouter;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object getObject() throws Exception {
        if (serviceProxy == null) {
            Class<?> ifc = getServiceInterface();
            Assert.notNull(ifc, "Property 'serviceInterface' is required");
            Assert.notNull(getSpiRouter(), "Property 'spiRouter' is required");
            serviceProxy = new ProxyFactory(ifc, this).getProxy(classLoader);
        }
        return serviceProxy;
    }

    @Override
    public Class<?> getObjectType() {
        return getServiceInterface();
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        log.debug("spi proxy invoke:{}", invocation.getMethod().getName());

        String service = invocation.getMethod().getDeclaringClass().getSimpleName();

        // 策略路由
        String prefix = this.spiRouter.route(invocation);
        prefix = prefix == null ? "" : prefix;
        log.debug("calc prefix result:{}", prefix);
        // 获取bean
        Object bean = this.applicationContext.getBean(prefix + service);
        // 获取方法调用返回
        Method method = bean.getClass().getMethod(invocation.getMethod().getName(),
                invocation.getMethod().getParameterTypes());
        try {
            return method.invoke(bean, invocation.getArguments());
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * 获取服务接口
     * 
     * @param serviceInterface
     */
    public void setServiceInterface(Class<?> serviceInterface) {
        Assert.notNull(serviceInterface, "'serviceInterface' must not be null");
        Assert.isTrue(serviceInterface.isInterface(), "'serviceInterface' must be an interface");
        this.serviceInterface = serviceInterface;
    }

    /**
     * Return the interface of the service to access.
     */
    public Class<?> getServiceInterface() {
        return this.serviceInterface;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

}

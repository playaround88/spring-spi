package org.opensource.spring.spi;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opensource.spring.spi.annotation.SPI;
import org.opensource.spring.spi.annotation.SPIScan;
import org.opensource.spring.spi.proxy.SPIProxyFactory;
import org.opensource.spring.spi.router.SPIRouter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

/**
 * SPI扩展扫描注册器
 * 
 * 用于在系统启动时，扫描配置的扫描路径下，所有{@link SPI}注解接口，为其生成动态代理实现，并注册到spring容器。
 * 
 * 该类在{@link SPIScan}中引入，系统配置{@link SPIScan}注解，就会在启动时触发改扫描注册器。
 *
 * @author wutianbiao
 * @date 2021-11-19
 */
public class SPIScanRegister
        implements ImportBeanDefinitionRegistrar, BeanFactoryAware, ResourceLoaderAware, BeanClassLoaderAware,
        EnvironmentAware {
    private static final Logger log = LoggerFactory.getLogger(SPIScanRegister.class);
    /**
     * 资源加载器
     */
    private ResourceLoader resourceLoader;
    /**
     * 类加载器
     */
    private ClassLoader classLoader;
    /**
     * 环境配置
     */
    private Environment environment;
    /**
     * bean工厂
     */
    private BeanFactory beanFactory;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 启动打印扫描路径配置
        logPackageScan(importingClassMetadata);
        // 扫描注册
        registerSpi(importingClassMetadata, registry);
    }

    /**
     * 打印扫描配置
     *
     * @param metadata
     */
    private void logPackageScan(AnnotationMetadata metadata) {
        Map<String, Object> defaultAttrs = metadata.getAnnotationAttributes(SPIScan.class.getName(), true);
        if (defaultAttrs != null && defaultAttrs.size() > 0) {
            log.info("SPI package scan: {}", buildPackages((String[]) defaultAttrs.get("basePackages")));
        }
    }

    /**
     * 仅日志使用，拼接配置
     *
     * @param basePackages
     * @return
     */
    private String buildPackages(String[] basePackages) {
        if (basePackages == null || basePackages.length == 0) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : basePackages) {
            stringBuilder.append(s).append(",");
        }
        stringBuilder.substring(0, stringBuilder.length() - 2);
        return stringBuilder.toString();
    }

    /**
     * 扫描包接口，并生成代理，注入spring容器
     *
     * @param metadata
     * @param registry
     */
    public void registerSpi(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        log.info("开始处理SPI扩展");
        // 定制扫描器
        log.info("定制接口扫描器");
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(SPI.class);
        scanner.addIncludeFilter(annotationTypeFilter);

        // 获取basePackage配置，遍历扫描注册
        Set<String> basePackages = getBasePackages(metadata);
        for (String basePackage : basePackages) {
            log.info("SPI扩展，开始处理{}", basePackage);
            // 扫描BeanDefinition
            Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition candidate : candidates) {
                try {
                    log.info("开始处理扩展接口:{}", candidate.getBeanClassName());
                    // 获取spi配置的路由
                    AnnotationMetadata spiAnno = null;
                    if (candidate instanceof AnnotatedBeanDefinition) {
                        spiAnno = ((AnnotatedBeanDefinition) candidate).getMetadata();
                    }
                    Map<String, Object> spiAnnotationAttr = spiAnno.getAnnotationAttributes(SPI.class.getName());
                    if (spiAnnotationAttr == null) {
                        continue;
                    }
                    String routeBeanName = null;
                    if (spiAnnotationAttr.get("route") instanceof String) {
                        routeBeanName = (String) spiAnnotationAttr.get("route");
                    }
                    SPIRouter route = beanFactory.getBean(routeBeanName, SPIRouter.class);

                    // 创建动态代理对象
                    SPIProxyFactory proxyFactory = this.beanFactory.getBean(SPIProxyFactory.class);
                    Class<?> serviceClaz = Class.forName(candidate.getBeanClassName());
                    proxyFactory.setServiceInterface(serviceClaz);
                    proxyFactory.setSpiRouter(route);
                    Object spiProxyObject = proxyFactory.getObject();

                    // 创建beanDefinition
                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                            .genericBeanDefinition(spiProxyObject.getClass());
                    beanDefinitionBuilder.addConstructorArgValue(Proxy.getInvocationHandler(spiProxyObject));
                    AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
                    beanDefinition.setPrimary(true); // set primary

                    // 注册bean
                    StringBuilder sb = new StringBuilder()
                            .append(serviceClaz.getSimpleName())
                            .append("#Proxy");
                    registry.registerBeanDefinition(sb.toString(), beanDefinition);

                    log.info("完成处理扩展接口:{}", candidate.getBeanClassName());
                } catch (Exception e) {
                    log.error("创建SPI扩展代理失败:{}", e.getMessage(), e);
                }
                log.info("处理SPI扩展结束:{}", basePackage);
            }
        }
    }

    /**
     * 创建扫描器，核心是扫描注解的接口
     *
     * @return
     */
    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {

                // 独立类接口
                if (beanDefinition.getMetadata().isIndependent() && beanDefinition.getMetadata().isInterface()) {
                    try {
                        Class<?> target = ClassUtils.forName(beanDefinition.getMetadata().getClassName(),
                                SPIScanRegister.this.classLoader);

                        // 只返回注解了SPI的接口
                        SPI[] annotationsByType = target.getAnnotationsByType(SPI.class);
                        return annotationsByType.length > 0;
                    } catch (Exception ex) {
                        this.logger.error(
                                "Could not load target class: {}" + beanDefinition.getMetadata().getClassName(), ex);
                    }
                }
                return false;
            }
        };
    }

    /**
     * 获取配置的扫描目录
     *
     * @param importingClassMetadata
     * @return
     */
    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(SPIScan.class.getCanonicalName());

        Set<String> basePackages = new HashSet<String>();
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (pkg != null && !"".equals(pkg)) {
                basePackages.add(pkg);
            }
        }

        // 未配置时，使用SPIScan的类路径
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

}

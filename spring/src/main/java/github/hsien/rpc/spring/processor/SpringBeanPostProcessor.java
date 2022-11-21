package github.hsien.rpc.spring.processor;

import github.hsien.rpc.common.loader.EnhancedServiceLoader;
import github.hsien.rpc.core.dto.RpcServiceConfig;
import github.hsien.rpc.core.remoting.RpcClient;
import github.hsien.rpc.core.remoting.RpcServer;
import github.hsien.rpc.core.remoting.RpcTransportType;
import github.hsien.rpc.core.remoting.proxy.RpcClientProxy;
import github.hsien.rpc.spring.annotation.RpcReference;
import github.hsien.rpc.spring.annotation.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * Use factory hook to enable new rpc server for classes annotated with {@link RpcService}
 * or inject rpc client proxy for fields annotated with {@link RpcReference}
 * during spring bean initialization.
 *
 * @author hsien
 */
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBeanPostProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, @NonNull String beanName) {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            EnhancedServiceLoader.load(RpcServer.class, RpcTransportType.NETTY)
                .publishService(RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build());
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, @NonNull String beanName) throws BeansException {
        for (Field field : bean.getClass().getDeclaredFields()) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcReference.group())
                    .version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(
                    EnhancedServiceLoader.load(RpcClient.class, RpcTransportType.NETTY), rpcServiceConfig);
                try {
                    Object clientProxy = rpcClientProxy.getProxy(field.getType());
                    field.setAccessible(true);
                    field.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    LOGGER.error("Failed to set filed {} annotated by {}", field.getName(),
                        RpcReference.class.getName());
                }
            }
        }
        return bean;
    }
}

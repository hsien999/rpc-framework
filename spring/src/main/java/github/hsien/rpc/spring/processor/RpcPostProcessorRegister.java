package github.hsien.rpc.spring.processor;


import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

/**
 * Scan annotation {@link SpringBeanPostProcessor}
 *
 * @author hsien
 */
public class RpcPostProcessorRegister implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata annotationMetadata,
                                        @NonNull BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(SpringBeanPostProcessor.class);
        registry.registerBeanDefinition("rpc-enable-post-processor", builder.getBeanDefinition());
    }
}

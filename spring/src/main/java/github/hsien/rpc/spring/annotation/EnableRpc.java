package github.hsien.rpc.spring.annotation;


import github.hsien.rpc.spring.processor.RpcPostProcessorRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable rpc in a spring application context
 *
 * @author hsien
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RpcPostProcessorRegister.class)
public @interface EnableRpc {
}

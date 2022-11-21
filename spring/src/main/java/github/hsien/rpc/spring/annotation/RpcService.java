package github.hsien.rpc.spring.annotation;


import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC service
 * <p>
 * Indicate a class as a rpc service
 *
 * @author hsien
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Component
public @interface RpcService {
    /**
     * Service version
     */
    String version() default "";

    /**
     * Service group
     */
    String group() default "";
}

package github.hsien.rpc.spring.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rpc reference
 * <p>
 * Indicate a field is to be called as rpc service
 *
 * @author hsien
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcReference {
    /**
     * Service version
     */
    String version() default "";

    /**
     * Service group
     */
    String group() default "";
}

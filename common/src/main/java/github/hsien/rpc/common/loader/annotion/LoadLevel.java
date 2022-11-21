package github.hsien.rpc.common.loader.annotion;

import github.hsien.rpc.common.loader.Scope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Service Provider Interface
 *
 * @author hsien
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoadLevel {
    /**
     * Service name
     * <p>
     * Note: name is case-insensitive
     */
    String name() default "";

    /**
     * Service priority
     * <p>
     * When multiple candidate service instances exist, the one with the highest priority is returned
     */
    int priority() default 0;

    /**
     * Service scope
     *
     * @see Scope
     */
    Scope scope() default Scope.SINGLETON;
}

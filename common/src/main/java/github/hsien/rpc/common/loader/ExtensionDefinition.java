package github.hsien.rpc.common.loader;

import github.hsien.rpc.common.loader.annotion.LoadLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Extension definition for service class
 *
 * @author hsien
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode(cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
final class ExtensionDefinition<S> {
    /**
     * Service name
     *
     * @see LoadLevel#name()
     */
    private final String name;
    /**
     * Service priority
     *
     * @see LoadLevel#priority()
     */
    private final int priority;
    /**
     * Service scope
     *
     * @see LoadLevel#scope()
     */
    private final Scope scope;
    /**
     * Service class type
     */
    private final Class<S> serviceClass;
}

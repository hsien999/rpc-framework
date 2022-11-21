package github.hsien.rpc.common.loader;

/**
 * Indicates the contexts in which a service is applicable.
 *
 * @author hsien
 */
public enum Scope {
    /**
     * Load extension in singleton mode
     */
    SINGLETON,

    /**
     * Load extension in prototype mode
     */
    PROTOTYPE
}

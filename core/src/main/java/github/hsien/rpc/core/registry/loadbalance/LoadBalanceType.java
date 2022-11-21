package github.hsien.rpc.core.registry.loadbalance;

import github.hsien.rpc.core.registry.loadbalance.consistenthash.ConsistentHashLoadBalance;
import github.hsien.rpc.core.registry.loadbalance.random.RandomLoadBalance;

/**
 * Load balance name constants interface
 *
 * @author hsien
 */
public class LoadBalanceType {
    /**
     * Consistent Hash Algorithm
     * {@link ConsistentHashLoadBalance}
     */
    public static final String CONSISTENT_HASH = "Consistent Hash";
    /**
     * Random Algorithm
     * {@link RandomLoadBalance}
     */
    public static final String RANDOM = "Random";
}

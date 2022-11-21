package github.hsien.rpc.core.registry.loadbalance.random;


import github.hsien.rpc.common.loader.annotion.LoadLevel;
import github.hsien.rpc.core.dto.RpcRequest;
import github.hsien.rpc.core.registry.loadbalance.AbstractLoadBalance;
import github.hsien.rpc.core.registry.loadbalance.LoadBalanceType;

import java.util.List;
import java.util.Random;

/**
 * Implementation of random load balancing strategy
 *
 * @author hsien
 */
@LoadLevel(name = LoadBalanceType.RANDOM)
public class RandomLoadBalance extends AbstractLoadBalance {
    private final Random random;

    public RandomLoadBalance() {
        random = new Random();
    }

    public RandomLoadBalance(long seed) {
        random = new Random(seed);
    }

    @Override
    protected String doSelect(List<String> addresses, RpcRequest rpcRequest) {
        return addresses.get(random.nextInt(addresses.size()));
    }
}

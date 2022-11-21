package github.hsien.rpc.core.registry.loadbalance.consistenthash;

import github.hsien.rpc.common.loader.annotion.LoadLevel;
import github.hsien.rpc.core.dto.RpcRequest;
import github.hsien.rpc.core.registry.loadbalance.AbstractLoadBalance;
import github.hsien.rpc.core.registry.loadbalance.LoadBalanceType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of consistent hash  load balancing strategy
 *
 * @author hsien
 * @see <a href="https://dubbo.apache.org/en/docs/v2.7/user/examples/loadbalance/">
 * Consistent_hashing wiki</a>
 * @see <a href="https://dubbo.apache.org/zh/blog/2019/05/01/dubbo-
 * %E4%B8%80%E8%87%B4%E6%80%A7hash%E8%B4%9F%E8%BD%BD%E5%9D%87%E8%A1%A1%E5%AE%9E%E7%8E%B0%E5%89%96%E6%9E%90/">
 * Dubbo一致性Hash负载均衡实现剖析</a>
 */
@LoadLevel(name = LoadBalanceType.CONSISTENT_HASH)
public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    private static final int DEFAULT_REPLICA_NUMBER = 160;
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected String doSelect(List<String> addresses, RpcRequest rpcRequest) {
        int identityHashCode = System.identityHashCode(addresses);
        // build rpc service name by rpcRequest
        String rpcServiceName = rpcRequest.getRpcServiceName();
        ConsistentHashSelector selector = selectors.get(rpcServiceName);
        // check for updates
        if (selector == null || selector.identityHashCode != identityHashCode) {
            selector = new ConsistentHashSelector(addresses, DEFAULT_REPLICA_NUMBER, identityHashCode);
            selectors.put(rpcServiceName, selector);
        }
        return selector.select(rpcServiceName + Arrays.stream(rpcRequest.getParameters()));
    }

    /**
     * Consistent hash selector
     */
    static class ConsistentHashSelector {
        private static final int SPLITS = 4;
        private final TreeMap<Long, String> virtualInvokers;
        private final int identityHashCode;

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;
            initInvokers(invokers, replicaNumber);
        }

        private static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
            return md.digest();
        }

        private static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        String select(String rpcServiceKey) {
            byte[] digest = md5(rpcServiceKey);
            return selectForKey(hash(digest, 0));
        }

        private void initInvokers(List<String> invokers, int replicaNumber) {
            for (String invoker : invokers) {
                for (int i = 0; i < replicaNumber / SPLITS; i++) {
                    byte[] digest = md5(invoker + i);
                    for (int h = 0; h < SPLITS; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        private String selectForKey(long hashCode) {
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();
            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }
            return entry.getValue();
        }
    }
}

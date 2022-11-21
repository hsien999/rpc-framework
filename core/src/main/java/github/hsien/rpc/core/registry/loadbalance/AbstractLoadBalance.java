package github.hsien.rpc.core.registry.loadbalance;

import github.hsien.rpc.common.util.CollectionUtils;
import github.hsien.rpc.core.dto.RpcRequest;

import java.util.List;

/**
 * Abstract class a load balancing policy
 *
 * @author hsien
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> addresses, RpcRequest rpcRequest) {
        if (CollectionUtils.isEmpty(addresses)) {
            return null;
        }
        if (addresses.size() == 1) {
            return addresses.get(0);
        }
        return doSelect(addresses, rpcRequest);
    }

    /**
     * Choose one from the service addresses list (exemption from empty check) by load balance policy
     *
     * @param addresses  address list
     * @param rpcRequest rpc request
     * @return the selected address
     */
    protected abstract String doSelect(List<String> addresses, RpcRequest rpcRequest);
}

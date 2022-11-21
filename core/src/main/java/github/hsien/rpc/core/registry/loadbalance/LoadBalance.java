package github.hsien.rpc.core.registry.loadbalance;

import github.hsien.rpc.common.loader.annotion.SPI;
import github.hsien.rpc.core.dto.RpcRequest;

import java.util.List;

/**
 * Interface for loading balance policy
 *
 * @author shuang.kou
 */
@SPI
public interface LoadBalance {
    /**
     * Choose one from the service addresses list by load balance policy
     *
     * @param addresses  address list
     * @param rpcRequest rpc request
     * @return the selected address
     */
    String selectServiceAddress(List<String> addresses, RpcRequest rpcRequest);
}

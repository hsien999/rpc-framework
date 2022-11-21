package github.hsien.rpc.core.dto;

import github.hsien.rpc.core.remoting.util.ServiceUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


/**
 * A generic rpc request message body
 *
 * @author hsien
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 3536708577156373876L;
    /**
     * request id
     */
    private String requestId;
    /**
     * interface name (class name or the first interface name if there is one)
     */
    private String interfaceName;
    /**
     * name of the called method
     */
    private String methodName;
    /**
     * parameter types of the called method
     */
    private Class<?>[] paramTypes;
    /**
     * parameter of the called method
     */
    private Object[] parameters;
    /**
     * group name of subscribed service
     */
    private String group;
    /**
     * version of subscribed service
     */
    private String version;

    /**
     * Get the rpc service name
     * rpc service name = interface name + group + version
     *
     * @return rpc service name
     */
    public String getRpcServiceName() {
        return ServiceUtils.getRpcServiceName(getInterfaceName(), getGroup(), getVersion());
    }
}

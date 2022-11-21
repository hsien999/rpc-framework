package github.hsien.rpc.core.dto;

import github.hsien.rpc.core.remoting.util.ServiceUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Rpc service info
 *
 * @author hsien
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class RpcServiceConfig {
    /**
     * service version
     */
    private String version = "";
    /**
     * group name of service
     */
    private String group = "";
    /**
     * provided service object in server site
     */
    private Object service;

    /**
     * Get the rpc service name
     * rpc service name = interface name + group + version
     *
     * @return rpc service name
     */
    public String getRpcServiceName() {
        return ServiceUtils.getRpcServiceName(getInterfaceName(), getGroup(), getVersion());
    }

    /**
     * Get the interface name, = class name or the first interface name if there is one
     *
     * @return interface name
     */
    private String getInterfaceName() {
        Class<?> clazz = this.service.getClass();
        Class<?>[] interfaces = clazz.getInterfaces();
        return interfaces.length > 0 ? interfaces[0].getCanonicalName() : clazz.getCanonicalName();
    }
}

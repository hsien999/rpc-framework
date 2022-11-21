package github.hsien.rpc.core.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * A generic rpc response message body
 *
 * @author hsien
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = 1104289926102715582L;
    /**
     * request id
     */
    private String requestId;
    /**
     * response code
     */
    private int code;
    /**
     * response message
     */
    private String message;
    /**
     * response body
     */
    private T data;

    /**
     * Return a common success rpc response
     *
     * @param data      response body (result of the service call)
     * @param requestId request id
     * @param <T>       the parameter type of result
     * @return RpcResponse<T> rpc response
     */
    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCode.SUCCESS.getCode());
        response.setMessage(RpcResponseCode.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    /**
     * Return a common failed rpc response
     *
     * @param rpcResponseCode rpc response code
     * @param <T>             the parameter type of result
     * @return rpc response
     */
    public static <T> RpcResponse<T> fail(RpcResponseCode rpcResponseCode) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCode.getCode());
        response.setMessage(rpcResponseCode.getMessage());
        return response;
    }
}

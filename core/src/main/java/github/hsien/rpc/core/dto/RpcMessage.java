package github.hsien.rpc.core.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * A generic rpc transport entity
 *
 * @author hsien
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class RpcMessage<T extends Serializable> {
    /**
     * message type
     */
    private byte messageType;
    /**
     * serialization type
     */
    private byte codec;
    /**
     * compress type
     */
    private byte compress;
    /**
     * message id
     */
    private int messageId;
    /**
     * message body (request/response/heartbeat)
     */
    private T data;
}

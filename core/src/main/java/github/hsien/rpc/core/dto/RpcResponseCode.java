package github.hsien.rpc.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author hsien
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCode {
    /**
     * success response
     */
    SUCCESS(200, "Success Invocation"),
    /**
     * error response
     */
    FAIL(500, "Error Invocation");

    /**
     * response code
     */
    private final int code;
    /**
     * response message
     */
    private final String message;
}

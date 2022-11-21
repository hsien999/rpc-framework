package github.hsien.rpc.example.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Hello entity
 *
 * @author hsien
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hello implements Serializable {
    private static final long serialVersionUID = -2687978975340845454L;
    private int code;
    private String message;
    private Date date;
}

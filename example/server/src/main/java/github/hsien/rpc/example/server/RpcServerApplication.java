package github.hsien.rpc.example.server;


import github.hsien.rpc.spring.annotation.EnableRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Rpc Server Application
 *
 * @author hsien
 */
@SpringBootApplication
@EnableRpc
public class RpcServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpcServerApplication.class, args);
    }
}

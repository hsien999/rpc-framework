package github.hsien.rpc.example.client;

import github.hsien.rpc.spring.annotation.EnableRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Rpc Client Application
 *
 * @author hsien
 */
@SpringBootApplication
@EnableRpc
public class RpcClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpcClientApplication.class, args);
    }
}

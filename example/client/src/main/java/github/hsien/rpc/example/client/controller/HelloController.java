package github.hsien.rpc.example.client.controller;

import github.hsien.rpc.example.service.Hello;
import github.hsien.rpc.example.service.HelloService;
import github.hsien.rpc.spring.annotation.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Hello controller for call hello service
 *
 * @author hsien
 */
@RestController
public class HelloController {
    private static final AtomicInteger CALL_COUNT = new AtomicInteger();

    @RpcReference(group = "test", version = "v1.0")
    private HelloService helloService;

    @GetMapping(value = "/hello", produces = "application/json")
    public Map<String, String> sayHello() {
        int count = CALL_COUNT.getAndIncrement();
        return Collections.singletonMap("hello",
            helloService.sayHello(new Hello(count, String.format("第%d个消息", count), new Date())));
    }
}

package github.hsien.rpc.example.server.service;

import github.hsien.rpc.example.service.Hello;
import github.hsien.rpc.example.service.HelloService;
import github.hsien.rpc.spring.annotation.RpcService;

import java.text.DateFormat;
import java.util.Locale;


/**
 * Hello service implement
 *
 * @author hsien
 */
@RpcService(group = "EN", version = "v1.0")
public class HelloServiceEN implements HelloService {
    @Override
    public String sayHello(Hello hello) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.ENGLISH);
        DateFormat tf = DateFormat.getTimeInstance(DateFormat.FULL, Locale.ENGLISH);
        String helloMsg = "[Service]=%s, [code]=%d, [msg]=%s, [date]=%s";
        return String.format(helloMsg, this.getClass().getName(), hello.getCode(),
            hello.getMessage(), df.format(hello.getDate()) + " " + tf.format(hello.getDate()));
    }
}

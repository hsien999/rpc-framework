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
@RpcService(group = "CN", version = "v1.0")
public class HelloServiceCN implements HelloService {
    @Override
    public String sayHello(Hello hello) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.CHINA);
        DateFormat tf = DateFormat.getTimeInstance(DateFormat.LONG, Locale.CHINA);
        String helloMsg = "[服务]=%s, [编码]=%d, [消息]=%s, [日期]=%s";
        return String.format(helloMsg, this.getClass().getName(), hello.getCode(),
            hello.getMessage(), df.format(hello.getDate()) + " " + tf.format(hello.getDate()));
    }
}

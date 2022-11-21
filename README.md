# rpc-framework

> A simple rpc framework for learning rpc-related basics only

## Start

1. Build and install by maven
2. Modify the address of the zookeeper registry
   in [client/rpc.properties](example/client/src/main/resources/rpc.properties)
   and [server/rpc.properties](example/server/src/main/resources/rpc.properties)
3. Run [RpcClientApplication](example/client/src/main/java/github/hsien/rpc/example/client/RpcClientApplication.java)
   and [RpcServerApplication](example/server/src/main/java/github/hsien/rpc/example/server/RpcServerApplication.java)
4. Test by a [get request](test/rest-api.http)

## TODO

- [x] **Use Netty (based on NIO) instead of BIO to implement network transfers.**
- [x] **Use Zookeeper to manage related service address information**
- [x] **Support for multiple open source serialization mechanisms: Kryo, Hessian and Protostuff.**
- [x] **Support for Netty server-side reusable Channels to improve the utilization of network connection resources.**
- [x] **Support for compressing/decompressing messages using Gzip.**
- [x] **Support Netty-based LengthFieldBasedFrameDecoder encoder for message body encoding and decoding.**
- [x] **Add Netty heartbeat mechanism to verify the connection between client and server to avoid reconnection.**
- [x] **Support load balancing algorithm for service calls, currently implementing random load balancing and consistency
  hash algorithm.**
- [x] **Enhance the SPI mechanism to support the specification of service name, priority and scope.**
- [x] **Integrating Spring, registering service provider implementation classes and service consumer beans via
  annotations.**
- [ ] **Add configurable features, such as serialization methods, registry implementations, etc., to avoid hard coding
  and integrate with SpringBoot autoconfiguration.**
- [ ] **Add unit test cases and integration test methods.**
- [ ] **Support Services Monitoring Center.**
- [ ] **Improve high availability and distribution in more ways.**
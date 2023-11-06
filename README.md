<h1 align="center" style="text-align:center;">
  SocketD
</h1>
<p align="center">
	<strong>基于连接的可扩展消息传输协议</strong>
</p>

<p align="center">
    <a target="_blank" href="https://search.maven.org/artifact/org.noear/socketd">
        <img src="https://img.shields.io/maven-central/v/org.noear/socketd.svg?label=Maven%20Central" alt="Maven" />
    </a>
    <a target="_blank" href="https://www.apache.org/licenses/LICENSE-2.0.txt">
		<img src="https://img.shields.io/:license-Apache2-blue.svg" alt="Apache 2" />
	</a>
   <a target="_blank" href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">
		<img src="https://img.shields.io/badge/JDK-8-green.svg" alt="jdk-8" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-11-green.svg" alt="jdk-11" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-17-green.svg" alt="jdk-17" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-21-green.svg" alt="jdk-21" />
	</a>
    <br />
    <a target="_blank" href='https://gitee.com/noear/socketd/stargazers'>
        <img src='https://gitee.com/noear/socketd/badge/star.svg' alt='gitee star'/>
    </a>
    <a target="_blank" href='https://github.com/noear/socketd/stargazers'>
        <img src="https://img.shields.io/github/stars/noear/socketd.svg?logo=github" alt="github star"/>
    </a>
</p>

<br/>
<p align="center">
	<a href="https://jq.qq.com/?_wv=1027&k=kjB5JNiC">
	<img src="https://img.shields.io/badge/QQ交流群-870505482-orange"/></a>
</p>


<hr />



SocketD 是一个基于连接的、可扩展的、主题消息驱动的传输协议。主要特性有：

* 异步通讯，非阻塞，由主题消息驱动
* 语言无关，二进制通信协议（支持 tcp, ws, udp）。支持多语言、多平台
* 背压流控，请求时不让你把服务端发死了
* 断线重连，自动连接恢复
* 双向通讯，单链接双向互发双向互听
* 多路复用
* 自动分片，数据超出 16Mb，会自动分片、自动重组（udp 除外）
* 扩展定制，可以为数据添加 meta 标注（就像 http header）
* 接口简单

### 快速入门与学习

请点击：[《快速入门与学习》](_docs/)。Java 之外的语言与平台会尽快跟进（欢迎有兴趣的同学加入社区）

### 适用场景

可用于 MSG、RPC、IM、MQ 等一些的场景开发，可替代 Http, Websocket, gRpc 等一些协议。比如移动设备与服务器的连接，比如一些微服务场景等等。


### 简单的协议


* link (url style)

```
tcp://19.10.2.3:9812/path?u=noear&t=1234
udp://19.10.2.3:9812/path?u=noear&t=1234
ws://19.10.2.3:1023/path?u=noear&t=1234
```


* codec

```
//udp only <2k, and no auto fragments
[len:int][flag:int][sid:str(<64)][\n][topic:str(<512)][\n][metaString:str(<4k)][\n][data:byte(<16m)]
```

* flag & flow

| Flag      | Server                               | Client                                          | 备注                      |
|-----------|--------------------------------------|-------------------------------------------------|-------------------------|
| Unknown   | ::close()                            | ::close()                                       |                         |
| Connect   | /                                    | c(Connect)->s::onOpen(),s(Connack)->c::onOpen() |                         |
| Connack   | s::onOpen(),s(Connack)->c            | /                                               |                         |
| Ping      | /                                    | c(Ping)->s(Pong)->c                             |                         |
| Pong      | s(Pong)->c                           | /                                               |                         |
| Close     | s(Close)->c::onClose()               | c(Close)->s::onClose()                          | 用于特殊场景（如：udp）           |
| Message   | s(Message)->c                        | c(Message)->s                                   |                         |
| Request   | s(Request)->c(Reply or ReplyEnd)->s  | c(Request)->s(Reply or ReplyEnd)->c             |               |
| Subscribe | s(Subscribe)->c(Reply...ReplyEnd)->s | c(Subscribe)->s(Reply...ReplyEnd)->c            |                         |
| Reply     | ->s(Reply)->c                        | ->c(Reply)->s                                   |                         |
| ReplyEnd  | ->s(ReplyEnd)->c                     | ->c(ReplyEnd)->s                                | 结束答复                    |

```
//The reply acceptor registration in the channel is removed after the reply is completed
```

### 加入到交流群：

| QQ交流群：870505482                       | 微信交流群（申请时输入：SocketD）                   |
|---------------------------|----------------------------------------|
|        | <img src="group_wx.png" width="120" /> 

交流群里，会提供 "保姆级" 支持和帮助。如有需要，也可提供技术培训和顾问服务

### 第一个程序：你好世界！

```java
public class Demo {
    public static void main(String[] args) throws Throwable {
        //::启动服务端
        SocketD.createServer(new ServerConfig("tcp").port(8602))
                .listen(new SimpleListener(){
                    @Override
                    public void onMessage(Session session, Message message) throws IOException {
                        if(message.isRequest()){
                            session.replyEnd(message, new StringEntity("And you too."));
                        }
                    }
                })
                .start();

        Thread.sleep(1000); //等会儿，确保服务端启动完成
        
        //::打开客户端会话
        Session session = SocketD.createClient("tcp://127.0.0.1:8602/hello?token=1b0VsGusEkddgr3d")
                .open();
        
        //发送并请求（且，收回答复）
        Entity reply = session.sendAndRequest("/demo", new StringEntity("Hello wrold!").meta("user","noear"));
    }
}
```



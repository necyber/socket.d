package labs;

import org.noear.socketd.broker.bio.BioBroker;
import org.noear.socketd.client.ClientConfig;
import org.noear.socketd.protocol.Listener;
import org.noear.socketd.protocol.Payload;
import org.noear.socketd.protocol.Session;
import org.noear.socketd.server.Server;
import org.noear.socketd.server.ServerConfig;
import org.noear.socketd.utils.Utils;

import java.io.IOException;

/**
 * @author noear
 * @since 2.0
 */
public class ClientTest {
    public static void main(String[] args) throws Exception {
        BioBroker broker = new BioBroker();

        //client
        ClientConfig clientConfig = new ClientConfig();
        Session session = broker.createClient(clientConfig)
                .url("emp:ws://localhost:6329/path?u=a&p=2")
                .listen(null) //如果要监听，加一下
                .heartbeatHandler(null) //如果要替代 ping,pong 心跳，加一下
                .autoReconnect(true) //自动重链
                .open();
        session.send(new Payload(Utils.guid(), "/user/created", "", "hi".getBytes()));
        //Payload response = session.sendAndRequest(null);
        //session.sendAndSubscribe(null, null);
    }
}

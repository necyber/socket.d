package labs.tcp.bio;

import org.noear.socketd.SocketD;
import org.noear.socketd.core.Message;
import org.noear.socketd.core.Session;
import org.noear.socketd.core.SimpleListener;
import org.noear.socketd.core.entity.StringEntity;
import org.noear.socketd.server.Server;
import org.noear.socketd.server.ServerConfig;

import java.io.IOException;


/**
 * @author noear
 * @since 2.0
 */
public class ServerTest {
    public static void main(String[] args) throws Exception {
        //server
        SocketD.createServer(new ServerConfig("tcp"))
                .listen(new ServerListener())
                .start();
    }

    public static class ServerListener extends SimpleListener {
        @Override
        public void onMessage(Session session, Message message) throws IOException {
            super.onMessage(session, message);

            if (message.isRequest()) {
                session.reply(message, new StringEntity("hi reply"));
            }

            if (message.isSubscribe()) {
                session.reply(message, new StringEntity("hi reply"));
                session.reply(message, new StringEntity("hi reply**"));
                session.reply(message, new StringEntity("hi reply****"));
            }


            session.send("demo", new StringEntity("test"));
        }
    }
}
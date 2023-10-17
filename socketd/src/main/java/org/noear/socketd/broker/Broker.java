package org.noear.socketd.broker;

import org.noear.socketd.client.Client;
import org.noear.socketd.client.ClientConfig;
import org.noear.socketd.server.Server;
import org.noear.socketd.server.ServerConfig;

/**
 * 经纪人
 *
 * @author noear
 * @since 2.0
 */
public interface Broker {
    static Broker getInstance() {
        return null;
    }

    Server createServer(ServerConfig config);

    Client createClient(ClientConfig config);
}
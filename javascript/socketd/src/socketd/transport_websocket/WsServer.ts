import {Server, ServerBase} from "../transport/server/Server";
import {WsChannelAssistant} from "./WsChannelAssistant";
import {ChannelSupporter} from "../transport/core/ChannelSupporter";
import {SdWebSocket, SdWebSocketCloseEvent, SdWebSocketErrorEvent, SdWebSocketEvent,
    SdWebSocketListener,
    SdWebSocketMessageEvent
} from "./impl/SdWebSocket";
import {SocketD} from "../SocketD";
import NodeWebSocket from 'ws';
import {ChannelDefault} from "../transport/core/ChannelDefault";
import {SdWebSocketNodeJs} from "./impl/SdWebSocketNodeJs";
import {ServerConfig} from "../transport/server/ServerConfig";

export class WsServer extends ServerBase<WsChannelAssistant> implements ChannelSupporter<SdWebSocket> {
    private _server: NodeWebSocket.Server;

    constructor(config: ServerConfig) {
        super(config, new WsChannelAssistant(config));
    }

    getTitle(): string {
        return "ws/js-websocket/v" + SocketD.version();
    }

    start(): Server {
        this._server = new NodeWebSocket.Server({
            port: this.getConfig().getPort()
        });

        const serverListener: SdWebSocketServerListener = new SdWebSocketServerListener(this);

        this._server.on("connection", ws => {
            const socket = new SdWebSocketNodeJs(ws, serverListener);
            const channl = new ChannelDefault(socket, serverListener.getServer());
            socket.attachmentPut(channl);
            socket.onOpen();
        });

        return this;
    }

    stop() {
        if (this._isStarted) {
            this._isStarted = false;
        } else {
            return;
        }

        try {
            if (this._server != null) {
                this._server.close();
            }
        } catch (e) {
            console.debug("Server stop error", e);
        }
    }
}

class SdWebSocketServerListener implements SdWebSocketListener {
    private _server: WsServer;

    constructor(server: WsServer) {
        this._server = server;
    }

    getServer(): WsServer {
        return this._server;
    }

    onOpen(e: SdWebSocketEvent): void {
        let channel = e.socket().attachment();
        this._server.getProcessor().onClose(channel);
    }

    onMessage(e: SdWebSocketMessageEvent): void {
        let channel = e.socket().attachment();
        let frame = this._server.getAssistant().read(e.data());

        if (frame != null) {
            this._server.getProcessor().onReceive(channel, frame);
        }
    }

    onClose(e: SdWebSocketCloseEvent): void {
        let channel = e.socket().attachment();
        this._server.getProcessor().onClose(channel);
    }

    onError(e: SdWebSocketErrorEvent): void {
        let channel = e.socket().attachment();
        if (channel) {
            //有可能未 onOpen，就 onError 了；此时通道未成
            this._server.getProcessor().onError(channel, e.error());
        }
    }
}
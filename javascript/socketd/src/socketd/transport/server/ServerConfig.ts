import {ConfigBase} from "../core/Config";

export class ServerConfig extends ConfigBase {
    private _schema: string;

    //主机名
    private _host: string;
    //端口
    private _port: number;

    constructor(schema: string) {
        super(false);
        //支持 sd: 开头的架构
        if (schema.startsWith("sd:")) {
            schema = schema.substring(3);
        }

        this._schema = schema;

        this._host = "";
        this._port = 8602;
    }


    /**
     * 获取协议架构
     */
    getSchema(): string {
        return this._schema;
    }

    /**
     * 获取主机
     */
    getHost(): string {
        return this._host;
    }

    /**
     * 配置主机
     */
    host(host: string): this {
        this._host = host;
        return this;
    }

    /**
     * 获取端口
     */
    getPort(): number {
        return this._port;
    }

    /**
     * 配置端口
     */
    port(port: number): this {
        this._port = port;
        return this;
    }

    /**
     * 获取本机地址
     */
    getLocalUrl(): string {
        if (this._host) {
            return "sd:" + this._schema + "://127.0.0.1:" + this._port;
        } else {
            return "sd:" + this._schema + "://" + this._host + ":" + this._port;
        }
    }

    toString(): string {
        return "ServerConfig{" +
            "schema='" + this._schema + '\'' +
            ", charset=" + this._charset +
            ", host='" + this._host + '\'' +
            ", port=" + this._port +
            ", coreThreads=" + this._coreThreads +
            ", maxThreads=" + this._maxThreads +
            ", idleTimeout=" + this._idleTimeout +
            ", replyTimeout=" + this._requestTimeout +
            ", readBufferSize=" + this._readBufferSize +
            ", writeBufferSize=" + this._writeBufferSize +
            ", maxUdpSize=" + this._maxUdpSize +
            '}';
    }
}
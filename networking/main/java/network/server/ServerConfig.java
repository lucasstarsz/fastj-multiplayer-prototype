package network.server;

import java.net.InetAddress;

public record ServerConfig(int port, int backlog, InetAddress localAddress) {

    public ServerConfig(int port, int backlog) {
        this(port, backlog, null);
    }

    public ServerConfig(int port) {
        this(port, 50, null);
    }
}

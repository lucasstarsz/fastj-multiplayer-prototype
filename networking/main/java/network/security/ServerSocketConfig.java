package network.security;

import java.net.InetAddress;

public record ServerSocketConfig(int port, int backlog, InetAddress localAddress) {

    public ServerSocketConfig(int port, int backlog) {
        this(port, backlog, null);
    }

    public ServerSocketConfig(int port) {
        this(port, 50, null);
    }
}

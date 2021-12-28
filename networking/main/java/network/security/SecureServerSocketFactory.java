package network.security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;

import network.security.util.SecureUtil;
import network.server.ServerConfig;

public class SecureServerSocketFactory {

    private static final String Instance = "JKS";

    public static SSLServerSocket getServerSocket(ServerConfig serverConfig, SecureServerConfig secureServerConfig) throws IOException, GeneralSecurityException {
        SSLContext sslContext = SecureUtil.generateSSLContext(secureServerConfig, Instance);

        SSLServerSocketFactory socketFactory = sslContext.getServerSocketFactory();
        return (SSLServerSocket) socketFactory.createServerSocket(
                serverConfig.port(),
                serverConfig.backlog(),
                serverConfig.localAddress()
        );
    }
}

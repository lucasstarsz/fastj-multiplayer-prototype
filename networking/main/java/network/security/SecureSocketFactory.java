package network.security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;

import network.security.util.SecureUtil;

public class SecureSocketFactory {

    private static final String Instance = "JKS";

    public static SSLSocket getSocket(SocketConfig socketConfig, SecureServerConfig secureServerConfig) throws IOException, GeneralSecurityException {
        SSLContext sslContext = SecureUtil.generateSSLContext(secureServerConfig, Instance);

        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        return (SSLSocket) socketFactory.createSocket(
                socketConfig.host(),
                socketConfig.port()
        );
    }
}


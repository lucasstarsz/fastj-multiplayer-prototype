package network.security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;

import network.client.ClientConfig;
import network.security.util.SecureUtil;

public class SecureSocketFactory {

    private static final String Instance = "JKS";

    public static SSLSocket getDefault(ClientConfig clientConfig) throws IOException {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        return (SSLSocket) sslSocketFactory.createSocket(clientConfig.host(), clientConfig.port());
    }

    public static SSLSocket getSocket(ClientConfig clientConfig, SecureServerConfig secureServerConfig) throws IOException, GeneralSecurityException {
        SSLContext sslContext = SecureUtil.generateSSLContext(secureServerConfig, Instance);
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        return (SSLSocket) socketFactory.createSocket(clientConfig.host(), clientConfig.port());
    }

    public static SSLSocket getSocket(ClientConfig clientConfig, SSLContext sslContext) throws IOException {
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        return (SSLSocket) socketFactory.createSocket(clientConfig.host(), clientConfig.port());
    }
}


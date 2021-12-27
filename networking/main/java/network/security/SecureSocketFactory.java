package network.security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import network.security.util.SecureUtil;

public class SecureSocketFactory {

    private static final String Instance = "JKS";

    public static SSLSocket getCertifiedSocket(String ip, int port, Path certificatePath, String certificatePassword, SecureTypes secureTypes)
            throws CertificateException, IOException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        if (!Files.exists(certificatePath)) {
            throw new NullPointerException("A file at \"" + certificatePath.toAbsolutePath() + "\" was not found.");
        }

        return getCertifiedSocket(ip, port, new FileInputStream(certificatePath.toFile()), certificatePassword, secureTypes);
    }

    public static SSLSocket getCertifiedSocket(String ip, int port, InputStream certificateFileStream, String certificatePassword, SecureTypes secureTypes)
            throws CertificateException, IOException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        SSLContext sslContext = SecureUtil.generateSSLContext(certificateFileStream, certificatePassword, secureTypes, Instance);

        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        return (SSLSocket) socketFactory.createSocket(ip, port);
    }
}


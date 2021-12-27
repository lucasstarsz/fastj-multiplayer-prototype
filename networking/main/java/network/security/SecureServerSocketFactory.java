package network.security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
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

public class SecureServerSocketFactory {

    private static final String Instance = "JKS";

    public static SSLServerSocket getCertifiedServerSocket(int port, Path certificatePath, String certificatePassword, SecureTypes secureTypes)
            throws IOException, KeyManagementException, NoSuchAlgorithmException, CertificateException, KeyStoreException, UnrecoverableKeyException {
        if (!Files.exists(certificatePath)) {
            throw new NullPointerException("A file at \"" + certificatePath.toAbsolutePath() + "\" was not found.");
        }

        return getCertifiedServerSocket(port, new FileInputStream(certificatePath.toFile()), certificatePassword, secureTypes);
    }

    public static SSLServerSocket getCertifiedServerSocket(int port, InputStream certificateFileStream, String certificatePassword, SecureTypes secureTypes)
            throws IOException, KeyManagementException, NoSuchAlgorithmException, CertificateException, KeyStoreException, UnrecoverableKeyException {
        SSLContext sslContext = SecureUtil.generateSSLContext(certificateFileStream, certificatePassword, secureTypes, Instance);

        SSLServerSocketFactory socketFactory = sslContext.getServerSocketFactory();
        return (SSLServerSocket) socketFactory.createServerSocket(port);
    }
}

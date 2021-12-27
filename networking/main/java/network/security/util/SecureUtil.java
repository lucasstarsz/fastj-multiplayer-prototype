package network.security.util;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import network.security.SecureTypes;

public class SecureUtil {

    public static X509TrustManager getTrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keystore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }

        return null;
    }

    public static X509KeyManager getKeyManager(KeyStore keystore, String password) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keystore, password.toCharArray());
        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

        for (KeyManager keyManager : keyManagers) {
            if (keyManager instanceof X509KeyManager) {
                return (X509KeyManager) keyManager;
            }
        }

        return null;
    }

    public static SSLContext generateSSLContext(InputStream certificateFileStream, String certificatePassword, SecureTypes secureTypes, String instance)
            throws CertificateException, IOException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance(instance);
        keyStore.load(certificateFileStream, certificatePassword.toCharArray());

        X509TrustManager keyStoreTrustManager = SecureUtil.getTrustManager(keyStore);
        X509KeyManager keyStoreKeyManager = SecureUtil.getKeyManager(keyStore, certificatePassword);

        SSLContext sslContext = SSLContext.getInstance(secureTypes.rawType);
        sslContext.init(
                new KeyManager[]{keyStoreKeyManager},
                new X509TrustManager[]{keyStoreTrustManager},
                null
        );

        return sslContext;
    }
}

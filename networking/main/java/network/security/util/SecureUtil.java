package network.security.util;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

import network.security.SecureServerConfig;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

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

    public static X509KeyManager getKeyManager(KeyStore keystore, String password) throws GeneralSecurityException {
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

    public static SSLContext generateSSLContext(SecureServerConfig secureServerConfig, String keyStoreType) throws IOException, GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(secureServerConfig.certificateFile(), secureServerConfig.certificatePassword().toCharArray());

        return createSSLContext(secureServerConfig, keyStore);
    }

    public static SSLContext generateSSLContext(SecureServerConfig secureServerConfig, String keyStoreType, Certificate certificate, String alias) throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry(alias, certificate);

        return createSSLContext(secureServerConfig, keyStore);
    }

    public static SSLContext createSSLContext(SecureServerConfig secureServerConfig, KeyStore keyStore) throws GeneralSecurityException {
        X509TrustManager keyStoreTrustManager = SecureUtil.getTrustManager(keyStore);
        X509KeyManager keyStoreKeyManager = SecureUtil.getKeyManager(keyStore, secureServerConfig.certificatePassword());

        SSLContext sslContext = SSLContext.getInstance(secureServerConfig.secureType().rawType);
        sslContext.init(
                new KeyManager[]{keyStoreKeyManager},
                new X509TrustManager[]{keyStoreTrustManager},
                null
        );

        return sslContext;
    }

    /**
     * Create a self-signed X.509 certificate.
     * <p>
     * See: <a href="https://stackoverflow.com/questions/1615871/creating-an-x509-certificate-in-java-without-bouncycastle/5488964#5488964"
     * target="_blank">This amazing StackOverflow post</a>.
     *
     * @param distinguishedName  the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
     * @param keyPair            the KeyPair to get public/private keys from.
     * @param days               how many days from now the certificate is valid for.
     * @param signatureAlgorithm the signing algorithm, eg "SHA1withRSA"
     */
    public static X509Certificate generateCertificate(String distinguishedName, KeyPair keyPair, int days, String signatureAlgorithm) throws GeneralSecurityException, IOException {
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        X509CertInfo certificateInfo = new X509CertInfo();

        Date from = new Date();
        Date to = new Date(from.getTime() + days * 86400000L);
        CertificateValidity certificateValidityPeriod = new CertificateValidity(from, to);

        BigInteger serialNumber = new BigInteger(64, new SecureRandom());
        X500Name owner = new X500Name(distinguishedName);
        AlgorithmId algorithmId = new AlgorithmId(AlgorithmId.MD5_oid);

        certificateInfo.set(X509CertInfo.VALIDITY, certificateValidityPeriod);
        certificateInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(serialNumber));
        certificateInfo.set(X509CertInfo.SUBJECT, owner);
        certificateInfo.set(X509CertInfo.ISSUER, owner);
        certificateInfo.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));
        certificateInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        certificateInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algorithmId));

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(certificateInfo);
        cert.sign(privateKey, signatureAlgorithm);

        // Update the algorithm, and resign.
        algorithmId = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
        certificateInfo.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algorithmId);
        cert = new X509CertImpl(certificateInfo);
        cert.sign(privateKey, signatureAlgorithm);
        return cert;
    }
}

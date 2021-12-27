package network.security;

public enum SecureTypes {
    @Deprecated
    SSL("SSL"),
    @Deprecated
    SSLv2("SSLv2"),
    @Deprecated
    SSLv3("SSLv3"),
    @Deprecated
    TLS("TLS"),
    @Deprecated
    TLSv1("TLSv1"),
    @Deprecated
    TLSv1_1("TLSv1.1"),
    TLSv1_2("TLSv1.2"),
    TLSv1_3("TLSv1.3");

    public final String rawType;

    SecureTypes(String rawType) {
        this.rawType = rawType;
    }
}
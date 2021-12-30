package server;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class ServerMain {
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        new GameServer().run();
    }
}

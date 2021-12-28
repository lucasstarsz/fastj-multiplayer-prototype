package game;

import java.io.IOException;
import java.security.GeneralSecurityException;

import network.security.SecureServerConfig;
import network.security.SecureTypes;
import network.server.Server;
import network.server.ServerConfig;
import util.FilePaths;
import util.Networking;

public class GameServer implements Runnable {

    private final Server server;
    private final ServerState serverState;

    public GameServer() throws IOException, GeneralSecurityException {
        ServerConfig serverConfig = new ServerConfig(Networking.Port);
        SecureServerConfig secureServerConfig = new SecureServerConfig(
                FilePaths.PrivateGameKeyPath,
                "sslpassword",
                SecureTypes.TLSv1_3
        );

        server = new Server(serverConfig, secureServerConfig);
        serverState = new ServerState(server);

        setupClientActions();
    }

    private void setupClientActions() {
        server.addOnClientConnect(serverState::syncAddPlayer);
        server.addOnClientDisconnect(serverState::syncRemovePlayer);
        server.addClientAction(Networking.Server.KeyPress, serverState::handleKeyPress);
        server.addClientAction(Networking.Server.KeyRelease, serverState::handleKeyRelease);
        server.addClientAction(Networking.Server.SyncTransform, serverState::syncPlayerTransform);
    }

    @Override
    public void run() {
        server.run();
    }
}

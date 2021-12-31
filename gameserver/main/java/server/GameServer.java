package server;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.UUID;

import core.util.Networking;
import network.security.SecureServerConfig;
import network.security.SecureTypes;
import network.server.ClientDataAction;
import network.server.Server;
import network.server.ServerClient;
import network.server.ServerCommand;
import network.server.ServerConfig;
import util.FilePaths;

public class GameServer implements Runnable {

    private final Server server;
    private final ServerState serverState;

    public GameServer() throws IOException, GeneralSecurityException {
        ServerConfig serverConfig = new ServerConfig(Networking.Port);
        SecureServerConfig secureServerConfig = new SecureServerConfig(
                FilePaths.PrivateGameKey,
                "sslprivatepassword",
                SecureTypes.TLSv1_3
        );

        server = new Server(serverConfig, secureServerConfig);
        serverState = new ServerState(server);
        setupClientActions();
    }

    private void setupClientActions() {
        server.addOnClientConnect(serverState::syncAddPlayer);
        server.addOnClientDisconnect(serverState::syncRemovePlayer);
        server.addServerCommand(new ServerCommand(Networking.ServerCommands.ToggleClientConnect, this::toggleClientConnect));
        server.addClientAction(new ClientDataAction(Networking.Server.KeyPress, serverState::handleKeyPress));
        server.addClientAction(new ClientDataAction(Networking.Server.KeyRelease, serverState::handleKeyRelease));
        server.addClientAction(new ClientDataAction(Networking.Server.SyncTransform, serverState::syncPlayerTransform));
        server.addClientAction(new ClientDataAction(Networking.Server.CreateSnowball, serverState::createSnowball));
    }

    private void toggleClientConnect(String s, Map<UUID, ServerClient> uuidServerClientMap) {
        if (server.isAcceptingClients()) {
            server.disallowClients();
        } else {
            server.allowClients();
        }
    }

    @Override
    public void run() {
        server.run();
    }
}

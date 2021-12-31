package server;

import tech.fastj.logging.Log;

import tech.fastj.input.keyboard.Keys;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import core.util.Networking;
import network.server.Server;
import network.server.ServerClient;

public class ServerState {

    private final Map<UUID, Integer> idToPlayers = new HashMap<>();
    private final Map<Integer, ServerClient> players = new HashMap<>();
    private final Server server;

    private int newPlayerIncrement = 1;

    public ServerState(Server server) {
        this.server = server;
    }

    void syncAddPlayer(ServerClient addedClient, Map<UUID, ServerClient> allClients) {
        try {
            int playerNumber = newPlayerIncrement++;
            addedClient.out().writeInt(playerNumber);
            addedClient.out().flush();
            Log.info(this.getClass(), "client {} set to player {}", addedClient.getId(), playerNumber);

            idToPlayers.put(addedClient.getId(), playerNumber);
            players.put(playerNumber, addedClient);

            // add new player to other clients
            for (ServerClient serverClient : allClients.values()) {
                if (serverClient == addedClient) {
                    continue;
                }
                Log.info(this.getClass(), "sending player {} to {}", playerNumber, serverClient);

                try {
                    serverClient.send(Networking.Client.AddPlayer, playerNumber);
                } catch (IOException exception) {
                    if (tryRemoveClosedClient(serverClient, addedClient)) {
                        Log.error(this.getClass(), "Server IO error", exception);
                    }
                }
            }

            // add other clients to new player
            for (Map.Entry<Integer, ServerClient> player : players.entrySet()) {
                if (player.getValue() == addedClient) {
                    continue;
                }

                try {
                    addedClient.send(Networking.Client.AddPlayer, player.getKey());
                } catch (IOException exception) {
                    if (tryRemoveClosedClient(addedClient)) {
                        Log.error(this.getClass(), "Server IO error", exception);
                    }
                }
            }

        } catch (IOException exception) {
            if (tryRemoveClosedClient(addedClient)) {
                Log.error(this.getClass(), "Server IO error", exception);
            }
        }
    }

    void syncRemovePlayer(ServerClient removedClient, Map<UUID, ServerClient> otherClients) {
        int disconnectedPlayerNumber = idToPlayers.remove(removedClient.getId());
        players.remove(disconnectedPlayerNumber);

        for (ServerClient serverClient : otherClients.values()) {
            try {
                serverClient.send(Networking.Client.RemovePlayer, disconnectedPlayerNumber);
            } catch (IOException exception) {
                if (tryRemoveClosedClient(serverClient)) {
                    Log.error(this.getClass(), "Server IO error", exception);
                }
            }
        }
    }

    void handleKeyPress(ServerClient currentClient, Map<UUID, ServerClient> allClients) {
        String key = "";
        try {
            int player = currentClient.in().readInt();
            key = currentClient.in().readUTF();
            // ensure identifier integrity
            Keys.valueOf(key);
            Log.trace(this.getClass(), "player {} pressed {}", player, key);

            String keyPressed = key;
            for (ServerClient serverClient : allClients.values()) {
                if (serverClient.equals(currentClient)) {
                    continue;
                }

                try {
                    serverClient.send(Networking.Client.PlayerKeyPress, player, keyPressed);
                } catch (IOException exception) {
                    if (tryRemoveClosedClient(serverClient, currentClient)) {
                        Log.error(this.getClass(), "Server IO error", exception);
                    }
                }
            }
        } catch (IOException exception) {
            if (tryRemoveClosedClient(currentClient)) {
                Log.error(this.getClass(), "Server IO error", exception);
            }
        } catch (IllegalArgumentException exception) {
            Log.warn(this.getClass(), "Invalid identifier press {} from client {}", key, currentClient.getId());
        }
    }

    void handleKeyRelease(ServerClient currentClient, Map<UUID, ServerClient> allClients) {
        String key = "";
        try {
            int player = currentClient.in().readInt();
            key = currentClient.in().readUTF();
            // ensure identifier integrity
            Keys.valueOf(key);
            Log.trace(this.getClass(), "player {} released {}", player, key);

            String keyReleased = key;
            for (ServerClient serverClient : allClients.values()) {
                if (serverClient.equals(currentClient)) {
                    continue;
                }

                try {
                    serverClient.send(Networking.Client.PlayerKeyRelease, player, keyReleased);
                } catch (IOException exception) {
                    if (tryRemoveClosedClient(serverClient, currentClient)) {
                        Log.error(this.getClass(), "Server IO error", exception);
                    }
                }
            }
        } catch (IOException exception) {
            if (tryRemoveClosedClient(currentClient)) {
                Log.error(this.getClass(), "Server IO error", exception);
            }
        } catch (IllegalArgumentException exception) {
            Log.warn(this.getClass(), "Invalid identifier release {} from client {}", key, currentClient.getId());
        }
    }

    void syncPlayerTransform(ServerClient currentClient, Map<UUID, ServerClient> allClients) {
        try {
            int syncPlayerNumber = currentClient.in().readInt();
            if (!players.containsKey(syncPlayerNumber)) {
                Log.warn("Player {} was not found.");
                server.removeClient(currentClient.getId());
            }

            float translationX = currentClient.in().readFloat();
            float translationY = currentClient.in().readFloat();
            float rotation = currentClient.in().readFloat();

            Log.trace(
                    this.getClass(),
                    "Syncing player {} to {} {} {}",
                    syncPlayerNumber,
                    translationX,
                    translationY,
                    rotation
            );

            for (ServerClient serverClient : allClients.values()) {
                if (serverClient.equals(currentClient)) {
                    continue;
                }

                try {
                    serverClient.send(
                            Networking.Client.SyncPlayerTransform,
                            syncPlayerNumber,
                            translationX,
                            translationY,
                            rotation
                    );
                } catch (IOException exception) {
                    if (tryRemoveClosedClient(serverClient, currentClient)) {
                        Log.error(this.getClass(), "Server IO error", exception);
                    }
                }
            }
        } catch (IOException exception) {
            if (tryRemoveClosedClient(currentClient)) {
                Log.error(this.getClass(), "Server IO error", exception);
            }
        }
    }

    public boolean tryRemoveClosedClient(ServerClient... serverClients) {
        boolean removedClosedClient = false;
        for (ServerClient serverClient : serverClients) {
            if (serverClient.isConnectionClosed()) {
                server.removeClient(serverClient.getId());
                removedClosedClient = true;
            }
        }
        return !removedClosedClient;
    }
}

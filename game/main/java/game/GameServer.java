package game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;

import tech.fastj.input.keyboard.Keys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import network.security.SecureServerConfig;
import network.security.SecureTypes;
import network.server.ServerConfig;
import network.server.Server;
import network.server.ServerClient;
import util.FilePaths;
import util.Networking;

public class GameServer implements Runnable {

    private int playerIncrement = 1;
    private final Map<UUID, Integer> idToPlayers = new HashMap<>();
    private final Map<Integer, ServerClient> players = new HashMap<>();
    private final Server server;

    public GameServer() throws IOException, GeneralSecurityException {
        server = new Server(
                new ServerConfig(Networking.Port),
                new SecureServerConfig(
                        FilePaths.PrivateGameKeyPath,
                        "sslpassword",
                        SecureTypes.TLSv1_3
                )
        );
        initialize();
    }

    private void initialize() {
        server.addClientAction(Networking.Server.KeyPress, (currentClient, allClients) -> {
            String key = "";
            try {
                int player = currentClient.in().readInt();
                key = currentClient.in().readUTF();
                // ensure key integrity
                Keys.valueOf(key);
                Log.info(GameServer.class, "player {} pressed {}", player, key);

                String keyPressed = key;
                for (ServerClient serverClient : allClients.values()) {
                    if (serverClient.equals(currentClient)) {
                        continue;
                    }

                    try {
                        serverClient.send(Networking.Client.PlayerKeyPress, player, keyPressed);
                    } catch (IOException exception) {
                        FastJEngine.error("Server IO error", exception);
                    }
                }
            } catch (IOException exception) {
                FastJEngine.error("Server IO error", exception);
            } catch (IllegalArgumentException exception) {
                Log.warn(GameServer.class, "Invalid key press {} from client {}", key, currentClient.getId());
            }
        });

        server.addClientAction(Networking.Server.KeyRelease, (currentClient, allClients) -> {
            String key = "";
            try {
                int player = currentClient.in().readInt();
                key = currentClient.in().readUTF();
                // ensure key integrity
                Keys.valueOf(key);
                Log.info(GameServer.class, "player {} released {}", player, key);

                String keyReleased = key;
                for (ServerClient serverClient : allClients.values()) {
                    if (serverClient.equals(currentClient)) {
                        continue;
                    }

                    try {
                        serverClient.send(Networking.Client.PlayerKeyRelease, player, keyReleased);
                    } catch (IOException exception) {
                        FastJEngine.error("Server IO error", exception);
                    }
                }
            } catch (IOException exception) {
                FastJEngine.error("Server IO error", exception);
            } catch (IllegalArgumentException exception) {
                Log.warn(GameServer.class, "Invalid key release {} from client {}", key, currentClient.getId());
            }
        });

        server.addClientAction(Networking.Server.SyncTransform, (currentClient, allClients) -> {
            try {
                int syncPlayerNumber = currentClient.in().readInt();
                if (!players.containsKey(syncPlayerNumber)) {
                    Log.warn("Player {} was not found.");
                    server.removeClient(currentClient.getId());
                }

                float translationX = currentClient.in().readFloat();
                float translationY = currentClient.in().readFloat();
                float rotation = currentClient.in().readFloat();

                Log.info(
                        GameServer.class,
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
                        FastJEngine.error("Server IO error", exception);
                    }
                }
            } catch (IOException exception) {
                FastJEngine.error("Server IO error", exception);
            }
        });

        server.addOnClientConnect((addedClient, allClients) -> {
            try {
                int playerNumber = playerIncrement++;
                addedClient.out().writeInt(playerNumber);
                addedClient.out().flush();
                Log.info(GameServer.class, "client {} set to player {}", addedClient.getId(), playerNumber);

                idToPlayers.put(addedClient.getId(), playerNumber);
                players.put(playerNumber, addedClient);

                // add new player to other clients
                for (ServerClient serverClient : allClients.values()) {
                    if (serverClient == addedClient) {
                        continue;
                    }
                    Log.info("sending player {} to {}", playerNumber, serverClient);

                    serverClient.send(Networking.Client.AddPlayer, playerNumber);
                }

                // add other clients to new player
                for (Map.Entry<Integer, ServerClient> player : players.entrySet()) {
                    if (player.getValue() == addedClient) {
                        continue;
                    }

                    addedClient.send(Networking.Client.AddPlayer, player.getKey());
                }

            } catch (IOException exception) {
                FastJEngine.error("Server IO error", exception);
            }
        });

        server.addOnClientDisconnect((removedClient, otherClients) -> {
            try {
                int disconnectedPlayerNumber = idToPlayers.remove(removedClient.getId());
                players.remove(disconnectedPlayerNumber);

                for (ServerClient serverClient : otherClients.values()) {
                    serverClient.send(Networking.Client.RemovePlayer, disconnectedPlayerNumber);
                }
            } catch (IOException exception) {
                FastJEngine.error("Server IO error", exception);
            }
        });
    }

    @Override
    public void run() {
        server.run();
    }
}

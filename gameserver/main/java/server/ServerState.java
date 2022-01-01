package server;

import tech.fastj.logging.Log;

import tech.fastj.input.keyboard.Keys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import core.util.Networking;
import network.server.Server;
import network.server.ServerClient;

public class ServerState {

    private final Map<UUID, Integer> idToPlayers = new HashMap<>();
    private final Map<Integer, ServerClient> players = new HashMap<>();
    private final Map<Integer, AtomicBoolean> alivePlayers = new HashMap<>();
    private boolean isMatchRunning = false;
    private final Server server;
    private final GameServer gameServer;

    private int newPlayerIncrement = 1;

    public ServerState(Server server, GameServer gameServer) {
        this.server = server;
        this.gameServer = gameServer;
    }

    void syncAddPlayer(ServerClient addedClient, Map<UUID, ServerClient> allClients) {
        try {
            int playerNumber = newPlayerIncrement++;
            addedClient.out().writeInt(playerNumber);
            addedClient.out().flush();
            Log.info(this.getClass(), "client {} set to player {}", addedClient.getId(), playerNumber);

            idToPlayers.put(addedClient.getId(), playerNumber);
            players.put(playerNumber, addedClient);
            alivePlayers.put(playerNumber, new AtomicBoolean(true));

            // add new player to other clients
            for (ServerClient serverClient : allClients.values()) {
                if (serverClient == addedClient) {
                    continue;
                }
                Log.debug(this.getClass(), "sending player {} to {}", playerNumber, serverClient.getId());

                try {
                    serverClient.send(Networking.Client.AddPlayer, playerNumber);
                } catch (IOException exception) {
                    if (tryRemoveClosedClient(serverClient, addedClient)) {
                        Log.error(this.getClass(), "Server IO error", exception);
                    }
                    if (addedClient.isConnectionClosed() || !server.getClients().containsValue(addedClient)) {
                        return;
                    }
                }
            }

            // add other clients to new player
            for (Map.Entry<UUID, ServerClient> player : server.getClients().entrySet()) {
                if (player.getValue() == addedClient) {
                    continue;
                }
                Log.debug(this.getClass(), "sending player {} to {}", idToPlayers.get(player.getKey()), addedClient.getId());

                try {
                    addedClient.send(Networking.Client.AddPlayer, idToPlayers.get(player.getKey()));
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

        if (players.values().size() > 1) {
            isMatchRunning = true;
        }
    }

    void syncRemovePlayer(ServerClient removedClient, Map<UUID, ServerClient> otherClients) {
        int disconnectedPlayerNumber = idToPlayers.remove(removedClient.getId());
        players.remove(disconnectedPlayerNumber);
        alivePlayers.remove(disconnectedPlayerNumber);

        for (ServerClient serverClient : otherClients.values()) {
            Log.debug(this.getClass(), "removing player {} from {}", disconnectedPlayerNumber, serverClient.getId());
            try {
                serverClient.send(Networking.Client.RemovePlayer, disconnectedPlayerNumber);
            } catch (IOException exception) {
                Log.debug("failed to remove from {}: {}", serverClient.getId(), exception.getMessage());
                if (tryRemoveClosedClient(serverClient)) {
                    Log.error(this.getClass(), "Server IO error", exception);
                }
            }
        }

        if (players.values().size() < 2) {
            isMatchRunning = false;
        }

        if (players.values().size() == 0) {
            resetServerState();
        }
    }

    void handleKeyPress(ServerClient currentClient, Map<UUID, ServerClient> allClients) {
        String key = "";
        try {
            int player = currentClient.in().readInt();
            if (!players.containsKey(player)) {
                Log.warn("Player {} was not found.", player);
                server.removeClient(currentClient.getId());
                return;
            }
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
            if (!players.containsKey(player)) {
                Log.warn("Player {} was not found.", player);
                server.removeClient(currentClient.getId());
                return;
            }

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
                Log.warn("Player {} was not found.", syncPlayerNumber);
                server.removeClient(currentClient.getId());
                return;
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
                            Networking.Client.PlayerSyncTransform,
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

    public void createSnowball(ServerClient currentClient, Map<UUID, ServerClient> allClients) {
        try {
            int playerNumber = currentClient.in().readInt();
            if (!players.containsKey(playerNumber)) {
                Log.warn("Player {} was not found.", playerNumber);
                server.removeClient(currentClient.getId());
                return;
            }

            float trajectoryX = currentClient.in().readFloat();
            float trajectoryY = currentClient.in().readFloat();
            float rotation = currentClient.in().readFloat();

            for (ServerClient serverClient : allClients.values()) {
                if (serverClient.equals(currentClient)) {
                    continue;
                }

                try {
                    serverClient.send(
                            Networking.Client.PlayerCreateSnowball,
                            playerNumber,
                            trajectoryX,
                            trajectoryY,
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
            if (!server.getClients().containsValue(serverClient)) {
                serverClient.shutdown();
                removedClosedClient = true;
            } else if (serverClient.isConnectionClosed()) {
                server.removeClient(serverClient.getId());
                removedClosedClient = true;
            }
        }
        return !removedClosedClient;
    }

    public void temperatureDeath(ServerClient currentClient, Map<UUID, ServerClient> allClients) {
        try {
            int playerNumber = currentClient.in().readInt();
            if (!players.containsKey(playerNumber)) {
                Log.warn("Player {} was not found.", playerNumber);
                server.removeClient(currentClient.getId());
                return;
            }

            int otherPlayerNumber = currentClient.in().readInt();

            for (ServerClient serverClient : allClients.values()) {
                if (serverClient.equals(currentClient)) {
                    continue;
                }

                try {
                    serverClient.send(
                            Networking.Client.PlayerTemperatureDeath,
                            playerNumber,
                            otherPlayerNumber
                    );
                } catch (IOException exception) {
                    if (tryRemoveClosedClient(serverClient, currentClient)) {
                        Log.error(this.getClass(), "Server IO error", exception);
                    }
                }
            }

            updatePlayerDeath(playerNumber, allClients);
        } catch (IOException exception) {
            if (tryRemoveClosedClient(currentClient)) {
                Log.error(this.getClass(), "Server IO error", exception);
            }
        }
    }

    public void hitDamageDeath(ServerClient currentClient, Map<UUID, ServerClient> allClients) {
        try {
            int playerNumber = currentClient.in().readInt();
            if (!players.containsKey(playerNumber)) {
                Log.warn("Player {} was not found.", playerNumber);
                server.removeClient(currentClient.getId());
                return;
            }

            int otherPlayerNumber = currentClient.in().readInt();

            for (ServerClient serverClient : allClients.values()) {
                if (serverClient.equals(currentClient)) {
                    continue;
                }

                try {
                    serverClient.send(
                            Networking.Client.PlayerHitDamageDeath,
                            playerNumber,
                            otherPlayerNumber
                    );
                } catch (IOException exception) {
                    if (tryRemoveClosedClient(serverClient, currentClient)) {
                        Log.error(this.getClass(), "Server IO error", exception);
                    }
                }
            }

            updatePlayerDeath(playerNumber, allClients);
        } catch (IOException exception) {
            if (tryRemoveClosedClient(currentClient)) {
                Log.error(this.getClass(), "Server IO error", exception);
            }
        }
    }

    private void updatePlayerDeath(int deadPlayer, Map<UUID, ServerClient> allClients) {
        if (!alivePlayers.containsKey(deadPlayer)) {
            Log.warn("Player {} was not found.", deadPlayer);
            return;
        }
        alivePlayers.get(deadPlayer).set(false);
        Log.info("{} player(s) left alive.", alivePlayers.values().stream().filter(AtomicBoolean::get).count());
        checkWinCondition(allClients);
    }

    void checkWinCondition(Map<UUID, ServerClient> allClients) {
        if (!isMatchRunning) {
            return;
        }

        if (alivePlayers.values().stream().filter(AtomicBoolean::get).count() == 1) {
            int alivePlayer = alivePlayers.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().get())
                    .findFirst()
                    .get()
                    .getKey();
            Log.info("Only player {} is alive.", alivePlayer);
            server.disallowClients();
            isMatchRunning = false;

            for (ServerClient serverClient : allClients.values()) {
                try {
                    serverClient.send(Networking.Client.PlayerWins, alivePlayer);
                } catch (IOException exception) {
                    if (tryRemoveClosedClient(serverClient)) {
                        Log.error(this.getClass(), "Server IO error", exception);
                    }
                }
            }

            try {
                gameServer.resetServer(new AtomicReference<>(server));
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetServerState() {
        players.clear();
        idToPlayers.clear();
        alivePlayers.clear();
        newPlayerIncrement = 1;
    }
}

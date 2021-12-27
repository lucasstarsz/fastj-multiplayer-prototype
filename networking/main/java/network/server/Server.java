package network.server;

import tech.fastj.logging.Log;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class Server implements Runnable {

    public static final byte ClientLeave = -1;
    public static final byte ClientAccepted = 0;
    public static final String StopServer = "stop";

    private final ServerSocket server;
    private final PrintStream error = System.err;
    private final ExecutorService commandInterpreter = Executors.newSingleThreadExecutor();
    private final LinkedHashMap<UUID, ServerClient> clients = new LinkedHashMap<>(5, 1.0f, false);

    private final List<BiConsumer<ServerClient, Map<UUID, ServerClient>>> clientConnectActions = new ArrayList<>();
    private final List<BiConsumer<ServerClient, Map<UUID, ServerClient>>> clientDisconnectActions = new ArrayList<>();
    private final Map<Byte, BiConsumer<ServerClient, Map<UUID, ServerClient>>> clientDataActions = new HashMap<>() {{
        put(ClientLeave, (client, clients) -> {
            removeClient(client.getId());
            Log.debug(Server.class, "Disconnected {}", client.getId());
        });
    }};
    private final Map<String, BiConsumer<String, Map<UUID, ServerClient>>> serverCommandActions = new HashMap<>() {{
        put(StopServer, (command, clients) -> shutdown());
    }};

    private ExecutorService clientManager;
    private volatile boolean isRunning;

    public Server(int port) throws IOException {
        server = new ServerSocket(port);
    }

    public Server(int port, int backlogAmount) throws IOException {
        server = new ServerSocket(port, backlogAmount);
    }

    public Server(int port, int backlogAmount, InetAddress inetAddress) throws IOException {
        server = new ServerSocket(port, backlogAmount, inetAddress);
    }

    public Map<UUID, ServerClient> getClients() {
        return Collections.unmodifiableMap(clients);
    }

    public boolean addClientAction(byte identifier, BiConsumer<ServerClient, Map<UUID, ServerClient>> action) {
        if (identifier < 1) {
            throw new IllegalArgumentException("The identifier cannot be less than 1.");
        }

        if (clientDataActions.containsKey(identifier)) {
            return false;
        }

        clientDataActions.put(identifier, action);
        return true;
    }

    public BiConsumer<ServerClient, Map<UUID, ServerClient>> replaceClientAction(byte identifier, BiConsumer<ServerClient, Map<UUID, ServerClient>> action) {
        return clientDataActions.put(identifier, action);
    }

    public BiConsumer<ServerClient, Map<UUID, ServerClient>> removeClientAction(byte identifier) {
        return clientDataActions.remove(identifier);
    }

    public void addOnClientConnect(BiConsumer<ServerClient, Map<UUID, ServerClient>> action) {
        clientConnectActions.add(action);
    }

    public void addOnClientDisconnect(BiConsumer<ServerClient, Map<UUID, ServerClient>> action) {
        clientDisconnectActions.add(action);
    }

    public void shutdown() {
        if (!isRunning) {
            return;
        }

        Log.info(Server.class, "Stopping server...");

        for (ServerClient serverClient : clients.values()) {
            clients.remove(serverClient.getId(), serverClient);
            serverClient.disconnect(ClientLeave, "Server has stopped.");
        }
        try {
            server.close();
        } catch (IOException exception) {
            Log.error(Server.class, "Exception while closing server", exception);
        }
        clientManager.shutdownNow();
        commandInterpreter.shutdownNow();
        isRunning = false;

        Log.info(Server.class, "Server stopped.");
    }

    public void removeClient(UUID clientId) {
        ServerClient removedClient = clients.remove(clientId);
        if (removedClient == null) {
            Log.warn(Server.class, "Client with id {} was not found.", clientId);
            return;
        }

        removedClient.shutdown();
        for (BiConsumer<ServerClient, Map<UUID, ServerClient>> clientDisconnectAction : clientDisconnectActions) {
            clientDisconnectAction.accept(removedClient, getClients());
        }
    }

    void receive(UUID clientId, byte identifier, Throwable exception) {
        ServerClient client = clients.get(clientId);
        if (client == null) {
            Log.warn(Server.class, "Client with id {} was not found.", clientId);
            return;
        }

        if (exception != null) {
            Log.debug(Server.class, "Disconnected error-filled {}: {}", clientId, exception.getMessage());
            removeClient(clientId);
            return;
        }

        clientDataActions.getOrDefault(
                identifier,
                (currentClient, allClients) -> Log.warn(
                        Server.class,
                        "Invalid identifier {} from client {}",
                        identifier,
                        currentClient.getId()
                )
        ).accept(client, getClients());
    }

    @Override
    public void run() {
        if (isRunning) {
            Log.warn(this.getClass(), "Server already running.");
            return;
        }
        clientManager = Executors.newCachedThreadPool();
        isRunning = true;

        commandInterpreter.submit(this::interpretCommands);
        Log.info(this.getClass(), "Now accepting clients...");

        while (isRunning) {
            try {
                acceptClient();
            } catch (IOException exception) {
                if (!server.isClosed() || !isRunning) {
                    shutdown();
                }
            }
        }

        shutdown();
    }

    private void interpretCommands() {
        Scanner serverInput = new Scanner(System.in);
        Log.info("Now accepting commands...");

        while (isRunning) {
            String input = serverInput.nextLine();
            String[] commandTokens = input.split("\\s+");

            serverCommandActions.getOrDefault(
                    commandTokens[0],
                    (command, clients) -> error.printf("Invalid command: \"%s\"%n", command)
            ).accept(input, getClients());
        }
    }

    private synchronized void acceptClient() throws IOException {
        Socket client = server.accept();
        UUID clientID = UUID.randomUUID();
        ServerClient serverClient = new ServerClient(client, clientID);

        clients.put(clientID, serverClient);
        serverClient.out().writeByte(ClientAccepted);
        serverClient.out().flush();

        Log.debug("client {} connected.", clientID);
        clientManager.submit(() -> serverClient.listen(this));

        for (BiConsumer<ServerClient, Map<UUID, ServerClient>> clientConnectAction : clientConnectActions) {
            clientConnectAction.accept(serverClient, getClients());
        }
    }
}

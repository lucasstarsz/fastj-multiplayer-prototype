package network.server;

import tech.fastj.logging.Log;

import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
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

import network.security.SecureServerConfig;
import network.security.SecureServerSocketFactory;

public class Server implements Runnable {

    public static final byte ClientLeave = -1;
    public static final byte ClientAccepted = 0;
    public static final String StopServer = "stop";

    private final SSLServerSocket server;
    private final ExecutorService commandInterpreter = Executors.newSingleThreadExecutor();
    private final ExecutorService clientAccepter = Executors.newSingleThreadExecutor();

    public final ServerCommand shutdownCommand = new ServerCommand(StopServer, (command, clients) -> shutdown());
    public final ServerCommand invalidCommand = new ServerCommand("", (command, clients) -> System.err.printf("Invalid command: \"%s\"%n", command));
    public final ClientDataAction clientDisconnectAction = new ClientDataAction(ClientLeave, (client, clients) -> {
        removeClient(client.getId());
        Log.debug(this.getClass(), "Disconnected {}", client.getId());
    });

    private final LinkedHashMap<UUID, ServerClient> clients;
    private final List<BiConsumer<ServerClient, Map<UUID, ServerClient>>> clientConnectActions;
    private final List<BiConsumer<ServerClient, Map<UUID, ServerClient>>> clientDisconnectActions;
    private final Map<Byte, ClientDataAction> clientDataActions;
    private final Map<String, ServerCommand> serverCommandActions;

    private ExecutorService clientManager;
    private volatile boolean isRunning;
    private volatile boolean isAcceptingClients;

    public Server(ServerConfig serverConfig, SecureServerConfig secureServerConfig) throws IOException, GeneralSecurityException {
        server = SecureServerSocketFactory.getServerSocket(serverConfig, secureServerConfig);

        clients = new LinkedHashMap<>(5, 1.0f, false);
        clientConnectActions = new ArrayList<>();
        clientDisconnectActions = new ArrayList<>();

        clientDataActions = new HashMap<>();
        clientDataActions.put(ClientLeave, clientDisconnectAction);
        serverCommandActions = new HashMap<>();
        serverCommandActions.put(StopServer, shutdownCommand);
    }

    public Map<UUID, ServerClient> getClients() {
        return Collections.unmodifiableMap(clients);
    }

    public boolean addClientAction(ClientDataAction dataAction) {
        if (dataAction.identifier() < 1) {
            throw new IllegalArgumentException("The identifier cannot be less than 1.");
        }

        if (clientDataActions.containsKey(dataAction.identifier())) {
            return false;
        }

        clientDataActions.put(dataAction.identifier(), dataAction);
        return true;
    }

    public ClientDataAction replaceClientAction(ClientDataAction dataAction) {
        return clientDataActions.put(dataAction.identifier(), dataAction);
    }

    public ClientDataAction removeClientAction(byte identifier) {
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

        Log.info(this.getClass(), "Stopping server...");

        for (ServerClient serverClient : clients.values()) {
            clients.remove(serverClient.getId(), serverClient);
            serverClient.disconnect(ClientLeave, "Server has stopped.");
        }

        try {
            server.close();
        } catch (IOException exception) {
            Log.error(this.getClass(), "Exception while closing server", exception);
        }

        clientManager.shutdownNow();
        commandInterpreter.shutdownNow();
        clientAccepter.shutdownNow();
        isRunning = false;

        Log.info(this.getClass(), "Server stopped.");
    }

    public void removeClient(UUID clientId) {
        ServerClient removedClient = clients.remove(clientId);
        if (removedClient == null) {
            Log.warn(this.getClass(), "Client with id {} was not found.", clientId);
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
            Log.warn(this.getClass(), "Client with id {} was not found.", clientId);
            return;
        }

        if (exception != null) {
            Log.debug(this.getClass(), "Disconnected error-filled {}: {}", clientId, exception.getMessage());
            removeClient(clientId);
            return;
        }

        clientDataActions.getOrDefault(
                identifier,
                new ClientDataAction(identifier, (currentClient, allClients) -> Log.warn(
                        this.getClass(),
                        "Invalid identifier {} from client {}",
                        identifier,
                        currentClient.getId()
                ))
        ).dataAction().accept(client, getClients());
    }

    @Override
    public void run() {
        if (isRunning) {
            Log.warn(this.getClass(), "Server already running.");
            return;
        }
        isRunning = true;
        commandInterpreter.submit(this::interpretCommands);
    }

    public void allowClients() {
        if (isAcceptingClients) {
            Log.warn(this.getClass(), "Server already accepting clients.");
            return;
        }

        clientAccepter.submit(this::acceptClients);
    }

    private void interpretCommands() {
        Scanner serverInput = new Scanner(System.in);
        Log.info(this.getClass(), "Now accepting commands...");

        while (isRunning) {
            String input = serverInput.nextLine();
            String[] commandTokens = input.split("\\s+");

            serverCommandActions.getOrDefault(commandTokens[0], invalidCommand)
                    .commandAction()
                    .accept(input, getClients());
        }
    }

    private void acceptClients() {
        isAcceptingClients = true;
        Log.info(this.getClass(), "Now accepting clients...");

        if (clientManager != null) {
            clientManager.shutdownNow();
        }
        clientManager = Executors.newCachedThreadPool();

        while (isAcceptingClients) {
            try {
                acceptClient();
            } catch (IOException exception) {
                if (!isRunning || !isAcceptingClients) {
                    break;
                }
            }
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

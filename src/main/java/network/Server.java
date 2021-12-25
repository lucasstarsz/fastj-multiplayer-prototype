package network;

import tech.fastj.logging.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class Server implements Runnable {

    public static final byte ClientLeave = -1;
    public static final byte ClientAccepted = 0;
    public static final byte ClientRemoved = -2;
    public static final String StopServer = "stop";
    private final PrintStream error = System.err;

    private final ExecutorService commandInterpreter = Executors.newSingleThreadExecutor();
    private final LinkedHashMap<UUID, ServerClient> clients = new LinkedHashMap<>(5, 1.0f, false);
    private final Map<Byte, BiConsumer<DataInputStream, ServerClient>> clientDataActions = new HashMap<>() {{
        put(ClientLeave, (dataStream, client) -> {
            clients.remove(client.getId(), client);
            client.disconnect(ClientLeave, "Disconnected from server.");
        });
    }};
    private final Map<String, BiConsumer<String, Map<UUID, ServerClient>>> serverCommandActions = new HashMap<>() {{
        put(StopServer, (command, clients) -> shutdown());
    }};

    private ExecutorService clientManager;
    private final ServerSocket server;
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

    public boolean addClientAction(byte identifier, BiConsumer<DataInputStream, ServerClient> action) {
        if (identifier < 1) {
            throw new IllegalArgumentException("The identifier cannot be less than 1.");
        }

        if (clientDataActions.containsKey(identifier)) {
            return false;
        }

        clientDataActions.put(identifier, action);
        return true;
    }

    public BiConsumer<DataInputStream, ServerClient> replaceClientAction(byte identifier, BiConsumer<DataInputStream, ServerClient> action) {
        return clientDataActions.put(identifier, action);
    }

    public BiConsumer<DataInputStream, ServerClient> removeClientAction(byte identifier) {
        return clientDataActions.remove(identifier);
    }

    public void receive(UUID clientId, byte identifier, Throwable exception) {
        ServerClient client = clients.get(clientId);
        if (client == null) {
            Log.warn(Server.class, "Client with id {} was not found.", clientId);
            return;
        }

        if (exception != null) {
            clients.remove(clientId, client);
            client.disconnect(ClientRemoved, exception.toString());
            return;
        }

        clientDataActions.getOrDefault(
                identifier,
                (dataInputStream, serverClient) -> Log.warn(
                        Server.class,
                        "Invalid identifier {} from client {}",
                        identifier,
                        client
                )
        ).accept(client.in(), client);
    }

    @Override
    public synchronized void run() {
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
                acceptClients();
            } catch (IOException e) {
                e.printStackTrace();
                shutdown();
            }
        }

        shutdown();
    }

    private void interpretCommands() {
        Scanner serverInput = new Scanner(System.in);
        while (isRunning) {
            String input = serverInput.nextLine().trim();
            String[] commandTokens = input.split("\\s+");

            serverCommandActions.getOrDefault(
                    commandTokens[0],
                    (command, clients) -> error.printf("Invalid command: \"%s\"", command)
            ).accept(input, getClients());
        }
    }

    private synchronized void acceptClients() throws IOException {
        Socket client = server.accept();
        UUID clientID = UUID.randomUUID();
        ServerClient serverClient = new ServerClient(client, clientID);

        clients.put(clientID, serverClient);
        serverClient.out().writeByte(ClientAccepted);
        clientManager.submit(() -> serverClient.listen(this));

        Log.debug("received client {}", clientID);
    }

    public synchronized void shutdown() {
        if (!isRunning) {
            return;
        }

        Log.info(Server.class, "Stopping server...");

        for (ServerClient serverClient : clients.values()) {
            clients.remove(serverClient.getId(), serverClient);
            serverClient.disconnect(ClientLeave, "Server has stopped.");
        }
        clientManager.shutdownNow();

        Log.info(Server.class, "Server stopped.");
    }

    public Map<UUID, ServerClient> getClients() {
        return Collections.unmodifiableMap(clients);
    }
}

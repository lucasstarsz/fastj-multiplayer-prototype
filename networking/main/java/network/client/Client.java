package network.client;

import tech.fastj.logging.Log;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import network.security.SecureServerConfig;
import network.security.SecureSocketFactory;
import network.server.Server;

public class Client implements Runnable {

    private final SSLSocket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private ExecutorService serverListener = Executors.newSingleThreadExecutor();
    private boolean isRunning;
    private final Map<Byte, Consumer<Client>> serverDataActions = new HashMap<>() {{
        put(Server.ClientAccepted, client -> {});
    }};

    public Client(ClientConfig clientConfig, SecureServerConfig secureServerConfig) throws IOException, GeneralSecurityException {
        socket = SecureSocketFactory.getSocket(clientConfig, secureServerConfig);
        socket.startHandshake();

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        out.flush();
        byte connectionStatus = in.readByte();
        if (connectionStatus != Server.ClientAccepted) {
            socket.close();
            throw new IOException("Bad connection status: " + connectionStatus);
        }
    }

    public boolean addServerAction(byte identifier, Consumer<Client> action) {
        if (identifier < 1) {
            throw new IllegalArgumentException("The identifier cannot be less than 1.");
        }

        if (serverDataActions.containsKey(identifier)) {
            return false;
        }

        serverDataActions.put(identifier, action);
        return true;
    }

    public Consumer<Client> replaceClientAction(byte identifier, Consumer<Client> action) {
        return serverDataActions.put(identifier, action);
    }

    public Consumer<Client> removeClientAction(byte identifier) {
        return serverDataActions.remove(identifier);
    }

    public void send(byte identifier, Object... data) throws IOException {
        synchronized (out) {
            out.writeByte(identifier);

            for (Object value : data) {
                if (value instanceof String s) {
                    out.writeUTF(s);
                } else if (value instanceof Integer i) {
                    out.writeInt(i);
                } else if (value instanceof Double d) {
                    out.writeDouble(d);
                } else if (value instanceof Long l) {
                    out.writeLong(l);
                } else if (value instanceof Float f) {
                    out.writeFloat(f);
                } else if (value instanceof Byte b) {
                    out.writeByte(b);
                } else if (value instanceof Boolean b) {
                    out.writeBoolean(b);
                } else if (value instanceof Character c) {
                    out.writeChar(c);
                } else if (value instanceof Short s) {
                    out.writeShort(s);
                } else if (value instanceof byte[] bytes) {
                    out.write(bytes);
                } else {
                    out.writeObject(value);
                }
            }

            out.flush();
        }
    }

    public void disconnect(byte identifier, String reason) {
        try {
            out.writeByte(identifier);
            out.writeUTF(reason);
            out.flush();
            serverListener.shutdownNow();
            shutdown();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            out.flush();
            socket.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (isRunning) {
            Log.warn(this.getClass(), "Client already listening to a server.");
            return;
        }
        serverListener = Executors.newSingleThreadExecutor();
        isRunning = true;

        serverListener.submit(this::listen);
    }

    synchronized void listen() {
        while (!socket.isClosed()) {
            try {
                byte identifier = in.readByte();
                serverDataActions.getOrDefault(
                        identifier,
                        client -> Log.warn("Invalid server identifier: {}", identifier)
                ).accept(this);
            } catch (IOException exception) {
                try {
                    if (in.read() == -1) {
                        Log.info("client closed.");
                        break;
                    }
                } catch (IOException exception1) {
                    Log.error("", exception);
                    Log.error("", exception1);
                }
                shutdown();
                break;
            }
        }
    }

    public ObjectInputStream in() {
        return in;
    }

    public ObjectOutputStream out() {
        return out;
    }
}

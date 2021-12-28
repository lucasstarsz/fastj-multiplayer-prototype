package network.server;

import tech.fastj.logging.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

public class ServerClient {

    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final UUID uuid;

    public ServerClient(Socket socketClient, UUID id) throws IOException {
        socket = socketClient;
        uuid = id;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public UUID getId() {
        return uuid;
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
            shutdown();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void shutdown() {
        if (socket.isClosed()) {
            Log.warn("client {} already closed.", uuid);
            return;
        }

        try {
            out.flush();
            socket.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    synchronized void listen(Server server) {
        while (!socket.isClosed()) {
            try {
                byte identifier = in.readByte();
                server.receive(uuid, identifier, null);
            } catch (IOException exception) {
                try {
                    if (in.read() == -1) {
                        server.removeClient(uuid);
                        Log.info(ServerClient.class, "client {} closed.", uuid);
                        break;
                    }
                } catch (IOException exception1) {
                    server.receive(uuid, Server.ClientLeave, exception);
                    server.removeClient(uuid);
                }
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

package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class ServerClient {

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final UUID uuid;

    public ServerClient(Socket socketClient, UUID id) throws IOException {
        socket = socketClient;
        uuid = id;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    public UUID getId() {
        return uuid;
    }

    public void disconnect(byte identifier, String reason) {
        try {
            out.writeByte(identifier);
            out.writeUTF(reason);
            socket.shutdownOutput();
            socket.shutdownInput();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized void listen(Server server) {
        while (true) {
            try {
                byte response = in.readByte();
                server.receive(uuid, response, null);
            } catch (IOException exception) {
                server.receive(uuid, Server.ClientLeave, exception);
                break;
            }
        }
    }

    DataInputStream in() {
        return in;
    }

    DataOutputStream out() {
        return out;
    }
}

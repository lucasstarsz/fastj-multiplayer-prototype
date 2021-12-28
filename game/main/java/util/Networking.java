package util;

public class Networking {
    public static final int Port = 49999;

    public static class Server {
        public static final byte KeyPress = 1;
        public static final byte KeyRelease = 2;
        public static final byte SyncTransform = 3;
    }

    public static class ServerCommands {
        public static final String ToggleClientConnect = "clientconnect";
    }

    public static class Client {
        public static final byte PlayerKeyPress = 1;
        public static final byte PlayerKeyRelease = 2;
        public static final byte AddPlayer = 3;
        public static final byte RemovePlayer = 4;
        public static final byte SyncPlayerTransform = 5;
    }
}

package core.util;

public class Networking {
    public static final int Port = 49999;

    public static class Server {
        public static final byte KeyPress = 1;
        public static final byte KeyRelease = 2;
        public static final byte SyncTransform = 3;
        public static final byte CreateSnowball = 4;
    }

    public static class ServerCommands {
        public static final String ToggleClientConnect = "cc";
    }

    public static class Client {
        public static final byte AddPlayer = 1;
        public static final byte RemovePlayer = 2;
        public static final byte PlayerKeyPress = 3;
        public static final byte PlayerKeyRelease = 4;
        public static final byte PlayerSyncTransform = 5;
        public static final byte PlayerCreateSnowball = 6;
    }
}

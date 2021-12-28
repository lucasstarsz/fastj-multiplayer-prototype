package network.server;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public record ClientDataAction(byte identifier, BiConsumer<ServerClient, Map<UUID, ServerClient>> dataAction) {
}

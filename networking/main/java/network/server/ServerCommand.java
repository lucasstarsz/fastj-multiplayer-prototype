package network.server;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public record ServerCommand(String keyword, BiConsumer<String, Map<UUID, ServerClient>> commandAction) {
}

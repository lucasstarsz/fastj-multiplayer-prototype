package game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.engine.config.ExceptionAction;
import tech.fastj.logging.LogLevel;
import tech.fastj.graphics.dialog.DialogConfig;
import tech.fastj.graphics.dialog.DialogUtil;

import javax.swing.SwingUtilities;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ClientMain {
    public static void main(String[] args) {
        try {
            FastJEngine.init("Game", new GameManager());
            FastJEngine.configureLogging(LogLevel.Debug);
            FastJEngine.configureExceptionAction(ExceptionAction.Throw);
            FastJEngine.run();
        } catch (Exception exception) {
            if (FastJEngine.isRunning()) {
                FastJEngine.forceCloseGame();
            }

            SwingUtilities.invokeLater(() -> {
                DialogConfig exceptionConfig = DialogConfig.create()
                        .withParentComponent(null)
                        .withTitle(exception.getClass().getName())
                        .withPrompt(
                                exception.getClass().getName() + ": " + exception.getMessage() + System.lineSeparator() +
                                        Arrays.stream(exception.getStackTrace())
                                                .map(stackTraceElement -> "at " + stackTraceElement.toString() + "\n")
                                                .collect(Collectors.toList())
                                                .toString()
                                                .replaceFirst("\\[", "")
                                                .replaceAll("](.*)\\[", "")
                                                .replaceAll("(, )?at", "    at")
                                                .replace("]", "")
                        )
                        .build();
                DialogUtil.showMessageDialog(exceptionConfig);
                System.exit(0);
            });
        }
    }
}

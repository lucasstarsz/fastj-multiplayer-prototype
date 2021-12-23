package game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.engine.config.ExceptionAction;
import tech.fastj.graphics.dialog.DialogConfig;
import tech.fastj.graphics.dialog.DialogUtil;

import javax.swing.SwingUtilities;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        try {
            FastJEngine.init("", new GameManager());
            FastJEngine.configureExceptionAction(ExceptionAction.Throw);
            FastJEngine.run();
            throw new IllegalStateException("yeeeeeeeeet");
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
                                                .replace("]", "")
                        )
                        .build();
                DialogUtil.showMessageDialog(exceptionConfig);
                System.exit(0);
            });
        }
    }
}

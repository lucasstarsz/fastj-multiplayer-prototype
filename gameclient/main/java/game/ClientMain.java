package game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.engine.config.ExceptionAction;
import tech.fastj.logging.LogLevel;
import tech.fastj.graphics.dialog.DialogConfig;
import tech.fastj.graphics.dialog.DialogUtil;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.stream.Collectors;

import util.Fonts;

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
                displayException("Error while running FastJ", exception);
                System.exit(0);
            });
        }
    }

    public static void displayException(String message, Exception exception) {
        StringBuilder formattedException = new StringBuilder(exception.getClass().getName() + ": " + exception.getMessage());
        Throwable currentException = exception;
        do {
            formattedException.append(System.lineSeparator())
                    .append("Caused by: ")
                    .append(currentException.getClass().getName())
                    .append(": ")
                    .append(currentException.getMessage())
                    .append(System.lineSeparator())
                    .append(formatStackTrace(currentException));
        } while ((currentException = currentException.getCause()) != null);

        JTextArea textArea = new JTextArea(formattedException.toString());
        textArea.setBackground(new Color(238, 238, 238));
        textArea.setEditable(false);
        textArea.setFont(Fonts.notoSans(Font.BOLD, 13));

        DialogConfig exceptionConfig = DialogConfig.create()
                .withParentComponent(null)
                .withTitle(exception.getClass().getName() + ": " + message)
                .withPrompt(textArea)
                .build();
        DialogUtil.showMessageDialog(exceptionConfig);
    }

    private static String formatStackTrace(Throwable exception) {
        return Arrays.stream(exception.getStackTrace())
                .map(stackTraceElement -> "at " + stackTraceElement.toString() + "\n")
                .collect(Collectors.toList())
                .toString()
                .replaceFirst("\\[", "")
                .replaceAll("](.*)\\[", "")
                .replaceAll("(, )?at", "    at")
                .replace("]", "");
    }
}

package game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.engine.config.ExceptionAction;
import tech.fastj.logging.LogLevel;
import tech.fastj.graphics.dialog.DialogConfig;
import tech.fastj.graphics.dialog.DialogMessageTypes;
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
            FastJEngine.configureExceptionAction(ExceptionAction.LogError);
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
        textArea.setFont(Fonts.notoSansMono(Font.BOLD, 13));

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
                .replaceAll("(, )?at ", "    at ")
                .replace("]", "")
                .trim();
    }

    public static boolean chooseExit() {
        String[] options = {
                "Yes",
                "No",
                "Cancel"
        };
        int chosenOption = DialogUtil.showOptionDialog(
                DialogConfig.create().withTitle("Exit?")
                        .withPrompt(
                                "You closed out the hostname-selection screen. Are you sure you want to exit?" +
                                        "\nClick \"Yes\" or the X button to exit." +
                                        "\nClick \"No\" or \"Cancel\" to return to the hostname-selection screen."
                        )
                        .build(),
                DialogMessageTypes.Warning,
                options,
                options[2]
        );

        return switch (chosenOption) {
            case 0, -1 -> true;
            default -> false;
        };
    }
}

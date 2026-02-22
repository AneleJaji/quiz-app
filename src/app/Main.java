package app;

import client.LoginGUI;
import server.QuizServer;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            String mode = args[0].trim().toLowerCase();
            if (mode.equals("server")) {
                QuizServer.main(new String[0]);
                return;
            }
            if (mode.equals("client")) {
                SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
                return;
            }
        }

        // Default: open client GUI if no args were provided
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}

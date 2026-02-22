package client;

import common.Protocol;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;

public class RegisterGUI extends JFrame {
    private final ServerConnection connection;
    private final JFrame parent;

    private JTextField fullNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField classField;
    private JComboBox<String> roleComboBox;

    public RegisterGUI(ServerConnection connection, JFrame parent) {
        this.connection = connection;
        this.parent = parent;

        setTitle("Quiz System - Register");
        setSize(450, 420);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel titleLabel = new JLabel("Create an Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(fieldPanel("Full Name:", fullNameField = new JTextField()));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        mainPanel.add(fieldPanel("Username:", usernameField = new JTextField()));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        mainPanel.add(fieldPanel("Password:", passwordField = new JPasswordField()));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        mainPanel.add(fieldPanel("Confirm Password:", confirmPasswordField = new JPasswordField()));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        mainPanel.add(fieldPanel("Email (optional):", emailField = new JTextField()));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        mainPanel.add(fieldPanel("Class/Grade (optional):", classField = new JTextField()));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel rolePanel = new JPanel(new BorderLayout());
        rolePanel.add(new JLabel("Role:"), BorderLayout.NORTH);
        roleComboBox = new JComboBox<>(new String[]{"STUDENT", "TEACHER"});
        rolePanel.add(roleComboBox, BorderLayout.CENTER);
        mainPanel.add(rolePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel buttonPanel = new JPanel();
        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");

        registerButton.addActionListener(e -> handleRegister());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel);

        add(mainPanel);
    }

    private JPanel fieldPanel(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(label), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();
        String email = emailField.getText().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.isEmpty() && !email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String message = Protocol.REGISTER + Protocol.DELIMITER + username +
                Protocol.DELIMITER + password + Protocol.DELIMITER +
                fullName + Protocol.DELIMITER + role;
            connection.sendMessage(message);

            String response = connection.receiveMessage();
            if (response == null) {
                JOptionPane.showMessageDialog(this, "No response from server",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] parts = response.split(java.util.regex.Pattern.quote(Protocol.DELIMITER));

            if (parts[0].equals(Protocol.REGISTER_SUCCESS)) {
                JOptionPane.showMessageDialog(this, "Registration successful! Please login.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                if (parent != null) {
                    parent.requestFocus();
                }
            } else {
                String errorText = parts.length > 1 ? parts[1] : ("Registration failed: " + response);
                JOptionPane.showMessageDialog(this, errorText,
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Communication error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

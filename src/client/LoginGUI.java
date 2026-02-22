package client;

import common.Protocol;
import model.User;

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
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;

public class LoginGUI extends JFrame {
	private final ServerConnection connection;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JComboBox<String> roleComboBox;
	private JButton loginButton;
	private JButton registerButton;

	public LoginGUI() {
		connection = new ServerConnection();

		setTitle("Quiz System - Login");
		setSize(400, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		initComponents();

		if (!connection.connect()) {
			JOptionPane.showMessageDialog(this,
				"Cannot connect to server. Please make sure the server is running.",
				"Connection Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void initComponents() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

		JLabel titleLabel = new JLabel("Quiz System Login");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(titleLabel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		JPanel usernamePanel = new JPanel(new BorderLayout());
		usernamePanel.add(new JLabel("Username:"), BorderLayout.NORTH);
		usernameField = new JTextField();
		usernamePanel.add(usernameField, BorderLayout.CENTER);
		mainPanel.add(usernamePanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		JPanel passwordPanel = new JPanel(new BorderLayout());
		passwordPanel.add(new JLabel("Password:"), BorderLayout.NORTH);
		passwordField = new JPasswordField();
		passwordPanel.add(passwordField, BorderLayout.CENTER);
		mainPanel.add(passwordPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		JPanel rolePanel = new JPanel(new BorderLayout());
		rolePanel.add(new JLabel("Role:"), BorderLayout.NORTH);
		roleComboBox = new JComboBox<>(new String[]{"STUDENT", "TEACHER"});
		rolePanel.add(roleComboBox, BorderLayout.CENTER);
		mainPanel.add(rolePanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		JPanel buttonPanel = new JPanel();
		loginButton = new JButton("Login");
		registerButton = new JButton("Register");

		loginButton.addActionListener(e -> handleLogin());
		registerButton.addActionListener(e -> handleRegister());

		buttonPanel.add(loginButton);
		buttonPanel.add(registerButton);
		mainPanel.add(buttonPanel);

		add(mainPanel);
	}

	private void handleLogin() {
		String username = usernameField.getText().trim();
		String password = new String(passwordField.getPassword());
		String selectedRole = (String) roleComboBox.getSelectedItem();

		if (username.isEmpty() || password.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please fill in all fields",
				"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			String message = Protocol.LOGIN + Protocol.DELIMITER + username + Protocol.DELIMITER + password;
			connection.sendMessage(message);

			String response = connection.receiveMessage();
			if (response == null) {
				JOptionPane.showMessageDialog(this, "No response from server",
					"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String[] parts = response.split(java.util.regex.Pattern.quote(Protocol.DELIMITER));

			if (parts[0].equals(Protocol.LOGIN_SUCCESS)) {
				int userId = Integer.parseInt(parts[1]);
				String fullName = parts[2];
				String role = parts[3];

				// Validate that selected role matches user's actual role
				if (!selectedRole.equals(role)) {
					JOptionPane.showMessageDialog(this, 
						"You cannot login as " + selectedRole + ". Your account is registered as " + role + ".",
						"Role Mismatch", JOptionPane.ERROR_MESSAGE);
					return;
				}

				User user = new User(userId, username, fullName, role);

				if (user.isTeacher()) {
					new TeacherDashboard(connection, user).setVisible(true);
				} else {
					new StudentDashboard(connection, user).setVisible(true);
				}

				this.dispose();
			} else {
				JOptionPane.showMessageDialog(this, parts.length > 1 ? parts[1] : "Login failed",
					"Login Failed", JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Communication error: " + e.getMessage(),
				"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void handleRegister() {
		RegisterGUI registerGUI = new RegisterGUI(connection, this);
		registerGUI.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
	}
}

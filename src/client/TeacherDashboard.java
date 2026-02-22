package client;

import common.Protocol;
import model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TeacherDashboard extends JFrame {
	private final ServerConnection connection;
	private final User currentUser;
	private JTable quizTable;
	private DefaultTableModel tableModel;
	private JButton createQuizButton;
	private JButton deleteQuizButton;
	private JButton viewLeaderboardButton;
	private JButton refreshButton;
	private final List<Integer> quizIds;

	public TeacherDashboard(ServerConnection connection, User user) {
		this.connection = connection;
		this.currentUser = user;
		this.quizIds = new ArrayList<>();

		setTitle("Teacher Dashboard - " + user.getFullName());
		setSize(900, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		initComponents();
		// Load quizzes in background
		new Thread(this::loadQuizzes).start();
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		topPanel.setBackground(new Color(70, 130, 180));

		JLabel welcomeLabel = new JLabel("Teacher Dashboard - " + currentUser.getFullName());
		welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
		welcomeLabel.setForeground(Color.WHITE);
		topPanel.add(welcomeLabel, BorderLayout.WEST);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setOpaque(false);
		
		refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(e -> loadQuizzes());
		buttonPanel.add(refreshButton);

		JButton logoutButton = new JButton("Logout");
		logoutButton.addActionListener(e -> handleLogout());
		buttonPanel.add(logoutButton);

		topPanel.add(buttonPanel, BorderLayout.EAST);

		add(topPanel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel titleLabel = new JLabel("All Quizzes");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		centerPanel.add(titleLabel, BorderLayout.NORTH);

		String[] columnNames = {"Quiz ID", "Quiz Name", "Teacher", "Questions", "Time Limit", "Status"};
		tableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		quizTable = new JTable(tableModel);
		quizTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		quizTable.getTableHeader().setReorderingAllowed(false);

		JScrollPane scrollPane = new JScrollPane(quizTable);
		centerPanel.add(scrollPane, BorderLayout.CENTER);

		add(centerPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new FlowLayout());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		createQuizButton = new JButton("Create New Quiz");
		deleteQuizButton = new JButton("Delete Quiz");
		viewLeaderboardButton = new JButton("View Leaderboard");

		createQuizButton.addActionListener(e -> handleCreateQuiz());
		deleteQuizButton.addActionListener(e -> handleDeleteQuiz());
		viewLeaderboardButton.addActionListener(e -> handleViewLeaderboard());

		createQuizButton.setBackground(new Color(34, 139, 34));
		createQuizButton.setForeground(Color.WHITE);
		deleteQuizButton.setBackground(new Color(220, 20, 60));
		deleteQuizButton.setForeground(Color.WHITE);

		bottomPanel.add(createQuizButton);
		bottomPanel.add(deleteQuizButton);
		bottomPanel.add(viewLeaderboardButton);

		add(bottomPanel, BorderLayout.SOUTH);
	}

	public void loadQuizzes() {
		try {
			connection.sendMessage(Protocol.GET_ACTIVE_QUIZZES);
			String response = connection.receiveMessage();

			if (response == null) return;

			String[] parts = response.split(java.util.regex.Pattern.quote(Protocol.DELIMITER));

			if (parts[0].equals(Protocol.QUIZ_LIST)) {
				javax.swing.SwingUtilities.invokeLater(() -> {
					tableModel.setRowCount(0);
					quizIds.clear();

					for (int i = 1; i < parts.length; i++) {
						String[] quizData = parts[i].split(java.util.regex.Pattern.quote(Protocol.SUB_DELIMITER));
						if (quizData.length >= 5) {
							int quizId = Integer.parseInt(quizData[0]);
							String quizName = quizData[1];
							String teacherName = quizData[2];
							int totalQuestions = Integer.parseInt(quizData[3]);
							int timeLimit = Integer.parseInt(quizData[4]);

							quizIds.add(quizId);
							tableModel.addRow(new Object[]{
								quizId, quizName, teacherName, totalQuestions,
								timeLimit + "s", "Active"
							});
						}
					}

					if (tableModel.getRowCount() == 0) {
						JLabel noQuizLabel = new JLabel("No quizzes available. Create one to get started!");
						noQuizLabel.setFont(new Font("Arial", Font.ITALIC, 14));
						noQuizLabel.setHorizontalAlignment(SwingConstants.CENTER);
					}
				});
			}
		} catch (IOException e) {
			javax.swing.SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(this, "Error loading quizzes: " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
			});
		}
	}

	private void handleCreateQuiz() {
		new CreateQuizGUI(connection, currentUser, this).setVisible(true);
	}

	private void handleDeleteQuiz() {
		int selectedRow = quizTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a quiz to delete",
				"No Selection", JOptionPane.WARNING_MESSAGE);
			return;
		}

		int quizId = quizIds.get(selectedRow);
		String quizName = (String) tableModel.getValueAt(selectedRow, 1);

		int confirm = JOptionPane.showConfirmDialog(this,
			"Are you sure you want to delete quiz: " + quizName + "?\n" +
				"This will also delete all associated quiz attempts!",
			"Confirm Deletion",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE);

		if (confirm == JOptionPane.YES_OPTION) {
			try {
				connection.sendMessage(Protocol.DELETE_QUIZ + Protocol.DELIMITER + quizId);
				String response = connection.receiveMessage();

				String[] parts = response.split(java.util.regex.Pattern.quote(Protocol.DELIMITER));

				if (parts[0].equals(Protocol.QUIZ_DELETED)) {
					JOptionPane.showMessageDialog(this, "Quiz deleted successfully!",
						"Success", JOptionPane.INFORMATION_MESSAGE);
					loadQuizzes();
				} else {
					JOptionPane.showMessageDialog(this, "Failed to delete quiz",
						"Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Error deleting quiz: " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void handleViewLeaderboard() {
		int selectedRow = quizTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a quiz to view leaderboard",
				"No Selection", JOptionPane.WARNING_MESSAGE);
			return;
		}

		int quizId = quizIds.get(selectedRow);
		String quizName = (String) tableModel.getValueAt(selectedRow, 1);

		new TeacherLeaderboardGUI(connection, quizId, quizName).setVisible(true);
	}

	private void handleLogout() {
		int confirm = JOptionPane.showConfirmDialog(this,
			"Are you sure you want to logout?",
			"Confirm Logout",
			JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			this.dispose();
			new LoginGUI().setVisible(true);
		}
	}
}

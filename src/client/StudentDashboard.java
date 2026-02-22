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
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StudentDashboard extends JFrame {
	private final ServerConnection connection;
	private final User currentUser;
	private JTable quizTable;
	private DefaultTableModel tableModel;
	private JButton takeQuizButton;
	private JButton viewResultsButton;
	private JButton refreshButton;
	private final List<Integer> quizIds;

	public StudentDashboard(ServerConnection connection, User user) {
		this.connection = connection;
		this.currentUser = user;
		this.quizIds = new ArrayList<>();

		setTitle("Student Dashboard - " + user.getFullName());
		setSize(800, 600);
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

		JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName());
		welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
		topPanel.add(welcomeLabel, BorderLayout.WEST);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		
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

		JLabel titleLabel = new JLabel("Available Quizzes");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		centerPanel.add(titleLabel, BorderLayout.NORTH);

		String[] columnNames = {"Quiz Name", "Teacher", "Questions", "Time per Question"};
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

		takeQuizButton = new JButton("Take Quiz");
		viewResultsButton = new JButton("View My Results");

		// Disable "Take Quiz" button if user is a teacher
		takeQuizButton.setEnabled(!currentUser.isTeacher());

		takeQuizButton.addActionListener(e -> handleTakeQuiz());
		viewResultsButton.addActionListener(e -> handleViewResults());

		bottomPanel.add(takeQuizButton);
		bottomPanel.add(viewResultsButton);

		add(bottomPanel, BorderLayout.SOUTH);
	}

	private void loadQuizzes() {
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
								quizName, teacherName, totalQuestions, timeLimit + "s"
							});
						}
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

	private void handleTakeQuiz() {
		// Validate that only students can take quizzes
		if (currentUser.isTeacher()) {
			JOptionPane.showMessageDialog(this,
				"Teachers cannot take quizzes. Please use the Teacher Dashboard to manage quizzes.",
				"Access Denied", JOptionPane.ERROR_MESSAGE);
			return;
		}

		int selectedRow = quizTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a quiz",
				"No Selection", JOptionPane.WARNING_MESSAGE);
			return;
		}

		int quizId = quizIds.get(selectedRow);
		String quizName = (String) tableModel.getValueAt(selectedRow, 0);

		int confirm = JOptionPane.showConfirmDialog(this,
			"Start quiz: " + quizName + "?",
			"Confirm",
			JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			new QuizTakingGUI(connection, currentUser, quizId).setVisible(true);
			this.dispose();
		}
	}

	private void handleViewResults() {
		new StudentResultsGUI(connection, currentUser).setVisible(true);
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

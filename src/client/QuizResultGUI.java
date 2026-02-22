package client;

import common.Protocol;
import model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;

public class QuizResultGUI extends JFrame {
	private final ServerConnection connection;
	private final User currentUser;
	private final int score;
	private final int totalQuestions;
	private final double percentage;
	private final long timeTaken;
	private final int quizId;
	private final String quizName;
	private DefaultTableModel leaderboardModel;

	public QuizResultGUI(ServerConnection connection, User user, int score,
						 int totalQuestions, double percentage, long timeTaken,
						 int quizId, String quizName) {
		this.connection = connection;
		this.currentUser = user;
		this.score = score;
		this.totalQuestions = totalQuestions;
		this.percentage = percentage;
		this.timeTaken = timeTaken;
		this.quizId = quizId;
		this.quizName = quizName;

		setTitle("Quiz Results");
		setSize(700, 600);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

		initComponents();
		loadLeaderboard();
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		topPanel.setBackground(new Color(240, 248, 255));

		JLabel titleLabel = new JLabel(quizName);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setAlignmentX(CENTER_ALIGNMENT);
		topPanel.add(titleLabel);
		topPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		JLabel resultLabel = new JLabel("Your Score: " + score + "/" + totalQuestions);
		resultLabel.setFont(new Font("Arial", Font.BOLD, 20));
		resultLabel.setAlignmentX(CENTER_ALIGNMENT);
		topPanel.add(resultLabel);

		JLabel percentLabel = new JLabel(String.format("Percentage: %.2f%%", percentage));
		percentLabel.setFont(new Font("Arial", Font.PLAIN, 18));
		percentLabel.setAlignmentX(CENTER_ALIGNMENT);
		topPanel.add(percentLabel);

		JLabel timeLabel = new JLabel("Time Taken: " + formatTime(timeTaken));
		timeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		timeLabel.setAlignmentX(CENTER_ALIGNMENT);
		topPanel.add(timeLabel);

		String grade = getGrade(percentage);
		JLabel gradeLabel = new JLabel("Grade: " + grade);
		gradeLabel.setFont(new Font("Arial", Font.BOLD, 20));
		gradeLabel.setAlignmentX(CENTER_ALIGNMENT);
		if (percentage >= 90) {
			gradeLabel.setForeground(new Color(0, 150, 0));
		} else if (percentage >= 60) {
			gradeLabel.setForeground(Color.BLUE);
		} else {
			gradeLabel.setForeground(Color.RED);
		}
		topPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		topPanel.add(gradeLabel);

		add(topPanel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

		JLabel leaderboardLabel = new JLabel("Leaderboard - Top 10");
		leaderboardLabel.setFont(new Font("Arial", Font.BOLD, 16));
		centerPanel.add(leaderboardLabel, BorderLayout.NORTH);

		String[] columnNames = {"Rank", "Student", "Score", "Percentage", "Time"};
		leaderboardModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable leaderboardTable = new JTable(leaderboardModel);
		leaderboardTable.getTableHeader().setReorderingAllowed(false);

		JScrollPane scrollPane = new JScrollPane(leaderboardTable);
		centerPanel.add(scrollPane, BorderLayout.CENTER);

		add(centerPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new FlowLayout());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JButton backButton = new JButton("Back to Dashboard");
		backButton.addActionListener(e -> {
			new StudentDashboard(connection, currentUser).setVisible(true);
			this.dispose();
		});

		JButton logoutButton = new JButton("Logout");
		logoutButton.addActionListener(e -> handleLogout());

		bottomPanel.add(backButton);
		bottomPanel.add(logoutButton);
		add(bottomPanel, BorderLayout.SOUTH);
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

	private void loadLeaderboard() {
		// Add loading message
		leaderboardModel.addRow(new Object[]{"", "Loading leaderboard...", "", "", ""});
		
		// Load leaderboard in background thread
		new Thread(() -> {
			try {
				connection.sendMessage(Protocol.GET_LEADERBOARD + Protocol.DELIMITER + quizId);
				String response = connection.receiveMessage();

				if (response == null) {
					javax.swing.SwingUtilities.invokeLater(() -> {
						leaderboardModel.setRowCount(0);
						leaderboardModel.addRow(new Object[]{"", "Failed to load leaderboard", "", "", ""});
					});
					return;
				}

				String[] parts = response.split(java.util.regex.Pattern.quote(Protocol.DELIMITER));

				if (parts[0].equals(Protocol.LEADERBOARD_DATA)) {
					javax.swing.SwingUtilities.invokeLater(() -> {
						leaderboardModel.setRowCount(0);

						for (int i = 1; i < parts.length; i++) {
							String[] entryData = parts[i].split(Protocol.SUB_DELIMITER);
							if (entryData.length >= 5) {
								String studentName = entryData[0];
								int entryScore = Integer.parseInt(entryData[1]);
								int entryTotal = Integer.parseInt(entryData[2]);
								String entryPercentage = entryData[3];
								long entryTime = Long.parseLong(entryData[4]);

								Object[] row = {
									i,
									studentName,
									entryScore + "/" + entryTotal,
									entryPercentage + "%",
									formatTime(entryTime)
								};
								leaderboardModel.addRow(row);
							}
						}
					});
				}
			} catch (IOException e) {
				javax.swing.SwingUtilities.invokeLater(() -> {
					leaderboardModel.setRowCount(0);
					leaderboardModel.addRow(new Object[]{"", "Error loading leaderboard", "", "", ""});
				});
			}
		}).start();
	}

	private String getGrade(double percentage) {
		if (percentage >= 90) return "A";
		if (percentage >= 80) return "B";
		if (percentage >= 70) return "C";
		if (percentage >= 60) return "D";
		return "F";
	}

	private String formatTime(long seconds) {
		long minutes = seconds / 60;
		long secs = seconds % 60;
		return String.format("%d:%02d", minutes, secs);
	}
}

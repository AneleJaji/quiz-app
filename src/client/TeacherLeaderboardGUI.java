package client;

import common.Protocol;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;

public class TeacherLeaderboardGUI extends JFrame {
	private final ServerConnection connection;
	private final int quizId;
	private final String quizName;
	private JTable leaderboardTable;
	private DefaultTableModel tableModel;

	public TeacherLeaderboardGUI(ServerConnection connection, int quizId, String quizName) {
		this.connection = connection;
		this.quizId = quizId;
		this.quizName = quizName;

		setTitle("Leaderboard - " + quizName);
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

		initComponents();
		// Load leaderboard in background
		new Thread(this::loadLeaderboard).start();
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel();
		topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		topPanel.setBackground(new Color(70, 130, 180));

		JLabel titleLabel = new JLabel("Leaderboard: " + quizName);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		titleLabel.setForeground(Color.WHITE);
		topPanel.add(titleLabel);

		add(topPanel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

		String[] columnNames = {"Rank", "Student Name", "Score", "Percentage", "Time Taken"};
		tableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		leaderboardTable = new JTable(tableModel);
		leaderboardTable.getTableHeader().setReorderingAllowed(false);
		leaderboardTable.setRowHeight(25);

		JScrollPane scrollPane = new JScrollPane(leaderboardTable);
		centerPanel.add(scrollPane, BorderLayout.CENTER);

		add(centerPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new FlowLayout());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JButton refreshButton = new JButton("Refresh");
		JButton closeButton = new JButton("Close");
		JButton logoutButton = new JButton("Logout");

		refreshButton.addActionListener(e -> loadLeaderboard());
		closeButton.addActionListener(e -> dispose());
		logoutButton.addActionListener(e -> handleLogout());

		bottomPanel.add(refreshButton);
		bottomPanel.add(closeButton);
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
		try {
			connection.sendMessage(Protocol.GET_LEADERBOARD + Protocol.DELIMITER + quizId);
			String response = connection.receiveMessage();

			if (response == null) return;

			String[] parts = response.split(java.util.regex.Pattern.quote(Protocol.DELIMITER));

			if (parts[0].equals(Protocol.LEADERBOARD_DATA)) {
				javax.swing.SwingUtilities.invokeLater(() -> {
					tableModel.setRowCount(0);

					if (parts.length == 1) {
						JLabel noDataLabel = new JLabel("No students have taken this quiz yet.");
						noDataLabel.setFont(new Font("Arial", Font.ITALIC, 14));
						noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
						return;
					}

					for (int i = 1; i < parts.length; i++) {
						String[] entryData = parts[i].split(java.util.regex.Pattern.quote(Protocol.SUB_DELIMITER));
						if (entryData.length >= 5) {
							String studentName = entryData[0];
							int score = Integer.parseInt(entryData[1]);
							int total = Integer.parseInt(entryData[2]);
							String percentage = entryData[3];
							long timeTaken = Long.parseLong(entryData[4]);

							tableModel.addRow(new Object[]{
								i,
								studentName,
								score + "/" + total,
								percentage + "%",
								formatTime(timeTaken)
							});
						}
					}

					if (tableModel.getRowCount() > 0) {
						leaderboardTable.setDefaultRenderer(Object.class, new RankCellRenderer());
					}
				});
			}
		} catch (IOException e) {
			javax.swing.SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(this, "Error loading leaderboard: " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
			});
		}
	}

	private String formatTime(long seconds) {
		long minutes = seconds / 60;
		long secs = seconds % 60;
		return String.format("%d:%02d", minutes, secs);
	}

	private class RankCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
													   boolean isSelected, boolean hasFocus,
													   int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (!isSelected) {
				if (row == 0) {
					c.setBackground(new Color(255, 215, 0, 80));
				} else if (row == 1) {
					c.setBackground(new Color(192, 192, 192, 80));
				} else if (row == 2) {
					c.setBackground(new Color(205, 127, 50, 80));
				} else {
					c.setBackground(Color.WHITE);
				}
			}

			return c;
		}
	}
}

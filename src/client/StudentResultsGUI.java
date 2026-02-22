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
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;

public class StudentResultsGUI extends JFrame {
	private final ServerConnection connection;
	private final User currentUser;
	private JTable resultsTable;
	private DefaultTableModel tableModel;

	public StudentResultsGUI(ServerConnection connection, User user) {
		this.connection = connection;
		this.currentUser = user;

		setTitle("My Quiz Results");
		setSize(800, 500);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

		initComponents();
		// Load results in background
		new Thread(this::loadResults).start();
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel();
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel titleLabel = new JLabel("My Quiz Results");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		topPanel.add(titleLabel);

		add(topPanel, BorderLayout.NORTH);

		String[] columnNames = {"Quiz Name", "Score", "Percentage", "Time Taken", "Date"};
		tableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		resultsTable = new JTable(tableModel);
		resultsTable.getTableHeader().setReorderingAllowed(false);

		JScrollPane scrollPane = new JScrollPane(resultsTable);
		add(scrollPane, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new FlowLayout());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> dispose());

		JButton logoutButton = new JButton("Logout");
		logoutButton.addActionListener(e -> handleLogout());

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

	private void loadResults() {
		try {
			connection.sendMessage(Protocol.GET_MY_RESULTS);
			String response = connection.receiveMessage();

			if (response == null) return;

			String[] parts = response.split(java.util.regex.Pattern.quote(Protocol.DELIMITER));

			if (parts[0].equals(Protocol.RESULTS_DATA)) {
				javax.swing.SwingUtilities.invokeLater(() -> {
					tableModel.setRowCount(0);

					for (int i = 1; i < parts.length; i++) {
						String[] resultData = parts[i].split(java.util.regex.Pattern.quote(Protocol.SUB_DELIMITER));
						if (resultData.length >= 6) {
							String quizName = resultData[0];
							int score = Integer.parseInt(resultData[1]);
							int total = Integer.parseInt(resultData[2]);
							String percentage = resultData[3];
							long timeTaken = Long.parseLong(resultData[4]);
							String date = resultData[5];

							tableModel.addRow(new Object[]{
								quizName,
								score + "/" + total,
								percentage + "%",
								formatTime(timeTaken),
								date
							});
						}
					}

					if (tableModel.getRowCount() == 0) {
						tableModel.addRow(new Object[]{"No results yet", "", "", "", ""});
					}
				});
			}
		} catch (IOException e) {
			javax.swing.SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(this, "Error loading results: " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
			});
		}
	}

	private String formatTime(long seconds) {
		long minutes = seconds / 60;
		long secs = seconds % 60;
		return String.format("%d:%02d", minutes, secs);
	}
}

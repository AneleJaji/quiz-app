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
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateQuizGUI extends JFrame {
	private final ServerConnection connection;
	private final User currentUser;
	private final TeacherDashboard parentDashboard;

	private JTextField quizNameField;
	private JSpinner timeLimitSpinner;
	private JPanel questionsPanel;
	private final List<QuestionPanel> questionPanels;
	private JButton addQuestionButton;
	private JButton createButton;
	private JButton cancelButton;

	public CreateQuizGUI(ServerConnection connection, User user, TeacherDashboard parent) {
		this.connection = connection;
		this.currentUser = user;
		this.parentDashboard = parent;
		this.questionPanels = new ArrayList<>();

		setTitle("Create New Quiz");
		setSize(800, 700);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

		JLabel titleLabel = new JLabel("Create New Quiz");
		titleLabel.setAlignmentX(LEFT_ALIGNMENT);
		topPanel.add(titleLabel);
		topPanel.add(Box.createRigidArea(new Dimension(0, 15)));

		JPanel namePanel = new JPanel(new BorderLayout());
		namePanel.add(new JLabel("Quiz Name:"), BorderLayout.NORTH);
		quizNameField = new JTextField();
		namePanel.add(quizNameField, BorderLayout.CENTER);
		namePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
		topPanel.add(namePanel);
		topPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		JPanel timePanel = new JPanel(new BorderLayout());
		timePanel.add(new JLabel("Time per Question (seconds):"), BorderLayout.NORTH);
		timeLimitSpinner = new JSpinner(new SpinnerNumberModel(30, 10, 300, 5));
		timePanel.add(timeLimitSpinner, BorderLayout.CENTER);
		timePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
		topPanel.add(timePanel);

		add(topPanel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

		JLabel questionsLabel = new JLabel("Questions:");
		centerPanel.add(questionsLabel, BorderLayout.NORTH);

		questionsPanel = new JPanel();
		questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));

		JScrollPane scrollPane = new JScrollPane(questionsPanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		centerPanel.add(scrollPane, BorderLayout.CENTER);

		addQuestionButton = new JButton("+ Add Question");
		addQuestionButton.addActionListener(e -> addQuestionPanel());
		centerPanel.add(addQuestionButton, BorderLayout.SOUTH);

		add(centerPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new FlowLayout());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		createButton = new JButton("Create Quiz");
		cancelButton = new JButton("Cancel");
		JButton logoutButton = new JButton("Logout");

		createButton.addActionListener(e -> handleCreateQuiz());
		cancelButton.addActionListener(e -> dispose());
		logoutButton.addActionListener(e -> handleLogout());

		bottomPanel.add(createButton);
		bottomPanel.add(cancelButton);
		bottomPanel.add(logoutButton);

		add(bottomPanel, BorderLayout.SOUTH);

		addQuestionPanel();
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

	private void addQuestionPanel() {
		QuestionPanel qPanel = new QuestionPanel(questionPanels.size() + 1);
		questionPanels.add(qPanel);
		questionsPanel.add(qPanel);
		questionsPanel.revalidate();
		questionsPanel.repaint();
	}

	private void handleCreateQuiz() {
		String quizName = quizNameField.getText().trim();
		if (quizName.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter a quiz name",
				"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (questionPanels.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please add at least one question",
				"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		List<String> questionData = new ArrayList<>();
		for (QuestionPanel qPanel : questionPanels) {
			String data = qPanel.getQuestionData();
			if (data == null) {
				JOptionPane.showMessageDialog(this,
					"Please fill in all fields for question " + qPanel.getQuestionNumber(),
					"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			questionData.add(data);
		}

		int timeLimit = (Integer) timeLimitSpinner.getValue();

		try {
			StringBuilder message = new StringBuilder(Protocol.CREATE_QUIZ);
			message.append(Protocol.DELIMITER).append(quizName);
			message.append(Protocol.DELIMITER).append(timeLimit);
			message.append(Protocol.DELIMITER).append(questionData.size());

			for (String data : questionData) {
				message.append(Protocol.DELIMITER).append(data);
			}

			connection.sendMessage(message.toString());
			String response = connection.receiveMessage();

			String[] parts = response.split(java.util.regex.Pattern.quote(Protocol.DELIMITER));

			if (parts[0].equals(Protocol.QUIZ_CREATED)) {
				JOptionPane.showMessageDialog(this,
					"Quiz created successfully!",
					"Success",
					JOptionPane.INFORMATION_MESSAGE);
				parentDashboard.loadQuizzes();
				dispose();
			} else {
				JOptionPane.showMessageDialog(this,
					"Failed to create quiz: " + (parts.length > 1 ? parts[1] : "Unknown error"),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error creating quiz: " + e.getMessage(),
				"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private class QuestionPanel extends JPanel {
		private int questionNumber;
		private JTextArea questionTextArea;
		private JTextField optionAField;
		private JTextField optionBField;
		private JTextField optionCField;
		private JTextField optionDField;
		private JComboBox<String> correctAnswerCombo;
		private JButton removeButton;

		public QuestionPanel(int questionNumber) {
			this.questionNumber = questionNumber;

			setLayout(new BorderLayout());
			setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Question " + questionNumber),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)
			));
			setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

			mainPanel.add(new JLabel("Question:"));
			questionTextArea = new JTextArea(2, 40);
			questionTextArea.setLineWrap(true);
			questionTextArea.setWrapStyleWord(true);
			JScrollPane questionScroll = new JScrollPane(questionTextArea);
			mainPanel.add(questionScroll);
			mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

			JPanel optionsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
			optionsPanel.add(new JLabel("Option A:"));
			optionAField = new JTextField();
			optionsPanel.add(optionAField);

			optionsPanel.add(new JLabel("Option B:"));
			optionBField = new JTextField();
			optionsPanel.add(optionBField);

			optionsPanel.add(new JLabel("Option C:"));
			optionCField = new JTextField();
			optionsPanel.add(optionCField);

			optionsPanel.add(new JLabel("Option D:"));
			optionDField = new JTextField();
			optionsPanel.add(optionDField);

			mainPanel.add(optionsPanel);
			mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

			JPanel correctPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			correctPanel.add(new JLabel("Correct Answer:"));
			correctAnswerCombo = new JComboBox<>(new String[]{"A", "B", "C", "D"});
			correctPanel.add(correctAnswerCombo);
			mainPanel.add(correctPanel);

			add(mainPanel, BorderLayout.CENTER);

		removeButton = new JButton("Remove");
		removeButton.addActionListener(e -> removeQuestion());
			add(removeButton, BorderLayout.EAST);
		}

		public int getQuestionNumber() {
			return questionNumber;
		}

		public String getQuestionData() {
			String questionText = questionTextArea.getText().trim();
			String optionA = optionAField.getText().trim();
			String optionB = optionBField.getText().trim();
			String optionC = optionCField.getText().trim();
			String optionD = optionDField.getText().trim();
			String correctAnswer = (String) correctAnswerCombo.getSelectedItem();

			if (questionText.isEmpty() || optionA.isEmpty() || optionB.isEmpty() ||
				optionC.isEmpty() || optionD.isEmpty()) {
				return null;
			}

			return questionText + Protocol.SUB_DELIMITER +
				optionA + Protocol.SUB_DELIMITER +
				optionB + Protocol.SUB_DELIMITER +
				optionC + Protocol.SUB_DELIMITER +
				optionD + Protocol.SUB_DELIMITER +
				correctAnswer;
		}

		private void removeQuestion() {
			if (questionPanels.size() <= 1) {
				JOptionPane.showMessageDialog(CreateQuizGUI.this,
					"Quiz must have at least one question",
					"Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			}

			questionPanels.remove(this);
			questionsPanel.remove(this);
			updateQuestionNumbers();
			questionsPanel.revalidate();
			questionsPanel.repaint();
		}
	}

	private void updateQuestionNumbers() {
		for (int i = 0; i < questionPanels.size(); i++) {
			QuestionPanel qPanel = questionPanels.get(i);
			qPanel.questionNumber = i + 1;
			qPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Question " + (i + 1)),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)
			));
		}
	}
}

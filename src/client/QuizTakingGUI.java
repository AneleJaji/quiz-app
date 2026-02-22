package client;

import common.Protocol;
import model.Question;
import model.Quiz;
import model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuizTakingGUI extends JFrame {
	private final ServerConnection connection;
	private final User currentUser;
	private final int quizId;
	private Quiz quiz;
	private List<Question> questions;
	private int currentQuestionIndex = 0;
	private List<String> studentAnswers;
	private long quizStartTime;

	private JLabel questionLabel;
	private JRadioButton optionA;
	private JRadioButton optionB;
	private JRadioButton optionC;
	private JRadioButton optionD;
	private ButtonGroup optionGroup;
	private JButton prevButton;
	private JButton nextButton;
	private JButton submitButton;
	private JLabel timerLabel;
	private JProgressBar progressBar;
	private Timer timer;
	private int timeRemaining;

	public QuizTakingGUI(ServerConnection connection, User user, int quizId) {
		this.connection = connection;
		this.currentUser = user;
		this.quizId = quizId;
		this.studentAnswers = new ArrayList<>();

		setTitle("Taking Quiz");
		setSize(900, 700);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(null);

		loadQuiz();
	}

	private void loadQuiz() {
		// Show loading message
		JPanel loadingPanel = new JPanel(new BorderLayout());
		JLabel loadingLabel = new JLabel("Loading quiz...", JLabel.CENTER);
		loadingLabel.setFont(new Font("Arial", Font.BOLD, 18));
		loadingPanel.add(loadingLabel, BorderLayout.CENTER);
		add(loadingPanel, BorderLayout.CENTER);
		
		// Load quiz data in background thread
		new Thread(() -> {
			try {
				connection.sendMessage(Protocol.START_QUIZ + Protocol.DELIMITER + quizId);
				String response = connection.receiveMessage();

				if (response == null) {
					javax.swing.SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(this, "No response from server",
							"Error", JOptionPane.ERROR_MESSAGE);
						dispose();
					});
					return;
				}

				String[] parts = response.split(java.util.regex.Pattern.quote(Protocol.DELIMITER));

				if (parts[0].equals(Protocol.QUIZ_DATA)) {
					quiz = new Quiz();
					quiz.setQuizId(Integer.parseInt(parts[1]));
					quiz.setQuizName(parts[2]);
					quiz.setTotalQuestions(Integer.parseInt(parts[3]));
					quiz.setTimeLimit(Integer.parseInt(parts[4]));

					questions = new ArrayList<>();
					for (int i = 5; i < parts.length; i++) {
						String[] questionData = parts[i].split(java.util.regex.Pattern.quote(Protocol.SUB_DELIMITER));
						Question question = new Question();
						question.setQuestionId(Integer.parseInt(questionData[0]));
						question.setQuestionText(questionData[1]);
						question.setOptionA(questionData[2]);
						question.setOptionB(questionData[3]);
						question.setOptionC(questionData[4]);
						question.setOptionD(questionData[5]);
						questions.add(question);
						studentAnswers.add("");
					}

					// Update UI on EDT
					javax.swing.SwingUtilities.invokeLater(() -> {
						remove(loadingPanel);
						initComponents();
						displayQuestion();
						quizStartTime = System.currentTimeMillis();
						startQuestionTimer();
						revalidate();
						repaint();
					});
				}
			} catch (IOException e) {
				javax.swing.SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(this, "Error loading quiz: " + e.getMessage(),
						"Error", JOptionPane.ERROR_MESSAGE);
					dispose();
				});
			}
		}).start();
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel titleLabel = new JLabel(quiz.getQuizName());
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		topPanel.add(titleLabel, BorderLayout.WEST);

		timerLabel = new JLabel("Time: " + quiz.getTimeLimit() + "s");
		timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
		timerLabel.setForeground(Color.BLUE);
		topPanel.add(timerLabel, BorderLayout.EAST);

		add(topPanel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

		progressBar = new JProgressBar(0, questions.size());
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setString("Question 1 of " + questions.size());
		centerPanel.add(progressBar);
		centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		questionLabel = new JLabel();
		questionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		centerPanel.add(questionLabel);
		centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		optionGroup = new ButtonGroup();
		optionA = new JRadioButton();
		optionB = new JRadioButton();
		optionC = new JRadioButton();
		optionD = new JRadioButton();

		optionA.setFont(new Font("Arial", Font.PLAIN, 14));
		optionB.setFont(new Font("Arial", Font.PLAIN, 14));
		optionC.setFont(new Font("Arial", Font.PLAIN, 14));
		optionD.setFont(new Font("Arial", Font.PLAIN, 14));

		optionGroup.add(optionA);
		optionGroup.add(optionB);
		optionGroup.add(optionC);
		optionGroup.add(optionD);

		optionA.setAlignmentX(Component.LEFT_ALIGNMENT);
		optionB.setAlignmentX(Component.LEFT_ALIGNMENT);
		optionC.setAlignmentX(Component.LEFT_ALIGNMENT);
		optionD.setAlignmentX(Component.LEFT_ALIGNMENT);

		centerPanel.add(optionA);
		centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		centerPanel.add(optionB);
		centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		centerPanel.add(optionC);
		centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		centerPanel.add(optionD);

		add(centerPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new FlowLayout());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		prevButton = new JButton("Previous");
		nextButton = new JButton("Next");
		submitButton = new JButton("Submit Quiz");

		prevButton.addActionListener(e -> previousQuestion());
		nextButton.addActionListener(e -> nextQuestion());
		submitButton.addActionListener(e -> submitQuiz());

		bottomPanel.add(prevButton);
		bottomPanel.add(nextButton);
		bottomPanel.add(submitButton);

		add(bottomPanel, BorderLayout.SOUTH);
	}

	private void displayQuestion() {
		if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
			return;
		}

		Question q = questions.get(currentQuestionIndex);

		questionLabel.setText("<html><body style='width: 700px'>" +
			(currentQuestionIndex + 1) + ". " + q.getQuestionText() +
			"</body></html>");
		optionA.setText("A. " + q.getOptionA());
		optionB.setText("B. " + q.getOptionB());
		optionC.setText("C. " + q.getOptionC());
		optionD.setText("D. " + q.getOptionD());

		String savedAnswer = studentAnswers.get(currentQuestionIndex);
		optionGroup.clearSelection();
		if ("A".equals(savedAnswer)) optionA.setSelected(true);
		else if ("B".equals(savedAnswer)) optionB.setSelected(true);
		else if ("C".equals(savedAnswer)) optionC.setSelected(true);
		else if ("D".equals(savedAnswer)) optionD.setSelected(true);

		prevButton.setEnabled(currentQuestionIndex > 0);
		nextButton.setEnabled(currentQuestionIndex < questions.size() - 1);

		progressBar.setValue(currentQuestionIndex + 1);
		progressBar.setString("Question " + (currentQuestionIndex + 1) + " of " + questions.size());

		startQuestionTimer();
	}

	private void startQuestionTimer() {
		if (timer != null) {
			timer.stop();
		}

		timeRemaining = quiz.getTimeLimit();

		timer = new Timer(1000, e -> {
			timeRemaining--;
			timerLabel.setText("Time: " + timeRemaining + "s");

			if (timeRemaining <= 5) {
				timerLabel.setForeground(Color.RED);
			} else {
				timerLabel.setForeground(Color.BLUE);
			}

			if (timeRemaining <= 0) {
				timer.stop();
				if (currentQuestionIndex < questions.size() - 1) {
					JOptionPane.showMessageDialog(this, "Time's up for this question!",
						"Time Up", JOptionPane.WARNING_MESSAGE);
					nextQuestion();
				} else {
					JOptionPane.showMessageDialog(this, "Time's up! Submitting quiz.",
						"Time Up", JOptionPane.WARNING_MESSAGE);
					submitQuiz();
				}
			}
		});
		timer.start();
	}

	private void saveCurrentAnswer() {
		String answer = "";
		if (optionA.isSelected()) answer = "A";
		else if (optionB.isSelected()) answer = "B";
		else if (optionC.isSelected()) answer = "C";
		else if (optionD.isSelected()) answer = "D";

		studentAnswers.set(currentQuestionIndex, answer);
	}

	private void previousQuestion() {
		saveCurrentAnswer();
		currentQuestionIndex--;
		displayQuestion();
	}

	private void nextQuestion() {
		saveCurrentAnswer();
		currentQuestionIndex++;
		displayQuestion();
	}

	private void submitQuiz() {
		// Validate user role - only students can submit quizzes
		if (currentUser.isTeacher()) {
			JOptionPane.showMessageDialog(this,
				"Teachers cannot submit quizzes. Only students can take quizzes.",
				"Access Denied", JOptionPane.ERROR_MESSAGE);
			this.dispose();
			return;
		}

		saveCurrentAnswer();

		if (timer != null) {
			timer.stop();
		}

		int confirm = JOptionPane.showConfirmDialog(this,
			"Are you sure you want to submit the quiz?",
			"Confirm Submission",
			JOptionPane.YES_NO_OPTION);

		if (confirm != JOptionPane.YES_OPTION) {
			startQuestionTimer();
			return;
		}

		long totalTime = (System.currentTimeMillis() - quizStartTime) / 1000;

		int finalTotalQuestions = questions.size();
		long finalTotalTime = totalTime;
		int finalQuizId = quizId;
		String finalQuizName = quiz.getQuizName();

		// Save quiz result in background thread
		new Thread(() -> {
			try {
				// Build message with all student answers
				StringBuilder message = new StringBuilder(Protocol.FINISH_QUIZ);
				message.append(Protocol.DELIMITER).append(finalQuizId);
				message.append(Protocol.DELIMITER).append(finalTotalTime);
				
				// Add all student answers
				for (String answer : studentAnswers) {
					message.append(Protocol.DELIMITER).append(answer);
				}
				
				System.out.println("Sending quiz submission with answers: " + message.toString());
				connection.sendMessage(message.toString());

				String response = connection.receiveMessage();
				System.out.println("Server response: " + response);
				
				// Parse server response to get score and percentage
				String[] responseParts = response.split(java.util.regex.Pattern.quote(Protocol.DELIMITER));
				int finalScore = 0;
				double finalPercentage = 0.0;
				
				if (responseParts.length >= 4 && responseParts[0].equals(Protocol.QUIZ_RESULT)) {
					finalScore = Integer.parseInt(responseParts[1]);
					finalPercentage = Double.parseDouble(responseParts[3]);
				}
				
				// Show results window
				final int score = finalScore;
				final double percentage = finalPercentage;
				javax.swing.SwingUtilities.invokeLater(() -> {
					new QuizResultGUI(connection, currentUser, score, finalTotalQuestions,
						percentage, finalTotalTime, finalQuizId, finalQuizName).setVisible(true);
					QuizTakingGUI.this.dispose();
				});
				
			} catch (IOException e) {
				System.err.println("Error saving quiz: " + e.getMessage());
				e.printStackTrace();
				javax.swing.SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(QuizTakingGUI.this, "Error saving quiz: " + e.getMessage(),
						"Error", JOptionPane.ERROR_MESSAGE);
				});
			}
		}).start();
	}
}

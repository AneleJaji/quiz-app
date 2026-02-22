package server;

import common.Protocol;
import model.Question;
import model.Quiz;
import model.QuizAttempt;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
	private final Socket clientSocket;
	private BufferedReader in;
	private PrintWriter out;
	private User currentUser;
	private final QuizDAO dao;

	public ClientHandler(Socket socket) {
		this.clientSocket = socket;
		this.dao = new QuizDAO();
	}

	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(clientSocket.getOutputStream(), true);

			System.out.println("Client connected: " + clientSocket.getInetAddress());

			String message;
			while ((message = in.readLine()) != null) {
				handleMessage(message);
			}
		} catch (IOException e) {
			System.out.println("Client disconnected: " + e.getMessage());
		} finally {
			closeConnection();
		}
	}

	private void handleMessage(String message) {
		String[] parts = message.split(java.util.regex.Pattern.quote(Protocol.DELIMITER));
		String command = parts[0];

		try {
			switch (command) {
				case Protocol.LOGIN:
					handleLogin(parts);
					break;
				case Protocol.REGISTER:
					handleRegister(parts);
					break;
				case Protocol.GET_ACTIVE_QUIZZES:
					handleGetActiveQuizzes();
					break;
				case Protocol.CREATE_QUIZ:
					handleCreateQuiz(parts);
					break;
				case Protocol.START_QUIZ:
					handleStartQuiz(parts);
					break;
				case Protocol.FINISH_QUIZ:
					handleFinishQuiz(parts);
					break;
				case Protocol.GET_LEADERBOARD:
					handleGetLeaderboard(parts);
					break;
				case Protocol.GET_MY_RESULTS:
					handleGetMyResults();
					break;
				case Protocol.DELETE_QUIZ:
					handleDeleteQuiz(parts);
					break;
				case Protocol.DISCONNECT:
					closeConnection();
					break;
				default:
					sendMessage(Protocol.ERROR + Protocol.DELIMITER + "Unknown command");
			}
		} catch (Exception e) {
			String errorMessage = e.getMessage();
			if (errorMessage == null || errorMessage.trim().isEmpty()) {
				errorMessage = "Server error while processing request";
			}
			System.err.println("Request failed: " + errorMessage);
			e.printStackTrace();
			sendMessage(Protocol.ERROR + Protocol.DELIMITER + errorMessage);
		}
	}

	private void handleLogin(String[] parts) throws SQLException {
		if (parts.length < 3) {
			sendMessage(Protocol.LOGIN_FAILED + Protocol.DELIMITER + "Invalid login format");
			return;
		}

		String username = parts[1];
		String password = parts[2];

		System.out.println("Login attempt: username=" + username);

		User user = dao.authenticateUser(username, password);
		if (user != null) {
			currentUser = user;
			System.out.println("Login successful: userId=" + user.getUserId() + ", role=" + user.getRole());
			String response = Protocol.LOGIN_SUCCESS + Protocol.DELIMITER +
				user.getUserId() + Protocol.DELIMITER +
				user.getFullName() + Protocol.DELIMITER +
				user.getRole();
			sendMessage(response);
		} else {
			System.out.println("Login failed for username: " + username);
			sendMessage(Protocol.LOGIN_FAILED + Protocol.DELIMITER + "Invalid credentials");
		}
	}

	private void handleRegister(String[] parts) throws SQLException {
		if (parts.length < 5) {
			sendMessage(Protocol.REGISTER_FAILED + Protocol.DELIMITER + "Invalid registration format");
			return;
		}

		String username = parts[1];
		String password = parts[2];
		String fullName = parts[3];
		String role = parts[4];

		User user = new User(username, password, fullName, role);
		boolean success = dao.registerUser(user);

		if (success) {
			sendMessage(Protocol.REGISTER_SUCCESS + Protocol.DELIMITER + "Registration successful");
		} else {
			sendMessage(Protocol.REGISTER_FAILED + Protocol.DELIMITER + "Username already exists");
		}
	}

	private void handleGetActiveQuizzes() throws SQLException {
		List<Quiz> quizzes = dao.getActiveQuizzes();
		StringBuilder response = new StringBuilder(Protocol.QUIZ_LIST);

		for (Quiz quiz : quizzes) {
			response.append(Protocol.DELIMITER);
			response.append(quiz.getQuizId()).append(Protocol.SUB_DELIMITER);
			response.append(quiz.getQuizName()).append(Protocol.SUB_DELIMITER);
			response.append(quiz.getTeacherName()).append(Protocol.SUB_DELIMITER);
			response.append(quiz.getTotalQuestions()).append(Protocol.SUB_DELIMITER);
			response.append(quiz.getTimeLimit());
		}

		sendMessage(response.toString());
	}

	private void handleCreateQuiz(String[] parts) throws SQLException {
		if (currentUser == null || !currentUser.isTeacher()) {
			sendMessage(Protocol.ERROR + Protocol.DELIMITER + "Only teachers can create quizzes");
			return;
		}

		String quizName = parts[1];
		int timeLimit = Integer.parseInt(parts[2]);
		int numQuestions = Integer.parseInt(parts[3]);

		Quiz quiz = new Quiz(quizName, currentUser.getUserId(), timeLimit);

		for (int i = 0; i < numQuestions; i++) {
			String[] questionData = parts[4 + i].split(java.util.regex.Pattern.quote(Protocol.SUB_DELIMITER));
			Question question = new Question(
				questionData[0],
				questionData[1],
				questionData[2],
				questionData[3],
				questionData[4],
				questionData[5]
			);
			quiz.addQuestion(question);
		}

		int quizId = dao.createQuiz(quiz);
		if (quizId > 0) {
			sendMessage(Protocol.QUIZ_CREATED + Protocol.DELIMITER + quizId);
		} else {
			sendMessage(Protocol.ERROR + Protocol.DELIMITER + "Failed to create quiz");
		}
	}

	private void handleStartQuiz(String[] parts) throws SQLException {
		int quizId = Integer.parseInt(parts[1]);
		Quiz quiz = dao.getQuizWithQuestions(quizId);

		if (quiz == null) {
			sendMessage(Protocol.ERROR + Protocol.DELIMITER + "Quiz not found");
			return;
		}

		StringBuilder response = new StringBuilder(Protocol.QUIZ_DATA);
		response.append(Protocol.DELIMITER).append(quiz.getQuizId());
		response.append(Protocol.DELIMITER).append(quiz.getQuizName());
		response.append(Protocol.DELIMITER).append(quiz.getTotalQuestions());
		response.append(Protocol.DELIMITER).append(quiz.getTimeLimit());

		for (Question q : quiz.getQuestions()) {
			response.append(Protocol.DELIMITER);
			response.append(q.getQuestionId()).append(Protocol.SUB_DELIMITER);
			response.append(q.getQuestionText()).append(Protocol.SUB_DELIMITER);
			response.append(q.getOptionA()).append(Protocol.SUB_DELIMITER);
			response.append(q.getOptionB()).append(Protocol.SUB_DELIMITER);
			response.append(q.getOptionC()).append(Protocol.SUB_DELIMITER);
			response.append(q.getOptionD());
		}

		sendMessage(response.toString());
	}

	private void handleFinishQuiz(String[] parts) throws SQLException {
		System.out.println("handleFinishQuiz called with " + parts.length + " parts");
		for (int i = 0; i < parts.length; i++) {
			System.out.println("  parts[" + i + "] = " + parts[i]);
		}
		
		if (currentUser == null) {
			System.out.println("ERROR: currentUser is null!");
			sendMessage(Protocol.ERROR + Protocol.DELIMITER + "Not logged in");
			return;
		}

		System.out.println("Current user: " + currentUser.getUsername());
		
		if (parts.length < 3) {
			sendMessage(Protocol.ERROR + Protocol.DELIMITER + "Invalid message format");
			return;
		}

		try {
			int quizId = Integer.parseInt(parts[1]);
			int timeTaken = Integer.parseInt(parts[2]);

			// Student answers start from parts[3]
			List<String> studentAnswers = new ArrayList<>();
			for (int i = 3; i < parts.length; i++) {
				studentAnswers.add(parts[i]);
			}

			System.out.println("Quiz submission: quizId=" + quizId + ", timeTaken=" + timeTaken + 
				", studentAnswers.size=" + studentAnswers.size());

			// Get quiz with correct answers from database
			Quiz quiz = dao.getQuizWithQuestions(quizId);
			if (quiz == null) {
				sendMessage(Protocol.ERROR + Protocol.DELIMITER + "Quiz not found");
				return;
			}

			// Calculate score by comparing student answers with correct answers
			int score = 0;
			List<Question> questions = quiz.getQuestions();
			for (int i = 0; i < questions.size() && i < studentAnswers.size(); i++) {
				Question q = questions.get(i);
				String studentAnswer = studentAnswers.get(i);
				String correctAnswer = q.getCorrectAnswer();
				
				System.out.println("Question " + i + ": student=" + studentAnswer + ", correct=" + correctAnswer);
				
				if (correctAnswer != null && correctAnswer.equalsIgnoreCase(studentAnswer)) {
					score++;
				}
			}

			System.out.println("Calculated score: " + score + " out of " + questions.size());

			// Save quiz attempt
			QuizAttempt attempt = new QuizAttempt(quizId, currentUser.getUserId(), score, questions.size(), timeTaken);
			int attemptId = dao.saveQuizAttempt(attempt);

			System.out.println("Database save result: attemptId=" + attemptId);

			if (attemptId > 0) {
				String response = Protocol.QUIZ_RESULT + Protocol.DELIMITER +
					score + Protocol.DELIMITER +
					questions.size() + Protocol.DELIMITER +
					attempt.getPercentage();
				System.out.println("Sending response: " + response);
				sendMessage(response);
			} else {
				System.out.println("ERROR: Failed to save quiz attempt");
				sendMessage(Protocol.ERROR + Protocol.DELIMITER + "Failed to save quiz result");
			}
		} catch (NumberFormatException e) {
			System.err.println("ERROR parsing numbers: " + e.getMessage());
			sendMessage(Protocol.ERROR + Protocol.DELIMITER + "Invalid number format: " + e.getMessage());
		}
	}

	private void handleGetLeaderboard(String[] parts) throws SQLException {
		int quizId = Integer.parseInt(parts[1]);
		List<QuizAttempt> leaderboard = dao.getLeaderboard(quizId);

		StringBuilder response = new StringBuilder(Protocol.LEADERBOARD_DATA);
		for (QuizAttempt attempt : leaderboard) {
			response.append(Protocol.DELIMITER);
			response.append(attempt.getStudentName()).append(Protocol.SUB_DELIMITER);
			response.append(attempt.getScore()).append(Protocol.SUB_DELIMITER);
			response.append(attempt.getTotalQuestions()).append(Protocol.SUB_DELIMITER);
			response.append(String.format("%.2f", attempt.getPercentage())).append(Protocol.SUB_DELIMITER);
			response.append(attempt.getTimeTaken());
		}

		sendMessage(response.toString());
	}

	private void handleGetMyResults() throws SQLException {
		if (currentUser == null) {
			sendMessage(Protocol.ERROR + Protocol.DELIMITER + "Not logged in");
			return;
		}

		List<QuizAttempt> results = dao.getStudentResults(currentUser.getUserId());
		StringBuilder response = new StringBuilder(Protocol.RESULTS_DATA);

		for (QuizAttempt attempt : results) {
			response.append(Protocol.DELIMITER);
			response.append(attempt.getQuizName()).append(Protocol.SUB_DELIMITER);
			response.append(attempt.getScore()).append(Protocol.SUB_DELIMITER);
			response.append(attempt.getTotalQuestions()).append(Protocol.SUB_DELIMITER);
			response.append(String.format("%.2f", attempt.getPercentage())).append(Protocol.SUB_DELIMITER);
			response.append(attempt.getTimeTaken()).append(Protocol.SUB_DELIMITER);
			response.append(attempt.getAttemptDate());
		}

		sendMessage(response.toString());
	}

	private void handleDeleteQuiz(String[] parts) throws SQLException {
		if (currentUser == null || !currentUser.isTeacher()) {
			sendMessage(Protocol.ERROR + Protocol.DELIMITER + "Only teachers can delete quizzes");
			return;
		}

		int quizId = Integer.parseInt(parts[1]);
		boolean success = dao.deleteQuiz(quizId);

		if (success) {
			sendMessage(Protocol.QUIZ_DELETED + Protocol.DELIMITER + quizId);
		} else {
			sendMessage(Protocol.ERROR + Protocol.DELIMITER + "Failed to delete quiz");
		}
	}

	private void sendMessage(String message) {
		if (out != null) {
			out.println(message);
		}
	}

	private void closeConnection() {
		try {
			if (in != null) in.close();
			if (out != null) out.close();
			if (clientSocket != null) clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

package server;

import model.Question;
import model.Quiz;
import model.QuizAttempt;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO {
	public User authenticateUser(String username, String password) throws SQLException {
		String query = "SELECT * FROM users WHERE username = ? AND password = ?";
		try (Connection conn = DatabaseConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, username);
			stmt.setString(2, password);

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return new User(
					rs.getInt("user_id"),
					rs.getString("username"),
					rs.getString("full_name"),
					rs.getString("role")
				);
			}
		}
		return null;
	}

	public boolean registerUser(User user) throws SQLException {
		String query = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
		try (Connection conn = DatabaseConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, user.getUsername());
			stmt.setString(2, user.getPassword());
			stmt.setString(3, user.getFullName());
			stmt.setString(4, user.getRole());

			return stmt.executeUpdate() > 0;
		}
	}

	public int createQuiz(Quiz quiz) throws SQLException {
		String quizQuery = "INSERT INTO quizzes (quiz_name, teacher_id, total_questions, time_limit) VALUES (?, ?, ?, ?)";
		try (Connection conn = DatabaseConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(quizQuery, Statement.RETURN_GENERATED_KEYS)) {

			stmt.setString(1, quiz.getQuizName());
			stmt.setInt(2, quiz.getTeacherId());
			stmt.setInt(3, quiz.getTotalQuestions());
			stmt.setInt(4, quiz.getTimeLimit());

			int affectedRows = stmt.executeUpdate();
			if (affectedRows > 0) {
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					int quizId = rs.getInt(1);

					String questionQuery = "INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer, question_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
					try (PreparedStatement qStmt = conn.prepareStatement(questionQuery)) {
						for (int i = 0; i < quiz.getQuestions().size(); i++) {
							Question q = quiz.getQuestions().get(i);
							qStmt.setInt(1, quizId);
							qStmt.setString(2, q.getQuestionText());
							qStmt.setString(3, q.getOptionA());
							qStmt.setString(4, q.getOptionB());
							qStmt.setString(5, q.getOptionC());
							qStmt.setString(6, q.getOptionD());
							qStmt.setString(7, q.getCorrectAnswer());
							qStmt.setInt(8, i + 1);
							qStmt.executeUpdate();
						}
					}
					return quizId;
				}
			}
		}
		return -1;
	}

	public List<Quiz> getActiveQuizzes() throws SQLException {
		List<Quiz> quizzes = new ArrayList<>();
		String query = "SELECT q.*, u.full_name as teacher_name FROM quizzes q " +
			"JOIN users u ON q.teacher_id = u.user_id WHERE q.is_active = TRUE " +
			"ORDER BY q.created_at DESC";

		try (Connection conn = DatabaseConnection.getConnection();
			 Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery(query)) {

			while (rs.next()) {
				Quiz quiz = new Quiz();
				quiz.setQuizId(rs.getInt("quiz_id"));
				quiz.setQuizName(rs.getString("quiz_name"));
				quiz.setTeacherId(rs.getInt("teacher_id"));
				quiz.setTeacherName(rs.getString("teacher_name"));
				quiz.setTotalQuestions(rs.getInt("total_questions"));
				quiz.setTimeLimit(rs.getInt("time_limit"));
				quiz.setActive(rs.getBoolean("is_active"));
				quizzes.add(quiz);
			}
		}
		return quizzes;
	}

	public Quiz getQuizWithQuestions(int quizId) throws SQLException {
		Quiz quiz = null;
		String quizQuery = "SELECT q.*, u.full_name as teacher_name FROM quizzes q " +
			"JOIN users u ON q.teacher_id = u.user_id WHERE q.quiz_id = ?";

		try (Connection conn = DatabaseConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(quizQuery)) {

			stmt.setInt(1, quizId);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				quiz = new Quiz();
				quiz.setQuizId(rs.getInt("quiz_id"));
				quiz.setQuizName(rs.getString("quiz_name"));
				quiz.setTeacherId(rs.getInt("teacher_id"));
				quiz.setTeacherName(rs.getString("teacher_name"));
				quiz.setTotalQuestions(rs.getInt("total_questions"));
				quiz.setTimeLimit(rs.getInt("time_limit"));
				quiz.setActive(rs.getBoolean("is_active"));

				String questionQuery = "SELECT * FROM questions WHERE quiz_id = ? ORDER BY question_order";
				try (PreparedStatement qStmt = conn.prepareStatement(questionQuery)) {
					qStmt.setInt(1, quizId);
					ResultSet qRs = qStmt.executeQuery();

					while (qRs.next()) {
						Question question = new Question();
						question.setQuestionId(qRs.getInt("question_id"));
						question.setQuizId(qRs.getInt("quiz_id"));
						question.setQuestionText(qRs.getString("question_text"));
						question.setOptionA(qRs.getString("option_a"));
						question.setOptionB(qRs.getString("option_b"));
						question.setOptionC(qRs.getString("option_c"));
						question.setOptionD(qRs.getString("option_d"));
						question.setCorrectAnswer(qRs.getString("correct_answer"));
						question.setQuestionOrder(qRs.getInt("question_order"));
						quiz.addQuestion(question);
					}
				}
			}
		}
		return quiz;
	}

	public boolean deleteQuiz(int quizId) throws SQLException {
		String query = "DELETE FROM quizzes WHERE quiz_id = ?";
		try (Connection conn = DatabaseConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, quizId);
			return stmt.executeUpdate() > 0;
		}
	}

	public int saveQuizAttempt(QuizAttempt attempt) throws SQLException {
		String query = "INSERT INTO quiz_attempts (quiz_id, student_id, score, total_questions, percentage, time_taken) VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection conn = DatabaseConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

			System.out.println("DEBUG: Preparing to save QuizAttempt");
			System.out.println("  quizId=" + attempt.getQuizId());
			System.out.println("  studentId=" + attempt.getStudentId());
			System.out.println("  score=" + attempt.getScore());
			System.out.println("  totalQuestions=" + attempt.getTotalQuestions());
			System.out.println("  percentage=" + attempt.getPercentage());
			System.out.println("  timeTaken=" + attempt.getTimeTaken());

			stmt.setInt(1, attempt.getQuizId());
			stmt.setInt(2, attempt.getStudentId());
			stmt.setInt(3, attempt.getScore());
			stmt.setInt(4, attempt.getTotalQuestions());
			stmt.setDouble(5, attempt.getPercentage());
			stmt.setInt(6, attempt.getTimeTaken());

			System.out.println("DEBUG: Executing INSERT statement...");
			int affectedRows = stmt.executeUpdate();
			System.out.println("DEBUG: Affected rows = " + affectedRows);

			if (affectedRows > 0) {
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					int attemptId = rs.getInt(1);
					System.out.println("DEBUG: Successfully saved QuizAttempt with ID = " + attemptId);
					return attemptId;
				}
			} else {
				System.out.println("DEBUG: Insert failed - no rows affected");
			}
		} catch (SQLException e) {
			System.out.println("ERROR: SQLException in saveQuizAttempt: " + e.getMessage());
			System.out.println("Error Code: " + e.getErrorCode());
			System.out.println("SQL State: " + e.getSQLState());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			System.out.println("ERROR: Unexpected exception in saveQuizAttempt: " + e.getMessage());
			e.printStackTrace();
			throw new SQLException("Failed to save quiz attempt: " + e.getMessage(), e);
		}
		return -1;
	}

	public List<QuizAttempt> getLeaderboard(int quizId) throws SQLException {
		List<QuizAttempt> leaderboard = new ArrayList<>();
		String query = "SELECT qa.*, u.full_name as student_name, q.quiz_name " +
			"FROM quiz_attempts qa " +
			"JOIN users u ON qa.student_id = u.user_id " +
			"JOIN quizzes q ON qa.quiz_id = q.quiz_id " +
			"WHERE qa.quiz_id = ? AND qa.attempt_id IN ( " +
			"  SELECT attempt_id FROM ( " +
			"    SELECT attempt_id, ROW_NUMBER() OVER ( " +
			"      PARTITION BY student_id " +
			"      ORDER BY score DESC, time_taken ASC " +
			"    ) as rn " +
			"    FROM quiz_attempts " +
			"    WHERE quiz_id = ? " +
			"  ) ranked WHERE rn = 1 " +
			") " +
			"ORDER BY qa.score DESC, qa.time_taken ASC " +
			"LIMIT 10";

		try (Connection conn = DatabaseConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setInt(1, quizId);
			stmt.setInt(2, quizId);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				QuizAttempt attempt = new QuizAttempt();
				attempt.setAttemptId(rs.getInt("attempt_id"));
				attempt.setQuizId(rs.getInt("quiz_id"));
				attempt.setStudentId(rs.getInt("student_id"));
				attempt.setStudentName(rs.getString("student_name"));
				attempt.setQuizName(rs.getString("quiz_name"));
				attempt.setScore(rs.getInt("score"));
				attempt.setTotalQuestions(rs.getInt("total_questions"));
				attempt.setPercentage(rs.getDouble("percentage"));
				attempt.setTimeTaken(rs.getInt("time_taken"));
				attempt.setAttemptDate(rs.getTimestamp("attempt_date"));
				leaderboard.add(attempt);
			}
		}
		return leaderboard;
	}

	public List<QuizAttempt> getStudentResults(int studentId) throws SQLException {
		List<QuizAttempt> results = new ArrayList<>();
		String query = "SELECT qa.*, q.quiz_name " +
			"FROM quiz_attempts qa " +
			"JOIN quizzes q ON qa.quiz_id = q.quiz_id " +
			"WHERE qa.student_id = ? " +
			"ORDER BY qa.attempt_date DESC";

		try (Connection conn = DatabaseConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setInt(1, studentId);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				QuizAttempt attempt = new QuizAttempt();
				attempt.setAttemptId(rs.getInt("attempt_id"));
				attempt.setQuizId(rs.getInt("quiz_id"));
				attempt.setStudentId(rs.getInt("student_id"));
				attempt.setQuizName(rs.getString("quiz_name"));
				attempt.setScore(rs.getInt("score"));
				attempt.setTotalQuestions(rs.getInt("total_questions"));
				attempt.setPercentage(rs.getDouble("percentage"));
				attempt.setTimeTaken(rs.getInt("time_taken"));
				attempt.setAttemptDate(rs.getTimestamp("attempt_date"));
				results.add(attempt);
			}
		}
		return results;
	}
}

# Client-Server Quiz System

A comprehensive quiz application built with Java and MySQL featuring real-time quiz taking, automatic grading, live leaderboards, and an admin dashboard for teachers.

## Features

### Core Features
- ✅ **User Authentication**: Separate login for teachers and students
- ✅ **Teacher Dashboard**: Create, manage, and delete quizzes
- ✅ **Student Interface**: Browse and take available quizzes  
- ✅ **Automatic Grading**: Instant scoring and feedback
- ✅ **Results Storage**: All quiz attempts saved to database

### Cool Add-ons
- ⏱️ **Timer Per Question**: Configurable time limits for each question
- 🏆 **Live Leaderboard**: Real-time ranking of top 10 performers
- 📊 **Admin Dashboard**: View quiz statistics and student performance
- 📈 **Student History**: Track all past quiz attempts

## Technology Stack

- **Backend**: Java (Socket Programming)
- **Frontend**: Java Swing (GUI)
- **Database**: MySQL
- **Architecture**: Client-Server Model

## Project Structure

```
Client-server application/
├── database/
│   └── quiz_schema.sql          # Database schema and sample data
├── src/
│   ├── common/
│   │   └── Protocol.java         # Communication protocol
│   ├── model/
│   │   ├── User.java            # User model
│   │   ├── Quiz.java            # Quiz model
│   │   ├── Question.java        # Question model
│   │   └── QuizAttempt.java     # Quiz attempt model
│   ├── server/
│   │   ├── DatabaseConnection.java
│   │   ├── QuizDAO.java         # Data access layer
│   │   ├── ClientHandler.java   # Handle client connections
│   │   └── QuizServer.java      # Main server
│   └── client/
│       ├── ServerConnection.java
│       ├── LoginGUI.java
│       ├── StudentDashboard.java
│       ├── TeacherDashboard.java
│       ├── QuizTakingGUI.java
│       ├── QuizResultGUI.java
│       ├── StudentResultsGUI.java
│       ├── CreateQuizGUI.java
│       └── TeacherLeaderboardGUI.java
├── lib/
│   └── mysql-connector-j-8.0.33.jar
└── README.md
```

## Prerequisites

1. **Java Development Kit (JDK)** 11 or higher
   - Download from: https://www.oracle.com/java/technologies/downloads/

2. **MySQL Server** 8.0 or higher
   - Download from: https://dev.mysql.com/downloads/mysql/

3. **MySQL JDBC Driver** (mysql-connector-j-8.0.33.jar)
   - Download from: https://dev.mysql.com/downloads/connector/j/

## Installation & Setup

### Step 1: Database Setup

1. Start MySQL server
2. Open MySQL command line or MySQL Workbench
3. Run the database schema:
   ```bash
   mysql -u root -p < database/quiz_schema.sql
   ```
   Or manually execute the SQL file in MySQL Workbench

4. Verify database creation:
   ```sql
   USE quiz_system;
   SHOW TABLES;
   ```

### Step 2: Configure Database Connection

Edit `src/server/DatabaseConnection.java` and update:
```java
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password";
```

### Step 3: Download MySQL JDBC Driver

1. Download MySQL Connector/J from https://dev.mysql.com/downloads/connector/j/
2. Extract and copy `mysql-connector-j-8.0.33.jar` to the `lib/` directory

### Step 4: Compile the Project

#### Windows (PowerShell):
```powershell
# Compile all Java files
javac -d bin -cp "lib/mysql-connector-j-8.0.33.jar" src/common/*.java src/model/*.java src/server/*.java src/client/*.java
```

#### Linux/Mac:
```bash
# Compile all Java files
javac -d bin -cp "lib/mysql-connector-j-8.0.33.jar" src/common/*.java src/model/*.java src/server/*.java src/client/*.java
```

### Step 5: Run the Application

#### Start the Server:
```powershell
# Windows
java -cp "bin;lib/mysql-connector-j-8.0.33.jar" server.QuizServer

# Linux/Mac
java -cp "bin:lib/mysql-connector-j-8.0.33.jar" server.QuizServer
```

#### Start the Client:
```powershell
# Windows (Open new terminal)
java -cp "bin;lib/mysql-connector-j-8.0.33.jar" client.LoginGUI

# Linux/Mac
java -cp "bin:lib/mysql-connector-j-8.0.33.jar" client.LoginGUI
```

## Default Login Credentials

### Teacher Account:
- Username: `teacher`
- Password: `teacher123`

### Student Accounts:
- Username: `student1`, Password: `pass123`
- Username: `student2`, Password: `pass123`
- Username: `student3`, Password: `pass123`

## Usage Guide

### For Teachers:

1. **Login** with teacher credentials
2. **Create Quiz**:
   - Click "Create New Quiz"
   - Enter quiz name and time limit per question
   - Add questions with 4 options each
   - Mark the correct answer
   - Click "Create Quiz"

3. **View Leaderboard**:
   - Select a quiz from the list
   - Click "View Leaderboard"
   - See top 10 performers with scores and time taken

4. **Delete Quiz**:
   - Select a quiz
   - Click "Delete Quiz"
   - Confirm deletion

### For Students:

1. **Login** with student credentials
2. **Take Quiz**:
   - Browse available quizzes
   - Click "Take Quiz"
   - Answer questions within time limit
   - Submit quiz to see results

3. **View Results**:
   - Click "View My Results"
   - See all past quiz attempts with scores and percentages

4. **Check Leaderboard**:
   - After completing a quiz, view the leaderboard
   - See your ranking among other students

## Features Detail

### Timer System
- Each question has a configurable time limit (10-300 seconds)
- Visual countdown timer with color warning (red when < 5 seconds)
- Auto-submit when time expires

### Leaderboard
- Real-time ranking based on score and time taken
- Top 3 highlighted with gold, silver, bronze
- Shows score, percentage, and time for each attempt

### Admin Dashboard
- Create and manage quizzes
- View all quizzes with statistics
- Delete quizzes and associated attempts
- Monitor student performance via leaderboards

## Database Schema

### Tables:
- **users**: Store teacher and student accounts
- **quizzes**: Quiz information and settings
- **questions**: Quiz questions with options and correct answers
- **quiz_attempts**: Student quiz submissions and scores
- **student_answers**: Detailed answer tracking

## Communication Protocol

The system uses a custom text-based protocol over TCP sockets:
- **Delimiter**: `|||` (separates message parts)
- **Sub-delimiter**: `:::` (separates sub-parts)

### Example Messages:
- Login: `LOGIN|||username|||password`
- Start Quiz: `START_QUIZ|||quizId`
- Submit Quiz: `FINISH_QUIZ|||quizId|||score|||total|||timeTaken`

## Troubleshooting

### "Cannot connect to server"
- Ensure the server is running first
- Check if port 9999 is available
- Verify firewall settings

### "Database connection failed"
- Verify MySQL is running
- Check database credentials in `DatabaseConnection.java`
- Ensure `quiz_system` database exists

### "ClassNotFoundException: com.mysql.cj.jdbc.Driver"
- Download MySQL Connector/J
- Place jar file in `lib/` directory
- Include in classpath when compiling and running

### "Port already in use"
- Change PORT in `QuizServer.java` (default: 9999)
- Close other applications using the port

## Future Enhancements

- 🔐 Password encryption (bcrypt/hash)
- 📝 Question categories and difficulty levels
- 📊 Advanced analytics and reports
- 🖼️ Support for images in questions
- 📱 Mobile client support
- 🌐 Web-based interface
- 💾 Export results to PDF/Excel

## Contributing

Feel free to fork this project and submit pull requests for any improvements.

## License

This project is open source and available for educational purposes.

## Contact

For questions or support, please open an issue in the repository.

---

**Developed with ❤️ using Java and MySQL**

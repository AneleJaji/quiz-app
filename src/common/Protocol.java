package common;

/**
 * Protocol class defining message types for client-server communication
 */
public class Protocol {
    // Message types
    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String REGISTER = "REGISTER";
    public static final String REGISTER_SUCCESS = "REGISTER_SUCCESS";
    public static final String REGISTER_FAILED = "REGISTER_FAILED";
    
    public static final String GET_ACTIVE_QUIZZES = "GET_ACTIVE_QUIZZES";
    public static final String QUIZ_LIST = "QUIZ_LIST";
    
    public static final String CREATE_QUIZ = "CREATE_QUIZ";
    public static final String QUIZ_CREATED = "QUIZ_CREATED";
    
    public static final String START_QUIZ = "START_QUIZ";
    public static final String QUIZ_DATA = "QUIZ_DATA";
    
    public static final String SUBMIT_ANSWER = "SUBMIT_ANSWER";
    public static final String ANSWER_RECEIVED = "ANSWER_RECEIVED";
    
    public static final String FINISH_QUIZ = "FINISH_QUIZ";
    public static final String QUIZ_RESULT = "QUIZ_RESULT";
    
    public static final String GET_LEADERBOARD = "GET_LEADERBOARD";
    public static final String LEADERBOARD_DATA = "LEADERBOARD_DATA";
    
    public static final String GET_MY_RESULTS = "GET_MY_RESULTS";
    public static final String RESULTS_DATA = "RESULTS_DATA";
    
    public static final String DELETE_QUIZ = "DELETE_QUIZ";
    public static final String QUIZ_DELETED = "QUIZ_DELETED";
    
    public static final String ERROR = "ERROR";
    public static final String DISCONNECT = "DISCONNECT";
    
    // Delimiter for message parts
    public static final String DELIMITER = "|||";
    public static final String SUB_DELIMITER = ":::";
}

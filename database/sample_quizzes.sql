USE quiz_system;

-- Sample quiz 1: Java Basics
INSERT INTO quizzes (quiz_name, teacher_id, total_questions, time_limit)
VALUES ('Java Basics', 1, 5, 30);

SET @java_quiz_id = LAST_INSERT_ID();

INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer, question_order)
VALUES
(@java_quiz_id, 'Which keyword is used to inherit a class in Java?', 'this', 'extends', 'implements', 'super', 'B', 1),
(@java_quiz_id, 'Which method is the entry point of a Java program?', 'start()', 'run()', 'main()', 'init()', 'C', 2),
(@java_quiz_id, 'Which of these is not a primitive type?', 'int', 'boolean', 'String', 'double', 'C', 3),
(@java_quiz_id, 'Which keyword is used to create an object?', 'new', 'class', 'void', 'static', 'A', 4),
(@java_quiz_id, 'Which package contains ArrayList?', 'java.util', 'java.io', 'java.net', 'java.lang', 'A', 5);

-- Sample quiz 2: SQL Basics
INSERT INTO quizzes (quiz_name, teacher_id, total_questions, time_limit)
VALUES ('SQL Basics', 1, 5, 25);

SET @sql_quiz_id = LAST_INSERT_ID();

INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer, question_order)
VALUES
(@sql_quiz_id, 'Which SQL statement is used to retrieve data?', 'INSERT', 'SELECT', 'UPDATE', 'DELETE', 'B', 1),
(@sql_quiz_id, 'Which clause filters rows in SQL?', 'ORDER BY', 'GROUP BY', 'WHERE', 'HAVING', 'C', 2),
(@sql_quiz_id, 'Which keyword sorts results ascending by default?', 'SORT', 'ORDER BY', 'GROUP BY', 'FILTER', 'B', 3),
(@sql_quiz_id, 'Which SQL statement adds new rows?', 'INSERT', 'SELECT', 'UPDATE', 'DROP', 'A', 4),
(@sql_quiz_id, 'Which statement removes a table?', 'DELETE', 'DROP', 'TRUNCATE', 'REMOVE', 'B', 5);

-- Sample quiz 3: Networking Basics
INSERT INTO quizzes (quiz_name, teacher_id, total_questions, time_limit)
VALUES ('Networking Basics', 1, 5, 20);

SET @net_quiz_id = LAST_INSERT_ID();

INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer, question_order)
VALUES
(@net_quiz_id, 'Which protocol is used for reliable transport?', 'UDP', 'TCP', 'IP', 'ICMP', 'B', 1),
(@net_quiz_id, 'Which port is used by HTTP?', '21', '22', '80', '443', 'C', 2),
(@net_quiz_id, 'What does DNS stand for?', 'Data Name System', 'Domain Name System', 'Digital Network Service', 'Domain Network Service', 'B', 3),
(@net_quiz_id, 'Which device routes traffic between networks?', 'Switch', 'Router', 'Hub', 'Bridge', 'B', 4),
(@net_quiz_id, 'Which IP version uses 128-bit addresses?', 'IPv4', 'IPv5', 'IPv6', 'IPv7', 'C', 5);

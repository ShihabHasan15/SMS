package student.management.system.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton DatabaseConnection class using JDBC + MySQL.
 *
 * ── Configuration ──────────────────────────────────────────────────────────
 *  Edit the four constants below to match your MySQL server before running.
 * ───────────────────────────────────────────────────────────────────────────
 */
public class DatabaseConnection {

    // ── MySQL connection settings — CHANGE THESE ───────────────
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "student_management";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";   // ← set your MySQL password

    // ── Full JDBC URL (auto-built from the constants above) ────
    private static final String DB_URL = String.format(
    "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        DB_HOST, DB_PORT, DB_NAME
    );

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");      // MySQL Connector/J 8+
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            this.connection.setAutoCommit(true);
            initializeSchema();
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "MySQL JDBC driver not found.\n" +
                "Place mysql-connector-j-*.jar in the lib/ folder and add it to the classpath.", e);
        }
    }

    /** Returns the single shared instance (thread-safe). */
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Creates the database schema on first run.
     * All tables use MySQL syntax: AUTO_INCREMENT, DATETIME, DOUBLE.
     */
    private void initializeSchema() throws SQLException {

        String createStudents = """
            CREATE TABLE IF NOT EXISTS students (
                id          INT           NOT NULL AUTO_INCREMENT,
                student_id  VARCHAR(20)   NOT NULL UNIQUE,
                first_name  VARCHAR(50)   NOT NULL,
                last_name   VARCHAR(50)   NOT NULL,
                email       VARCHAR(100)  NOT NULL UNIQUE,
                phone       VARCHAR(20),
                department  VARCHAR(100)  NOT NULL,
                year        INT           NOT NULL,
                gpa         DOUBLE        DEFAULT 0.0,
                created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;

        String createCourses = """
            CREATE TABLE IF NOT EXISTS courses (
                id          INT           NOT NULL AUTO_INCREMENT,
                course_code VARCHAR(20)   NOT NULL UNIQUE,
                course_name VARCHAR(150)  NOT NULL,
                credits     INT           NOT NULL,
                department  VARCHAR(100)  NOT NULL,
                instructor  VARCHAR(100),
                PRIMARY KEY (id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;

        String createEnrollments = """
            CREATE TABLE IF NOT EXISTS enrollments (
                id         INT          NOT NULL AUTO_INCREMENT,
                student_id INT          NOT NULL,
                course_id  INT          NOT NULL,
                grade      VARCHAR(5)   DEFAULT 'N/A',
                semester   VARCHAR(30)  NOT NULL,
                PRIMARY KEY (id),
                UNIQUE KEY uq_enrollment (student_id, course_id, semester),
                CONSTRAINT fk_enroll_student FOREIGN KEY (student_id)
                    REFERENCES students(id) ON DELETE CASCADE,
                CONSTRAINT fk_enroll_course  FOREIGN KEY (course_id)
                    REFERENCES courses(id)  ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createStudents);
            stmt.execute(createCourses);
            stmt.execute(createEnrollments);
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}

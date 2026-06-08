package student.management.system.dao;

import student.management.system.db.DatabaseConnection;
import student.management.system.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the courses and enrollments tables.
 */
public class CourseDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── COURSES : CREATE ──────────────────────────────────────────

    public boolean addCourse(Course c) throws SQLException {
        String sql = """
            INSERT INTO courses (course_code, course_name, credits, department, instructor)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, c.getCourseCode());
            ps.setString(2, c.getCourseName());
            ps.setInt   (3, c.getCredits());
            ps.setString(4, c.getDepartment());
            ps.setString(5, c.getInstructor());
            return ps.executeUpdate() > 0;
        }
    }

    // ── COURSES : READ ────────────────────────────────────────────

    public List<Course> getAllCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY course_code";
        try (Statement stmt = getConn().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Course getCourseById(int id) throws SQLException {
        String sql = "SELECT * FROM courses WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // ── COURSES : DELETE ──────────────────────────────────────────

    public boolean deleteCourse(int id) throws SQLException {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ── ENROLLMENTS ───────────────────────────────────────────────

    public boolean enrollStudent(int studentDbId, int courseId,
                                 String semester) throws SQLException {
        String sql = """
            INSERT IGNORE INTO enrollments (student_id, course_id, semester)
            VALUES (?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt   (1, studentDbId);
            ps.setInt   (2, courseId);
            ps.setString(3, semester);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateGrade(int studentDbId, int courseId,
                               String semester, String grade) throws SQLException {
        String sql = """
            UPDATE enrollments SET grade = ?
            WHERE student_id = ? AND course_id = ? AND semester = ?
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, grade);
            ps.setInt   (2, studentDbId);
            ps.setInt   (3, courseId);
            ps.setString(4, semester);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Returns a list of Object[] rows:
     *   [courseCode, courseName, credits, instructor, semester, grade]
     */
    public List<Object[]> getEnrollmentsForStudent(int studentDbId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = """
            SELECT c.course_code, c.course_name, c.credits,
                   c.instructor, e.semester, e.grade
            FROM enrollments e
            JOIN courses c ON c.id = e.course_id
            WHERE e.student_id = ?
            ORDER BY e.semester, c.course_code
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, studentDbId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString(1), rs.getString(2),
                        rs.getInt   (3), rs.getString(4),
                        rs.getString(5), rs.getString(6)
                    });
                }
            }
        }
        return list;
    }

    // ── Helper ────────────────────────────────────────────────────

    private Course mapRow(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId         (rs.getInt   ("id"));
        c.setCourseCode (rs.getString("course_code"));
        c.setCourseName (rs.getString("course_name"));
        c.setCredits    (rs.getInt   ("credits"));
        c.setDepartment (rs.getString("department"));
        c.setInstructor (rs.getString("instructor"));
        return c;
    }
}

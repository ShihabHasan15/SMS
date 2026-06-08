package student.management.system.dao;

import student.management.system.db.DatabaseConnection;
import student.management.system.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the students table.
 * Every DB operation uses PreparedStatement to prevent SQL injection.
 */
public class StudentDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────

    public boolean addStudent(Student s) throws SQLException {
        String sql = """
            INSERT INTO students
              (student_id, first_name, last_name, email, phone, department, year, gpa)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, s.getStudentId());
            ps.setString(2, s.getFirstName());
            ps.setString(3, s.getLastName());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getPhone());
            ps.setString(6, s.getDepartment());
            ps.setInt   (7, s.getYear());
            ps.setDouble(8, s.getGpa());
            return ps.executeUpdate() > 0;
        }
    }

    // ── READ ──────────────────────────────────────────────────────

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY last_name, first_name";

        try (Statement stmt = getConn().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Student getStudentById(int id) throws SQLException {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Student getStudentByStudentId(String studentId) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Full-text search across student_id, first_name, last_name, email, department.
     */
    public List<Student> searchStudents(String keyword) throws SQLException {
        List<Student> list = new ArrayList<>();
        String pattern = "%" + keyword.toLowerCase() + "%";
        String sql = """
            SELECT * FROM students
            WHERE LOWER(student_id)  LIKE ?
               OR LOWER(first_name)  LIKE ?
               OR LOWER(last_name)   LIKE ?
               OR LOWER(email)       LIKE ?
               OR LOWER(department)  LIKE ?
            ORDER BY last_name, first_name
            """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) ps.setString(i, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int getTotalCount() throws SQLException {
        try (Statement stmt = getConn().createStatement();
             ResultSet rs   = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public double getAverageGpa() throws SQLException {
        try (Statement stmt = getConn().createStatement();
             ResultSet rs   = stmt.executeQuery("SELECT AVG(gpa) FROM students")) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────

    public boolean updateStudent(Student s) throws SQLException {
        String sql = """
            UPDATE students
            SET first_name = ?, last_name = ?, email = ?, phone = ?,
                department = ?, year = ?, gpa = ?
            WHERE id = ?
            """;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getPhone());
            ps.setString(5, s.getDepartment());
            ps.setInt   (6, s.getYear());
            ps.setDouble(7, s.getGpa());
            ps.setInt   (8, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // ── DELETE ────────────────────────────────────────────────────

    public boolean deleteStudent(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Helper ────────────────────────────────────────────────────

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId        (rs.getInt   ("id"));
        s.setStudentId (rs.getString("student_id"));
        s.setFirstName (rs.getString("first_name"));
        s.setLastName  (rs.getString("last_name"));
        s.setEmail     (rs.getString("email"));
        s.setPhone     (rs.getString("phone"));
        s.setDepartment(rs.getString("department"));
        s.setYear      (rs.getInt   ("year"));
        s.setGpa       (rs.getDouble("gpa"));
        s.setCreatedAt (rs.getString("created_at"));
        return s;
    }
}

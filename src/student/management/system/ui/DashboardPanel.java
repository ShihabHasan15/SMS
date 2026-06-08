package student.management.system.ui;

import student.management.system.dao.CourseDAO;
import student.management.system.dao.StudentDAO;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Summary dashboard shown on the Home tab.
 */
public class DashboardPanel extends JPanel {

    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO  courseDAO  = new CourseDAO();

    // Stat card labels
    private final JLabel lbStudents  = bigNumber("—");
    private final JLabel lbCourses   = bigNumber("—");
    private final JLabel lbAvgGpa    = bigNumber("—");
    private final JLabel lbDepts     = bigNumber("—");

    public DashboardPanel() {
        super(new BorderLayout(0, 0));
        setBackground(new Color(245, 247, 250));
        buildUI();
        refresh();
    }

    private void buildUI() {

        // ── Greeting banner ───────────────────────────────────────
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(67, 97, 238));
        banner.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel title = new JLabel("Student Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Welcome! Here's your academic snapshot.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(new Color(199, 210, 254));

        JPanel txt = new JPanel(new GridLayout(2, 1, 0, 4));
        txt.setOpaque(false);
        txt.add(title); txt.add(sub);
        banner.add(txt, BorderLayout.WEST);

        JButton btnRefresh = new JButton("↺  Refresh Stats");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setForeground(new Color(67, 97, 238));
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refresh());
        banner.add(btnRefresh, BorderLayout.EAST);

        add(banner, BorderLayout.NORTH);

        // ── Stat cards ────────────────────────────────────────────
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setBackground(new Color(245, 247, 250));
        cards.setBorder(new EmptyBorder(24, 24, 16, 24));

        cards.add(card("Total Students",  lbStudents, new Color(67,  97, 238), "👤"));
        cards.add(card("Total Courses",   lbCourses,  new Color(25, 135,  84), "📚"));
        cards.add(card("Average GPA",     lbAvgGpa,   new Color(255,  165,  0), "🎓"));
        cards.add(card("Departments",     lbDepts,    new Color(220,  53,  69), "🏫"));

        add(cards, BorderLayout.CENTER);

        // ── Info panel ────────────────────────────────────────────
        JPanel info = new JPanel(new BorderLayout());
        info.setBackground(Color.WHITE);
        info.setBorder(new CompoundBorder(
            new EmptyBorder(0, 24, 24, 24),
            new CompoundBorder(
                new LineBorder(new Color(229, 231, 235)),
                new EmptyBorder(16, 20, 16, 20))));

        JLabel hdr = new JLabel("Quick Tips");
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 14));
        hdr.setForeground(new Color(55, 65, 81));

        JTextArea tips = new JTextArea(
            "• Use the Students tab to add, edit, search and delete students.\n" +
            "• Use the Courses tab to manage the course catalogue and student enrollments.\n" +
            "• Click 'Refresh Stats' anytime to update the dashboard numbers.\n" +
            "• The database file (student_management.db) is created automatically in the project folder."
        );
        tips.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tips.setForeground(new Color(75, 85, 99));
        tips.setEditable(false);
        tips.setOpaque(false);
        tips.setLineWrap(true);
        tips.setWrapStyleWord(true);

        info.add(hdr,  BorderLayout.NORTH);
        info.add(tips, BorderLayout.CENTER);

        add(info, BorderLayout.SOUTH);
    }

    // ── Stat card builder ─────────────────────────────────────────

    private JPanel card(String title, JLabel numLabel, Color accent, String icon) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(
            new LineBorder(new Color(229, 231, 235)),
            new EmptyBorder(20, 20, 20, 20)));

        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        numLabel.setForeground(accent);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(107, 114, 128));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.add(ico);

        p.add(top,     BorderLayout.NORTH);
        p.add(numLabel, BorderLayout.CENTER);
        p.add(lbl,     BorderLayout.SOUTH);
        return p;
    }

    private static JLabel bigNumber(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 36));
        return l;
    }

    // ── Data ──────────────────────────────────────────────────────

    private void refresh() {
        try {
            lbStudents.setText(String.valueOf(studentDAO.getTotalCount()));
            lbAvgGpa  .setText(String.format("%.2f", studentDAO.getAverageGpa()));
            lbCourses .setText(String.valueOf(courseDAO.getAllCourses().size()));

            // count distinct departments
            List<student.management.system.model.Student> all = studentDAO.getAllStudents();
            long depts = all.stream()
                .map(student.management.system.model.Student::getDepartment)
                .distinct().count();
            lbDepts.setText(String.valueOf(depts));
        } catch (SQLException ex) {
            lbStudents.setText("ERR");
        }
    }
}

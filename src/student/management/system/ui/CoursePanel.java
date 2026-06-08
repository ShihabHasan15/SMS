package student.management.system.ui;

import student.management.system.dao.CourseDAO;
import student.management.system.dao.StudentDAO;
import student.management.system.model.Course;
import student.management.system.model.Student;
import student.management.system.ui.StudentPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for managing courses and viewing/assigning enrollments.
 */
public class CoursePanel extends JPanel {

    private final CourseDAO  courseDAO  = new CourseDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    // ── Course table ──────────────────────────────────────────────
    private final String[] COURSE_COLS = {
        "ID", "Code", "Course Name", "Credits", "Department", "Instructor"
    };
    private final DefaultTableModel courseModel =
        new DefaultTableModel(COURSE_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    private final JTable courseTable = new JTable(courseModel);

    // ── Enrollment table ──────────────────────────────────────────
    private final String[] ENROLL_COLS = {
        "Code", "Course Name", "Credits", "Instructor", "Semester", "Grade"
    };
    private final DefaultTableModel enrollModel =
        new DefaultTableModel(ENROLL_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    private final JTable enrollTable = new JTable(enrollModel);

    private final JComboBox<Student> cbStudents = new JComboBox<>();

    public CoursePanel() {
        super(new BorderLayout(0, 8));
        setBackground(StudentPanel.BG);
        buildUI();
        loadCourses();
        loadStudentCombo();
    }

    // ── Build UI ──────────────────────────────────────────────────

    private void buildUI() {

        // ── Split pane: courses (top) + enrollments (bottom) ───
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                buildCourseSection(), buildEnrollmentSection());
        split.setDividerLocation(300);
        split.setResizeWeight(0.5);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    // ── Course section ────────────────────────────────────────────

    private JPanel buildCourseSection() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(StudentPanel.BG);
        p.setBorder(new EmptyBorder(8, 8, 4, 8));

        JLabel lbl = sectionLabel("📚  Course Catalogue");
        p.add(lbl, BorderLayout.NORTH);

        styleTable(courseTable);
        JScrollPane scroll = new JScrollPane(courseTable);
        scroll.getViewport().setBackground(Color.WHITE);
        p.add(scroll, BorderLayout.CENTER);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        bar.setBackground(StudentPanel.BG);

        JButton btnAdd = accentBtn("＋ Add Course",    StudentPanel.ACCENT);
        JButton btnDel = accentBtn("✕ Remove",        StudentPanel.ACCENT_RED);
        btnAdd.addActionListener(e -> addCourse());
        btnDel.addActionListener(e -> deleteCourse());
        bar.add(btnDel);
        bar.add(btnAdd);
        p.add(bar, BorderLayout.SOUTH);

        return p;
    }

    // ── Enrollment section ────────────────────────────────────────

    private JPanel buildEnrollmentSection() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(StudentPanel.BG);
        p.setBorder(new EmptyBorder(4, 8, 8, 8));

        // header with student selector
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        header.setBackground(StudentPanel.BG);
        header.add(sectionLabel("🎓  Enrollments for: "));
        cbStudents.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbStudents.setPreferredSize(new Dimension(250, 28));
        cbStudents.addActionListener(e -> loadEnrollments());
        header.add(cbStudents);

        JButton btnEnroll = accentBtn("Enroll in Course", StudentPanel.ACCENT);
        btnEnroll.addActionListener(e -> enrollStudent());
        header.add(btnEnroll);

        JButton btnGrade = accentBtn("Set Grade", new Color(25, 135, 84));
        btnGrade.addActionListener(e -> setGrade());
        header.add(btnGrade);

        p.add(header, BorderLayout.NORTH);

        styleTable(enrollTable);
        JScrollPane scroll = new JScrollPane(enrollTable);
        scroll.getViewport().setBackground(Color.WHITE);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // ── Data ops ──────────────────────────────────────────────────

    private void loadCourses() {
        try {
            courseModel.setRowCount(0);
            for (Course c : courseDAO.getAllCourses()) {
                courseModel.addRow(new Object[]{
                    c.getId(), c.getCourseCode(), c.getCourseName(),
                    c.getCredits(), c.getDepartment(), c.getInstructor()
                });
            }
        } catch (SQLException ex) {
            err("Load courses failed: " + ex.getMessage());
        }
    }

    private void loadStudentCombo() {
        try {
            cbStudents.removeAllItems();
            for (Student s : studentDAO.getAllStudents()) cbStudents.addItem(s);
            loadEnrollments();
        } catch (SQLException ex) {
            err("Load students failed: " + ex.getMessage());
        }
    }

    private void loadEnrollments() {
        enrollModel.setRowCount(0);
        Student s = (Student) cbStudents.getSelectedItem();
        if (s == null) return;
        try {
            for (Object[] row : courseDAO.getEnrollmentsForStudent(s.getId())) {
                enrollModel.addRow(row);
            }
        } catch (SQLException ex) {
            err("Load enrollments failed: " + ex.getMessage());
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────

    private void addCourse() {
        JTextField code    = tf(); JTextField name  = tf();
        JTextField dept    = tf(); JTextField instr = tf();
        JSpinner   credits = new JSpinner(new SpinnerNumberModel(3, 1, 6, 1));

        JPanel frm = formPanel(new Object[][]{
            {"Course Code *:", code},   {"Course Name *:", name},
            {"Department *:",  dept},   {"Instructor:",    instr},
            {"Credits *:",     credits}
        });

        int ok = JOptionPane.showConfirmDialog(this, frm,
            "Add Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        if (code.getText().isBlank() || name.getText().isBlank() || dept.getText().isBlank()) {
            err("Fields marked * are required."); return;
        }
        try {
            courseDAO.addCourse(new Course(
                code.getText().trim(), name.getText().trim(),
                (Integer) credits.getValue(),
                dept.getText().trim(), instr.getText().trim()));
            loadCourses();
            ok("Course added!");
        } catch (SQLException ex) {
            err("Could not add course:\n" + ex.getMessage());
        }
    }

    private void deleteCourse() {
        int row = courseTable.getSelectedRow();
        if (row < 0) { info("Select a course first."); return; }
        int id   = (Integer) courseModel.getValueAt(row, 0);
        String c = (String)  courseModel.getValueAt(row, 2);
        int    r = JOptionPane.showConfirmDialog(this,
            "Delete course \"" + c + "\"?", "Confirm",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        try { courseDAO.deleteCourse(id); loadCourses(); }
        catch (SQLException ex) { err(ex.getMessage()); }
    }

    private void enrollStudent() {
        Student stu = (Student) cbStudents.getSelectedItem();
        if (stu == null) { info("No student selected."); return; }

        try {
            List<Course> courses = courseDAO.getAllCourses();
            if (courses.isEmpty()) { info("No courses available."); return; }

            JComboBox<Course>  cbCourse   = new JComboBox<>(courses.toArray(new Course[0]));
            JTextField         tfSemester = tf();
            tfSemester.setText("Fall 2024");

            JPanel frm = formPanel(new Object[][]{
                {"Course:",   cbCourse},
                {"Semester:", tfSemester}
            });
            int ok = JOptionPane.showConfirmDialog(this, frm,
                "Enroll " + stu.getFullName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) return;

            Course c = (Course) cbCourse.getSelectedItem();
            courseDAO.enrollStudent(stu.getId(), c.getId(), tfSemester.getText().trim());
            loadEnrollments();
            ok("Enrolled!");
        } catch (SQLException ex) {
            err("Enrollment failed:\n" + ex.getMessage());
        }
    }

    private void setGrade() {
        int row = enrollTable.getSelectedRow();
        if (row < 0) { info("Select an enrollment row first."); return; }

        Student stu = (Student) cbStudents.getSelectedItem();
        if (stu == null) return;

        String   code     = (String) enrollModel.getValueAt(row, 0);
        String   semester = (String) enrollModel.getValueAt(row, 4);
        String[] grades   = {"A+","A","A-","B+","B","B-","C+","C","C-","D","F","N/A"};
        JComboBox<String> cbGrade = new JComboBox<>(grades);
        cbGrade.setSelectedItem(enrollModel.getValueAt(row, 5));

        int ok = JOptionPane.showConfirmDialog(this,
            new Object[]{"Grade for " + code + " (" + semester + "):", cbGrade},
            "Set Grade", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            // find course id by code
            List<Course> all = courseDAO.getAllCourses();
            Course match = all.stream()
                .filter(c -> c.getCourseCode().equals(code))
                .findFirst().orElse(null);
            if (match == null) return;
            courseDAO.updateGrade(stu.getId(), match.getId(),
                semester, (String) cbGrade.getSelectedItem());
            loadEnrollments();
        } catch (SQLException ex) {
            err("Could not set grade:\n" + ex.getMessage());
        }
    }

    // ── Style helpers ─────────────────────────────────────────────

    private static void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(28);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(229, 231, 235));
        t.setSelectionBackground(new Color(199, 210, 254));
        t.setSelectionForeground(Color.BLACK);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(StudentPanel.TABLE_HDR);
        t.getTableHeader().setForeground(Color.WHITE);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, r, c);
                if (!sel) setBackground(r % 2 == 0 ? Color.WHITE : StudentPanel.ROW_ALT);
                return this;
            }
        });
    }

    private static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(StudentPanel.TABLE_HDR);
        l.setBorder(new EmptyBorder(4, 0, 4, 0));
        return l;
    }

    private static JButton accentBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(5, 12, 5, 12));
        return b;
    }

    private static JTextField tf() {
        JTextField f = new JTextField(18);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return f;
    }

    private static JPanel formPanel(Object[][] rows) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0; lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(5, 0, 5, 10);
        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 1; fc.fill = GridBagConstraints.HORIZONTAL;
        fc.insets = new Insets(5, 0, 5, 0); fc.weightx = 1;
        for (int i = 0; i < rows.length; i++) {
            lc.gridy = fc.gridy = i;
            p.add(new JLabel(rows[i][0].toString()), lc);
            p.add((Component) rows[i][1], fc);
        }
        return p;
    }

    private void err (String m) { JOptionPane.showMessageDialog(this, m, "Error",   JOptionPane.ERROR_MESSAGE); }
    private void ok  (String m) { JOptionPane.showMessageDialog(this, m, "Success", JOptionPane.INFORMATION_MESSAGE); }
    private void info(String m) { JOptionPane.showMessageDialog(this, m, "Info",    JOptionPane.INFORMATION_MESSAGE); }
}

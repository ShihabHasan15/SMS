package student.management.system.ui;

import student.management.system.dao.StudentDAO;
import student.management.system.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Full CRUD panel for students.
 * Layout: search bar → table → button toolbar
 */
public class StudentPanel extends JPanel {

    // ── Palette ──────────────────────────────────────────────────
    static final Color ACCENT     = new Color(67, 97, 238);
    static final Color ACCENT_RED = new Color(220, 53, 69);
    static final Color BG         = new Color(245, 247, 250);
    static final Color TABLE_HDR  = new Color(55, 65, 81);
    static final Color ROW_ALT    = new Color(237, 242, 255);

    // ── Data ─────────────────────────────────────────────────────
    private final StudentDAO dao = new StudentDAO();

    // ── Table ────────────────────────────────────────────────────
    private final String[] COLS = {
        "#", "Student ID", "First Name", "Last Name",
        "Email", "Phone", "Department", "Year", "GPA"
    };
    private final DefaultTableModel tableModel =
        new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    private final JTable table = new JTable(tableModel);

    // ── Toolbar widgets ───────────────────────────────────────────
    private final JTextField tfSearch = new JTextField(20);
    private final JLabel     lblStats = new JLabel();

    public StudentPanel() {
        super(new BorderLayout(0, 0));
        setBackground(BG);
        buildUI();
        loadAll();
    }

    // ── UI ────────────────────────────────────────────────────────

    private void buildUI() {

        // ── Top search bar ─────────────────────────────────────
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.setBackground(BG);
        top.setBorder(new EmptyBorder(8, 8, 0, 8));

        tfSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tfSearch.putClientProperty("JTextField.placeholderText", "Search students…");
        tfSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            new EmptyBorder(5, 10, 5, 10)));

        JButton btnSearch = iconButton("🔍 Search", ACCENT);
        btnSearch.addActionListener(e -> doSearch());
        tfSearch.addActionListener(e -> doSearch());

        JButton btnRefresh = iconButton("↺ Refresh", new Color(108, 117, 125));
        btnRefresh.addActionListener(e -> loadAll());

        lblStats.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStats.setForeground(new Color(107, 114, 128));

        top.add(tfSearch);
        top.add(btnSearch);
        top.add(btnRefresh);
        top.add(Box.createHorizontalStrut(20));
        top.add(lblStats);
        add(top, BorderLayout.NORTH);

        // ── Table ──────────────────────────────────────────────
        styleTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ── Button bar ─────────────────────────────────────────
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bar.setBackground(BG);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                        new Color(229, 231, 235)));

        JButton btnAdd    = iconButton("＋ Add Student",    ACCENT);
        JButton btnEdit   = iconButton("✎ Edit",           new Color(25, 135, 84));
        JButton btnDelete = iconButton("✕ Delete",         ACCENT_RED);
        JButton btnView   = iconButton("👁 View Details",  new Color(13, 110, 253));

        btnAdd   .addActionListener(e -> addStudent());
        btnEdit  .addActionListener(e -> editStudent());
        btnDelete.addActionListener(e -> deleteStudent());
        btnView  .addActionListener(e -> viewDetails());

        bar.add(btnView);
        bar.add(btnEdit);
        bar.add(btnDelete);
        bar.add(btnAdd);
        add(bar, BorderLayout.SOUTH);
    }

    private void styleTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(229, 231, 235));
        table.setSelectionBackground(new Color(199, 210, 254));
        table.setSelectionForeground(Color.BLACK);
        table.setFocusable(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(TABLE_HDR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);

        // stripe rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(
                        tbl, val, sel, foc, r, c);
                if (!sel) comp.setBackground(r % 2 == 0 ? Color.WHITE : ROW_ALT);
                setHorizontalAlignment(c == 0 || c == 7 || c == 8
                        ? SwingConstants.CENTER : SwingConstants.LEFT);
                return comp;
            }
        });

        // column widths
        int[] widths = {40, 90, 100, 100, 180, 110, 170, 50, 55};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    }

    // ── Data operations ───────────────────────────────────────────

    private void loadAll() {
        try {
            populate(dao.getAllStudents());
        } catch (SQLException ex) {
            showError("Failed to load students: " + ex.getMessage());
        }
    }

    private void doSearch() {
        String kw = tfSearch.getText().trim();
        try {
            List<Student> res = kw.isEmpty()
                    ? dao.getAllStudents()
                    : dao.searchStudents(kw);
            populate(res);
        } catch (SQLException ex) {
            showError("Search failed: " + ex.getMessage());
        }
    }

    private void populate(List<Student> students) {
        tableModel.setRowCount(0);
        for (Student s : students) {
            tableModel.addRow(new Object[]{
                s.getId(), s.getStudentId(), s.getFirstName(), s.getLastName(),
                s.getEmail(), s.getPhone(), s.getDepartment(), s.getYear(),
                String.format("%.2f", s.getGpa())
            });
        }
        updateStats();
    }

    private void updateStats() {
        try {
            int    total  = dao.getTotalCount();
            double avgGpa = dao.getAverageGpa();
            lblStats.setText(String.format(
                "Total: %d students  |  Avg GPA: %.2f", total, avgGpa));
        } catch (SQLException ignored) {}
    }

    // ── CRUD actions ──────────────────────────────────────────────

    private void addStudent() {
        StudentFormDialog dlg =
            new StudentFormDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        Student s = dlg.getResult();
        if (s == null) return;
        try {
            dao.addStudent(s);
            loadAll();
            showSuccess("Student added successfully!");
        } catch (SQLException ex) {
            showError("Could not add student:\n" + ex.getMessage());
        }
    }

    private void editStudent() {
        Student selected = getSelectedStudent();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a student to edit.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StudentFormDialog dlg =
            new StudentFormDialog((Frame) SwingUtilities.getWindowAncestor(this), selected);
        dlg.setVisible(true);
        Student updated = dlg.getResult();
        if (updated == null) return;
        updated.setId(selected.getId());
        try {
            dao.updateStudent(updated);
            loadAll();
            showSuccess("Student updated successfully!");
        } catch (SQLException ex) {
            showError("Could not update student:\n" + ex.getMessage());
        }
    }

    private void deleteStudent() {
        Student s = getSelectedStudent();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete " + s.getFullName() + "?\nThis action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            dao.deleteStudent(s.getId());
            loadAll();
            showSuccess("Student deleted.");
        } catch (SQLException ex) {
            showError("Could not delete student:\n" + ex.getMessage());
        }
    }

    private void viewDetails() {
        Student s = getSelectedStudent();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Please select a student.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String details = String.format("""
            ╔══════════════════════════════╗
               Student Details
            ╚══════════════════════════════╝
            ID        : %s
            Name      : %s
            Email     : %s
            Phone     : %s
            Department: %s
            Year      : %d
            GPA       : %.2f
            Registered: %s
            """,
            s.getStudentId(), s.getFullName(), s.getEmail(),
            s.getPhone() != null ? s.getPhone() : "—",
            s.getDepartment(), s.getYear(), s.getGpa(),
            s.getCreatedAt());
        JOptionPane.showMessageDialog(this, details,
            "Student: " + s.getFullName(), JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Student getSelectedStudent() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (Integer) tableModel.getValueAt(row, 0);
        try { return dao.getStudentById(id); }
        catch (SQLException ex) { showError(ex.getMessage()); return null; }
    }

    private static JButton iconButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(6, 14, 6, 14));
        return b;
    }

    private void showError  (String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE);
    }
    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}

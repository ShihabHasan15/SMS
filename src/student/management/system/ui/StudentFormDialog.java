package student.management.system.ui;

import student.management.system.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StudentFormDialog extends JDialog {

    private static final Color BG        = new Color(245, 247, 250);
    private static final Color ACCENT    = new Color(67, 97, 238);
    private static final Color LABEL_CLR = new Color(55, 65, 81);

    private final JTextField tfStudentId  = styledField();
    private final JTextField tfFirstName  = styledField();
    private final JTextField tfLastName   = styledField();
    private final JTextField tfEmail      = styledField();
    private final JTextField tfPhone      = styledField();
    private final JComboBox<String> cbDept;
    private final JTextField tfYear = styledField();
    private final JTextField tfGpa  = styledField();

    private Student result;   
    private final boolean editMode;


    public StudentFormDialog(Frame parent, Student existing) {
        super(parent, existing == null ? "Add New Student" : "Edit Student", true);
        this.editMode = existing != null;

        String[] depts = {
            "Computer Science", "Electrical Engineering",
            "Mechanical Engineering", "Civil Engineering",
            "Business Administration", "Mathematics", "Physics", "Chemistry"
        };
        cbDept = new JComboBox<>(depts);
        cbDept.setFont(new Font("Segoe UI", Font.PLAIN, 13));

//        spYear = new JSpinner(new SpinnerNumberModel(
//        Integer.valueOf(1),   
//        Integer.valueOf(1),   
//        Integer.valueOf(6),   
//        Integer.valueOf(1)    
//        ));
//        spGpa = new JSpinner(new SpinnerNumberModel(
//        Double.valueOf(0.00),
//        Double.valueOf(0.00),
//        Double.valueOf(4.00),
//        Double.valueOf(0.01)
//        ));
//        ((JSpinner.DefaultEditor) spGpa.getEditor()).getTextField()
//                .setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(
//                        new javax.swing.text.NumberFormatter(
//                                new java.text.DecimalFormat("0.00"))));

        if (existing != null) prefill(existing);

        buildUI();
        pack();
        setMinimumSize(new Dimension(480, 520));
        setLocationRelativeTo(parent);
    }


    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(ACCENT);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel(editMode ? "Edit Student Details" : "Register New Student");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(title);
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        form.setBorder(new EmptyBorder(20, 24, 10, 24));
        GridBagConstraints lc = labelConstraints();
        GridBagConstraints fc = fieldConstraints();

        int row = 0;
        addRow(form, lc, fc, row++, "Student ID *",  tfStudentId);
        tfStudentId.setEditable(!editMode);          // immutable when editing
        addRow(form, lc, fc, row++, "First Name *",  tfFirstName);
        addRow(form, lc, fc, row++, "Last Name *",   tfLastName);
        addRow(form, lc, fc, row++, "Email *",       tfEmail);
        addRow(form, lc, fc, row++, "Phone",         tfPhone);
        addRow(form, lc, fc, row++, "Department *",  cbDept);
        addRow(form, lc, fc, row++, "Year *", tfYear);
        addRow(form, lc, fc, row++, "GPA", tfGpa);

        root.add(form, BorderLayout.CENTER);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bar.setBackground(BG);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = accentButton(editMode ? "Update" : "Save Student");
        btnSave.addActionListener(e -> onSave());

        bar.add(btnCancel);
        bar.add(btnSave);
        root.add(bar, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(btnSave);
    }

    private void addRow(JPanel p, GridBagConstraints lc,
                        GridBagConstraints fc, int row,
                        String label, JComponent comp) {
        lc.gridy = fc.gridy = row;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(LABEL_CLR);
        p.add(lbl, lc);
        p.add(comp, fc);
    }


    private void onSave() {
        String sid  = tfStudentId.getText().trim();
        String fn   = tfFirstName.getText().trim();
        String ln   = tfLastName.getText().trim();
        String em   = tfEmail.getText().trim();
        String ph   = tfPhone.getText().trim();
        String dept = (String) cbDept.getSelectedItem();
        int year;
        double gpa;

        try {
           
            year = Integer.parseInt(tfYear.getText().trim());
            gpa = Double.parseDouble(tfGpa.getText().trim());
         
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
            "Year and GPA must be valid numbers.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        return;
        }

        if (gpa < 0.0 || gpa > 4.0) {
                JOptionPane.showMessageDialog(this,
                "GPA must be between 0.00 and 4.00.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
            }
        
        
        if (sid.isEmpty() || fn.isEmpty() || ln.isEmpty() || em.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Fields marked with * are required.", "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!em.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid email address.", "Invalid Email",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        result = new Student(sid, fn, ln, em, ph, dept, year, gpa);
        dispose();
    }


    private void prefill(Student s) {
        tfStudentId.setText(s.getStudentId());
        tfFirstName.setText(s.getFirstName());
        tfLastName .setText(s.getLastName());
        tfEmail    .setText(s.getEmail());
        tfPhone    .setText(s.getPhone() != null ? s.getPhone() : "");
        cbDept     .setSelectedItem(s.getDepartment());
        tfYear.setText(String.valueOf(s.getYear()));
        tfGpa.setText(String.valueOf(s.getGpa()));
    }

    public Student getResult() { return result; }


    private static JTextField styledField() {
        JTextField tf = new JTextField(20);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            new EmptyBorder(4, 8, 4, 8)));
        return tf;
    }

    private static JButton accentButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static GridBagConstraints labelConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx  = 0; c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(6, 0, 6, 14);
        c.weightx = 0;
        return c;
    }

    private static GridBagConstraints fieldConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx  = 1; c.fill  = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 6, 0);
        c.weightx = 1;
        return c;
    }
}

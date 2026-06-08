package student.management.system.ui;

import student.management.system.db.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * Root JFrame — a tabbed main window containing the Dashboard,
 * Students, and Courses panels.
 */
public class MainFrame extends JFrame {

    private static final Color ACCENT = new Color(67, 97, 238);
    private static final Color BG     = new Color(245, 247, 250);
    private static final Color SIDEBAR_BG    = new Color(20, 83, 45);  
    private static final Color SIDEBAR_HOVER = new Color(22, 101, 52); 

    public MainFrame() {
        super("Student Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));
        setPreferredSize(new Dimension(1200, 750));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try { DatabaseConnection.getInstance().closeConnection(); }
                catch (SQLException ignored) {}
            }
        });

        buildUI();
        pack();
        setLocationRelativeTo(null);   // center on screen
    }

    private void buildUI() {
        // ── Sidebar nav  ──────────────────────────────────────────
        // We use a JTabbedPane styled to look like a modern sidebar.
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setBackground(SIDEBAR_BG); 
        tabs.setForeground(Color.BLACK);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // custom tab appearance
        UIManager.put("TabbedPane.selected",             ACCENT);
        UIManager.put("TabbedPane.tabAreaBackground",    SIDEBAR_BG);
        UIManager.put("TabbedPane.unselectedBackground", SIDEBAR_HOVER);

        tabs.addTab("  🏠  Dashboard  ",  null, new DashboardPanel(), "System overview");
        tabs.addTab("  👤  Students   ",  null, new StudentPanel(),   "Manage students");
        tabs.addTab("  📚  Courses    ",  null, new CoursePanel(),    "Manage courses & enrollments");

        // fix tab width
        tabs.setPreferredSize(new Dimension(1200, 720));

        // ── Status bar ────────────────────────────────────────────
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(30, 41, 59));
        statusBar.setBorder(new EmptyBorder(4, 14, 4, 14));

        JLabel status = new JLabel("✔  Connected to SQLite database");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        status.setForeground(new Color(134, 239, 172));

        JLabel version = new JLabel("SMS v1.0  |  Java " +
                System.getProperty("java.version"));
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(new Color(148, 163, 184));

        statusBar.add(status,  BorderLayout.WEST);
        statusBar.add(version, BorderLayout.EAST);

        // ── Compose ───────────────────────────────────────────────
        setLayout(new BorderLayout());
        add(tabs,      BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    // ── Entry point ───────────────────────────────────────────────

    public static void main(String[] args) {
        // Use system look-and-feel for native widgets
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // fallback to default
        }

        // Verify JDBC connection before opening the window
        try {
            DatabaseConnection.getInstance();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Failed to connect to the database:\n" + e.getMessage() +
                "\n\nMake sure sqlite-jdbc-*.jar is in the lib/ folder " +
                "and on the classpath.",
                "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

package student.management.system.model;

/**
 * Plain Java object representing one row in the courses table.
 */
public class Course {

    private int    id;
    private String courseCode;
    private String courseName;
    private int    credits;
    private String department;
    private String instructor;

    // ── Constructors ─────────────────────────────────────────────

    public Course() {}

    public Course(String courseCode, String courseName,
                  int credits, String department, String instructor) {
        this.courseCode  = courseCode;
        this.courseName  = courseName;
        this.credits     = credits;
        this.department  = department;
        this.instructor  = instructor;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public int    getId()                     { return id; }
    public void   setId(int id)               { this.id = id; }

    public String getCourseCode()             { return courseCode; }
    public void   setCourseCode(String cc)    { this.courseCode = cc; }

    public String getCourseName()             { return courseName; }
    public void   setCourseName(String cn)    { this.courseName = cn; }

    public int    getCredits()                { return credits; }
    public void   setCredits(int c)           { this.credits = c; }

    public String getDepartment()             { return department; }
    public void   setDepartment(String dept)  { this.department = dept; }

    public String getInstructor()             { return instructor; }
    public void   setInstructor(String ins)   { this.instructor = ins; }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}

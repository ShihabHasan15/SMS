package student.management.system.model;

/**
 * Plain Java object representing one row in the students table.
 */
public class Student {

    private int    id;
    private String studentId;   // e.g. "STU-001"
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;
    private int    year;
    private double gpa;
    private String createdAt;

    // ── Constructors ─────────────────────────────────────────────

    public Student() {}

    /** Used when inserting a new student (no DB-generated id yet). */
    public Student(String studentId, String firstName, String lastName,
                   String email, String phone, String department,
                   int year, double gpa) {
        this.studentId  = studentId;
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.email      = email;
        this.phone      = phone;
        this.department = department;
        this.year       = year;
        this.gpa        = gpa;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }

    public String getStudentId()              { return studentId; }
    public void   setStudentId(String sid)    { this.studentId = sid; }

    public String getFirstName()              { return firstName; }
    public void   setFirstName(String fn)     { this.firstName = fn; }

    public String getLastName()               { return lastName; }
    public void   setLastName(String ln)      { this.lastName = ln; }

    public String getFullName()               { return firstName + " " + lastName; }

    public String getEmail()                  { return email; }
    public void   setEmail(String email)      { this.email = email; }

    public String getPhone()                  { return phone; }
    public void   setPhone(String phone)      { this.phone = phone; }

    public String getDepartment()             { return department; }
    public void   setDepartment(String dept)  { this.department = dept; }

    public int    getYear()                   { return year; }
    public void   setYear(int year)           { this.year = year; }

    public double getGpa()                    { return gpa; }
    public void   setGpa(double gpa)          { this.gpa = gpa; }

    public String getCreatedAt()              { return createdAt; }
    public void   setCreatedAt(String ca)     { this.createdAt = ca; }

    @Override
    public String toString() {
        return studentId + " - " + getFullName();
    }
}

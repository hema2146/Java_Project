
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalDate;

public class StudentPlacementSystem extends JFrame {

    // Database connection
    static final String URL =
            "jdbc:mysql://localhost:3306/placement_db";
    static final String USER = "root";
    static final String PASS = "your_mysql_password";

    public StudentPlacementSystem() {
        setTitle("Student Placement Management System");
        setSize(700, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JButton student = new JButton("Add Student");
        JButton company = new JButton("Add Company");
        JButton job = new JButton("Add Job");
        JButton view = new JButton("View Students");
        JButton apply = new JButton("Apply for Job");
        JButton reports = new JButton("Placement Report");

        JPanel panel = new JPanel();
        panel.add(student);
        panel.add(company);
        panel.add(job);
        panel.add(view);
        panel.add(apply);
        panel.add(reports);

        add(panel);

        student.addActionListener(e -> addStudent());
        company.addActionListener(e -> addCompany());
        job.addActionListener(e -> addJob());
        view.addActionListener(e -> viewStudents());
        apply.addActionListener(e -> applyJob());
        reports.addActionListener(e -> report());
    }

    // Connect to MySQL
    Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // Read input using dialog boxes
    String input(String message) {
        return JOptionPane.showInputDialog(this, message);
    }

    // Show messages
    void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }

    // 1. Add Student
    void addStudent() {
        try {
            String roll = input("Roll Number:");
            String name = input("Student Name:");
            String dept = input("Department:");

            if (roll == null || name == null || dept == null) {
                return;
            }

            if (roll.trim().isEmpty()
                    || name.trim().isEmpty()
                    || dept.trim().isEmpty()) {
                message("Roll number, name, and department are required.");
                return;
            }

            double cgpa = Double.parseDouble(input("CGPA:"));
            int year = Integer.parseInt(input("Graduation Year:"));
            String skills = input("Skills:");

            if (skills == null) {
                return;
            }

            if (cgpa < 0 || cgpa > 10) {
                message("CGPA must be between 0 and 10.");
                return;
            }

            String sql = "INSERT INTO students "
                    + "(roll_number, name, department, cgpa, "
                    + "graduation_year, skills) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection c = connect();
                 PreparedStatement p = c.prepareStatement(sql)) {

                p.setString(1, roll.trim());
                p.setString(2, name.trim());
                p.setString(3, dept.trim());
                p.setDouble(4, cgpa);
                p.setInt(5, year);
                p.setString(6, skills);

                p.executeUpdate();
                message("Student added successfully!");
            }

        } catch (SQLException | NumberFormatException e) {
            message("Error: " + e.getMessage());
        }
    }

    // 2. Add Company
    void addCompany() {
        try {
            String name = input("Company Name:");

            if (name == null || name.trim().isEmpty()) {
                return;
            }

            String industry = input("Industry:");
            String location = input("Location:");
            String email = input("Contact Email:");

            if (industry == null || location == null || email == null) {
                return;
            }

            String sql = "INSERT INTO companies "
                    + "(company_name, industry, location, contact_email) "
                    + "VALUES (?, ?, ?, ?)";

            try (Connection c = connect();
                 PreparedStatement p = c.prepareStatement(sql)) {

                p.setString(1, name.trim());
                p.setString(2, industry);
                p.setString(3, location);
                p.setString(4, email);

                p.executeUpdate();
                message("Company added successfully!");
            }

        } catch (SQLException e) {
            message("Error: " + e.getMessage());
        }
    }

    // 3. Add Job
    void addJob() {
        try {
            String companyInput = input("Company ID:");
            String title = input("Job Title:");
            String cgpaInput = input("Minimum CGPA:");
            String dept = input("Eligible Department (or ALL):");
            String deadlineInput = input("Deadline (YYYY-MM-DD):");

            if (companyInput == null || title == null
                    || cgpaInput == null || dept == null
                    || deadlineInput == null) {
                return;
            }

            int companyId = Integer.parseInt(companyInput);
            double minCgpa = Double.parseDouble(cgpaInput);
            Date deadline = Date.valueOf(deadlineInput);

            if (title.trim().isEmpty() || dept.trim().isEmpty()) {
                message("Job title and eligible department are required.");
                return;
            }

            if (minCgpa < 0 || minCgpa > 10) {
                message("Minimum CGPA must be between 0 and 10.");
                return;
            }

            if (deadline.toLocalDate().isBefore(LocalDate.now())) {
                message("Deadline cannot be in the past.");
                return;
            }

            String sql = "INSERT INTO jobs "
                    + "(company_id, job_title, minimum_cgpa, "
                    + "eligible_department, deadline) "
                    + "VALUES (?, ?, ?, ?, ?)";

            try (Connection c = connect();
                 PreparedStatement p = c.prepareStatement(sql)) {

                p.setInt(1, companyId);
                p.setString(2, title.trim());
                p.setDouble(3, minCgpa);
                p.setString(4, dept.trim());
                p.setDate(5, deadline);

                p.executeUpdate();
                message("Job added successfully!");
            }

        } catch (SQLException e) {
            message("Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            message("Enter valid numeric values.");
        } catch (IllegalArgumentException e) {
            message("Enter a valid deadline in YYYY-MM-DD format.");
        }
    }

    // 4. View Students
    void viewStudents() {
        String sql = "SELECT student_id, roll_number, name, "
                + "department, cgpa FROM students";

        try (Connection c = connect();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {

            DefaultTableModel model = new DefaultTableModel(
                    new String[]{
                            "ID", "Roll No.", "Name",
                            "Department", "CGPA"
                    }, 0
            );

            while (r.next()) {
                model.addRow(new Object[]{
                        r.getInt("student_id"),
                        r.getString("roll_number"),
                        r.getString("name"),
                        r.getString("department"),
                        r.getDouble("cgpa")
                });
            }

            JTable table = new JTable(model);

            JOptionPane.showMessageDialog(
                    this,
                    new JScrollPane(table),
                    "Student Records",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {
            message("Error: " + e.getMessage());
        }
    }

    // 5. Apply for Job after checking eligibility
    void applyJob() {
        try {
            String studentInput = input("Student ID:");
            String jobInput = input("Job ID:");

            if (studentInput == null || jobInput == null) {
                return;
            }

            int studentId = Integer.parseInt(studentInput);
            int jobId = Integer.parseInt(jobInput);

            String check = "SELECT s.cgpa, s.department, "
                    + "j.minimum_cgpa, j.eligible_department, j.deadline "
                    + "FROM students s CROSS JOIN jobs j "
                    + "WHERE s.student_id = ? AND j.job_id = ?";

            try (Connection c = connect();
                 PreparedStatement p = c.prepareStatement(check)) {

                p.setInt(1, studentId);
                p.setInt(2, jobId);

                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) {
                        message("Student or job not found.");
                        return;
                    }

                    double cgpa = r.getDouble("cgpa");
                    String dept = r.getString("department");
                    double minCgpa = r.getDouble("minimum_cgpa");
                    String eligibleDept =
                            r.getString("eligible_department");
                    Date deadline = r.getDate("deadline");

                    boolean eligible = cgpa >= minCgpa
                            && ("ALL".equalsIgnoreCase(eligibleDept)
                            || dept.equalsIgnoreCase(eligibleDept));

                    if (!eligible) {
                        message("You are not eligible for this job.");
                        return;
                    }

                    if (deadline.toLocalDate()
                            .isBefore(LocalDate.now())) {
                        message("Application deadline has passed.");
                        return;
                    }
                }

                String sql = "INSERT INTO applications "
                        + "(student_id, job_id, application_date, status) "
                        + "VALUES (?, ?, ?, ?)";

                try (PreparedStatement p2 = c.prepareStatement(sql)) {
                    p2.setInt(1, studentId);
                    p2.setInt(2, jobId);
                    p2.setDate(3, Date.valueOf(LocalDate.now()));
                    p2.setString(4, "Applied");

                    p2.executeUpdate();
                    message("Application submitted successfully!");
                }
            }

        } catch (SQLException e) {
            message("Application failed: " + e.getMessage());
        } catch (NumberFormatException e) {
            message("Enter valid numeric IDs.");
        }
    }

    // 6. Placement Report
    void report() {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM students) AS students, "
                + "(SELECT COUNT(*) FROM companies) AS companies, "
                + "(SELECT COUNT(*) FROM jobs) AS jobs, "
                + "(SELECT COUNT(*) FROM applications) AS applications, "
                + "(SELECT COUNT(*) FROM applications "
                + "WHERE status = 'Selected') AS selected";

        try (Connection c = connect();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {

            if (r.next()) {
                message(
                        "PLACEMENT REPORT\n\n"
                        + "Students: " + r.getInt("students") + "\n"
                        + "Companies: " + r.getInt("companies") + "\n"
                        + "Jobs: " + r.getInt("jobs") + "\n"
                        + "Applications: " + r.getInt("applications") + "\n"
                        + "Selected Applications: "
                        + r.getInt("selected")
                );
            }

        } catch (SQLException e) {
            message("Error: " + e.getMessage());
        }
    }

    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                new StudentPlacementSystem().setVisible(true);

            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Please add the MySQL JDBC driver."
                );
            }
        });
    }
}

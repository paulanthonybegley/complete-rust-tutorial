import java.math.BigDecimal;
import java.util.List;

public class Employee {

    private BigDecimal salary;
    private int grade;
    private String level;
    private Department department;

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public BigDecimal bonus() {
        return salary.multiply(new BigDecimal("0.05"));
    }
}

public class Department {

    private String name;
    private BigDecimal budget;
    private List<Employee> staff;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public List<Employee> getStaff() { return staff; }
    public void setStaff(List<Employee> staff) { this.staff = staff; }

    public void promoteEach() {
        for (Employee employee : staff) {
            // reaching into the OTHER object's privates to promote
            int grade = employee.getGrade();
            employee.setGrade(grade + 1);
            employee.setLevel("LEVEL_" + (grade + 1));
            if (employee.getGrade() > 6) {
                employee.setSalary(employee.getSalary().multiply(new BigDecimal("1.10")));
            }
        }
    }

    public boolean isOverBudget() {
        BigDecimal payroll = BigDecimal.ZERO;
        for (Employee employee : staff) {
            payroll = payroll.add(employee.getSalary());
        }
        return payroll.compareTo(budget) > 0;
    }
}
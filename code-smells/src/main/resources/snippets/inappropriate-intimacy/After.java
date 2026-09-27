import java.math.BigDecimal;
import java.util.List;

public class Employee {

    private final BigDecimal salary;
    private final int grade;
    private final String level;
    private final Department department;

    public Employee(BigDecimal salary, int grade, String level, Department department) {
        this.salary = salary;
        this.grade = grade;
        this.level = level;
        this.department = department;
    }

    public BigDecimal monthlyPay() { return salary; }

    public Employee promoted() {
        int next = grade + 1;
        BigDecimal nextSalary = next > 6 ? salary.multiply(new BigDecimal("1.10")) : salary;
        return new Employee(nextSalary, next, "LEVEL_" + next, department);
    }

    public BigDecimal bonus() {
        return salary.multiply(new BigDecimal("0.05"));
    }
}

public class Department {

    private final String name;
    private final BigDecimal budget;
    private final List<Employee> staff;

    public Department(String name, BigDecimal budget, List<Employee> staff) {
        this.name = name;
        this.budget = budget;
        this.staff = List.copyOf(staff);
    }

    // the only relationship: the department manages a roster
    public List<Employee> staff() { return staff; }

    public Department promotePaidEnumerable() {
        // delegation surfaces in the signature, not hidden in the body
        return new Department(name, budget, staff.stream().map(Employee::promoted).toList());
    }

    public boolean isOverBudget() {
        BigDecimal payroll = staff.stream()
                .map(Employee::monthlyPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return payroll.compareTo(budget) > 0;
    }

    public String name() { return name; }
}
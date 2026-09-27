import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseTracker {

    private List<Expense> expenses = new ArrayList<>();
    private BigDecimal budget = new BigDecimal("1000.00");
    private int budgetPeriod;
    private String accountantEmail;
    private String accountantName;
    private BigDecimal taxRate;
    private LocalDate reminderCutoff;
    private List<Contact> birthdayContacts = new ArrayList<>();

    public void addExpense(Expense expense) {
        expenses.add(expense);
        if (expensesTotal().compareTo(budget) > 0) {
            throw new IllegalStateException("Budget exceeded");
        }
    }

    public BigDecimal expensesTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Expense expense : expenses) {
            sum = sum.add(expense.getAmount());
        }
        return sum;
    }

    public void saveBudget() { /* ... */ }
    public void rollOverBudget() { /* ... */ }

    public void sendBudgetReport() {
        String summary = "Budget " + budget + " spent " + expensesTotal() + " for period " + budgetPeriod;
        email(accountantEmail, "Budget report", summary);
    }

    public BigDecimal taxFor(BigDecimal amount) {
        return amount.multiply(taxRate);
    }

    public void sendTaxSummary() {
        BigDecimal tax = taxFor(expensesTotal());
        email(accountantEmail, "Tax summary", "Owe " + tax);
    }

    public void remindBirthdays() {
        for (Contact contact : birthdayContacts) {
            if (contact.getBirthday().isAfter(reminderCutoff.minusYears(1))) {
                email(contact.getEmail(), "Reminder", "Birthday coming for " + contact.getName());
            }
        }
    }

    public void addContact(Contact contact) {
        birthdayContacts.add(contact);
    }

    private void email(String to, String subject, String body) { /* ... */ }
}
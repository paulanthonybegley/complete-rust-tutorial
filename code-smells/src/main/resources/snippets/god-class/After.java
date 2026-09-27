import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseTracker {

    private final Budget budget = new Budget(new BigDecimal("1000.00"));
    private final ReceiptEmailer emailer = new ReceiptEmailer();
    private final ReminderService reminderService = new ReminderService();

    public void addExpense(Expense expense) {
        budget.add(expense);
    }

    public BigDecimal expensesTotal() {
        return budget.spent();
    }

    public void sendBudgetReport() {
        emailer.sendBudgetReport(budget, 1);
    }

    public void sendTaxSummary() {
        emailer.sendTaxSummary(budget);
    }

    public void remindBirthdays(LocalDate reminderCutoff) {
        reminderService.remindBirthdays(reminderCutoff);
    }

    public void addContact(Contact contact) {
        reminderService.addContact(contact);
    }
}

class Budget {

    private final List<Expense> expenses = new ArrayList<>();
    private final BigDecimal limit;

    Budget(BigDecimal limit) {
        this.limit = limit;
    }

    public void add(Expense expense) {
        expenses.add(expense);
        if (spent().compareTo(limit) > 0) {
            throw new IllegalStateException("Budget exceeded");
        }
    }

    public BigDecimal spent() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Expense expense : expenses) {
            sum = sum.add(expense.getAmount());
        }
        return sum;
    }

    public BigDecimal limit() {
        return limit;
    }

    public BigDecimal taxFor(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate);
    }
}

class ReminderService {

    private final List<Contact> birthdayContacts = new ArrayList<>();

    public void remindBirthdays(LocalDate cutoff) {
        for (Contact contact : birthdayContacts) {
            if (contact.getBirthday().isAfter(cutoff.minusYears(1))) {
                email(contact.getEmail(), "Reminder", "Birthday coming for " + contact.getName());
            }
        }
    }

    public void addContact(Contact contact) {
        birthdayContacts.add(contact);
    }

    private void email(String to, String subject, String body) { /* ... */ }
}

class ReceiptEmailer {

    public void sendBudgetReport(Budget budget, int period) {
        String summary = "Budget " + budget.limit() + " spent " + budget.spent() + " for period " + period;
        send("accountant@example.com", "Budget report", summary);
    }

    public void sendTaxSummary(Budget budget) {
        BigDecimal tax = budget.taxFor(budget.spent(), new BigDecimal("0.20"));
        send("accountant@example.com", "Tax summary", "Owe " + tax);
    }

    private void send(String to, String subject, String body) { /* ... */ }
}
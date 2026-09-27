import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Customer {
    private final String tier;
    private final LocalDate joined;
    private final int loyaltyPoints;
    private final List<String> pastEmails;

    public Customer(String tier, LocalDate joined, int loyaltyPoints, List<String> pastEmails) {
        this.tier = tier;
        this.joined = joined;
        this.loyaltyPoints = loyaltyPoints;
        this.pastEmails = pastEmails;
    }

    public String getTier() { return tier; }
    public LocalDate getJoined() { return joined; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public List<String> getPastEmails() { return pastEmails; }
}

public class DiscountService {

    // poor DiscountService: reaches into the customer six times
    public BigDecimal discountFor(Customer customer, BigDecimal orderTotal) {
        BigDecimal discount = BigDecimal.ZERO;
        if (customer.getTier().equals("GOLD")) {
            discount = discount.add(new BigDecimal("10"));
        }
        if (customer.getPastEmails().size() > 50) {
            discount = discount.add(new BigDecimal("5"));
        }
        if (customer.getJoined().isBefore(customer.getJoined().withYear(2019))) {
            discount = discount.add(new BigDecimal("5"));
        }
        if (customer.getLoyaltyPoints() > 1000 && customer.getTier().equals("GOLD")) {
            discount = discount.add(new BigDecimal("5"));
        }
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }
        return discount;
    }
}
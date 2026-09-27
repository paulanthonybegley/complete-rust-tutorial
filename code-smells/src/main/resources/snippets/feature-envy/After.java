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

    public BigDecimal discountFor(BigDecimal orderTotal) {
        BigDecimal discount = BigDecimal.ZERO;
        if (tier.equals("GOLD")) {
            discount = discount.add(new BigDecimal("10"));
        }
        if (pastEmails.size() > 50) {
            discount = discount.add(new BigDecimal("5"));
        }
        if (joined.isBefore(LocalDate.of(2019, 1, 1))) {
            discount = discount.add(new BigDecimal("5"));
        }
        if (loyaltyPoints > 1000 && tier.equals("GOLD")) {
            discount = discount.add(new BigDecimal("5"));
        }
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }
        return discount;
    }
}
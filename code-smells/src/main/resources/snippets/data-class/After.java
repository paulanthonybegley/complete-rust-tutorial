import java.math.BigDecimal;
import java.util.List;

public class Order {

    private final List<OrderLine> items;
    private final String customerEmail;
    private final String shippingCountry;
    private BigDecimal paid;
    private boolean shipped;

    public Order(List<OrderLine> items, String customerEmail, String shippingCountry) {
        this.items = items;
        this.customerEmail = customerEmail;
        this.shippingCountry = shippingCountry;
    }

    public BigDecimal total() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderLine line : items) {
            subtotal = subtotal.add(line.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
        }
        return subtotal.add(shipping());
    }

    private BigDecimal shipping() {
        return shippingCountry.equals("US") ? new BigDecimal("5") : new BigDecimal("25");
    }

    public void markShipped() {
        if (paid == null) {
            throw new IllegalStateException("Cannot ship an unpaid order");
        }
        if (shipped) {
            throw new IllegalStateException("Already shipped");
        }
        shipped = true;
    }

    public void confirmPaid(BigDecimal amount) {
        if (amount.compareTo(total()) < 0) {
            throw new IllegalStateException("Payment " + amount + " is less than " + total());
        }
        paid = amount;
    }

    public String receipt() {
        return "To " + customerEmail + " total " + total();
    }
}
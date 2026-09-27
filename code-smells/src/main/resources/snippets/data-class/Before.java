import java.math.BigDecimal;
import java.util.List;

public class OrderDraft {
    private List<OrderLine> items;
    private String customerEmail;
    private String shippingCountry;
    private BigDecimal paid;
    private boolean shipped;

    // forty getters and setters, no behaviour at all
    public List<OrderLine> getItems() { return items; }
    public void setItems(List<OrderLine> items) { this.items = items; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String email) { this.customerEmail = email; }
    public String getShippingCountry() { return shippingCountry; }
    public void setShippingCountry(String country) { this.shippingCountry = country; }
    public BigDecimal getPaid() { return paid; }
    public void setPaid(BigDecimal paid) { this.paid = paid; }
    public boolean isShipped() { return shipped; }
    public void setShipped(boolean shipped) { this.shipped = shipped; }
}

public class OrderService {

    public BigDecimal totalFor(OrderDraft draft) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderLine line : draft.getItems()) {
            subtotal = subtotal.add(line.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
        }
        BigDecimal shipping = draft.getShippingCountry().equals("US") ? new BigDecimal("5") : new BigDecimal("25");
        return subtotal.add(shipping);
    }

    public void markShipped(OrderDraft draft) {
        if (draft.getPaid() == null) {
            throw new IllegalStateException("Cannot ship an unpaid order");
        }
        if (draft.isShipped()) {
            throw new IllegalStateException("Already shipped");
        }
        draft.setShipped(true);
    }

    public String receiptText(OrderDraft draft, BigDecimal total) {
        return "To " + draft.getCustomerEmail() + " total " + total;
    }
}
import java.math.BigDecimal;
import java.util.List;

// the forwarding layer is gone: callers talk to the real class directly.
public class Order {
    private final List<String> items;
    private String status = "NEW";
    private BigDecimal total = BigDecimal.ZERO;

    public Order(List<String> items) {
        this.items = items;
    }

    public String getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public List<String> getItems() { return items; }
    public void fulfill() { status = "FULFILLED"; }
    public void add(String sku) { items.add(sku); }
}
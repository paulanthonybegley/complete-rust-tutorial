import java.math.BigDecimal;
import java.util.List;

public class Order {
    private String status;
    private BigDecimal total;
    private List<String> items;

    public String getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public List<String> getItems() { return items; }
    public void fulfill() { status = "FULFILLED"; }
    public void add(String sku) { items.add(sku); }
}

public class OrderManager {

    // the toll booth: forwards everything, decides nothing.
    private final Order order;

    public OrderManager(Order order) {
        this.order = order;
    }

    public String getStatus() { return order.getStatus(); }
    public BigDecimal getTotal() { return order.getTotal(); }
    public List<String> getItems() { return order.getItems(); }
    public void fulfill() { order.fulfill(); }
    public void add(String sku) { order.add(sku); }
}
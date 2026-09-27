import java.math.BigDecimal;
import java.util.List;

public class OrderProcessor {

    public void processOrder(Order order, Customer customer, Inventory inventory, Mailer mailer) {
        // validate everything first
        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order has no items");
        }
        if (customer.getAddress() == null) {
            throw new IllegalArgumentException("Customer has no address");
        }
        if (inventory.hasStock(order) == false) {
            throw new IllegalArgumentException("Not enough stock");
        }
        // now the money work
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderLine line : order.getItems()) {
            subtotal = subtotal.add(line.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
        }
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.20"));
        BigDecimal shipping = shippingFor(customer, order);
        BigDecimal total = subtotal.add(tax).add(shipping);
        // discount season
        if (customer.getMemberSince().isBefore(customer.getMemberSince().withYear(2020))) {
            total = total.multiply(new BigDecimal("0.9"));
        }
        // adjust stock now that it is paid for
        for (OrderLine line : order.getItems()) {
            inventory.reserve(line.getSku(), line.getQuantity());
        }
        order.setStatus("PAID");
        order.setTotal(total);
        // let people know
        mailer.sendReceipt(customer.getEmail(), order);
        mailer.sendWarehousePickNote(order);
    }

    // platform noise, not really a second responsibility
    private BigDecimal shippingFor(Customer customer, Order order) {
        return customer.getAddress().getCountry().equals("US")
                ? new BigDecimal("5.00")
                : new BigDecimal("25.00");
    }
}
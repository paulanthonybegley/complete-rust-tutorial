import java.math.BigDecimal;

public class OrderProcessor {

    public void processOrder(Order order, Customer customer, Inventory inventory, Mailer mailer) {
        validate(order, customer, inventory);
        BigDecimal total = price(order, customer);
        reserveStock(order, inventory);
        order.setStatus("PAID");
        order.setTotal(total);
        notify(order, customer, mailer);
    }

    private void validate(Order order, Customer customer, Inventory inventory) {
        assertHasItems(order);
        assertHasAddress(customer);
        assertStockAvailable(order, inventory);
    }

    private void assertHasItems(Order order) {
        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order has no items");
        }
    }

    private void assertHasAddress(Customer customer) {
        if (customer.getAddress() == null) {
            throw new IllegalArgumentException("Customer has no address");
        }
    }

    private void assertStockAvailable(Order order, Inventory inventory) {
        if (inventory.hasStock(order) == false) {
            throw new IllegalArgumentException("Not enough stock");
        }
    }

    private BigDecimal price(Order order, Customer customer) {
        BigDecimal subtotal = subtotal(order);
        BigDecimal total = subtotal
                .add(tax(subtotal))
                .add(shippingFor(customer, order));
        return applyLoyaltyDiscount(customer, total);
    }

    private BigDecimal subtotal(Order order) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderLine line : order.getItems()) {
            subtotal = subtotal.add(line.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
        }
        return subtotal;
    }

    private BigDecimal tax(BigDecimal subtotal) {
        return subtotal.multiply(new BigDecimal("0.20"));
    }

    private BigDecimal shippingFor(Customer customer, Order order) {
        return customer.getAddress().getCountry().equals("US")
                ? new BigDecimal("5.00")
                : new BigDecimal("25.00");
    }

    private BigDecimal applyLoyaltyDiscount(Customer customer, BigDecimal total) {
        if (customer.getMemberSince().isBefore(customer.getMemberSince().withYear(2020))) {
            return total.multiply(new BigDecimal("0.9"));
        }
        return total;
    }

    private void reserveStock(Order order, Inventory inventory) {
        for (OrderLine line : order.getItems()) {
            inventory.reserve(line.getSku(), line.getQuantity());
        }
    }

    private void notify(Order order, Customer customer, Mailer mailer) {
        mailer.sendReceipt(customer.getEmail(), order);
        mailer.sendWarehousePickNote(order);
    }
}
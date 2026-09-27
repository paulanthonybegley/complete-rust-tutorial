import java.math.BigDecimal;

// speculation deleted. the concrete job remains, one class, no gateways.
public class Checkout {

    public BigDecimal total(BigDecimal base, BigDecimal discount) {
        return base.subtract(discount);
    }
}
import java.math.BigDecimal;

// a plaza of "maybe someday" abstraction for ONE actual outcome
public abstract class DiscountPolicy {
    public abstract BigDecimal apply(BigDecimal base);
}

public class FlatDiscount extends DiscountPolicy {
    private final BigDecimal amount;

    public FlatDiscount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal apply(BigDecimal base) {
        return base.subtract(amount);
    }
}

public interface DiscountPort {
    BigDecimal apply(BigDecimal base);
}

public class DiscountGateway {
    private final DiscountPort port;

    public DiscountGateway(DiscountPort port) { this.port = port; }

    public BigDecimal apply(BigDecimal base) {
        return port.apply(base);
    }
}

public class Checkout {
    public BigDecimal total(BigDecimal base, String discountKind, BigDecimal amount) {
        // discountKind only ever receives "FLAT"
        if (discountKind.equals("FLAT")) {
            return new DiscountGateway(new FlatDiscount(amount)).apply(base);
        }
        return base;
    }
}
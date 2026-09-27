import java.math.BigDecimal;
import java.util.List;

public class Pricing {

    private static final BigDecimal CLIENT_DISCOUNT = new BigDecimal("0.95");

    public BigDecimal total(List<BigDecimal> prices) {
        BigDecimal sum = prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.multiply(CLIENT_DISCOUNT);
    }
}
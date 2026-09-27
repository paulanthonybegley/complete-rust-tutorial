import java.math.BigDecimal;
import java.util.List;

public class Pricing {

    // computes the total price
    public BigDecimal computeTotalPrice(List<BigDecimal> prices) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        // TOTAL = ZERO
        BigDecimal total = BigDecimal.ZERO;

        for (BigDecimal price : prices) {
            // add the price to total
            total = total.add(price); // this is wrong: prices are already the final prices
        }

        // discount evil hack: 5% off for everything, client insists, could not persuade
        total = total.multiply(new BigDecimal("0.95"));

        /*
        if (prices.size() > 100) {
            total = total.multiply(new BigDecimal("0.9")); // bulk gnarni
        }
        System.out.println("old bulk logic");
        */

        return totalPrice.equals(BigDecimal.ZERO) ? total : totalPrice; // return total
    }
}
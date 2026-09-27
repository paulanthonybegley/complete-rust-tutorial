import java.math.BigDecimal;
import java.util.regex.Pattern;

public class Payment {

    public double price;
    public String currency;
    public String customerEmail;
    public String sku;
    public int quantity;

    public Payment(double price, String currency, String customerEmail, String sku, int quantity) {
        this.price = price;
        this.currency = currency;
        this.customerEmail = customerEmail;
        this.sku = sku;
        this.quantity = quantity;
    }

    public double total() {
        if (currency.equals("EUR")) {
            return price * quantity * 1.21;
        }
        return price * quantity;
    }

    // validation repeated at EVERY call site, slightly differently each time
    public static boolean looksLikeEmail(String value) {
        return Pattern.matches(".+@.+\\..+", value);
    }

    public static boolean looksLikeSku(String value) {
        return value.length() == 8;
    }
}

public class Checkout {

    public String pay(Payment payment, String customerEmail) {
        if (!Payment.looksLikeEmail(customerEmail)) {
            throw new IllegalArgumentException("Bad email " + customerEmail);
        }
        if (payment.price < 0) {
            throw new IllegalArgumentException("Negative price " + payment.price);
        }
        if (!Payment.looksLikeSku(payment.sku)) {
            throw new IllegalArgumentException("Bad sku " + payment.sku);
        }
        double taxed = payment.total();
        String money = payment.currency + " " + taxed;
        return "Charge to " + customerEmail + " amount " + money;
    }
}
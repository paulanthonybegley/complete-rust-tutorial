import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class Money {
    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must be non-negative, got " + amount);
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency required");
        }
        this.amount = amount;
        this.currency = currency;
    }

    public Money times(int quantity) {
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    public Money withRate(BigDecimal rate) {
        return new Money(amount.multiply(rate), currency);
    }

    public Money ensureSameCurrencyAs(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot mix " + currency + " and " + other.currency);
        }
        return this;
    }

    public BigDecimal amount() { return amount; }
    public String currency() { return currency; }
}

public final class Sku {
    private final String value;

    public Sku(String value) {
        if (value == null || value.length() != 8) {
            throw new IllegalArgumentException("SKU must be 8 chars, got '" + value + "'");
        }
        this.value = value;
    }

    public String value() { return value; }
}

public final class EmailAddress {
    private static final Pattern AT = Pattern.compile(".+@.+\\..+");
    private final String value;

    public EmailAddress(String value) {
        if (value == null || !AT.matcher(value).matches()) {
            throw new IllegalArgumentException("Bad email '" + value + "'");
        }
        this.value = value;
    }

    public String value() { return value; }
}

public class Payment {
    private final Money price;
    private final Sku sku;
    private final int quantity;

    public Payment(Money price, Sku sku, int quantity) {
        this.price = price;
        this.sku = sku;
        this.quantity = quantity;
    }

    public Money total() {
        return price.times(quantity).withRate(new BigDecimal("1.21"));
    }
}

public class Checkout {
    public String pay(Payment payment, EmailAddress customerEmail) {
        Money taxed = payment.total();
        return "Charge to " + customerEmail.value() + " amount " + taxed.currency() + " " + taxed.amount();
    }
}
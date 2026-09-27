import java.math.BigDecimal;
import java.time.LocalDate;

public class Order {

    private final String id;
    private final BigDecimal amount;
    private final LocalDate placedOn;

    public Order(String id, BigDecimal amount, LocalDate placedOn) {
        this.id = id;
        this.amount = amount;
        this.placedOn = placedOn;
    }

    public BigDecimal total() {
        return amount;
    }

    public BigDecimal totalAfter(Voucher voucher) {
        if (voucher == null) {
            return total();
        }
        return voucher.validOn(LocalDate.now())
                ? amount.subtract(voucher.discount())
                : amount;
    }

    public String receiptText(BigDecimal total) {
        return "Order " + id + " total " + total;
    }
}

public final class Voucher {

    private final String code;
    private final BigDecimal discount;
    private final LocalDate expiresOn;

    public Voucher(String code, BigDecimal discount, LocalDate expiresOn) {
        this.code = code;
        this.discount = discount;
        this.expiresOn = expiresOn;
    }

    public boolean validOn(LocalDate day) {
        return expiresOn.isAfter(day);
    }

    public BigDecimal discount() {
        return discount;
    }

    public String code() {
        return code;
    }
}
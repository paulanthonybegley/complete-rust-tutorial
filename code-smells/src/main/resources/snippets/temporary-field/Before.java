import java.math.BigDecimal;
import java.time.LocalDate;

public class Order {

    private final String id;
    private final BigDecimal amount;
    private final LocalDate placedOn;

    // only ever meaningful on the checkout path
    private BigDecimal promotionalDiscount;
    private String voucherCode;
    private LocalDate expiresOn;

    public Order(String id, BigDecimal amount, LocalDate placedOn) {
        this.id = id;
        this.amount = amount;
        this.placedOn = placedOn;
    }

    public void applyVoucher(String voucherCode, BigDecimal discount, LocalDate expiresOn) {
        this.voucherCode = voucherCode;
        this.promotionalDiscount = discount;
        this.expiresOn = expiresOn;
    }

    public BigDecimal total() {
        BigDecimal total = amount;
        if (promotionalDiscount != null && expiresOn != null && expiresOn.isAfter(LocalDate.now())) {
            total = total.subtract(promotionalDiscount);
        }
        return total;
    }

    public String receiptText() {
        StringBuilder out = new StringBuilder("Order " + id);
        if (voucherCode != null) {
            out.append(" [voucher ").append(voucherCode).append("]");
        }
        out.append(" total ").append(total());
        return out.toString();
    }

    public boolean isDiscounted() {
        return promotionalDiscount != null;
    }
}
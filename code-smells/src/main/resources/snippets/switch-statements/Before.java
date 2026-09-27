import java.math.BigDecimal;

public class OrderHandler {

    // three different switches on the SAME typeCode, in one file.
    // add one more type -> edit all three. forget one -> bug.

    public BigDecimal priceFor(String typeCode, BigDecimal base) {
        switch (typeCode) {
            case "BOOK": return base.multiply(new BigDecimal("0.9"));
            case "DIGITAL": return base.multiply(new BigDecimal("0.5"));
            case "TICKET": return base.add(new BigDecimal("2"));
            default: return base;
        }
    }

    public String labelFor(String typeCode) {
        switch (typeCode) {
            case "BOOK": return "Paperback book";
            case "DIGITAL": return "Digital download";
            case "TICKET": return "Event ticket";
            default: return "Misc";
        }
    }

    public boolean needsShipping(String typeCode) {
        switch (typeCode) {
            case "BOOK": return true;
            case "DIGITAL": return false;
            case "TICKET": return true;
            default: return false;
        }
    }
}
import java.math.BigDecimal;

public interface OrderKind {
    BigDecimal price(BigDecimal base);
    String label();
    boolean needsShipping();
}

public class Book implements OrderKind {
    public BigDecimal price(BigDecimal base) { return base.multiply(new BigDecimal("0.9")); }
    public String label() { return "Paperback book"; }
    public boolean needsShipping() { return true; }
}

public class Digital implements OrderKind {
    public BigDecimal price(BigDecimal base) { return base.multiply(new BigDecimal("0.5")); }
    public String label() { return "Digital download"; }
    public boolean needsShipping() { return false; }
}

public class Ticket implements OrderKind {
    public BigDecimal price(BigDecimal base) { return base.add(new BigDecimal("2")); }
    public String label() { return "Event ticket"; }
    public boolean needsShipping() { return true; }
}

public class OrderKindFactory {
    public OrderKind from(String typeCode) {
        switch (typeCode) {
            case "BOOK": return new Book();
            case "DIGITAL": return new Digital();
            case "TICKET": return new Ticket();
            default: throw new IllegalArgumentException("Unknown kind " + typeCode);
        }
    }
}

// adding ORDER-ETC = one new class, factory line + test. Never touch the three behaviours again.
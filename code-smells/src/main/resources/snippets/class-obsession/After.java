// two real contracts, no museum floors in between.
public abstract class Report {

    private final String renderedAt;

    protected Report(String renderedAt) {
        this.renderedAt = renderedAt;
    }

    public abstract String render();

    protected String meta() {
        return "rendered@" + renderedAt;
    }
}

public class OrderReport extends Report {
    public OrderReport(String renderedAt) { super(renderedAt); }
    public String render() { return "orders " + meta(); }
}

public class InvoiceReport extends Report {
    public InvoiceReport(String renderedAt) { super(renderedAt); }
    public String render() { return "invoices " + meta(); }
}

// the audit fields moved into the one thing that truly owns them
public class AuditRecord {
    private final String createdBy;
    private final String protocol;
    private final int intervalDays;

    public AuditRecord(String createdBy, String protocol, int intervalDays) {
        this.createdBy = createdBy;
        this.protocol = protocol;
        this.intervalDays = intervalDays;
    }

    public String describe() {
        return createdBy + "/" + protocol + "/" + intervalDays;
    }
}
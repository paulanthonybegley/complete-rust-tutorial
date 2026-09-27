// a museum of abstraction: four levels, three new things to remember.
public abstract class AuditableEntity {
    protected String createdBy;
}

public abstract class ProtocolAuditableEntity extends AuditableEntity {
    protected String protocol;
}

public abstract class IntervalAuditableEntity extends ProtocolAuditableEntity {
    protected int intervalDays;
}

public class Report extends IntervalAuditableEntity {
    public String render() {
        return createdBy + "/" + protocol + "/" + intervalDays;
    }
}

// and a second museum, in report flavours
public abstract class AbstractReport {
    public abstract String render();
}

public abstract class TimestampedReport extends AbstractReport {
    protected long renderedAt;
}

public class OrderReport extends TimestampedReport {
    public String render() { return "orders@" + renderedAt; }
}

public class InvoiceReport extends TimestampedReport {
    public String render() { return "invoices@" + renderedAt; }
}
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// One class, one reason to change. Schema changes -> ReportRepository.
public class ReportRepository {

    public List<ReportRow> load() throws SQLException {
        ResultSet rs = runQuery("SELECT id, customer, amount FROM orders");
        List<ReportRow> rows = new ArrayList<>();
        while (rs.next()) {
            rows.add(new ReportRow(
                    rs.getLong("id"),
                    rs.getString("customer"),
                    rs.getBigDecimal("amount")));
        }
        return rows;
    }

    private ResultSet runQuery(String sql) { /* ... */ throw new UnsupportedOperationException(); }
}

// Layout changes -> ReportRenderer.
class ReportRenderer {

    public String renderHtml(List<ReportRow> rows) {
        StringBuilder html = new StringBuilder("<table>");
        for (ReportRow row : rows) {
            html.append("<tr><td>").append(row.amount()).append("</td></tr>");
        }
        return html.append("</table>").toString();
    }

    public String summarizeCsv(List<ReportRow> rows) {
        StringBuilder csv = new StringBuilder("id,customer,amount\n");
        for (ReportRow row : rows) {
            csv.append(row.id()).append(",").append(row.customer()).append(",").append(row.amount()).append("\n");
        }
        return csv.toString();
    }
}

// Email content changes -> ReportMailer.
class ReportMailer {

    private final ReportRenderer renderer;

    ReportMailer(ReportRenderer renderer) { this.renderer = renderer; }

    public void emailWeekly(List<ReportRow> rows) {
        send("ops@example.com", "Weekly report", renderer.renderHtml(rows));
    }

    public void emailMonthEnd(List<ReportRow> rows) {
        send("finance@example.com", "Month end", renderer.summarizeCsv(rows));
    }

    private void send(String to, String subject, String body) { /* ... */ }
}

record ReportRow(long id, String customer, java.math.BigDecimal amount) {}
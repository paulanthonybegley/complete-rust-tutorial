import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportService {

    // THIS class changes for three different reasons:
    // 1. when the DB schema changes
    // 2. when the report layout changes
    // 3. when the email content changes

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

    public void emailWeekly(List<ReportRow> rows) {
        send("ops@example.com", "Weekly report", renderHtml(rows));
    }

    public void emailMonthEnd(List<ReportRow> rows) {
        send("finance@example.com", "Month end", summarizeCsv(rows));
    }

    private ResultSet runQuery(String sql) { /* ... */ throw new UnsupportedOperationException(); }
    private void send(String to, String subject, String body) { /* ... */ }
}

record ReportRow(long id, String customer, java.math.BigDecimal amount) {}
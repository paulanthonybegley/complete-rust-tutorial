import java.time.LocalDate;

public class CurrentUser {

    // works a two-hour week: one trivial method, nothing else
    private final String name;

    public CurrentUser(String name) {
        this.name = name;
    }

    public String greeting() {
        return "Hello, " + name;
    }
}

public class SessionWrapper {

    public LocalDate today() {
        return LocalDate.now();
    }

    public void nothingElse() {
        // even the name does no work
    }
}
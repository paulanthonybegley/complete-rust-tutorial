import java.time.LocalDate;

// one class where two freeloaders used to stand.
public class UserSession {

    private final String name;

    public UserSession(String name) {
        this.name = name;
    }

    public String greeting() {
        return "Hello, " + name;
    }
}
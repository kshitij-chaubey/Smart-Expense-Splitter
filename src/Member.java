import java.io.Serializable;
import java.util.Objects;

public class Member implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;

    public Member(String name) {
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return name.equalsIgnoreCase(member.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }

    @Override
    public String toString() {
        return name;
    }
}
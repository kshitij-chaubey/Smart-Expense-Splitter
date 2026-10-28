import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public abstract class Expense implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String description;
    protected double totalAmount;
    protected Member paidBy;
    protected LocalDateTime timestamp;

    public Expense(String description, double totalAmount, Member paidBy) throws InvalidExpenseException {
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidExpenseException("Expense description cannot be empty.");
        }
        if (totalAmount <= 0) {
            throw new InvalidExpenseException("Total expense amount must be greater than zero.");
        }
        if (paidBy == null) {
            throw new InvalidExpenseException("A valid payer must be selected.");
        }
        this.description = description.trim();
        this.totalAmount = totalAmount;
        this.paidBy = paidBy;
        this.timestamp = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Member getPaidBy() {
        return paidBy;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a"));
    }

    public abstract Map<Member, Double> calculateSplits();
    public abstract String getExpenseTypeLabel();
}
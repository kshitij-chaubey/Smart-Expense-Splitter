import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonExpense extends Expense {
    private final List<Member> participants;

    public CommonExpense(String description, double totalAmount, Member paidBy, List<Member> participants)
            throws InvalidExpenseException {
        super(description, totalAmount, paidBy);
        if (participants == null || participants.isEmpty()) {
            throw new InvalidExpenseException("Common expense must have at least one participant.");
        }
        this.participants = participants;
    }

    @Override
    public Map<Member, Double> calculateSplits() {
        Map<Member, Double> splits = new HashMap<>();
        double share = totalAmount / participants.size();
        for (Member m : participants) {
            splits.put(m, share);
        }
        return splits;
    }

    @Override
    public String getExpenseTypeLabel() {
        return "Common (Equal Split)";
    }
}
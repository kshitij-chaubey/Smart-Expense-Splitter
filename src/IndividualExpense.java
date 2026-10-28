import java.util.HashMap;
import java.util.Map;

public class IndividualExpense extends Expense {
    private final Map<Member, Double> customSplits;

    public IndividualExpense(String description, double totalAmount, Member paidBy, Map<Member, Double> customSplits)
            throws InvalidExpenseException {
        super(description, totalAmount, paidBy);
        if (customSplits == null || customSplits.isEmpty()) {
            throw new InvalidExpenseException("Custom split values must be provided.");
        }

        double sum = 0.0;
        for (Map.Entry<Member, Double> entry : customSplits.entrySet()) {
            if (entry.getValue() < 0) {
                throw new InvalidExpenseException("Individual share for " + entry.getKey().getName() + " cannot be negative.");
            }
            sum += entry.getValue();
        }

        // Allow a 0.05 tolerance for floating-point rounding
        if (Math.abs(sum - totalAmount) > 0.05) {
            throw new InvalidExpenseException(
                String.format("Split mismatch! Individual shares sum to Rs. %.2f, but total bill is Rs. %.2f.", sum, totalAmount)
            );
        }
        this.customSplits = new HashMap<>(customSplits);
    }

    @Override
    public Map<Member, Double> calculateSplits() {
        return new HashMap<>(customSplits);
    }

    @Override
    public String getExpenseTypeLabel() {
        return "Individual (Custom Split)";
    }
}
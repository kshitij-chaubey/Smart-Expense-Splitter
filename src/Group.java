import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String groupName;
    private final ArrayList<Member> members;
    private final ArrayList<Expense> expenses;

    public Group(String groupName) throws InvalidExpenseException {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new InvalidExpenseException("Group name cannot be empty.");
        }
        this.groupName = groupName.trim();
        this.members = new ArrayList<>();
        this.expenses = new ArrayList<>();
    }

    public String getGroupName() {
        return groupName;
    }

    public ArrayList<Member> getMembers() {
        return members;
    }

    public ArrayList<Expense> getExpenses() {
        return expenses;
    }

    public void addMember(String memberName) throws InvalidExpenseException {
        if (memberName == null || memberName.trim().isEmpty()) {
            throw new InvalidExpenseException("Member name cannot be empty.");
        }
        Member newMember = new Member(memberName);
        if (members.contains(newMember)) {
            throw new InvalidExpenseException("Member '" + newMember.getName() + "' already exists.");
        }
        members.add(newMember);
    }

    public void removeMember(Member member) throws InvalidExpenseException {
        if (member == null) {
            throw new InvalidExpenseException("Please select a member to remove.");
        }
        for (Expense exp : expenses) {
            if (exp.getPaidBy().equals(member) || exp.calculateSplits().containsKey(member)) {
                throw new InvalidExpenseException("Cannot remove member involved in existing expenses.");
            }
        }
        members.remove(member);
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    public void removeExpense(int index) throws InvalidExpenseException {
        if (index < 0 || index >= expenses.size()) {
            throw new InvalidExpenseException("Please select a valid expense to delete.");
        }
        expenses.remove(index);
    }

    public Map<Member, Double> getNetBalances() {
        Map<Member, Double> balances = new HashMap<>();
        for (Member m : members) balances.put(m, 0.0);

        for (Expense exp : expenses) {
            Member payer = exp.getPaidBy();
            balances.put(payer, balances.getOrDefault(payer, 0.0) + exp.getTotalAmount());

            for (Map.Entry<Member, Double> split : exp.calculateSplits().entrySet()) {
                Member participant = split.getKey();
                balances.put(participant, balances.getOrDefault(participant, 0.0) - split.getValue());
            }
        }
        return balances;
    }

    public List<String> getSettlementSummary() {
        Map<Member, Double> balances = getNetBalances();
        List<Map.Entry<Member, Double>> debtors = new ArrayList<>();
        List<Map.Entry<Member, Double>> creditors = new ArrayList<>();

        for (Map.Entry<Member, Double> entry : balances.entrySet()) {
            if (entry.getValue() < -0.01) debtors.add(entry);
            else if (entry.getValue() > 0.01) creditors.add(entry);
        }

        Collections.sort(debtors, (a, b) -> Double.compare(a.getValue(), b.getValue()));
        Collections.sort(creditors, (a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<String> settlements = new ArrayList<>();
        int i = 0, j = 0;

        while (i < debtors.size() && j < creditors.size()) {
            Map.Entry<Member, Double> debtor = debtors.get(i);
            Map.Entry<Member, Double> creditor = creditors.get(j);

            double settled = Math.min(-debtor.getValue(), creditor.getValue());
            settlements.add(String.format("%s owes %s -> Rs. %.2f", debtor.getKey().getName(), creditor.getKey().getName(), settled));

            debtor.setValue(debtor.getValue() + settled);
            creditor.setValue(creditor.getValue() - settled);

            if (Math.abs(debtor.getValue()) < 0.01) i++;
            if (Math.abs(creditor.getValue()) < 0.01) j++;
        }

        if (settlements.isEmpty()) settlements.add("All settled up! No pending dues.");
        return settlements;
    }

    @Override
    public String toString() {
        return groupName;
    }
}
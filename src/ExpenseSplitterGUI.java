import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseSplitterGUI extends JFrame {

    private static final String DATA_FILE = "expense_data.ser";

    // Soft Pastel Color Palette
    private static final Color BG_MAIN = new Color(248, 249, 252);
    private static final Color BG_CARD = new Color(255, 255, 255);
    private static final Color PASTEL_PRIMARY = new Color(216, 226, 255); // Soft Periwinkle Blue
    private static final Color PASTEL_ACCENT = new Color(214, 245, 232);  // Soft Mint
    private static final Color PASTEL_WARN = new Color(255, 220, 224);    // Soft Rose (Delete buttons)
    private static final Color TEXT_DARK = new Color(40, 44, 52);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    private final ArrayList<Group> groups = new ArrayList<>();
    private Group currentGroup = null;

    // UI Components
    private final DefaultListModel<Group> groupListModel = new DefaultListModel<>();
    private final JList<Group> groupJList = new JList<>(groupListModel);
    private final DefaultListModel<Member> memberListModel = new DefaultListModel<>();
    private final JList<Member> memberJList = new JList<>(memberListModel);

    private final DefaultTableModel expenseTableModel = new DefaultTableModel(
            new String[]{"Date", "Description", "Type", "Paid By", "Amount (Rs.)"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Prevent accidental cell text editing
        }
    };
    private final JTable expenseTable = new JTable(expenseTableModel);

    private final JTextArea netBalanceArea = new JTextArea();
    private final JTextArea settlementArea = new JTextArea();
    private final JLabel activeGroupHeader = new JLabel("Select or create a group to begin");

    public ExpenseSplitterGUI() {
        setTitle("Smart Expense Splitter - OOP Edition");
        setSize(1080, 700);
        setMinimumSize(new Dimension(960, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout(16, 16));

        ((JComponent) getContentPane()).setBorder(new EmptyBorder(16, 16, 16, 16));

        add(createSidebarPanel(), BorderLayout.WEST);
        add(createMainWorkspacePanel(), BorderLayout.CENTER);

        // Load saved data on startup
        loadData();
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new GridLayout(2, 1, 0, 16));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(280, 0));

        // 1. Groups Card
        JPanel groupCard = createStyledCard("1. Groups");
        groupJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupJList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        groupJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentGroup = groupJList.getSelectedValue();
                refreshWorkspace();
            }
        });

        JPanel groupBtnPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        groupBtnPanel.setOpaque(false);
        JButton addGroupBtn = createPastelButton("+ Create", PASTEL_PRIMARY);
        JButton delGroupBtn = createPastelButton("- Delete", PASTEL_WARN);
        addGroupBtn.addActionListener(e -> handleCreateGroup());
        delGroupBtn.addActionListener(e -> handleDeleteGroup());
        groupBtnPanel.add(addGroupBtn);
        groupBtnPanel.add(delGroupBtn);

        groupCard.add(new JScrollPane(groupJList), BorderLayout.CENTER);
        groupCard.add(groupBtnPanel, BorderLayout.SOUTH);

        // 2. Members Card
        JPanel memberCard = createStyledCard("2. Group Members");
        memberJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberJList.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel memberBtnPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        memberBtnPanel.setOpaque(false);
        JButton addMemberBtn = createPastelButton("+ Add", PASTEL_ACCENT);
        JButton delMemberBtn = createPastelButton("- Remove", PASTEL_WARN);
        addMemberBtn.addActionListener(e -> handleAddMember());
        delMemberBtn.addActionListener(e -> handleDeleteMember());
        memberBtnPanel.add(addMemberBtn);
        memberBtnPanel.add(delMemberBtn);

        memberCard.add(new JScrollPane(memberJList), BorderLayout.CENTER);
        memberCard.add(memberBtnPanel, BorderLayout.SOUTH);

        sidebar.add(groupCard);
        sidebar.add(memberCard);
        return sidebar;
    }

    private JPanel createMainWorkspacePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setOpaque(false);

        // Top Bar
        JPanel headerBar = new JPanel(new BorderLayout(12, 0));
        headerBar.setBackground(BG_CARD);
        headerBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        activeGroupHeader.setFont(new Font("SansSerif", Font.BOLD, 18));
        activeGroupHeader.setForeground(TEXT_DARK);

        JButton addExpenseBtn = createPastelButton("+ Add New Expense", PASTEL_PRIMARY);
        addExpenseBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        addExpenseBtn.addActionListener(e -> openAddExpenseDialog());

        headerBar.add(activeGroupHeader, BorderLayout.WEST);
        headerBar.add(addExpenseBtn, BorderLayout.EAST);

        // Center Content: Split between Expense History and Settlement Summary
        JPanel centerGrid = new JPanel(new GridLayout(2, 1, 0, 16));
        centerGrid.setOpaque(false);

        // Top Half: Expense Table + View Details & Delete Buttons
        JPanel tableCard = createStyledCard("Expense History (Double-click a row or click 'View Expense Details')");
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        expenseTable.setRowHeight(28);
        expenseTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        expenseTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        expenseTable.getTableHeader().setBackground(PASTEL_PRIMARY);

        // Double-click row to view details
        expenseTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && expenseTable.getSelectedRow() != -1) {
                    handleViewExpenseDetails();
                }
            }
        });

        JPanel tableBottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tableBottomBar.setOpaque(false);

        JButton viewDetailsBtn = createPastelButton("View Expense Details", PASTEL_ACCENT);
        viewDetailsBtn.addActionListener(e -> handleViewExpenseDetails());

        JButton deleteExpenseBtn = createPastelButton("- Delete Expense", PASTEL_WARN);
        deleteExpenseBtn.addActionListener(e -> handleDeleteExpense());

        tableBottomBar.add(viewDetailsBtn);
        tableBottomBar.add(deleteExpenseBtn);

        tableCard.add(new JScrollPane(expenseTable), BorderLayout.CENTER);
        tableCard.add(tableBottomBar, BorderLayout.SOUTH);

        // Bottom Half: Net Balances & Who Owes Whom
        JPanel summaryGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        summaryGrid.setOpaque(false);

        JPanel netBalanceCard = createStyledCard("Member Net Balances");
        netBalanceArea.setEditable(false);
        netBalanceArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        netBalanceArea.setMargin(new Insets(8, 8, 8, 8));
        netBalanceCard.add(new JScrollPane(netBalanceArea), BorderLayout.CENTER);

        JPanel settlementCard = createStyledCard("Settlement Summary (Who Owes Whom)");
        settlementArea.setEditable(false);
        settlementArea.setFont(new Font("Monospaced", Font.BOLD, 13));
        settlementArea.setBackground(new Color(250, 253, 250));
        settlementArea.setMargin(new Insets(8, 8, 8, 8));
        settlementCard.add(new JScrollPane(settlementArea), BorderLayout.CENTER);

        summaryGrid.add(netBalanceCard);
        summaryGrid.add(settlementCard);

        centerGrid.add(tableCard);
        centerGrid.add(summaryGrid);

        mainPanel.add(headerBar, BorderLayout.NORTH);
        mainPanel.add(centerGrid, BorderLayout.CENTER);
        return mainPanel;
    }

    // --- Serialization Methods ---

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(groups);
        } catch (IOException e) {
            showError("Failed to save data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                ArrayList<Group> loaded = (ArrayList<Group>) ois.readObject();
                if (loaded != null) {
                    groups.clear();
                    groups.addAll(loaded);
                    groupListModel.clear();
                    for (Group g : groups) {
                        groupListModel.addElement(g);
                    }
                    if (!groups.isEmpty()) {
                        groupJList.setSelectedIndex(0);
                        currentGroup = groups.get(0);
                    }
                    refreshWorkspace();
                }
            } catch (Exception e) {
                showError("Failed to load saved data.");
            }
        }
    }

    // --- Event Handlers ---

    private void handleCreateGroup() {
        String name = JOptionPane.showInputDialog(this, "Enter new group name (e.g., Jaipur Trip, Flatmates):",
                "Create Group", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;
        try {
            Group g = new Group(name);
            groups.add(g);
            groupListModel.addElement(g);
            groupJList.setSelectedValue(g, true);
            saveData();
        } catch (InvalidExpenseException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleDeleteGroup() {
        if (currentGroup == null) {
            showError("Please select a group to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the group '" + currentGroup.getGroupName() + "' and all its expenses?",
                "Confirm Delete Group",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            groups.remove(currentGroup);
            groupListModel.removeElement(currentGroup);
            currentGroup = groupListModel.isEmpty() ? null : groupListModel.firstElement();
            if (currentGroup != null) {
                groupJList.setSelectedValue(currentGroup, true);
            } else {
                refreshWorkspace();
            }
            saveData();
        }
    }

    private void handleAddMember() {
        if (currentGroup == null) {
            showError("Please select or create a group first.");
            return;
        }
        String name = JOptionPane.showInputDialog(this, "Enter member name:",
                "Add Member to " + currentGroup.getGroupName(), JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;
        try {
            currentGroup.addMember(name);
            saveData();
            refreshWorkspace();
        } catch (InvalidExpenseException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleDeleteMember() {
        if (currentGroup == null) {
            showError("Please select a group first.");
            return;
        }
        Member selectedMember = memberJList.getSelectedValue();
        if (selectedMember == null) {
            showError("Please select a member from the Group Members list to remove.");
            return;
        }
        try {
            currentGroup.removeMember(selectedMember);
            saveData();
            refreshWorkspace();
        } catch (InvalidExpenseException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleViewExpenseDetails() {
        if (currentGroup == null) {
            showError("Please select a group first.");
            return;
        }
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an expense row from the table first.");
            return;
        }

        Expense exp = currentGroup.getExpenses().get(selectedRow);
        Map<Member, Double> splits = exp.calculateSplits();

        StringBuilder details = new StringBuilder();
        details.append("=========================================\n");
        details.append("             EXPENSE RECEIPT             \n");
        details.append("=========================================\n");
        details.append(String.format("Description  : %s\n", exp.getDescription()));
        details.append(String.format("Date & Time  : %s\n", exp.getFormattedTimestamp()));
        details.append(String.format("Split Type   : %s\n", exp.getExpenseTypeLabel()));
        details.append(String.format("Paid By      : %s\n", exp.getPaidBy().getName()));
        details.append(String.format("Total Amount : Rs. %.2f\n", exp.getTotalAmount()));
        details.append("-----------------------------------------\n");
        details.append("Individual Share Breakdown:\n\n");

        for (Member m : currentGroup.getMembers()) {
            if (splits.containsKey(m)) {
                double share = splits.get(m);
                String note = m.equals(exp.getPaidBy())
                        ? "(Paid the bill)"
                        : "(Owes " + exp.getPaidBy().getName() + ")";
                details.append(String.format(" * %-12s : Rs. %8.2f  %s\n", m.getName(), share, note));
            } else {
                details.append(String.format(" * %-12s : Rs.     0.00  (Not involved)\n", m.getName()));
            }
        }
        details.append("=========================================\n");

        JTextArea detailArea = new JTextArea(details.toString());
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        detailArea.setBackground(BG_CARD);
        detailArea.setMargin(new Insets(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(detailArea);
        scrollPane.setPreferredSize(new Dimension(440, 320));
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Expense Details - " + exp.getDescription(),
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void handleDeleteExpense() {
        if (currentGroup == null) {
            showError("Please select a group first.");
            return;
        }
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please click on an expense row in the Expense History table to delete it.");
            return;
        }
        try {
            currentGroup.removeExpense(selectedRow);
            saveData();
            refreshWorkspace();
        } catch (InvalidExpenseException ex) {
            showError(ex.getMessage());
        }
    }

    private void openAddExpenseDialog() {
        if (currentGroup == null) {
            showError("Please select a group first.");
            return;
        }
        if (currentGroup.getMembers().size() < 2) {
            showError("Add at least 2 members to the group before splitting expenses.");
            return;
        }

        JDialog dialog = new JDialog(this, "Add Expense - " + currentGroup.getGroupName(), true);
        dialog.setSize(460, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(12, 12));
        ((JComponent) dialog.getContentPane()).setBorder(new EmptyBorder(16, 16, 16, 16));
        dialog.getContentPane().setBackground(BG_CARD);

        // Top Form Fields
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setOpaque(false);

        JTextField descField = new JTextField();
        JTextField amountField = new JTextField();
        JComboBox<Member> payerCombo = new JComboBox<>(currentGroup.getMembers().toArray(new Member[0]));

        JRadioButton commonRadio = new JRadioButton("Common Expense (Equal)", true);
        JRadioButton individualRadio = new JRadioButton("Individual / Custom Split");
        commonRadio.setOpaque(false);
        individualRadio.setOpaque(false);
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(commonRadio);
        typeGroup.add(individualRadio);

        formPanel.add(new JLabel("Description:"));
        formPanel.add(descField);
        formPanel.add(new JLabel("Total Amount (Rs.):"));
        formPanel.add(amountField);
        formPanel.add(new JLabel("Paid By:"));
        formPanel.add(payerCombo);
        formPanel.add(commonRadio);
        formPanel.add(individualRadio);

        // Dynamic CardLayout Panel for Common vs Individual Split
        CardLayout cardLayout = new CardLayout();
        JPanel dynamicSplitPanel = new JPanel(cardLayout);
        dynamicSplitPanel.setBorder(BorderFactory.createTitledBorder("Split Configuration"));
        dynamicSplitPanel.setOpaque(false);

        // Card 1: Common Expense (Checkboxes for who participated)
        JPanel commonPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        commonPanel.setOpaque(false);
        Map<Member, JCheckBox> participantChecks = new HashMap<>();
        for (Member m : currentGroup.getMembers()) {
            JCheckBox cb = new JCheckBox(m.getName() + " (Equal Share)", true);
            cb.setOpaque(false);
            participantChecks.put(m, cb);
            commonPanel.add(cb);
        }

        // Card 2: Individual Expense (Exact custom amount per member)
        JPanel individualPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        individualPanel.setOpaque(false);
        Map<Member, JTextField> customAmountFields = new HashMap<>();
        for (Member m : currentGroup.getMembers()) {
            individualPanel.add(new JLabel(m.getName() + "'s share (Rs.):"));
            JTextField tf = new JTextField("0.0");
            customAmountFields.put(m, tf);
            individualPanel.add(tf);
        }

        dynamicSplitPanel.add(new JScrollPane(commonPanel), "COMMON");
        dynamicSplitPanel.add(new JScrollPane(individualPanel), "INDIVIDUAL");

        commonRadio.addActionListener(e -> cardLayout.show(dynamicSplitPanel, "COMMON"));
        individualRadio.addActionListener(e -> cardLayout.show(dynamicSplitPanel, "INDIVIDUAL"));

        // Save Button
        JButton saveBtn = createPastelButton("Save & Split Expense", PASTEL_ACCENT);
        saveBtn.addActionListener(e -> {
            try {
                String desc = descField.getText();
                double amount;
                try {
                    amount = Double.parseDouble(amountField.getText().trim());
                } catch (NumberFormatException nfe) {
                    throw new InvalidExpenseException("Please enter a valid numeric total amount.");
                }

                Member payer = (Member) payerCombo.getSelectedItem();
                Expense newExpense;

                if (commonRadio.isSelected()) {
                    List<Member> selectedMembers = new ArrayList<>();
                    for (Map.Entry<Member, JCheckBox> entry : participantChecks.entrySet()) {
                        if (entry.getValue().isSelected()) {
                            selectedMembers.add(entry.getKey());
                        }
                    }
                    newExpense = new CommonExpense(desc, amount, payer, selectedMembers);
                } else {
                    Map<Member, Double> customSplits = new HashMap<>();
                    for (Map.Entry<Member, JTextField> entry : customAmountFields.entrySet()) {
                        String valStr = entry.getValue().getText().trim();
                        double share = valStr.isEmpty() ? 0.0 : Double.parseDouble(valStr);
                        if (share > 0) {
                            customSplits.put(entry.getKey(), share);
                        }
                    }
                    newExpense = new IndividualExpense(desc, amount, payer, customSplits);
                }

                currentGroup.addExpense(newExpense);
                saveData();
                refreshWorkspace();
                dialog.dispose();

            } catch (NumberFormatException ex) {
                showError("One of the individual share fields contains an invalid number.");
            } catch (InvalidExpenseException ex) {
                showError(ex.getMessage());
            }
        });

        dialog.add(formPanel, BorderLayout.NORTH);
        dialog.add(dynamicSplitPanel, BorderLayout.CENTER);
        dialog.add(saveBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void refreshWorkspace() {
        memberListModel.clear();
        expenseTableModel.setRowCount(0);
        netBalanceArea.setText("");
        settlementArea.setText("");

        if (currentGroup == null) {
            activeGroupHeader.setText("Select or create a group to begin");
            return;
        }

        activeGroupHeader.setText("Group: " + currentGroup.getGroupName());

        for (Member m : currentGroup.getMembers()) {
            memberListModel.addElement(m);
        }

        for (Expense exp : currentGroup.getExpenses()) {
            expenseTableModel.addRow(new Object[]{
                    exp.getFormattedTimestamp(),
                    exp.getDescription(),
                    exp.getExpenseTypeLabel(),
                    exp.getPaidBy().getName(),
                    String.format("%.2f", exp.getTotalAmount())
            });
        }

        // Update Net Balances
        StringBuilder balanceBuilder = new StringBuilder();
        Map<Member, Double> netBalances = currentGroup.getNetBalances();
        for (Member m : currentGroup.getMembers()) {
            double net = netBalances.getOrDefault(m, 0.0);
            String status = net > 0.01 ? "(gets back)" : (net < -0.01 ? "(owes)" : "(settled)");
            balanceBuilder.append(String.format("%-12s : %+9.2f Rs.  %s\n", m.getName(), net, status));
        }
        netBalanceArea.setText(balanceBuilder.toString());

        // Update Settlement Summary
        StringBuilder settlementBuilder = new StringBuilder();
        for (String line : currentGroup.getSettlementSummary()) {
            settlementBuilder.append("* ").append(line).append("\n\n");
        }
        settlementArea.setText(settlementBuilder.toString());
    }

    // --- UI Helpers ---

    private JPanel createStyledCard(String title) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_DARK);
        card.add(titleLabel, BorderLayout.NORTH);
        return card;
    }

    private JButton createPastelButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ExpenseSplitterGUI().setVisible(true);
        });
    }
} 
# 💸 Smart Expense Splitter (OOP Edition)

A modern, desktop-based expense-sharing application built with **Java Swing** and **Object-Oriented Programming (OOP)** principles. It allows flatmates, travel groups, and project teams to track shared bills, calculate individual net balances, and minimize transaction settlements using an optimized greedy algorithm.

---

## 🌟 Key Features

- **👥 Multi-Group & Member Management**
  - Create and manage isolated groups (e.g., *Jaipur Trip*, *Flatmates*, *Project Dinner*).
  - Add and remove members with state-safety validation (members linked to existing expenses cannot be accidentally deleted).

- **⚖️ Flexible Expense Splitting Modes**
  - **Common Expense (Equal Split):** Distribute costs equally across all or selected participants with dynamic checkboxes.
  - **Individual Expense (Custom Split):** Specify exact custom monetary amounts for each member, featuring validation against the total bill (with floating-point precision tolerance).

- **🧾 Detailed Expense Receipts**
  - View full itemized receipts for any transaction by double-clicking table rows or selecting **View Expense Details**.
  - Displays timestamps, payer information, split mode, and who owes what.

- **🧠 Greedy Debt Simplification Algorithm**
  - Calculates net credit/debit balances across all members.
  - Employs an $O(N \log N)$ two-pointer greedy algorithm to settle balances with the **minimum number of pairwise transactions** ("who owes whom").

- **💾 Automatic Data Persistence**
  - Uses native Java Object Serialization (`expense_data.ser`) to seamlessly auto-save and restore groups, members, and transaction history across app restarts.

- **🎨 Pastel Modern Swing UI**
  - Custom card-based layout with soft pastel color accents, high-contrast typography, and intuitive two-column workspace.

---

## 🏗️ System Architecture & OOP Design

The project is structured according to clean Object-Oriented design principles:

```mermaid
flowchart TD
    subgraph UI ["Presentation Layer (Swing)"]
        GUI["ExpenseSplitterGUI"]
        AddDialog["Add Expense Modal (CardLayout)"]
        ReceiptDialog["Receipt Detail Modal"]
    end

    subgraph Domain ["Domain Model Layer"]
        Group["Group"]
        Member["Member"]
        Expense["abstract Expense"]
        CommonExpense["CommonExpense"]
        IndividualExpense["IndividualExpense"]
    end

    subgraph Logic ["Business & Algorithm Layer"]
        SplitLogic["calculateSplits() (Polymorphic)"]
        NetBal["getNetBalances()"]
        GreedyAlgo["getSettlementSummary() (Two-Pointer Greedy)"]
        ExceptionGuard["InvalidExpenseException"]
    end

    subgraph Storage ["Persistence Layer"]
        SerFile[("expense_data.ser")]
    end

    GUI --> Group
    Group o-- Member
    Group o-- Expense
    Expense <|-- CommonExpense
    Expense <|-- IndividualExpense

    CommonExpense --> SplitLogic
    IndividualExpense --> SplitLogic
    SplitLogic --> NetBal --> GreedyAlgo
    ExceptionGuard -.-> Group
    Group <--> SerFile
```

### OOP Principles Applied

1. **Inheritance & Abstraction:**
   - [`Expense.java`](src/Expense.java) serves as an `abstract` base class defining shared metadata (`description`, `totalAmount`, `paidBy`, `timestamp`) and declaring abstract methods `calculateSplits()` and `getExpenseTypeLabel()`.
   - [`CommonExpense.java`](src/CommonExpense.java) and [`IndividualExpense.java`](src/IndividualExpense.java) inherit from `Expense` and supply concrete split behaviors.

2. **Polymorphism:**
   - The GUI and settlement engines process collections of `Expense` references uniformly; the runtime calculates accurate shares by invoking the polymorphic `calculateSplits()` method without knowing the concrete split subtype.

3. **Encapsulation & Validation:**
   - Domain models protect their state with private fields and expose controlled access.
   - Robust input validation guards total amounts, non-empty names, valid payers, and split sum consistency.

4. **Custom Exception Handling:**
   - [`InvalidExpenseException.java`](src/InvalidExpenseException.java) catches business rule violations and displays user-friendly error dialogs without crashing the runtime.

---

## 📊 Debt Settlement Algorithm

Instead of requiring every debtor to pay every creditor directly (which can require up to $N(N - 1) / 2$ transactions), the app simplifies debt:

1. **Calculate Net Balances:**
   $$\text{Net Balance}_i = \sum \text{Paid By}_i - \sum \text{Owed By}_i$$
2. **Partition & Sort:**
   - Debtors ($\text{Net} < 0$) sorted ascending.
   - Creditors ($\text{Net} > 0$) sorted descending.
3. **Two-Pointer Greedy Matching:**
   - Match the largest debtor with the largest creditor.
   - Settle $\min(|\text{Debtor Balance}|, \text{Creditor Balance})$.
   - Advance pointers when balance reaches zero.

---

## 📁 Project Directory Structure

```text
smartExpenseSplitter/
├── src/
│   ├── CommonExpense.java            # Concrete class for equal expense splits
│   ├── Expense.java                  # Abstract base class for expenses
│   ├── ExpenseSplitterGUI.java       # Main Swing GUI & event controller
│   ├── Group.java                    # Group aggregate (manages members, expenses, balances)
│   ├── IndividualExpense.java        # Concrete class for custom amount splits
│   ├── InvalidExpenseException.java  # Custom checked exception class
│   ├── Member.java                   # Member entity with case-insensitive identity
│   └── expense_data.ser              # Auto-generated serialized state file
├── .vscode/
│   └── settings.json                 # Editor workspace configuration
└── README.md                         # Project documentation
```

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK) 8** or higher (JDK 17 or 21 recommended).
- Any standard IDE (VS Code, IntelliJ IDEA, Eclipse) or command-line terminal.

---

### Running from the Command Line

1. **Clone or navigate into the project directory:**
   ```bash
   cd c:\Users\hp\Desktop\smartExpenseSplitter
   ```

2. **Compile the source files:**
   ```bash
   javac -d bin src/*.java
   ```
   *(Or compile directly in `src` if preferred: `javac src/*.java`)*

3. **Run the application:**
   ```bash
   java -cp bin ExpenseSplitterGUI
   ```
   *(If compiled in `src`, run: `java -cp src ExpenseSplitterGUI`)*

---

### Running in VS Code

1. Open the `smartExpenseSplitter` folder in VS Code.
2. Ensure the **Extension Pack for Java** is installed.
3. Open [`src/ExpenseSplitterGUI.java`](src/ExpenseSplitterGUI.java).
4. Click the **Run** button at the top-right corner, or press `F5` / `Ctrl + F5`.

---

## 📖 User Guide

1. **Create a Group:**
   - In the left sidebar under **1. Groups**, click **+ Create** and enter a name (e.g., `Goa Trip`).
2. **Add Group Members:**
   - Under **2. Group Members**, click **+ Add** and enter names (at least 2 members are required to split expenses).
3. **Log an Expense:**
   - Click **+ Add New Expense** in the top bar.
   - Enter description, total bill amount, and select who paid.
   - Choose split mode:
     - **Common Expense:** Check which members participated in the bill.
     - **Individual Split:** Specify individual amounts (must sum to total).
   - Click **Save & Split Expense**.
4. **View Receipt & Summaries:**
   - The **Expense History** table lists transactions. Double-click any row to view its receipt breakdown.
   - The **Member Net Balances** panel displays who is in net surplus or deficit.
   - The **Settlement Summary** panel provides the exact minimal transactions needed to clear all balances.

---

## 🛠️ Technologies Used

- **Language:** Java (JDK 8+)
- **GUI Toolkit:** Java Swing & Java AWT
- **Persistence:** Java Object Serialization (`java.io.Serializable`)
- **Architecture:** Layered Object-Oriented Architecture


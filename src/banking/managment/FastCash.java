package banking.managment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class FastCash extends JFrame implements ActionListener {

    private final String pin;
    private JButton tk100, tk500, tk1000, tk2000, tk5000, tk10000, backButton;

    public FastCash(String pin) {
        this.pin = pin;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Fast Cash Withdrawal");
        setSize(1550, 1080);
        setLocation(0, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        // Background setup
        ImageIcon atmImage = new ImageIcon(ClassLoader.getSystemResource("icon/atm2.png"));
        Image scaledImage = atmImage.getImage().getScaledInstance(1550, 830, Image.SCALE_SMOOTH);
        JLabel background = new JLabel(new ImageIcon(scaledImage));
        background.setBounds(0, 0, 1550, 830);
        add(background);

        // Title label
        JLabel titleLabel = new JLabel("SELECT WITHDRAWAL AMOUNT");
        titleLabel.setBounds(445, 180, 700, 35);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("System", Font.BOLD, 23));
        background.add(titleLabel);

        // Create amount buttons
        tk100 = createCashButton("Rs. 100", 410, 274, background);
        tk500 = createCashButton("Rs. 500", 700, 274, background);
        tk1000 = createCashButton("Rs. 1000", 410, 318, background);
        tk2000 = createCashButton("Rs. 2000", 700, 318, background);
        tk5000 = createCashButton("Rs. 5000", 410, 362, background);
        tk10000 = createCashButton("Rs. 10000", 700, 362, background);
        backButton = createCashButton("BACK", 700, 406, background);

        setVisible(true);
    }

    private JButton createCashButton(String text, int x, int y, JLabel background) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(65, 125, 128));
        button.setBounds(x, y, 150, 35);
        button.addActionListener(this);
        background.add(button);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            dispose();
            new main_Class(pin);
            return;
        }

        try {
            String amountText = ((JButton) e.getSource()).getText().substring(4);
            int amount = Integer.parseInt(amountText);
            processWithdrawal(amount);
        } catch (NumberFormatException ex) {
            showError("Invalid amount format");
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void processWithdrawal(int amount) throws SQLException {
        if (amount <= 0) {
            showError("Amount must be positive");
            return;
        }

        try (Connection conn = new Connn().connection) {
            conn.setAutoCommit(false); // Start transaction

            // Check balance
            int currentBalance = getAccountBalance(conn);
            if (currentBalance < amount) {
                showError("Insufficient balance");
                return;
            }

            // Record transaction
            recordTransaction(conn, amount);

            // Update balance
            updateAccountBalance(conn, amount);

            conn.commit(); // Commit transaction
            showSuccess("Rs. " + amount + " debited successfully");

            dispose();
            new main_Class(pin);
        }
    }

    private int getAccountBalance(Connection conn) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE pin = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pin);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("balance") : 0;
        }
    }

    private void recordTransaction(Connection conn, int amount) throws SQLException {
        String sql = "INSERT INTO bank (pin, transaction_date, transaction_type, amount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pin);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setString(3, "Withdrawal");
            stmt.setInt(4, amount);
            stmt.executeUpdate();
        }
    }

    private void updateAccountBalance(Connection conn, int amount) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance - ? WHERE pin = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, amount);
            stmt.setString(2, pin);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated == 0) {
                throw new SQLException("Account not found");
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FastCash("test123"));
    }
}
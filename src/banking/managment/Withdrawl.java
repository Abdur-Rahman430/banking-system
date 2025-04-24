package banking.managment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;

public class Withdrawl extends JFrame implements ActionListener {

    private final String pin;
    private JTextField textField; // Changed from TextField to JTextField
    private JButton withdrawButton, backButton;

    public Withdrawl(String pin) {
        this.pin = pin;
        initializeUI();
    }

    private void initializeUI() {
        // Window setup
        setTitle("Withdrawal");
        setSize(1550, 1080);
        setLocation(0, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        // Background image
        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icon/atm2.png"));
        Image i2 = i1.getImage().getScaledInstance(1550, 830, Image.SCALE_SMOOTH);
        JLabel background = new JLabel(new ImageIcon(i2));
        background.setBounds(0, 0, 1550, 830);
        add(background);

        // Labels
        JLabel maxWithdrawalLabel = new JLabel("MAXIMUM WITHDRAWAL IS RS.10,000");
        maxWithdrawalLabel.setForeground(Color.WHITE);
        maxWithdrawalLabel.setFont(new Font("System", Font.BOLD, 16));
        maxWithdrawalLabel.setBounds(460, 180, 700, 35);
        background.add(maxWithdrawalLabel);

        JLabel amountLabel = new JLabel("PLEASE ENTER YOUR AMOUNT");
        amountLabel.setForeground(Color.WHITE);
        amountLabel.setFont(new Font("System", Font.BOLD, 16));
        amountLabel.setBounds(460, 220, 400, 35);
        background.add(amountLabel);

        // Text field
        textField = new JTextField();
        textField.setBackground(new Color(65, 125, 128));
        textField.setForeground(Color.WHITE);
        textField.setBounds(460, 260, 320, 25);
        textField.setFont(new Font("Raleway", Font.BOLD, 22));
        background.add(textField);

        // Buttons
        withdrawButton = new JButton("WITHDRAW");
        withdrawButton.setBounds(700, 362, 150, 35);
        withdrawButton.setBackground(new Color(65, 125, 128));
        withdrawButton.setForeground(Color.WHITE);
        withdrawButton.addActionListener(this);
        background.add(withdrawButton);

        backButton = new JButton("BACK");
        backButton.setBounds(700, 406, 150, 35);
        backButton.setBackground(new Color(65, 125, 128));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(this);
        background.add(backButton);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == withdrawButton) {
            processWithdrawal();
        } else if (e.getSource() == backButton) {
            dispose();
            new main_Class(pin);
        }
    }

    private void processWithdrawal() {
        String amountText = textField.getText().trim();

        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the amount you want to withdraw",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int amount = Integer.parseInt(amountText);

            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter a positive amount",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (amount > 10000) {
                JOptionPane.showMessageDialog(this, "Maximum withdrawal is Rs. 10,000",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Connn c = new Connn();
            try {
                c.connection.setAutoCommit(false); // Start transaction

                // Check current balance from accounts table
                int balance = getCurrentBalance(c.connection);

                if (balance < amount) {
                    JOptionPane.showMessageDialog(this, "Insufficient Balance",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Record withdrawal transaction
                recordTransaction(c.connection, amount);

                // Update account balance
                updateBalance(c.connection, amount);

                c.connection.commit();
                JOptionPane.showMessageDialog(this, "Rs. " + amount + " Debited Successfully");
                dispose();
                new main_Class(pin);

            } catch (SQLException ex) {
                c.connection.rollback();
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                c.connection.setAutoCommit(true);
                c.connection.close();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private int getCurrentBalance(Connection conn) throws SQLException {
        String query = "SELECT balance FROM accounts WHERE pin = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, pin);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("balance") : 0;
        }
    }

    private void recordTransaction(Connection conn, int amount) throws SQLException {
        String query = "INSERT INTO bank (pin, transaction_date, transaction_type, amount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, pin);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setString(3, "Withdrawl");
            stmt.setInt(4, amount);
            stmt.executeUpdate();
        }
    }

    private void updateBalance(Connection conn, int amount) throws SQLException {
        String query = "UPDATE accounts SET balance = balance - ? WHERE pin = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, amount);
            stmt.setString(2, pin);
            int updated = stmt.executeUpdate();

            if (updated == 0) {
                throw new SQLException("Account not found");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Withdrawl("test123"));
    }
}
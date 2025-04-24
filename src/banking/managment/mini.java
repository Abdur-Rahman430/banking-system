package banking.managment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.math.BigDecimal;

public class mini extends JFrame implements ActionListener {
    private final String pin;
    private JButton exitButton;
    private JLabel statementLabel, bankLabel, cardLabel, balanceLabel;

    public mini(String pin) {
        this.pin = pin;
        initializeUI();
        loadStatementData();
    }

    private void initializeUI() {
        // Window configuration
        setTitle("Mini Statement");
        getContentPane().setBackground(new Color(255, 204, 204));
        setSize(400, 600);
        setLocation(20, 20);
        setLayout(null);

        // Bank name label
        bankLabel = new JLabel("TechCoder A.V");
        bankLabel.setFont(new Font("System", Font.BOLD, 15));
        bankLabel.setBounds(150, 20, 200, 20);
        add(bankLabel);

        // Card number label
        cardLabel = new JLabel();
        cardLabel.setBounds(20, 80, 300, 20);
        add(cardLabel);

        // Statement label
        statementLabel = new JLabel();
        statementLabel.setBounds(20, 140, 400, 200);
        add(statementLabel);

        // Balance label
        balanceLabel = new JLabel();
        balanceLabel.setBounds(20, 400, 300, 20);
        add(balanceLabel);

        // Exit button
        exitButton = new JButton("Exit");
        exitButton.setBounds(20, 500, 100, 25);
        exitButton.addActionListener(this);
        exitButton.setBackground(Color.BLACK);
        exitButton.setForeground(Color.WHITE);
        add(exitButton);

        setVisible(true);
    }

    private void loadStatementData() {
        try (Connection conn = new Connn().connection) {
            // Load card information
            loadCardDetails(conn);

            // Load transactions and calculate balance
            loadTransactions(conn);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error: " + e.getMessage());
        }
    }

    private void loadCardDetails(Connection conn) throws SQLException {
        String query = "SELECT card_number FROM login WHERE pin = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, pin);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String cardNumber = rs.getString("card_number");
                String maskedCard = cardNumber.substring(0, 4) + "XXXXXXXX" +
                        (cardNumber.length() > 12 ? cardNumber.substring(12) : "");
                cardLabel.setText("Card Number: " + maskedCard);
            }
        }
    }

    private void loadTransactions(Connection conn) throws SQLException {
        String query = "SELECT transaction_date, transaction_type, amount FROM bank WHERE pin = ? ORDER BY transaction_date DESC";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, pin);
            ResultSet rs = stmt.executeQuery();

            StringBuilder statementText = new StringBuilder("<html>");
            BigDecimal balance = BigDecimal.ZERO;

            while (rs.next()) {
                String date = rs.getString("transaction_date");
                String type = rs.getString("transaction_type");
                BigDecimal amount = rs.getBigDecimal("amount");

                statementText.append(date)
                        .append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                        .append(type)
                        .append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                        .append(String.format("%,.2f", amount))
                        .append("<br><br>");

                if ("Deposit".equalsIgnoreCase(type)) {
                    balance = balance.add(amount);
                } else {
                    balance = balance.subtract(amount);
                }
            }

            statementLabel.setText(statementText.toString());
            balanceLabel.setText(String.format("Your Total Balance is Rs %,.2f", balance));
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new mini("1234"));
    }
}
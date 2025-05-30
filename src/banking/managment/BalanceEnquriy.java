package banking.managment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class BalanceEnquriy extends JFrame implements ActionListener {

    String pin;
    JLabel label2;
    JButton b1;

    BalanceEnquriy(String pin) {
        this.pin = pin;

        // UI Setup (unchanged from your original)
        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icon/atm2.png"));
        Image i2 = i1.getImage().getScaledInstance(1550, 830, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel l3 = new JLabel(i3);
        l3.setBounds(0, 0, 1550, 830);
        add(l3);

        JLabel label1 = new JLabel("Your Current Balance is Rs ");
        label1.setForeground(Color.WHITE);
        label1.setFont(new Font("System", Font.BOLD, 16));
        label1.setBounds(430, 180, 700, 35);
        l3.add(label1);

        label2 = new JLabel();
        label2.setForeground(Color.WHITE);
        label2.setFont(new Font("System", Font.BOLD, 16));
        label2.setBounds(430, 220, 400, 35);
        l3.add(label2);

        b1 = new JButton("Back");
        b1.setBounds(700, 406, 150, 35);
        b1.setBackground(new Color(65, 125, 128));
        b1.setForeground(Color.WHITE);
        b1.addActionListener(this);
        l3.add(b1);

        // Fixed balance calculation
        calculateBalance();

        setLayout(null);
        setSize(1550, 1080);
        setLocation(0, 0);
        setVisible(true);
    }

    private void calculateBalance() {
        int balance = 0;
        try {
            Connn c = new Connn();

            // Option 1: Get balance directly from accounts table (recommended)
            String query = "SELECT balance FROM accounts WHERE pin = ?";
            PreparedStatement stmt = c.connection.prepareStatement(query);
            stmt.setString(1, pin);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                balance = rs.getInt("balance");
            } else {
                // Option 2: Calculate from transactions if account doesn't exist
                ResultSet resultSet = c.statement.executeQuery(
                        "SELECT type, amount FROM bank WHERE pin = '" + pin + "'");

                while (resultSet.next()) {
                    if ("Deposit".equals(resultSet.getString("type"))) {
                        balance += resultSet.getInt("amount");
                    } else {
                        balance -= resultSet.getInt("amount");
                    }
                }

                // Create account if it doesn't exist
                if (balance != 0) {
                    String insertQuery = "INSERT INTO accounts (pin, balance) VALUES (?, ?)";
                    PreparedStatement insertStmt = c.connection.prepareStatement(insertQuery);
                    insertStmt.setString(1, pin);
                    insertStmt.setInt(2, balance);
                    insertStmt.executeUpdate();
                }
            }

            label2.setText(String.format("%,d", balance));

        } catch (Exception e) {
            e.printStackTrace();
            label2.setText("Error");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        new main_Class(pin);
    }

    public static void main(String[] args) {
        new BalanceEnquriy("1234"); // Test with actual PIN
    }
}
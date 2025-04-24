package banking.managment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Date;

public class Deposit extends JFrame implements ActionListener {
    String pin;
    JTextField textField; // Changed from TextField to JTextField
    JButton b1, b2;

    Deposit(String pin) {
        this.pin = pin;

        // Your existing UI setup code...
        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icon/atm2.png"));
        Image i2 = i1.getImage().getScaledInstance(1550, 830, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel l3 = new JLabel(i3);
        l3.setBounds(0, 0, 1550, 830);
        add(l3);

        JLabel label1 = new JLabel("ENTER AMOUNT YOU WANT TO DEPOSIT");
        label1.setForeground(Color.WHITE);
        label1.setFont(new Font("System", Font.BOLD, 16));
        label1.setBounds(460, 180, 400, 35);
        l3.add(label1);

        textField = new JTextField(); // Changed to JTextField
        textField.setBackground(new Color(65, 125, 128));
        textField.setForeground(Color.WHITE);
        textField.setBounds(460, 230, 320, 25);
        textField.setFont(new Font("Raleway", Font.BOLD, 22));
        l3.add(textField);

        b1 = new JButton("DEPOSIT");
        b1.setBounds(700, 362, 150, 35);
        b1.setBackground(new Color(65, 125, 128));
        b1.setForeground(Color.WHITE);
        b1.addActionListener(this);
        l3.add(b1);

        b2 = new JButton("BACK");
        b2.setBounds(700, 406, 150, 35);
        b2.setBackground(new Color(65, 125, 128));
        b2.setForeground(Color.WHITE);
        b2.addActionListener(this);
        l3.add(b2);

        setLayout(null);
        setSize(1550, 1080);
        setLocation(0, 0);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == b2) {
            setVisible(false);
            new main_Class(pin);
            return;
        }

        String amountText = textField.getText().trim();

        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter the Amount you want to Deposit");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(null, "Please enter a positive amount");
                return;
            }

            Connn c = new Connn();
            try {
                // Verify connection
                if (c.connection == null || c.connection.isClosed()) {
                    JOptionPane.showMessageDialog(null, "Database connection not available");
                    return;
                }

                // Start transaction
                c.connection.setAutoCommit(false);

                // 1. Update or create account
                String updateQuery = "UPDATE accounts SET balance = balance + ? WHERE pin = ?";
                try (PreparedStatement updateStatement = c.connection.prepareStatement(updateQuery)) {
                    updateStatement.setDouble(1, amount);
                    updateStatement.setString(2, pin);
                    int rowsUpdated = updateStatement.executeUpdate();

                    if (rowsUpdated == 0) {
                        String createAccountQuery = "INSERT INTO accounts (pin, balance) VALUES (?, ?)";
                        try (PreparedStatement createStatement = c.connection.prepareStatement(createAccountQuery)) {
                            createStatement.setString(1, pin);
                            createStatement.setDouble(2, amount);
                            createStatement.executeUpdate();
                        }
                    }
                }

                // 2. Record transaction
                String insertQuery = "INSERT INTO bank (pin, transaction_date, transaction_type, amount) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStatement = c.connection.prepareStatement(insertQuery)) {
                    insertStatement.setString(1, pin);
                    insertStatement.setTimestamp(2, new Timestamp(new Date().getTime()));
                    insertStatement.setString(3, "Deposit");
                    insertStatement.setDouble(4, amount);
                    insertStatement.executeUpdate();
                }

                c.connection.commit();
                JOptionPane.showMessageDialog(null, "Rs. " + amount + " Deposited Successfully");
                setVisible(false);
                new main_Class(pin);

            } catch (SQLException ex) {
                try {
                    if (c.connection != null) {
                        c.connection.rollback();
                    }
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    if (c.connection != null) {
                        c.connection.setAutoCommit(true);
                        c.connection.close();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Please enter a valid number");
        }
    }

    public static void main(String[] args) {
        new Deposit("");
    }
}
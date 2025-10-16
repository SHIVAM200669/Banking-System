import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Deposit extends JFrame {
    private int userId;
    private Dashboard dashboard;

    public Deposit(int userId, Dashboard dashboard) {
        this.userId = userId;
        this.dashboard = dashboard;

        setTitle("Deposit");
        setSize(350, 180);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel center = new JPanel(new GridLayout(3, 1, 5, 5));
        center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField amountField = new JTextField();
        amountField.setColumns(10);
        center.add(new JLabel("Amount to deposit:"));
        center.add(amountField);

        JButton depositBtn = new JButton("Deposit");
        depositBtn.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(amountField.getText());
                if (amt <= 0) {
                    JOptionPane.showMessageDialog(this, "Enter a positive amount");
                    return;
                }

                try (Connection con = DBConnection.getConnection()) {
                    con.setAutoCommit(false);

                    PreparedStatement updateBalance = con.prepareStatement(
                            "UPDATE accounts SET balance = balance + ? WHERE user_id = ?"
                    );
                    updateBalance.setDouble(1, amt);
                    updateBalance.setInt(2, userId);
                    int updatedRows = updateBalance.executeUpdate();

                    if (updatedRows == 0) {
                        JOptionPane.showMessageDialog(this, "Account not found");
                        return;
                    }

                    PreparedStatement insertTransaction = con.prepareStatement(
                            "INSERT INTO transactions(acc_no, type, amount) " +
                                    "SELECT acc_no, ?, ? FROM accounts WHERE user_id = ?"
                    );
                    insertTransaction.setString(1, "Deposit");
                    insertTransaction.setDouble(2, amt);
                    insertTransaction.setInt(3, userId);
                    insertTransaction.executeUpdate();

                    con.commit();

                    JOptionPane.showMessageDialog(this, "Deposited ₹" + amt, "Success", JOptionPane.INFORMATION_MESSAGE);
                    dashboard.refreshDashboard();
                    dispose();

                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Database error: " + sqlEx.getMessage());
                }

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Enter a valid number");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        center.add(depositBtn);
        add(center, BorderLayout.CENTER);
        setVisible(true);
    }
}

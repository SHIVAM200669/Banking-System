import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Withdraw extends JFrame {
    private int userId;
    private Dashboard dashboard;

    public Withdraw(int userId, Dashboard dashboard) {
        this.userId = userId;
        this.dashboard = dashboard;

        setTitle("Withdraw");
        setSize(350, 180);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel center = new JPanel(new GridLayout(3, 1, 5, 5));
        center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField amountField = new JTextField();
        center.add(new JLabel("Amount to withdraw:"));
        center.add(amountField);

        JButton withdrawBtn = new JButton("Withdraw");
        withdrawBtn.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(amountField.getText().trim());
                if (amt <= 0) {
                    JOptionPane.showMessageDialog(this, "Enter a positive amount");
                    return;
                }

                try (Connection con = DBConnection.getConnection()) {
                    con.setAutoCommit(false); // start transaction
                    try {
                        PreparedStatement ps = con.prepareStatement(
                                "SELECT acc_no, balance FROM accounts WHERE user_id=? LIMIT 1"
                        );
                        ps.setInt(1, userId);
                        ResultSet rs = ps.executeQuery();

                        if (rs.next()) {
                            int acc = rs.getInt("acc_no");
                            double balance = rs.getDouble("balance");

                            if (balance < amt) {
                                JOptionPane.showMessageDialog(this, "Insufficient funds");
                                return;
                            }

                            PreparedStatement ups = con.prepareStatement(
                                    "UPDATE accounts SET balance = balance - ? WHERE acc_no = ? AND balance >= ?"
                            );
                            ups.setDouble(1, amt);
                            ups.setInt(2, acc);
                            ups.setDouble(3, amt);

                            int rowsUpdated = ups.executeUpdate();
                            if (rowsUpdated == 0) {
                                JOptionPane.showMessageDialog(this, "Withdrawal failed, insufficient funds");
                                con.rollback();
                                return;
                            }

                            PreparedStatement ins = con.prepareStatement(
                                    "INSERT INTO transactions(acc_no, type, amount) VALUES(?,?,?)"
                            );
                            ins.setInt(1, acc);
                            ins.setString(2, "Withdraw");
                            ins.setDouble(3, amt);
                            ins.executeUpdate();

                            con.commit();
                            JOptionPane.showMessageDialog(this, "Withdrawn ₹" + amt, "Success", JOptionPane.INFORMATION_MESSAGE);
                            dashboard.refreshDashboard();
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(this, "Account not found");
                        }
                    } catch (Exception ex) {
                        con.rollback();
                        throw ex;
                    }
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Enter a valid number");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        center.add(withdrawBtn);
        add(center, BorderLayout.CENTER);
        setVisible(true);
    }
}

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Transfer extends JFrame {
    private int userId;
    private Dashboard dashboard;

    public Transfer(int userId, Dashboard dashboard) {
        this.userId = userId;
        this.dashboard = dashboard;
        setTitle("Transfer Funds");
        setSize(420,220);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        JPanel p = new JPanel(new GridLayout(5,1,5,5));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JTextField toAccField = new JTextField();
        JTextField amountField = new JTextField();

        p.add(new JLabel("Recipient Account No:")); p.add(toAccField);
        p.add(new JLabel("Amount to transfer:")); p.add(amountField);

        JButton transferBtn = new JButton("Transfer");
        transferBtn.addActionListener(e -> {
            try {
                int toAcc = Integer.parseInt(toAccField.getText().trim());
                double amt = Double.parseDouble(amountField.getText().trim());
                if (amt <= 0) { JOptionPane.showMessageDialog(this, "Enter positive amount"); return; }

                try (Connection con = DBConnection.getConnection()) {
                    con.setAutoCommit(false);

                    PreparedStatement psTo = con.prepareStatement("SELECT acc_no FROM accounts WHERE acc_no=?");
                    psTo.setInt(1, toAcc);
                    ResultSet rsTo = psTo.executeQuery();
                    if (!rsTo.next()) { JOptionPane.showMessageDialog(this, "Recipient account not found"); return; }

                    PreparedStatement updFrom = con.prepareStatement(
                            "UPDATE accounts SET balance = balance - ? WHERE user_id=? AND balance >= ?"
                    );
                    updFrom.setDouble(1, amt);
                    updFrom.setInt(2, userId);
                    updFrom.setDouble(3, amt);
                    int rows = updFrom.executeUpdate();
                    if (rows == 0) {
                        JOptionPane.showMessageDialog(this, "Insufficient funds");
                        con.rollback();
                        return;
                    }

                    PreparedStatement updTo = con.prepareStatement(
                            "UPDATE accounts SET balance = balance + ? WHERE acc_no=?"
                    );
                    updTo.setDouble(1, amt);
                    updTo.setInt(2, toAcc);
                    updTo.executeUpdate();

                    PreparedStatement insFrom = con.prepareStatement(
                            "INSERT INTO transactions(acc_no,type,amount) SELECT acc_no,?,? FROM accounts WHERE user_id=?"
                    );
                    insFrom.setString(1, "Transfer - Debit");
                    insFrom.setDouble(2, amt);
                    insFrom.setInt(3, userId);
                    insFrom.executeUpdate();

                    PreparedStatement insTo = con.prepareStatement(
                            "INSERT INTO transactions(acc_no,type,amount) VALUES(?,?,?)"
                    );
                    insTo.setInt(1, toAcc);
                    insTo.setString(2, "Transfer - Credit");
                    insTo.setDouble(3, amt);
                    insTo.executeUpdate();

                    con.commit();
                    JOptionPane.showMessageDialog(this, "Transferred ₹" + amt + " to account " + toAcc);
                    dashboard.refreshDashboard();
                    dispose();
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Enter valid numbers");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        p.add(transferBtn);
        add(p, BorderLayout.CENTER);
        setVisible(true);
    }
}

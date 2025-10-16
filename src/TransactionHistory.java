import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TransactionHistory extends JFrame {
    private int userId;

    public TransactionHistory(int userId) {
        this.userId = userId;
        setTitle("Transaction History");
        setSize(700, 400);
        setLocationRelativeTo(null);

        String[] cols = {"Transaction ID", "Type", "Amount", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT t.id, t.type, t.amount, t.date FROM transactions t JOIN accounts a ON t.acc_no=a.acc_no WHERE a.user_id=? ORDER BY t.date DESC");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getTimestamp("date")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }

        add(new JScrollPane(table));
        setVisible(true);
    }
}

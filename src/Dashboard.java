import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Dashboard extends JFrame {
    private int userId;
    private JLabel nameLabel, accLabel, balanceLabel;
    private DefaultTableModel model;
    private ScrollPanel animationPanel;
    private double displayedBalance = 0;
    private double actualBalance = 0;

    public Dashboard(int userId) {
        this.userId = userId;
        setTitle("MyBank Dashboard");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        Color primary = new Color(25, 118, 210);
        Color accent = new Color(0, 150, 136);
        Color background = new Color(245, 247, 250);

        JLabel header = new JLabel("MyBank Dashboard", JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 30));
        header.setOpaque(true);
        header.setBackground(primary);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(1000, 80));
        add(header, BorderLayout.NORTH);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(300, 0));
        left.setBackground(Color.WHITE);
        left.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel infoCard = new JPanel(new GridLayout(3, 1, 5, 5));
        infoCard.setBackground(new Color(236, 239, 241));
        infoCard.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primary, 2), "Account Details"));
        nameLabel = new JLabel("Name: "); nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        accLabel = new JLabel("Account No: "); accLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        balanceLabel = new JLabel("Balance: ₹0.00"); balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 20)); balanceLabel.setForeground(accent);
        infoCard.add(nameLabel); infoCard.add(accLabel); infoCard.add(balanceLabel);
        left.add(infoCard);
        left.add(Box.createVerticalStrut(20));

        JButton depositBtn = createButton("Deposit", accent);
        JButton withdrawBtn = createButton("Withdraw", accent);
        JButton transferBtn = createButton("Transfer", accent);
        JButton historyBtn = createButton("Transaction History", primary);
        JButton refreshBtn = createButton("Refresh Dashboard", new Color(0, 172, 193));
        JButton resetPinBtn = createButton("Reset Transaction PIN", new Color(255, 143, 0));
        JButton logoutBtn = createButton("Logout", new Color(211, 47, 47));

        depositBtn.addActionListener(e -> { if (verifyTransactionPin()) new Deposit(userId, this); });
        withdrawBtn.addActionListener(e -> { if (verifyTransactionPin()) new Withdraw(userId, this); });
        transferBtn.addActionListener(e -> { if (verifyTransactionPin()) new Transfer(userId, this); });
        historyBtn.addActionListener(e -> new TransactionHistory(userId));
        refreshBtn.addActionListener(e -> refreshDashboard());
        resetPinBtn.addActionListener(e -> resetTransactionPin());
        logoutBtn.addActionListener(e -> { new Login(); dispose(); });

        left.add(depositBtn); left.add(Box.createVerticalStrut(10));
        left.add(withdrawBtn); left.add(Box.createVerticalStrut(10));
        left.add(transferBtn); left.add(Box.createVerticalStrut(10));
        left.add(historyBtn); left.add(Box.createVerticalStrut(10));
        left.add(refreshBtn); left.add(Box.createVerticalStrut(10));
        left.add(resetPinBtn); left.add(Box.createVerticalStrut(10));
        left.add(logoutBtn);

        add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(background);
        right.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Date", "Type", "Amount"};
        model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(primary);
        table.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primary, 2), "Recent Transactions"));
        right.add(scroll, BorderLayout.CENTER);

        animationPanel = new ScrollPanel();
        animationPanel.setPreferredSize(new Dimension(1000, 80));
        animationPanel.setBackground(new Color(250, 250, 250));
        right.add(animationPanel, BorderLayout.SOUTH);

        Timer scrollTimer = new Timer(50, e -> {
            animationPanel.x += 1;
            if (animationPanel.x > animationPanel.getFontMetrics(animationPanel.getFont()).stringWidth(animationPanel.msg)) {
                animationPanel.x = -animationPanel.getWidth();
            }
            animationPanel.repaint();
        });
        scrollTimer.start();

        add(right, BorderLayout.CENTER);

        loadUserInfo();
        loadTransactions();
        startBalanceAnimation();
        setVisible(true);
    }

    class ScrollPanel extends JPanel {
        int x = -200;
        String msg = "Welcome to MyBank — Secure. Smart. Simple.";
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.setColor(new Color(33, 33, 33));
            g2.drawString(msg, x, getHeight() / 2 + 8);
        }
    }

    private JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(240, 40));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                new Thread(() -> {
                    for (int i = 0; i < 15; i++) {
                        btn.setBackground(btn.getBackground().darker());
                        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                    }
                }).start();
            }
            public void mouseExited(MouseEvent e) {
                new Thread(() -> {
                    for (int i = 0; i < 15; i++) {
                        btn.setBackground(bgColor);
                        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                    }
                }).start();
            }
        });
        return btn;
    }

    private boolean verifyTransactionPin() {
        JPasswordField pinField = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(this, pinField, "Enter Transaction PIN", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) return false;

        String pin = new String(pinField.getPassword()).trim();
        if (!pin.matches("\\d{6}")) {
            JOptionPane.showMessageDialog(this, "Transaction PIN must be 6 digits!");
            return false;
        }

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT id FROM users WHERE id=? AND transaction_pin=?");
            ps.setInt(1, userId);
            ps.setString(2, pin);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error verifying transaction PIN");
            return false;
        }
    }

    private void resetTransactionPin() {
        JPasswordField currentPassField = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(this, currentPassField, "Enter Current Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) return;

        String currentPass = new String(currentPassField.getPassword()).trim();
        if (currentPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT id FROM users WHERE id=? AND password=?");
            ps.setInt(1, userId);
            ps.setString(2, currentPass);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Incorrect password");
                return;
            }

            JPasswordField newPin1 = new JPasswordField();
            JPasswordField newPin2 = new JPasswordField();
            Object[] message = {
                    "Enter New Transaction PIN (6 digits):", newPin1,
                    "Confirm New Transaction PIN:", newPin2
            };
            int pinOption = JOptionPane.showConfirmDialog(this, message, "Set New Transaction PIN", JOptionPane.OK_CANCEL_OPTION);
            if (pinOption != JOptionPane.OK_OPTION) return;

            String pin1 = new String(newPin1.getPassword()).trim();
            String pin2 = new String(newPin2.getPassword()).trim();
            if (!pin1.equals(pin2)) {
                JOptionPane.showMessageDialog(this, "PINs do not match");
                return;
            }
            if (!pin1.matches("\\d{6}")) {
                JOptionPane.showMessageDialog(this, "Transaction PIN must be 6 digits!");
                return;
            }

            PreparedStatement update = con.prepareStatement("UPDATE users SET transaction_pin=? WHERE id=?");
            update.setString(1, pin1);
            update.setInt(2, userId);
            update.executeUpdate();
            JOptionPane.showMessageDialog(this, "Transaction PIN updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUserInfo() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT u.name, a.acc_no, a.balance FROM users u JOIN accounts a ON u.id=a.user_id WHERE u.id=? LIMIT 1"
            );
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nameLabel.setText("Name: " + rs.getString("name"));
                accLabel.setText("Account No: " + rs.getInt("acc_no"));
                actualBalance = rs.getDouble("balance");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startBalanceAnimation() {
        Timer timer = new Timer(20, e -> {
            if (displayedBalance < actualBalance) {
                displayedBalance += (actualBalance - displayedBalance) * 0.1;
                balanceLabel.setText(String.format("Balance: ₹%.2f", displayedBalance));
            } else {
                ((Timer) e.getSource()).stop();
                balanceLabel.setText(String.format("Balance: ₹%.2f", actualBalance));
            }
        });
        timer.start();
    }

    private void loadTransactions() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT t.date, t.type, t.amount FROM transactions t JOIN accounts a ON t.acc_no=a.acc_no WHERE a.user_id=? ORDER BY t.date DESC LIMIT 15"
            );
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{rs.getTimestamp("date"), rs.getString("type"), "₹" + rs.getDouble("amount")});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshDashboard() {
        loadUserInfo();
        loadTransactions();
        startBalanceAnimation();
    }
}

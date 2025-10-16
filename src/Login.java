import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Login extends JFrame {

    private JTextField usernameField;
    private JPasswordField loginPinField;

    public Login() {
        setTitle("MyBank - Login");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Welcome to MyBank", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setOpaque(true);
        title.setBackground(new Color(25, 118, 210));
        title.setForeground(Color.WHITE);
        title.setPreferredSize(new Dimension(400, 60));
        add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        usernameField = new JTextField(15);
        JLabel pinLabel = new JLabel("Login PIN:");
        loginPinField = new JPasswordField(15);

        JButton loginBtn = new JButton("Login");
        JButton signupBtn = new JButton("Sign Up");
        JButton resetPinBtn = new JButton("Reset Login PIN");
        JButton forgotPinBtn = new JButton("Forgot Login PIN");

        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(userLabel, gbc);
        gbc.gridx = 1; centerPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(pinLabel, gbc);
        gbc.gridx = 1; centerPanel.add(loginPinField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        centerPanel.add(loginBtn, gbc);

        gbc.gridy = 3;
        centerPanel.add(signupBtn, gbc);

        gbc.gridy = 4;
        centerPanel.add(resetPinBtn, gbc);

        gbc.gridy = 5;
        centerPanel.add(forgotPinBtn, gbc);

        add(centerPanel, BorderLayout.CENTER);

        loginBtn.addActionListener(e -> loginUser());
        signupBtn.addActionListener(e -> {
            new Signup();
            dispose();
        });
        resetPinBtn.addActionListener(e -> resetLoginPin());
        forgotPinBtn.addActionListener(e -> forgotLoginPin());

        setVisible(true);
    }

    private void loginUser() {
        String username = usernameField.getText().trim();
        String loginPin = new String(loginPinField.getPassword()).trim();
        if (username.isEmpty() || loginPin.isEmpty()) return;
        if (!loginPin.matches("\\d{6}")) return;

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT id, name FROM users WHERE username=? AND login_pin=?"
            );
            ps.setString(1, username);
            ps.setString(2, loginPin);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");
                new Dashboard(userId);
                dispose();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetLoginPin() {
        String username = JOptionPane.showInputDialog(this, "Enter your username:");
        if (username == null || username.trim().isEmpty()) return;
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement checkUser = con.prepareStatement("SELECT id FROM users WHERE username=?");
            checkUser.setString(1, username.trim());
            ResultSet rs = checkUser.executeQuery();
            if (!rs.next()) return;
            int userId = rs.getInt("id");

            JPasswordField currentPinField = new JPasswordField();
            int option = JOptionPane.showConfirmDialog(this, currentPinField, "Enter Current Login PIN", JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) return;
            String currentPin = new String(currentPinField.getPassword()).trim();

            PreparedStatement verify = con.prepareStatement("SELECT id FROM users WHERE id=? AND login_pin=?");
            verify.setInt(1, userId);
            verify.setString(2, currentPin);
            ResultSet vrs = verify.executeQuery();
            if (!vrs.next()) return;

            JPasswordField newPin1 = new JPasswordField();
            JPasswordField newPin2 = new JPasswordField();
            Object[] message = {
                    "Enter New Login PIN (6 digits):", newPin1,
                    "Confirm New Login PIN:", newPin2
            };
            int pinOption = JOptionPane.showConfirmDialog(this, message, "Reset Login PIN", JOptionPane.OK_CANCEL_OPTION);
            if (pinOption != JOptionPane.OK_OPTION) return;
            String pin1 = new String(newPin1.getPassword()).trim();
            String pin2 = new String(newPin2.getPassword()).trim();
            if (!pin1.equals(pin2) || !pin1.matches("\\d{6}")) return;

            PreparedStatement update = con.prepareStatement("UPDATE users SET login_pin=? WHERE id=?");
            update.setString(1, pin1);
            update.setInt(2, userId);
            update.executeUpdate();
            JOptionPane.showMessageDialog(this, "Login PIN updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void forgotLoginPin() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        panel.add(new JLabel("Registered Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Account Password:"));
        panel.add(passwordField);

        int option = JOptionPane.showConfirmDialog(this, panel, "Forgot Login PIN Verification", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (email.isEmpty() || password.isEmpty()) return;

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT id FROM users WHERE email=? AND password=?");
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Email or password incorrect!");
                return;
            }
            int userId = rs.getInt("id");

            JPasswordField newPin1 = new JPasswordField();
            JPasswordField newPin2 = new JPasswordField();
            Object[] msg = {
                    "Enter New Login PIN (6 digits):", newPin1,
                    "Confirm New Login PIN:", newPin2
            };
            int pinOption = JOptionPane.showConfirmDialog(this, msg, "Reset Login PIN", JOptionPane.OK_CANCEL_OPTION);
            if (pinOption != JOptionPane.OK_OPTION) return;

            String pin1 = new String(newPin1.getPassword()).trim();
            String pin2 = new String(newPin2.getPassword()).trim();
            if (!pin1.equals(pin2) || !pin1.matches("\\d{6}")) return;

            PreparedStatement update = con.prepareStatement("UPDATE users SET login_pin=? WHERE id=?");
            update.setString(1, pin1);
            update.setInt(2, userId);
            update.executeUpdate();
            JOptionPane.showMessageDialog(this, "Login PIN reset successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}

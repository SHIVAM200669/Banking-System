import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Signup extends JFrame {

    private JTextField nameField, emailField, usernameField;
    private JPasswordField passwordField, confirmPasswordField;
    private JPasswordField loginPinField, confirmLoginPinField;
    private JPasswordField transactionPinField, confirmTransactionPinField;

    public Signup() {
        setTitle("MyBank - Sign Up");
        setSize(450, 550); // increased height
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Create New Account", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setOpaque(true);
        title.setBackground(new Color(25, 118, 210));
        title.setForeground(Color.WHITE);
        title.setPreferredSize(new Dimension(450, 60));
        add(title, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(15);
        emailField = new JTextField(15);
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        confirmPasswordField = new JPasswordField(15);
        loginPinField = new JPasswordField(15);
        confirmLoginPinField = new JPasswordField(15);
        transactionPinField = new JPasswordField(15);
        confirmTransactionPinField = new JPasswordField(15);

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; panel.add(nameField, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; panel.add(emailField, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; panel.add(usernameField, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; panel.add(passwordField, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; panel.add(confirmPasswordField, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Login PIN (6 digits):"), gbc);
        gbc.gridx = 1; panel.add(loginPinField, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Confirm Login PIN:"), gbc);
        gbc.gridx = 1; panel.add(confirmLoginPinField, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Transaction PIN (6 digits):"), gbc);
        gbc.gridx = 1; panel.add(transactionPinField, gbc);

        y++; gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Confirm Transaction PIN:"), gbc);
        gbc.gridx = 1; panel.add(confirmTransactionPinField, gbc);

        JButton signupBtn = new JButton("Sign Up");
        JButton backBtn = new JButton("Back to Login");

        y++; gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        panel.add(signupBtn, gbc);

        y++; gbc.gridy = y;
        panel.add(backBtn, gbc);

        add(panel, BorderLayout.CENTER);

        signupBtn.addActionListener(e -> signupUser());
        backBtn.addActionListener(e -> {
            new Login();
            dispose();
        });

        setVisible(true);
    }

    private void signupUser() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPass = new String(confirmPasswordField.getPassword()).trim();
        String loginPin = new String(loginPinField.getPassword()).trim();
        String confirmLoginPin = new String(confirmLoginPinField.getPassword()).trim();
        String transactionPin = new String(transactionPinField.getPassword()).trim();
        String confirmTransactionPin = new String(confirmTransactionPinField.getPassword()).trim();


        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPass.isEmpty()
                || loginPin.isEmpty() || confirmLoginPin.isEmpty() || transactionPin.isEmpty() || confirmTransactionPin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        if (!password.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }

        if (!loginPin.equals(confirmLoginPin)) {
            JOptionPane.showMessageDialog(this, "Login PINs do not match!");
            return;
        }

        if (!transactionPin.equals(confirmTransactionPin)) {
            JOptionPane.showMessageDialog(this, "Transaction PINs do not match!");
            return;
        }


        if (loginPin.length() != 6 || transactionPin.length() != 6
                || !loginPin.matches("\\d{6}") || !transactionPin.matches("\\d{6}")) {
            JOptionPane.showMessageDialog(this, "Login and Transaction PIN must be 6 digits!");
            return;
        }


        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement check = con.prepareStatement(
                    "SELECT * FROM users WHERE email=? OR username=?"
            );
            check.setString(1, email);
            check.setString(2, username);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Email or Username already exists!");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users (name, email, username, password, login_pin, transaction_pin) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, username);
            ps.setString(4, password);
            ps.setString(5, loginPin);
            ps.setString(6, transactionPin);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int userId = keys.getInt(1);

                    PreparedStatement acc = con.prepareStatement(
                            "INSERT INTO accounts (user_id, balance) VALUES (?, 0)"
                    );
                    acc.setInt(1, userId);
                    acc.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Signup successful! You can now log in.");
                    new Login();
                    dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Signup failed. Please try again.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Signup::new);
    }
}

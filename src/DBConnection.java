import java.sql.*;

public class DBConnection {
    static final String URL = "jdbc:mysql://localhost:3306/bankdb?useSSL=false&serverTimezone=UTC";
    static final String USER = "root";
    static final String PASS = "Poonam@1";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

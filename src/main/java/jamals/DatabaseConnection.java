package jamals;
import java.sql.*;

public class DatabaseConnection {

    public static String URL,USER,PASSWORD;

    public DatabaseConnection(String URL,String USER,String PASSWORD){
        this.URL = URL;
        this.USER = USER;
        this.PASSWORD = PASSWORD;
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
    


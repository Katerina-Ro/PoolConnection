import java.sql.*;

public class Main {
    static Connection connection = null;

    static String sql = "SELECT * FROM ";
    static String sql2 = "SELECT * FROM ";
    static String sql3 = "SELECT * FROM ";
    static String sql4 = "SELECT * FROM ";
    static String sql5 = "SELECT * FROM ";

    static ConnectionPool cp;

    static {
        try {
            cp = new ConnectionPool("", "", "", 2);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
                try {
            connection = cp.getConnection();
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(sql);
                while (result.next()) {
                    String s = result.getString(1);
                    System.out.println(s);
                }
                result.close();
            }
            connection.close();
        connection = cp.getConnection();
        try (Statement statement2 = connection.createStatement()) {
            ResultSet result = statement2.executeQuery(sql2);
            while (result.next()) {
                String s = result.getString(2);
                System.out.println(s);
            }
            result.close();
        }
                    connection.close();
        connection = cp.getConnection();
        try (Statement statement3 = connection.createStatement()) {
            ResultSet result = statement3.executeQuery(sql3);
            while (result.next()) {
                String s = result.getString(1);
                System.out.println(s);
            }
            result.close();
        }
                    connection.close();
        connection = cp.getConnection();
        try (Statement statement4 = connection.createStatement()) {
            ResultSet result = statement4.executeQuery(sql4);
            while (result.next()) {
                String s = result.getString(3);
                System.out.println(s);
            }
            result.close();
        }
                    connection.close();
        connection = cp.getConnection();
        try (Statement statement5 = connection.createStatement()) {
            ResultSet result = statement5.executeQuery(sql5);
            while (result.next()) {
                String s = result.getString(2);
                System.out.println(s);
            }
            result.close();
        }
                    connection.close();
        } finally {
            if (connection != null) {
            cp.returnConnection(connection);
            }
                }
    }
}


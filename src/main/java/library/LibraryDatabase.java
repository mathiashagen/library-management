package library;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LibraryDatabase {

    private static LibraryDatabase libraryDatabase = null;
    Connection connection = null;

    private LibraryDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:library.db");
            connection.setAutoCommit(false);
            System.out.println("Opened database successfully");
            String createUserTable = "CREATE TABLE IF NOT EXISTS USERS (\n"
                    + "	username varchar(255) NOT NULL UNIQUE,\n" + "	password varchar(255) NOT NULL UNIQUE,\n"
                    + " PRIMARY KEY (username)\n" + ");";
            Statement statement = connection.createStatement();
            statement.execute(createUserTable);
            String addUser = "INSERT INTO USERS (username, password) Values ('test', 'testpassord');";
            Statement statement2 = connection.createStatement();
            statement2.executeUpdate(addUser);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public static LibraryDatabase getInstance() {
        if (libraryDatabase == null) {
            libraryDatabase = new LibraryDatabase();
        }
        return libraryDatabase;
    }

    public void insertBookInformation(String bookId, String title, String publisher) throws SQLException {
        String sql = "INSERT INTO BOOKS (book_id, title, publisher) " + "VALUES ('" + bookId + "', '" + title + "', '"
                + publisher + "');";
        executeUpdate(sql);
    }

    public void question2(String oldUsername, String newUsername, String newPassword) throws SQLException {
        String sql = "UPDATE USERS SET username = ? , " + "password = ? " + "WHERE username = ? ";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, newUsername);
        preparedStatement.setString(2, newPassword);
        preparedStatement.setString(3, oldUsername);
        preparedStatement.executeUpdate();
    }

    public void deleteLoanRecord(String bookStockId) throws SQLException {
        String sql = "DELETE FROM BOOK_LOANS WHERE book_stock_id = " + bookStockId;
        executeUpdate(sql);
    }

    public String question4(String title) throws SQLException {
        String sql = "SELECT BOOKS.title, BRANCHES.branch_name, BORROWERS.borrower_name FROM BOOK_LOANS LEFT JOIN BORROWERS on BORROWERS.borrower_id = BOOK_LOANS.borrower_id LEFT JOIN BOOK_STOCKS on BOOK_STOCKS.book_stock_id = BOOK_LOANS.book_stock_id LEFT JOIN BRANCHES on BRANCHES.branch_id = BOOK_STOCKS.branch_id LEFT JOIN BOOKS on BOOKS.book_id = BOOK_STOCKS.book_id WHERE BOOKS.title = '"
                + title + "'";
        ResultSet rs = executeQuery(sql);
        StringBuilder sBuilder = new StringBuilder();
        while (rs.next()) {
            sBuilder.append(rs.getString("title") + "\t" + rs.getString("branch_name") + "\t"
                    + rs.getString("borrower_name") + "\r\n");
        }
        return sBuilder.toString();
    }

    public String question5(String branchName) throws SQLException {
        String sql = "SELECT BOOKS.title, BORROWERS.borrower_name, BORROWERS.borrower_address FROM BOOK_LOANS LEFT JOIN BORROWERS on BORROWERS.borrower_id = BOOK_LOANS.borrower_id LEFT JOIN BOOK_STOCKS on BOOK_STOCKS.book_stock_id = BOOK_LOANS.book_stock_id LEFT JOIN BOOKS on BOOKS.book_id = BOOK_STOCKS.book_id LEFT JOIN BRANCHES on BRANCHES.branch_id = BOOK_STOCKS.branch_id WHERE BRANCHES.branch_name = '"
                + branchName + "' and BOOK_LOANS.due_date = date('2020-11-23')";
        ResultSet rs = executeQuery(sql);
        StringBuilder sBuilder = new StringBuilder();
        while (rs.next()) {
            sBuilder.append(rs.getString("title") + "\t" + rs.getString("borrower_name") + "\t"
                    + rs.getString("borrower_address") + "\r\n");
        }
        return sBuilder.toString();
    }

    public String question6() throws SQLException {
        String sql = "SELECT BRANCHES.branch_name, Count(BRANCHES.branch_name)\r\nFROM BOOK_STOCKS\r\nLEFT JOIN BRANCHES on BRANCHES.branch_id = BOOK_STOCKS.branch_id\r\nWHERE BOOK_STOCKS.loaned = 1\r\nGROUP BY BRANCHES.branch_name";
        ResultSet rs = executeQuery(sql);
        StringBuilder sBuilder = new StringBuilder();
        while (rs.next()) {
            sBuilder.append(rs.getString("branch_name") + "\t" + rs.getInt("Count(BRANCHES.branch_name)") + "\r\n");
        }
        return sBuilder.toString();
    }

    public void listBooks() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM BOOKS;");

        while (rs.next()) {
            String id = rs.getString("book_id");
            String name = rs.getString("title");
            String age = rs.getString("publisher");

            System.out.println("ID = " + id);
            System.out.println("NAME = " + name);
            System.out.println("AGE = " + age);
            System.out.println();
        }
        rs.close();
        statement.close();
    }

    public synchronized void executeUpdate(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    public void closeConnection() throws SQLException {
        connection.close();
    }
}

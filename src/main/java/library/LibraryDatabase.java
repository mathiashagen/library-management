package library;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LibraryDatabase {

    Connection connection = null;

    public LibraryDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:library.db");
            connection.setAutoCommit(false);
            System.out.println("Opened database successfully");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public void insertBookInformation(String bookId, String title, String publisher) throws SQLException {
        Statement statement = connection.createStatement();
        String sql = "INSERT INTO BOOKS (book_id, title, publisher) " + "VALUES ('" + bookId + "', '" + title + "', '"
                + publisher + "');";
        statement.executeUpdate(sql);
    }

    public void deleteLoanRecord() {
        // TODO: delete loan record from database
    }

    public void question4() {
        // TODO: Retrieve the names of all borrowers who borrowed the book titled "A"
        // for each library branch.
    }

    public void question5() {
        // TODO: For each book that is loaned out from the branch "A" and whose due date
        // is today, retrieve the book title, the borrower's name(s), and the borrower's
        // address(es).
    }

    public void question6() {
        // TODO: For each branch, retrieve the branch name and the total number of books
        // loaded out from that branch.
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

    public void closeConnection() throws SQLException {
        connection.close();
    }
}

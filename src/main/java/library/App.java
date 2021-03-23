package library;

import java.sql.SQLException;

/**
 * Hello world!
 */
public final class App {
    /**
     * Says hello to the world.
     * 
     * @param args The arguments of the program.
     * @throws SQLException
     */
    public static void main(String[] args) throws ClassNotFoundException {

        // testing database functions
        // db file has to be in project folder
        LibraryDatabase libraryDatabase = new LibraryDatabase();
        try {
            libraryDatabase.insertBookInformation("696969b", "Java boka", "Universitetforlaget");
            libraryDatabase.closeConnection();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);

        }

    }
}

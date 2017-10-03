/*
 * LibraryModel.java
 * Author:
 * Created on:
 */



import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class LibraryModel {

    // For use in creating dialogs and making them modal
    private JFrame dialogParent;
    private Connection con;

    public LibraryModel(JFrame parent, String userid, String password) {
        dialogParent = parent;
        try{
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://db.ecs.vuw.ac.nz/" + userid + "_jdbc";
            this.con = DriverManager.getConnection(url,userid, password);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the books and authors
     * @param isbn
     * @return
     */
    public String bookLookup(int isbn) {
        String title = "ISBN not found";
        String edition = "unspecified";
        String noCopies = "unspecified";
        String copiesLeft = "unspecified";
        String author = "";

        try {
            String search = "SELECT * FROM Book NATURAL JOIN Book_Author NATURAL JOIN AUTHOR " +
                "WHERE isbn = " + isbn + " ORDER BY AuthorSeqNo ASC;";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(search);

            while (rs.next()) {
                title = rs.getString("Title");
                edition = rs.getString("edition_no");
                noCopies = rs.getString("numofcop");
                copiesLeft = rs.getString("numleft");
                author += rs.getString("surname") + ",";
            }

            if (author.equals(""))
                author = "unspecified";

            con.setAutoCommit(true);
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isbn + ": " + title + "\nEdition: " + edition + "\nNumber of copies: " +
                noCopies +"\nCopies left: " + copiesLeft + "\nAuthors: " + author ;
    }

    public String showCatalogue() {
        return "Show Catalogue Stub";
    }

    public String showLoanedBooks() {
        return "Show Loaned Books Stub";
    }

    public String showAuthor(int authorID) {
        return "Show Author Stub";
    }

    public String showAllAuthors() {
        return "Show All Authors Stub";
    }

    public String showCustomer(int customerID) {
        return "Show Customer Stub";
    }

    public String showAllCustomers() {
        return "Show All Customers Stub";
    }

    public String borrowBook(int isbn, int customerID,
                             int day, int month, int year) {
        return "Borrow Book Stub";
    }

    public String returnBook(int isbn, int customerid) {
        return "Return Book Stub";
    }

    public void closeDBConnection() {
    }

    public String deleteCus(int customerID) {
        return "Delete Customer";
    }

    public String deleteAuthor(int authorID) {
        return "Delete Author";
    }

    public String deleteBook(int isbn) {
        return "Delete Book";
    }
}
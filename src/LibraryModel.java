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
        String result = "";

        try {
            // con.setAutoCommit(false);
            String search = "SELECT isbn FROM book ORDER BY isbn ASC;";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(search);

            while(rs.next()) {
                int isbn = rs.getInt("isbn");
                result += "\n\n" + bookLookup(isbn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String showLoanedBooks() {
        String result = "";

        try {
            // con.setReadOnly(true);
            String search = "SELECT * FROM Book WHERE numofcop > numLeft ORDER BY isbn ASC;";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(search);

            while(rs.next()) {
                int isbn = rs.getInt("isbn");
                result += bookLookup(isbn) + "\n"; // find a way to show amount loaned
            }

            con.setAutoCommit(true);
            stmt.close();

            if (result.equals("")) result = "No books on loan";

        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String showAuthor(int authorID) {
        String result = "";
        String books = "";

        try {
            // con.setReadOnly(true);
            String search = "SELECT * FROM Book NATURAL JOIN Book_Author NATURAL JOIN AUTHOR " +
                    "WHERE AuthorId = " + authorID +
                    "ORDER BY AuthorSeqNo ASC;";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(search);

            while(rs.next()) {
                if (result.equals("")) result = authorID + " - " +
                        rs.getString("name") + " " +
                        rs.getString("surname");
                books += rs.getInt("isbn") + ", " + rs.getString("title") + "\n";
            }
            // con.setAutoCommit(true);
            stmt.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return "Show Author: " + result + "\n" + books;
    }

    public String showAllAuthors() {
        String result = "";

        try {
            String search = "SELECT * FROM author;";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(search);

            while(rs.next()) {
                result += rs.getInt("AuthorId") + ": " + rs.getString("name") + rs.getString("surname");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return "Show All Authors\n" + result;
    }

    // Dont forget to check if there are no customers with that ID
    public String showCustomer(int customerID) {
        String result = "";
        String books = "";
        try {
            // con.setReadOnly(true);
            // con.setAutoCommit(false);
            String search = "SELECT * FROM customer WHERE customerID = " + customerID + ";";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(search);

            while(rs.next()) {
                result += customerID + ", " + rs.getString("f_name") + " " +
                        rs.getString("l_name") + ", " + rs.getString("city");
            }

            // Start the second part of the search
            search = "SELECT * FROM Cust_book NATURAL JOIN book " +
                    "WHERE customerId = " + customerID + ";";
            rs = stmt.executeQuery(search);

            while(rs.next()) {
                books += "\n\t" + rs.getInt("isbn") + ", " + rs.getString("title");
            }
            stmt.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        if (rs.equals("")) return "No customer with the ID: " + customerID;

        return "Show Customer:\n" + result + books;
    }

    public String showAllCustomers() {
        String result = "";

        try {
            // con.setReadOnly(true);
            String search = "SELECT * FROM customer";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(search);

            result += rs.getInt("customerid")+ ", " + rs.getString("f_name") +
                    " " + rs.getString("l_name") +
                    ", " + rs.getString("city") + " \n ";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Show All Customers:\n" + result;
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
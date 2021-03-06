/*
 * LibraryModel.java
 * Author:
 * Created on:
 */



import javax.swing.*;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
                title = rs.getString("title");
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
                noCopies + "\nCopies left: " + copiesLeft + "\nAuthors: " + author ;
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
            String search = "SELECT * FROM Book NATURAL JOIN Book_Author NATURAL JOIN Author " +
                    "WHERE AuthorId = " + authorID + " " +
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

        return "Show Author:\n" + result + "\n" + books;
    }

    public String showAllAuthors() {
        String result = "";

        try {
            String search = "SELECT * FROM author;";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(search);

            while(rs.next()) {
                result += rs.getInt("AuthorId") + " - " + rs.getString("name") +
                        rs.getString("surname") + "\n";
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return "Show All Authors\n" + result;
    }

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

            // if there's no-one with this ID, return
            if (result.equals("")) return "No customer with the ID: " + customerID;

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
        String result = "";
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC+12:00"));
        cal.clear();
        cal.set(year, month, day);
        Date due = new Date(cal.getTimeInMillis());
        Date currentDate = new Date(System.currentTimeMillis());

        try { //check customer exists
            con.setReadOnly(false);
            con.setAutoCommit(false);
            con.setTransactionIsolation(con.TRANSACTION_SERIALIZABLE);
            // check if the last one is necessary

            Statement stmt = con.createStatement();
            String searchCustValid = "SELECT * FROM Customer WHERE customerid =" + customerID + " FOR UPDATE;";
            ResultSet resCustValid = stmt.executeQuery(searchCustValid);

            String name = "";
            while(resCustValid.next()) {
                name = resCustValid.getString("f_name") + " " + resCustValid.getString("l_name");
            }
            if(name.equals("")) {
                return "Invalid Customer ID: " + customerID;
            }

            try { // check book exists with the selected isbn
                String searchBookValid = "SELECT * FROM book WHERE isbn = " + isbn + ";";
                ResultSet resBookValid = stmt.executeQuery(searchBookValid);
                String title = "";
                while(resBookValid.next()) {
                    title = resBookValid.getString("title");
                }
                if(title.equals("")) {
                    con.commit();
                    con.setAutoCommit(false);
                    return "Invalid ISBN: " + isbn;
                }

                try { // check to see if customer already has the book out
                    String searchBookAndCust = "SELECT * FROM cust_book WHERE isbn = " +
                            isbn + " AND customerid = " + customerID + ";";
                    ResultSet resBookCust = stmt.executeQuery(searchBookAndCust);

                    while(resBookCust.next()) {
                        con.commit();
                        con.setAutoCommit(false);
                        return "Customer has the book out";
                    }

                    try { // check to see if there is a book available
                        String searchBook = "SELECT * FROM book WHERE isbn = " +
                                isbn + " AND numLeft >0 FOR UPDATE;";
                        ResultSet resBook = stmt.executeQuery(searchBook);
                        String bookTitle = "";
                        while(resBook.next()) {
                            bookTitle = resBook.getString("title");
                        }
                        if(bookTitle.equals("")) {
                            con.commit();
                            con.setAutoCommit(false);
                            return "There are no copies available";
                        }

                        // Maybe I don't need this
                        JOptionPane.showMessageDialog(dialogParent, "Locked the tuples(s), ready to update. Click Ok to continue");

                        try {
                            String searchBorrowBook = "UPDATE book SET numleft = (SELECT numleft FROM book WHERE isbn = " +
                                    isbn + ")-1 WHERE isbn=" + isbn + ";";
                            stmt.executeUpdate(searchBorrowBook);
                            String customerBookQuery = "INSERT INTO Cust_book VALUES(" + isbn + "," +
                                    "'" + year + "-" + month + "-" + day + "'" + "," + customerID + ");";
                            stmt.executeUpdate(customerBookQuery);

                            result += "Book: " + isbn + ", " + title + "\nLoaned to: " + customerID +
                                    "\nDue Date: " + day + "/" + month + "/" + year;
                            con.commit();
                            con.setAutoCommit(false);
                        } catch(Exception e) {
                            e.printStackTrace(); // update the book
                        }
                    } catch(Exception e) {
                        e.printStackTrace(); // check to see if there's a book available
                    }
                } catch(Exception e) {
                    e.printStackTrace(); // check if customer already has the book out
                }
            } catch(Exception e) {
                e.printStackTrace(); // book isbn check
            }
        } catch(Exception e) {
            e.printStackTrace(); // customerID check
        }
        return "Borrow Book:\n" + result;
    }

    public String returnBook(int isbn, int customerid) {
        String result = "";

        try { // check if customer exists
            con.setReadOnly(false);
            con.setAutoCommit(false);
            con.setTransactionIsolation(con.TRANSACTION_SERIALIZABLE);

            Statement stmt = con.createStatement();
            String searchCustValid = "SELECT * FROM Customer WHERE customerid = " +
                    customerid + " FOR UPDATE;";
            ResultSet resCustomer = stmt.executeQuery(searchCustValid);
            String name="";
            while(resCustomer.next()){
                name = resCustomer.getString("f_name") + " " + resCustomer.getString("l_name");
            }
            if(name.equals("")){
                con.commit();
                con.setAutoCommit(false);
                return "Invalid CustomerID: " + customerid;
            }
            try { // check that a book with that isbn exists
                String searchBookValid = "SELECT * FROM book WHERE isbn = " + isbn + ";";
                ResultSet book = stmt.executeQuery(searchBookValid);
                String title="";
                while(book.next()){
                    title = book.getString("title");
                }
                if( title.equals("")){
                    con.commit();
                    con.setAutoCommit(false);
                    return "Invalid Isbn: "+ isbn;
                }
                try { // check they are already borrowing the book
                    String searchBookBorrowed = "SELECT * FROM cust_book WHERE isbn = " + isbn +
                            " AND customerid = " + customerid + ";";
                    ResultSet borrowedBook = stmt.executeQuery(searchBookBorrowed);
                    while(borrowedBook.next()){
                        con.commit();
                        con.setAutoCommit(false);
                        return "This customer does not have the book out.";
                    }
                    JOptionPane.showMessageDialog(dialogParent, "Locked the tuples(s), ready to update. Click Ok to continue");

                    try { // update the book
                        String searchBorrowBook = "UPDATE book SET numleft = (SELECT numleft FROM book WHERE isbn = " +
                                isbn + ")+1 WHERE isbn = " + isbn + ";";
                        stmt.executeUpdate(searchBorrowBook);

                        // update tuple to customer_books
                        String customerBookQuery = "DELETE FROM Cust_book WHERE isbn = " +
                                isbn + " AND customerid = " + customerid + ";";
                        stmt.executeUpdate(customerBookQuery);
                        result += "Book: " + isbn + ", " + title + "\nReturned by: " + customerid + "\n";
                        con.commit();
                        con.setAutoCommit(false);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // do I rollback if I fail?
        return result;
    }

    public void closeDBConnection() {
        try {
            con.close();
        } catch (SQLException e) {
            System.out.println("Close Connection - ERROR");
        }
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
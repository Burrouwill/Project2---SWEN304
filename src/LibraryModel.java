/*
 * LibraryModel.java
 * Author:
 * Created on:
 */


import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;

public class LibraryModel {

    // For use in creating dialogs and making them modal
    private JFrame dialogParent;
    
    // Connection
    private Connection con = null;

    public LibraryModel(JFrame parent, String userid, String password) {
	dialogParent = parent;
	
	// Register a PostgreSQL Driver
	try{
		Class.forName("org.postgresql.Driver");
		}
		catch (ClassNotFoundException cnfe){
			// Change this text
		System.out.println("Can not find"+
		"the driver class: "+
		"\nEither I have not installed it"+
		"properly or \n postgresql.jar "+
		" file is not in my CLASSPATH");
		}
	
	
	// Establish a Connection
	String url = "jdbc:postgresql://localhost:5432/burrouwill_jdbc";

			try{
			con = DriverManager.getConnection(url,
			userid, password);
			}
			catch (SQLException sqlex){
			System.out.println("Can not connect");
			System.out.println(sqlex.getMessage());
			}
	
	
    }
    /**
     * Returns information about a certain book based on its isbn. 
     * @param isbn
     * @return
     */
    public String bookLookup(int isbn) {
	
    	try {
    		// Prepare the query
    		PreparedStatement stmt = con.prepareStatement(
    			"SELECT * FROM book\n"
    		  + "JOIN book_author ON book.isbn = book_author.isbn\n"
    		  + "JOIN author ON book_author.authorid = author.authorid\n"
    		  + "WHERE book.isbn = ?\n"
    		  + "ORDER BY author.authorid;");
    		// Set first param to isbn
    		stmt.setInt(1, isbn);
    		ResultSet rs = stmt.executeQuery();
    		
    		// Get the info from the query
    		if (rs.next()) {
    			
    			String title = rs.getString("title");
    			String edition = rs.getString("edition_no");
    			String noOfCopies = rs.getString("numofcop");
    			String copiesLeft = rs.getString("numleft");
    			
    		// Retrieve multiple surnames
                StringBuilder authors = new StringBuilder();
                boolean isFirstAuthor = true;
                do {
                    String surname = rs.getString("surname");
                    if (isFirstAuthor) {
                        authors.append(surname.trim());
                        isFirstAuthor = false;
                    } else {
                        authors.append(", ").append(surname.trim());
                    }
                } while (rs.next());
    			
    	     // Format & return string
    			return 	"Book Lookup:\n"
    					+ "\tISBN: " + isbn + " Book Title: " + title + "\n"
    					+ "\tEdition: " + edition + " - Number Of Copies: " + noOfCopies + " - Copies Left: " + copiesLeft + "\n"
    					+ "\tAuthors: " + authors;
    		} else {
    			return "Book not found.";
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    		return "Error occured while looking up the requested book.";
    	}
    	
    }
    
    /**
     * Prints the information of all books in the database.
     * @return
     */
    public String showCatalogue() {
	
    	try { 
    		
    		// Execute Query
    		Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT book.isbn FROM book");
    		
    		// Get all of the isbn's
    		List<String> isbnAll = new ArrayList<>();
    		
    		while (rs.next()) {
    			isbnAll.add(rs.getString("isbn"));
    		}

    		// Create & return the list of books 
    		Collections.sort(isbnAll);
    		String result = isbnAll.stream()
    				.map(Integer::parseInt)
    			    .map(isbn -> bookLookup(isbn))
    			    .map(isbn -> isbn.trim())
    			    .map(string -> string.replace("Book Lookup:", ""))
    			    .map(string -> string.replace("Book not found.", ""))
    			    .filter(string -> !string.equals(""))
    			    .collect(Collectors.joining("\n"));
    		
    		
    		System.out.println(result);
    			return result;
    	             
    	} catch (SQLException e) {
    		e.printStackTrace();
    		return "Error occured while retrieving catalogue.";
    	}
    	
    	
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
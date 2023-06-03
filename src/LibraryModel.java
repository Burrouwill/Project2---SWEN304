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
    		
    			return result;
    	             
    	} catch (SQLException e) {
    		e.printStackTrace();
    		return "Error occured while retrieving catalogue.";
    	}
    	
    	
    }
    /**
     * 
     * @return
     */
    public String showLoanedBooks() {
	
    	try {
    		
    	// Execute Query
		Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM cust_book");
        
        return "";
    	
    	
    	} catch (SQLException e) {
    		e.printStackTrace();
    		return "Error occured while retrieving loaned books.";
    	}
    	
    }
    /**
     * Returns a string containing info about  an author and all the books they have authored.
     * @param authorID
     * @return
     */
    public String showAuthor(int authorID) {
    	try {
    		// Prepare the query
    		PreparedStatement stmt = con.prepareStatement(
    			    "SELECT * FROM author " +
    			    "JOIN book_author ON book_author.authorid = author.authorid " +
    			    "JOIN book ON book.isbn = book_author.isbn " +
    			    "WHERE author.authorid = ?;"
    			);

    		// Set first param to authorID
    		stmt.setInt(1, authorID);
    		ResultSet rs = stmt.executeQuery();
    		
    		// Get the info from the query
    		if (rs.next()) {
    			
    			String nameAndId = authorID+" - "+rs.getString("name").trim()+" "+rs.getString("surname");
    			
    			List<String> booksWritten = new ArrayList<>();
    			do {
                    booksWritten.add(rs.getString("isbn").trim() + " - " + rs.getString("title").trim());
                } while (rs.next());
    			
    		// Build the string
                StringBuilder author = new StringBuilder();
                author.append("Show Author:\n");
                author.append("\t"+nameAndId+"\n");
                author.append("\tBooks Written:\n");
                booksWritten.stream().forEach(book -> author.append("\t\t"+book+"\n"));
                
    	     // Format & return string
    			return 	author.toString();
    		} else {
    			return "Author not found.";
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    		return "Error occured while looking up the requested author.";
    	}
    }
    
    /**
     * Returns a string containing all of the authors in the database table author.
     * @return
     */
    public String showAllAuthors() {
    	try {
    		
        	// Execute Query
    		Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery
            		("SELECT * FROM author ORDER BY authorid");
            
            StringBuilder result = new StringBuilder();
            result.append("Show All Authors: \n");
            while (rs.next()) {
                int authorID = rs.getInt("authorid");
                String name = rs.getString("name");
                String surname = rs.getString("surname");
                String fullNameId = "\t"+authorID+": "+surname.trim()+", "+name.trim()+"\n";
                result.append(fullNameId);
            }
    			return result.toString();
        	
        	
        	} catch (SQLException e) {
        		e.printStackTrace();
        		return "Error occured while retrieving all authors.";
        	}
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
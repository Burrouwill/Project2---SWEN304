/*
 * LibraryModel.java
 * Author: burrouwill
 * Created on: 03/06/22
 */


import java.sql.*;
import java.time.LocalDate;
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
			con = DriverManager.getConnection(url,userid, password);
			 // Set default transaction isolation level = readOnly
	        con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
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
     * Returns a sting outlining the details of all books currently on loan. 
     * @return
     */
    public String showLoanedBooks() {
	
    	try { 
    		
    		// Execute Query
    		Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT *  FROM cust_book\n"
            		+ "JOIN book ON book.isbn = cust_book.isbn\n"
            		+ "JOIN customer ON customer.customerid = cust_book.customerid");
            // Get info on loaned books (If there are any)
            	if (rs.next()) {
        			List<String> booksWritten = new ArrayList<>();
        			do {
                        booksWritten.add(rs.getString("isbn").trim() + " - " + rs.getString("title").trim()+" \n\t\t(Loaned To: "+rs.getString("f_name").trim()+" ("+rs.getString("customerid")+") "+rs.getString("l_name").trim()+", Due Date: "+rs.getString("duedate")+")");
                    } while (rs.next());
        			
        		// Build the string
                    StringBuilder author = new StringBuilder();
                    author.append("Books on Loan:\n");
                    booksWritten.stream().forEach(book -> author.append("\t"+book+"\n"));
                    
        	     // Format & return string
        			return 	author.toString();
        		} else {
        			return "No books currently on loan.";
        		}
    	             
    	} catch (SQLException e) {
    		e.printStackTrace();
    		return "Error occured while retrieving books on loan.";
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
    /**
     * Returns all the information in the database for a given customer, including books currently borrowed.
     * @param customerID
     * @return
     */
    public String showCustomer(int customerID) {
    	try {
    		// Prepare the query
    		PreparedStatement stmt = con.prepareStatement(
    			    "SELECT * "
    			  + "FROM customer "
    			  + "LEFT JOIN cust_book ON customer.customerid = cust_book.customerid "
    			  + "LEFT JOIN book ON cust_book.isbn = book.isbn "
    			  + "WHERE customer.customerid = ?;"
    			);

    		// Set first param to authorID
    		stmt.setInt(1, customerID);
    		ResultSet rs = stmt.executeQuery();
    		
    		// Get the info from the query
    		if (rs.next()) {
    			
    			String fName = rs.getString("f_name").trim();
    			String lName = rs.getString("l_name").trim();
    			String city = rs.getString("city");
    			
    			if (city == null) {city = "(No city)";}
    			
    			
    			String nameAndId = customerID+": "+lName+" "+fName+" - "+city;
    			
    			List<String> booksLoaned = new ArrayList<>();
    			do {
                    
    				String isbn = rs.getString("isbn");
    				String title = rs.getString("title");
    				
    				if (isbn == null || title == null) {break;}
    				
    				
    				booksLoaned.add(isbn.trim() + " - " + title.trim());
                } while (rs.next());
    			
    		// Build the string
                StringBuilder author = new StringBuilder();
                author.append("Show Customer:\n");
                author.append("\t"+nameAndId+"\n");
                author.append("\tBooks Borrowed:\n");
                
                booksLoaned.stream().filter(string -> !string.equals("-"));
                
                if (booksLoaned.isEmpty()) {author.append("(No books borrowed)");
                } else {booksLoaned.stream().forEach(book -> author.append("\t\t"+book+"\n"));}
                
    	     // Return string
    			return 	author.toString();
    		} else {
    			return "Customer not found.";
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    		return "Error occured while looking up the requested customer.";
    	}
    }
    
    /**
     * Returns a string of all the customers is the database.
     * @return
     */
    public String showAllCustomers() {
    	try {
    		
        	// Execute Query
    		Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery
            		("SELECT * FROM customer ORDER BY customerid");
            
            // Build and return the string 
            StringBuilder result = new StringBuilder();
            result.append("Show All Customers: \n");
            while (rs.next()) {
                String customerID = rs.getString("customerid");
                String lastName = rs.getString("l_name");
                String firstName = rs.getString("f_name");
                String city = rs.getString("city");

                // Check for null values
                if (customerID == null) {
                    customerID = "N/A";
                }
                if (lastName == null) {
                    lastName = "(No Last Name)";
                }
                if (firstName == null) {
                    firstName = "(No First Name)";
                }
                if (city == null) {
                    city = "(No City)";
                }

                String fullNameId = "\t" + customerID + ": " + lastName.trim() + ", " + firstName.trim() + " - " + city + "\n";
                result.append(fullNameId);
            }
        	
            return result.toString();
        	
        	} catch (SQLException e) {
        		e.printStackTrace();
        		return "Error occured while retrieving all customers.";
        	}
    }
    
    
    /**
     * Borrows a book & assigns it to a customer if valid args given.
     * @param isbn
     * @param customerID
     * @param day
     * @param month
     * @param year
     * @return
     */
    public String borrowBook(int isbn, int customerID, int day, int month, int year) {
	
    	try {
            con.setAutoCommit(false); // Start a transaction

            // Check whether the customer exists and lock them
            PreparedStatement customerStmt = con.prepareStatement("SELECT * FROM Customer WHERE customerID = ? FOR UPDATE");
            customerStmt.setInt(1, customerID);
            ResultSet customerResult = customerStmt.executeQuery();
            if (!customerResult.next()) {
                con.rollback(); // Rollback the transaction
                return "Customer not found.";
            }

            // Lock the book if it exists and a copy is available
            PreparedStatement bookStmt = con.prepareStatement("SELECT * FROM Book WHERE isbn = ? AND numleft > 0 FOR UPDATE");
            bookStmt.setInt(1, isbn);
            ResultSet bookResult = bookStmt.executeQuery();
            if (!bookResult.next()) {
                con.rollback(); // Rollback the transaction
                return "Book not found or no copies available.";
            }

            // Insert tuple in the Cust_Book table
            PreparedStatement insertStmt = con.prepareStatement("INSERT INTO Cust_Book (isbn, customerId, dueDate) VALUES (?, ?, ?)");
            insertStmt.setInt(1, isbn);
            insertStmt.setInt(2, customerID);
            LocalDate date = LocalDate.of(year, month, day);
            insertStmt.setDate(3, java.sql.Date.valueOf(date));
            insertStmt.executeUpdate();


            // Double check / Pause
            JOptionPane.showMessageDialog(dialogParent, "Click OK to continue the transaction.");

            // Update the Book table
            PreparedStatement updateStmt = con.prepareStatement("UPDATE Book SET numleft = numleft - 1 WHERE isbn = ?");
            updateStmt.setInt(1, isbn);
            updateStmt.executeUpdate();

            con.commit(); // Commit the transaction
            return "Book borrowed successfully.";
            
        } catch (SQLException e) {
            try {
                con.rollback(); // Rollback the transaction
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return "Error occurred while borrowing the book.";
            
        } finally {
            try {
                con.setAutoCommit(true); // Reset auto-commit to true
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    	
    	
    }
    
    /**
     * Returns a book if it was previously borrowed.
     * @param isbn
     * @param customerid
     * @return
     */
    public String returnBook(int isbn, int customerid) {
    	try {
            con.setAutoCommit(false); // Start a transaction

            // Delete tuple from the Cust_Book table
            PreparedStatement deleteStmt = con.prepareStatement("DELETE FROM Cust_Book WHERE isbn = ? AND customerid = ?");
            deleteStmt.setInt(1, isbn);
            deleteStmt.setInt(2, customerid);
            int rowsAffected = deleteStmt.executeUpdate();

            if (rowsAffected == 0) {
                con.rollback(); // Rollback the transaction
                return "Book not found or not borrowed by the customer.";
            }

            // Double check / Pause
            JOptionPane.showMessageDialog(dialogParent, "Click OK to continue the transaction.");

            // Update the Book table
            PreparedStatement updateStmt = con.prepareStatement("UPDATE Book SET numleft = numleft + 1 WHERE isbn = ?");
            updateStmt.setInt(1, isbn);
            updateStmt.executeUpdate();

            con.commit(); // Commit the transaction
            return "Book returned successfully.";
            
        } catch (SQLException e) {
            try {
                con.rollback(); // Rollback the transaction
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return "Error occurred while returning the book.";
            
        } finally {
            try {
                con.setAutoCommit(true); // Reset auto-commit to true
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    /**
     * Closes the connection to the Database.
     */
    public void closeDBConnection() {
    	try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Deletes a customer from the Database
     * @param customerID
     * @return
     */
    public String deleteCus(int customerID) {
    	try {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM Customer WHERE customerid = ?");
            stmt.setInt(1, customerID);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                return "Customer not found.";
            } else {
                return "Customer deleted successfully.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error occurred while deleting the customer.";
        }
    }
    /**
     * Deletes an Author from the database
     */
    public String deleteAuthor(int authorID) {
    	try {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM Author WHERE authorid = ?");
            stmt.setInt(1, authorID);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                return "Author not found.";
            } else {
                return "Author deleted successfully.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error occurred while deleting the author.";
        }
    }
    
    /**
     * Deletes a book from the Database
     * @param isbn
     * @return
     */
    public String deleteBook(int isbn) {
    	try {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM Book WHERE isbn = ?");
            stmt.setInt(1, isbn);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                return "Book not found.";
            } else {
                return "Book deleted successfully.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error occurred while deleting the book.";
        }
    }
}
package entities;

import entities.enums.Genre;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Book {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private int publicationYear;
    private Genre genre;
    private boolean isAvailable;
    private String borrowerName;
    private Date borrowDate;
    private Date returnDueDate;

    public Book(String isbn, String title, String author, String publisher,
                int publicationYear, Genre genre) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.genre = genre;
        this.isAvailable = true;
        this.borrowerName = null;
        this.borrowDate = null;
        this.returnDueDate = null;
    }

    public Book(String isbn, String title, String author, String publisher,
                int publicationYear, Genre genre, boolean isAvailable,
                String borrowerName, Date borrowDate, Date returnDueDate) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.genre = genre;
        this.isAvailable = isAvailable;
        this.borrowerName = borrowerName;
        this.borrowDate = borrowDate;
        this.returnDueDate = returnDueDate;
    }

    public String toFileFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String borrowDateStr = (borrowDate != null) ? sdf.format(borrowDate) : "null";
        String returnDueDateStr = (returnDueDate != null) ? sdf.format(returnDueDate) : "null";
        String borrowerStr = (borrowerName != null) ? borrowerName : "null";

        return String.format("%s|%s|%s|%s|%d|%s|%b|%s|%s|%s",
                isbn, title, author, publisher, publicationYear,
                genre.toString(), isAvailable, borrowerStr, borrowDateStr, returnDueDateStr);
    }

    public static Book fromFileFormat(String line) {
        String[] parts = line.split("\\|");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            String isbn = parts[0];
            String title = parts[1];
            String author = parts[2];
            String publisher = parts[3];
            int year = Integer.parseInt(parts[4]);
            Genre genre = Genre.valueOf(parts[5]);
            boolean isAvailable = Boolean.parseBoolean(parts[6]);
            String borrowerName = parts[7].equals("null") ? null : parts[7];
            Date borrowDate = parts[8].equals("null") ? null : sdf.parse(parts[8]);
            Date returnDueDate = parts.length > 9 && !parts[9].equals("null") ? sdf.parse(parts[9]) : null;

            return new Book(isbn, title, author, publisher, year, genre,
                    isAvailable, borrowerName, borrowDate, returnDueDate);
        } catch (Exception e) {
            System.err.println("Error parsing book from file: " + e.getMessage());
            return null;
        }
    }

    public void borrowBook(String borrowerName, Date returnDueDate) {
        if (!isAvailable) {
            return;
        }
        this.isAvailable = false;
        this.borrowerName = borrowerName;
        this.borrowDate = new Date();
        this.returnDueDate = returnDueDate;
    }

    public void borrowBook(String borrowerName) {
        borrowBook(borrowerName, null);
    }


    public void returnBook() {
        this.isAvailable = true;
        this.borrowerName = null;
        this.borrowDate = null;
        this.returnDueDate = null;
    }


    public String getFieldValue(String fieldName) {
        switch (fieldName.toLowerCase()) {
            case "isbn": return isbn;
            case "title": return title;
            case "author": return author;
            case "publisher": return publisher;
            case "publicationyear":
            case "year": return String.valueOf(publicationYear);
            case "genre": return genre.toString();
            case "isavailable":
            case "available": return String.valueOf(isAvailable);
            case "borrowername":
            case "borrower": return borrowerName != null ? borrowerName : "";
            case "borrowdate":
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                return borrowDate != null ? sdf.format(borrowDate) : "";
            case "returnduedate":
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                return returnDueDate != null ? sdf2.format(returnDueDate) : "";
            default: return "";
        }
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String status = isAvailable ? "Available" :
                String.format("Borrowed by %s on %s (Due: %s)",
                        borrowerName,
                        borrowDate != null ? sdf.format(borrowDate) : "N/A",
                        returnDueDate != null ? sdf.format(returnDueDate) : "N/A");

        return String.format(
                "ISBN: %s | Title: %s | Author: %s | Publisher: %s | Year: %d | Genre: %s | Status: %s",
                isbn, title, author, publisher, publicationYear, genre, status
        );
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public int getPublicationYear() { return publicationYear; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }

    public Date getBorrowDate() { return borrowDate; }
    public void setBorrowDate(Date borrowDate) { this.borrowDate = borrowDate; }

    public Date getReturnDueDate() { return returnDueDate; }
    public void setReturnDueDate(Date returnDueDate) { this.returnDueDate = returnDueDate; }
}
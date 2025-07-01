package utils;


import entities.Book;
import managers.LibraryManager;

import java.io.*;
import java.util.ArrayList;

/**
 * Handles all file I/O operations for the Library Management System
 */
public class FileHandler {
    private static final String DATA_FILE = "data/books.txt";
    private static final String DATA_DIR = "data";


    private static void ensureDataDirectory() {
        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public static boolean saveToFile(LibraryManager manager) {
        ensureDataDirectory();

        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            writer.println("ISBN|Title|Author|Publisher|Year|Genre|Available|Borrower|BorrowDate|ReturnDueDate");

            for (Book book : manager.getBooks()) {
                writer.println(book.toFileFormat());
            }

            System.out.println("Data saved successfully to " + DATA_FILE);
            return true;

        } catch (IOException e) {
            System.out.println("Error saving to file: " + e.getMessage());
            return false;
        }
    }

    public static void loadFromFile(LibraryManager manager) {
        ensureDataDirectory();

        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("No existing data file found. Creating with sample data");
            saveToFile(manager);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            ArrayList<Book> loadedBooks = new ArrayList<>();
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                if (!line.trim().isEmpty()) {
                    Book book = Book.fromFileFormat(line);
                    if (book != null) {
                        loadedBooks.add(book);
                    }
                }
            }

            manager.setBooks(loadedBooks);
            System.out.println("Loaded " + loadedBooks.size() + " books from file.");

        } catch (IOException e) {
            System.out.println("Error loading from file: " + e.getMessage());
        }
    }

}
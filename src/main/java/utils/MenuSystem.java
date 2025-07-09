package utils;


import entities.Book;
import entities.enums.Genre;
import managers.LibraryManager;

import java.util.*;

/**
 * Provides the command-line interface for the Library Management System
 */
public class MenuSystem {
    private LibraryManager libraryManager;
    private Scanner scanner;
    private boolean running;

    public MenuSystem() {
        this.libraryManager = new LibraryManager();
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    public void start() {
        System.out.println("\nWELCOME TO THE LIBRARY MANAGEMENT SYSTEM");

        FileHandler.loadFromFile(libraryManager);

        while (running) {
            displayMainMenu();
            processMainMenuChoice();
        }

        scanner.close();
        System.out.println("\nThank you for using the Library Management System!");
    }

    private void displayMainMenu() {
        System.out.println("\n=== MAIN MENU ===");
        System.out.println("1. List All Books");
        System.out.println("2. Search Books");
        System.out.println("3. Add New Book");
        System.out.println("4. Sort Books");
        System.out.println("5. Filter Books (Advanced Search)");
        System.out.println("6. Library Statistics");
        System.out.println("7. Borrow/Return Book");
        System.out.println("8. Save & Exit");
        System.out.println("9. Exit Without Saving");
        System.out.print("\nEnter your choice (1-9): ");
    }

    private void processMainMenuChoice() {
        String input = scanner.nextLine();
        int choice = Validator.validateMenuChoice(input, 1, 9);

        switch (choice) {
            case 1:
                libraryManager.listAllBooks();
                pauseForUser();
                break;
            case 2:
                searchMenu();
                break;
            case 3:
                addNewBook();
                break;
            case 4:
                sortMenu();
                break;
            case 5:
                filterMenu();
                break;
            case 6:
                displayStatistics();
                break;
            case 7:
                borrowReturnMenu();
                break;
            case 8:
                saveAndExit();
                break;
            case 9:
                exitWithoutSaving();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private void searchMenu() {
        System.out.println("\n=== SEARCH BOOKS ===");
        System.out.println("Search by:");
        System.out.println("1. ISBN");
        System.out.println("2. Title");
        System.out.println("3. Author");
        System.out.println("4. Publisher");
        System.out.println("5. Year");
        System.out.println("6. Genre");
        System.out.println("7. Availability");
        System.out.println("8. Borrower Name");
        System.out.println("9. Back to Main Menu");
        System.out.print("\nEnter your choice (1-9): ");

        String input = scanner.nextLine();
        int choice = Validator.validateMenuChoice(input, 1, 9);

        if (choice == 9) return;

        String attribute = getAttributeFromChoice(choice);
        if (attribute == null) {
            System.out.println("Invalid choice.");
            return;
        }

        System.out.print("Enter search value: ");
        String searchValue = scanner.nextLine().trim();

        List<Book> results = libraryManager.searchBooks(attribute, searchValue);

        if (results.isEmpty()) {
            System.out.println("\nNo books found matching your search.");
        } else {
            System.out.println("\nFound " + results.size() + " book(s):");
            for (int i = 0; i < results.size(); i++) {
                System.out.println((i + 1) + ". " + results.get(i));
            }

            System.out.println("\nOptions:");
            System.out.println("1. Update a book");
            System.out.println("2. Delete a book");
            System.out.println("3. Back to Main Menu");
            System.out.print("Enter your choice (1-3): ");

            String optionInput = scanner.nextLine();
            int option = Validator.validateMenuChoice(optionInput, 1, 3);

            if (option == 1 || option == 2) {
                System.out.print("Enter the number of the book: ");
                String bookNumInput = scanner.nextLine();
                int bookNum = Validator.validateMenuChoice(bookNumInput, 1, results.size());

                if (bookNum != -1) {
                    Book selectedBook = results.get(bookNum - 1);
                    if (option == 1) {
                        updateBook(selectedBook);
                    } else {
                        deleteBook(selectedBook);
                    }
                }
            }
        }
    }


    private void addNewBook() {
        System.out.println("\n=== ADD NEW BOOK ===");

        String isbn;
        while (true) {
            System.out.print("Enter ISBN (10 or 13 digits): ");
            isbn = scanner.nextLine().trim();
            if (Validator.isValidISBN(isbn)) {
                if (libraryManager.getBookByIsbn(isbn) != null) {
                    System.out.println("Error: A book with this ISBN already exists!");
                    return;
                }
                break;
            } else {
                System.out.println(Validator.getErrorMessage("isbn"));
            }
        }

        String title;
        while (true) {
            System.out.print("Enter Title: ");
            title = scanner.nextLine().trim();
            if (Validator.isNotEmpty(title)) {
                title = Validator.sanitizeInput(title);
                break;
            } else {
                System.out.println("Title cannot be empty.");
            }
        }

        String author;
        while (true) {
            System.out.print("Enter Author (LastName_FirstName): ");
            author = scanner.nextLine().trim();
            if (Validator.isValidName(author)) {
                break;
            } else {
                System.out.println(Validator.getErrorMessage("author"));
            }
        }
        String publisher;
        while (true) {
            System.out.print("Enter Publisher: ");
            publisher = scanner.nextLine().trim();
            if (Validator.isValidPublisher(publisher)) {
                publisher = Validator.sanitizeInput(publisher);
                break;
            } else {
                System.out.println(Validator.getErrorMessage("publisher"));
            }
        }

        int year;
        while (true) {
            System.out.print("Enter Publication Year: ");
            String yearStr = scanner.nextLine().trim();
            if (Validator.isValidYear(yearStr)) {
                year = Integer.parseInt(yearStr);
                break;
            } else {
                System.out.println(Validator.getErrorMessage("year"));
            }
        }

        String genre;
        while (true) {
            System.out.print("Enter Genre: ");
            genre = scanner.nextLine().trim();
            if (Validator.isValidGenre(genre)) {
                genre = Validator.sanitizeInput(genre);
                break;
            } else {
                System.out.println(Validator.getErrorMessage("genre"));
            }
        }

        Book newBook = new Book(isbn, title, author, publisher, year, Genre.fromString(genre));
        if (libraryManager.createBook(newBook)) {
            System.out.println("\nBook added successfully!");
            System.out.println(newBook);
        } else {
            System.out.println("\nFailed to add book.");
        }

        pauseForUser();
    }


    private void updateBook(Book book) {
        System.out.println("\n=== UPDATE BOOK ===");
        System.out.println("Current: " + book);
        System.out.println("\nWhat would you like to update?");
        System.out.println("1. Title");
        System.out.println("2. Author");
        System.out.println("3. Publisher");
        System.out.println("4. Year");
        System.out.println("5. Genre");
        System.out.println("6. Cancel");
        System.out.print("Enter your choice (1-6): ");

        String input = scanner.nextLine();
        int choice = Validator.validateMenuChoice(input, 1, 6);

        if (choice == 6) return;

        Map<String, String> updates = new HashMap<>();

        switch (choice) {
            case 1:
                System.out.print("Enter new Title: ");
                String title = Validator.sanitizeInput(scanner.nextLine());
                if (Validator.isNotEmpty(title)) {
                    updates.put("title", title);
                }
                break;
            case 2:
                System.out.print("Enter new Author (LastName_FirstName): ");
                String author = scanner.nextLine().trim();
                if (Validator.isValidName(author)) {
                    updates.put("author", author);
                }
                break;
            case 3:
                System.out.print("Enter new Publisher: ");
                String publisher = Validator.sanitizeInput(scanner.nextLine());
                if (Validator.isValidPublisher(publisher)) {
                    updates.put("publisher", publisher);
                }
                break;
            case 4:
                System.out.print("Enter new Year: ");
                String yearStr = scanner.nextLine().trim();
                if (Validator.isValidYear(yearStr)) {
                    updates.put("year", yearStr);
                }
                break;
            case 5:
                System.out.print("Enter new Genre: ");
                String genre = Validator.sanitizeInput(scanner.nextLine());
                if (Validator.isValidGenre(genre)) {
                    updates.put("genre", genre);
                }
                break;
        }

        if (!updates.isEmpty() && libraryManager.updateBook(book.getIsbn(), updates)) {
            System.out.println("\nBook updated successfully!");
            System.out.println("Updated: " + book);
        } else {
            System.out.println("\nFailed to update book.");
        }

        pauseForUser();
    }

    private void deleteBook(Book book) {
        System.out.println("\n=== DELETE BOOK ===");
        System.out.println("Book to delete: " + book);
        System.out.print("Are you sure you want to delete this book? (yes/no): ");

        String confirmation = scanner.nextLine().trim().toLowerCase();
        if (confirmation.equals("yes") || confirmation.equals("y")) {
            if (libraryManager.deleteBook(book.getIsbn())) {
                System.out.println("\nBook deleted successfully!");
            } else {
                System.out.println("\nFailed to delete book.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }

        pauseForUser();
    }

    private void sortMenu() {
        System.out.println("\n=== SORT BOOKS ===");
        System.out.println("Sort by:");
        System.out.println("1. ISBN");
        System.out.println("2. Title");
        System.out.println("3. Author");
        System.out.println("4. Publisher");
        System.out.println("5. Year");
        System.out.println("6. Genre");
        System.out.println("7. Availability");
        System.out.println("8. Back to Main Menu");
        System.out.print("Enter your choice (1-8): ");

        String input = scanner.nextLine();
        int choice = Validator.validateMenuChoice(input, 1, 8);

        if (choice == 8) return;

        String attribute = getAttributeFromChoice(choice);
        if (attribute == null) {
            System.out.println("Invalid choice.");
            return;
        }

        System.out.print("Sort order (1=Ascending, 2=Descending): ");
        String orderInput = scanner.nextLine();
        int order = Validator.validateMenuChoice(orderInput, 1, 2);
        boolean ascending = (order == 1);

        libraryManager.sortBooks(attribute, ascending);
        libraryManager.listAllBooks();

        pauseForUser();
    }

    private void filterMenu() {
        System.out.println("\n=== ADVANCED FILTER ===");
        System.out.println("You can filter by multiple criteria.");
        System.out.println("Leave blank to skip a criterion.");

        Map<String, String> criteria = new HashMap<>();

        System.out.print("Title contains: ");
        String title = scanner.nextLine().trim();
        if (!title.isEmpty()) criteria.put("title", title);

        System.out.print("Author contains: ");
        String author = scanner.nextLine().trim();
        if (!author.isEmpty()) criteria.put("author", author);

        System.out.print("Publisher contains: ");
        String publisher = scanner.nextLine().trim();
        if (!publisher.isEmpty()) criteria.put("publisher", publisher);

        System.out.print("Year (use >2000, <2020, or =2015): ");
        String year = scanner.nextLine().trim();
        if (!year.isEmpty()) criteria.put("year", year);

        System.out.print("Genre contains: ");
        String genre = scanner.nextLine().trim();
        if (!genre.isEmpty()) criteria.put("genre", genre);

        System.out.print("Available only? (yes/no/all): ");
        String available = scanner.nextLine().trim().toLowerCase();
        if (available.equals("yes") || available.equals("y")) {
            criteria.put("available", "true");
        } else if (available.equals("no") || available.equals("n")) {
            criteria.put("available", "false");
        }

        if (criteria.isEmpty()) {
            System.out.println("No filter criteria specified.");
            return;
        }

        List<Book> results = libraryManager.filterBooks(criteria);

        System.out.println("\n=== FILTER RESULTS ===");
        System.out.println("Found " + results.size() + " book(s) matching all criteria:");

        for (int i = 0; i < results.size(); i++) {
            System.out.println((i + 1) + ". " + results.get(i));
        }

        pauseForUser();
    }

    private void displayStatistics() {
        System.out.println("\n=== LIBRARY STATISTICS ===");

        Map<String, Object> stats = libraryManager.getAdvancedLibraryStats(libraryManager);

        System.out.println("Total Books: " + stats.get("totalBooks"));
        System.out.println("Available Books: " + stats.get("availableBooks"));
        System.out.println("Borrowed Books: " + stats.get("borrowedBooks"));

        System.out.println("\nGenre Distribution:");
        Map<?, Long> genreCount = (Map<?, Long>) stats.get("genreDistribution");
        for (Map.Entry<?, Long> entry : genreCount.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\nPublisher Distribution:");
        Map<String, Long> publisherCount = (Map<String, Long>) stats.get("publisherDistribution");
        for (Map.Entry<String, Long> entry : publisherCount.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\nMost Popular Genre: " + stats.get("mostPopularGenre"));

        Book oldestBook = (Book) stats.get("oldestBook");
        if (oldestBook != null) {
            System.out.println("Oldest Book: " + oldestBook.getTitle() + " (" + oldestBook.getPublicationYear() + ")");
        }

        Book newestBook = (Book) stats.get("newestBook");
        if (newestBook != null) {
            System.out.println("Newest Book: " + newestBook.getTitle() + " (" + newestBook.getPublicationYear() + ")");
        }

        System.out.println("\nTop Borrowers:");

        Map<String, Long> topBorrowers = (Map<String, Long>) stats.get("topBorrowers");
        if (topBorrowers != null && !topBorrowers.isEmpty()) {
            for (Map.Entry<String, Long> entry : topBorrowers.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " book");
            }
        } else {
            System.out.println("  No top borrowers found.");
        }

        List<Book> overdueBooks = (List<Book>) stats.get("overdueBooks");
        System.out.println("\nOverdue Books: " + (overdueBooks != null ? overdueBooks.size() : 0));

        pauseForUser();
    }


    private void borrowReturnMenu() {
        System.out.println("\n=== BORROW/RETURN BOOK ===");
        System.out.println("1. Borrow a book");
        System.out.println("2. Return a book");
        System.out.println("3. Back to Main Menu");
        System.out.print("Enter your choice (1-3): ");

        String input = scanner.nextLine();
        int choice = Validator.validateMenuChoice(input, 1, 3);

        if (choice == 3) return;

        System.out.print("Enter ISBN of the book: ");
        String isbn = scanner.nextLine().trim();

        Book book = libraryManager.getBookByIsbn(isbn);
        if (book == null) {
            System.out.println("Book not found with ISBN: " + isbn);
            pauseForUser();
            return;
        }

        if (choice == 1) {
            if (!book.isAvailable()) {
                System.out.println("This book is already borrowed by: " + book.getBorrowerName());
            } else {
                System.out.print("Enter borrower name (LastName_FirstName): ");
                String borrowerName = scanner.nextLine().trim();
                if (Validator.isValidName(borrowerName)) {
                    book.borrowBook(borrowerName);
                    System.out.println("Book borrowed successfully!");
                    System.out.println(book);
                } else {
                    System.out.println(Validator.getErrorMessage("borrower"));
                }
            }
        } else {
            if (book.isAvailable()) {
                System.out.println("This book is not currently borrowed.");
            } else {
                book.returnBook();
                System.out.println("Book returned successfully!");
                System.out.println(book);
            }
        }

        pauseForUser();
    }


    private String getAttributeFromChoice(int choice) {
        switch (choice) {
            case 1:
                return "isbn";
            case 2:
                return "title";
            case 3:
                return "author";
            case 4:
                return "publisher";
            case 5:
                return "year";
            case 6:
                return "genre";
            case 7:
                return "available";
            case 8:
                return "borrower";
            default:
                return null;
        }
    }

    private void pauseForUser() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void saveAndExit() {
        System.out.print("Saving data...");
        if (FileHandler.saveToFile(libraryManager)) {
            System.out.println("Success!");
        } else {
            System.out.println("Failed!");
            System.out.print("Exit anyway? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("yes") && !confirm.equals("y")) {
                return;
            }
        }
        running = false;
    }

    private void exitWithoutSaving() {
        System.out.print("Are you sure you want to exit without saving? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("yes") || confirm.equals("y")) {
            running = false;
        }
    }
}
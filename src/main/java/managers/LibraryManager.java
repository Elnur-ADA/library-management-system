package managers;


import entities.Book;
import entities.enums.Genre;

import java.util.*;
import java.util.stream.Collectors;

public class LibraryManager {
    private ArrayList<Book> books;

    public LibraryManager() {
        this.books = new ArrayList<>();
    }

    public boolean createBook(Book book) {

        for (Book b : books) {
            if (b.getIsbn().equals(book.getIsbn())) {
                return false;
            }
        }
        books.add(book);
        return true;
    }

    public void listAllBooks() {
        System.out.println("\n=== LIBRARY CATALOG ===");
        System.out.println("Total Books: " + books.size());
        System.out.println("=" + "=".repeat(100));

        if (books.isEmpty()) {
            System.out.println("No books in the library.");
            return;
        }

        for (int i = 0; i < books.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, books.get(i).toString());
        }
        System.out.println("=" + "=".repeat(100));
    }

    public List<Book> searchBooks(String attribute, String searchValue) {
        List<Book> results = new ArrayList<>();
        String searchLower = searchValue.toLowerCase();

        for (Book book : books) {
            String fieldValue = book.getFieldValue(attribute).toLowerCase();
            if (fieldValue.contains(searchLower)) {
                results.add(book);
            }
        }

        return results;
    }

    public Book searchFirstBook(String attribute, String searchValue) {
        List<Book> results = searchBooks(attribute, searchValue);
        return results.isEmpty() ? null : results.getFirst();
    }


    public boolean updateBook(String isbn, Map<String, String> updates) {
        Book book = searchFirstBook("isbn", isbn);
        if (book == null) {
            return false;
        }

        for (Map.Entry<String, String> entry : updates.entrySet()) {
            String field = entry.getKey().toLowerCase();
            String value = entry.getValue();

            try {
                switch (field) {
                    case "title":
                        book.setTitle(value);
                        break;
                    case "author":
                        book.setAuthor(value);
                        break;
                    case "publisher":
                        book.setPublisher(value);
                        break;
                    case "year":
                    case "publicationyear":
                        book.setPublicationYear(Integer.parseInt(value));
                        break;
                    case "genre":
                        book.setGenre(Genre.valueOf(value));
                        break;
                    case "available":
                    case "isavailable":
                        book.setAvailable(Boolean.parseBoolean(value));
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error updating field " + field + ": " + e.getMessage());
            }
        }

        return true;
    }

    public boolean deleteBook(String isbn) {
        Book book = searchFirstBook("isbn", isbn);
        if (book == null) {
            return false;
        }
        books.remove(book);
        return true;
    }

    public void sortBooks(String attribute, boolean ascending) {
        Comparator<Book> comparator = null;

        switch (attribute.toLowerCase()) {
            case "isbn":
                comparator = Comparator.comparing(Book::getIsbn);
                break;
            case "title":
                comparator = Comparator.comparing(Book::getTitle);
                break;
            case "author":
                comparator = Comparator.comparing(Book::getAuthor);
                break;
            case "publisher":
                comparator = Comparator.comparing(Book::getPublisher);
                break;
            case "year":
            case "publicationyear":
                comparator = Comparator.comparing(Book::getPublicationYear);
                break;
            case "genre":
                comparator = Comparator.comparing(Book::getGenre);
                break;
            case "available":
            case "isavailable":
                comparator = Comparator.comparing(Book::isAvailable);
                break;
            case "borrower":
            case "borrowername":
                comparator = Comparator.comparing(
                        book -> book.getBorrowerName() != null ? book.getBorrowerName() : ""
                );
                break;
            case "borrowdate":
                comparator = Comparator.comparing(
                        book -> book.getBorrowDate() != null ? book.getBorrowDate() : new Date(0)
                );
                break;
            default:
                System.out.println("Invalid sort attribute: " + attribute);
                return;
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        books.sort(comparator);
        System.out.println("Books sorted by " + attribute + " (" +
                (ascending ? "ascending" : "descending") + ")");
    }


    public List<Book> filterBooks(Map<String, String> criteria) {
        List<Book> results = new ArrayList<>(books);

        for (Map.Entry<String, String> entry : criteria.entrySet()) {
            String field = entry.getKey().toLowerCase();
            String value = entry.getValue().toLowerCase();

            results = results.stream().filter(book -> {
                String bookValue = book.getFieldValue(field).toLowerCase();

                if (field.equals("year") || field.equals("publicationyear")) {
                    try {
                        if (value.startsWith(">")) {
                            int threshold = Integer.parseInt(value.substring(1));
                            return book.getPublicationYear() > threshold;
                        } else if (value.startsWith("<")) {
                            int threshold = Integer.parseInt(value.substring(1));
                            return book.getPublicationYear() < threshold;
                        } else if (value.startsWith("=")) {
                            int exact = Integer.parseInt(value.substring(1));
                            return book.getPublicationYear() == exact;
                        } else {
                            int exact = Integer.parseInt(value);
                            return book.getPublicationYear() == exact;
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }

                if (field.equals("available") || field.equals("isavailable")) {
                    return bookValue.equals(value);
                }

                return bookValue.contains(value);
            }).collect(Collectors.toList());
        }

        return results;
    }


    public Map<String, Object> getLibraryStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalBooks", books.size());
        stats.put("availableBooks", books.stream().filter(Book::isAvailable).count());
        stats.put("borrowedBooks", books.stream().filter(b -> !b.isAvailable()).count());

        Map<Genre, Long> genreCount = books.stream()
                .collect(Collectors.groupingBy(Book::getGenre, Collectors.counting()));
        stats.put("genreDistribution", genreCount);

        Map<String, Long> publisherCount = books.stream()
                .collect(Collectors.groupingBy(Book::getPublisher, Collectors.counting()));
        stats.put("publisherDistribution", publisherCount);

        return stats;
    }
    public Map<String, Object> getAdvancedLibraryStats(LibraryManager manager) {
        Map<String, Object> stats = manager.getLibraryStats();
        List<Book> books = manager.getBooks();

        stats.put("mostPopularGenre", getMostPopularGenre(books));
        stats.put("oldestBook", getOldestBook(books));
        stats.put("newestBook", getNewestBook(books));
        stats.put("averagePublicationYear", getAveragePublicationYear(books));
        stats.put("overdueBooks", getOverdueBooks(books));
        stats.put("topBorrowers", getTopBorrowers(books));

        return stats;
    }

    private Genre getMostPopularGenre(List<Book> books) {
        return books.stream()
                .collect(Collectors.groupingBy(Book::getGenre, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private Book getOldestBook(List<Book> books) {
        return books.stream()
                .min(Comparator.comparing(Book::getPublicationYear))
                .orElse(null);
    }

    private Book getNewestBook(List<Book> books) {
        return books.stream()
                .max(Comparator.comparing(Book::getPublicationYear))
                .orElse(null);
    }

    private double getAveragePublicationYear(List<Book> books) {
        return books.stream()
                .mapToInt(Book::getPublicationYear)
                .average()
                .orElse(0.0);
    }

    private List<Book> getOverdueBooks(List<Book> books) {
        Date today = new Date();
        return books.stream()
                .filter(book -> !book.isAvailable() &&
                        book.getReturnDueDate() != null &&
                        book.getReturnDueDate().before(today))
                .collect(Collectors.toList());
    }

    private Map<String, Long> getTopBorrowers(List<Book> books) {
        return books.stream()
                .filter(book -> !book.isAvailable() && book.getBorrowerName() != null)
                .collect(Collectors.groupingBy(Book::getBorrowerName, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public ArrayList<Book> getBooks() {
        return books;
    }

    public void setBooks(ArrayList<Book> books) {
        this.books = books;
    }

    public int getTotalBooks() {
        return books.size();
    }

    public Book getBookByIsbn(String isbn) {
        return searchFirstBook("isbn", isbn);
    }
}
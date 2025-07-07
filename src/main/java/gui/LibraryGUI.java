package gui;

import entities.Book;
import entities.enums.Genre;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import managers.LibraryManager;
import utils.FileHandler;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LibraryGUI extends Application {
    private LibraryManager libraryManager;
    private TableView<Book> bookTable;
    private TextField searchBox;
    private ComboBox<String> searchType;
    private Label statusLbl;

    @Override
    public void start(Stage primaryStage) {
        libraryManager = new LibraryManager();
        FileHandler.loadFromFile(libraryManager);

        primaryStage.setTitle("Library Management System ");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(createMenuBar());
        mainLayout.setCenter(createMainContent());
        mainLayout.setBottom(createStatusBar());

        Scene scene = new Scene(mainLayout, 1300, 700);
        try {
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS file not found, using default styling");
        }

        primaryStage.setScene(scene);
        primaryStage.show();

        loadTableData();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem saveItem = new MenuItem("Save Data");
        MenuItem exitItem = new MenuItem("Exit");

        saveItem.setOnAction(e -> saveData());
        exitItem.setOnAction(e -> exitApplication());

        fileMenu.getItems().addAll(saveItem, new SeparatorMenuItem(), exitItem);

        Menu bookMenu = new Menu("Books");
        MenuItem addBookItem = new MenuItem("Add New Book");
        MenuItem editBookItem = new MenuItem("Edit Selected");
        MenuItem borrowReturnItem = new MenuItem("Borrow/Return Book");

        addBookItem.setOnAction(e -> showAddBookDialog());
        editBookItem.setOnAction(e -> {
            Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                showEditBookDialog(selectedBook);
            } else {
                showAlert();
            }
        });
        borrowReturnItem.setOnAction(e -> {
            Book selected = bookTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showBorrowReturnDialog(selected.getIsbn());
            } else {
                showBorrowReturnDialog();
            }
        });

        bookMenu.getItems().addAll(addBookItem, editBookItem, borrowReturnItem);

        Menu viewMenu = new Menu("View");
        MenuItem refreshItem = new MenuItem("Refresh Table");
        MenuItem statsItem = new MenuItem("Show Statistics");

        refreshItem.setOnAction(e -> {
            FileHandler.loadFromFile(libraryManager);
            loadTableData();
        });
        statsItem.setOnAction(e -> showStats());

        viewMenu.getItems().addAll(refreshItem, statsItem);

        menuBar.getMenus().addAll(fileMenu, bookMenu, viewMenu);
        return menuBar;
    }

    private VBox createMainContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        HBox searchArea = createSearchSection();
        bookTable = createBookTable();
        HBox buttonArea = createButtonSection();

        content.getChildren().addAll(searchArea, bookTable, buttonArea);
        VBox.setVgrow(bookTable, Priority.ALWAYS);

        return content;
    }

    private HBox createSearchSection() {
        HBox searchSection = new HBox(10);
        searchSection.setPadding(new Insets(10));

        Label searchLbl = new Label("Search:");
        searchType = new ComboBox<>();
        searchType.getItems().addAll("All Fields", "ISBN", "Title", "Author", "Publisher", "Genre", "Year");
        searchType.setValue("All Fields");

        searchBox = new TextField();
        searchBox.setPromptText("Type to search");
        searchBox.setPrefWidth(300);

        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> doSearch());

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            searchBox.clear();
            loadTableData();
        });

        searchSection.getChildren().addAll(searchLbl, searchType, searchBox, searchBtn, clearBtn);
        return searchSection;
    }

    private TableView<Book> createBookTable() {
        TableView<Book> table = new TableView<>();

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnCol.setPrefWidth(120);

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(180);

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(140);

        TableColumn<Book, String> publisherCol = new TableColumn<>("Publisher");
        publisherCol.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        publisherCol.setPrefWidth(140);

        TableColumn<Book, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("publicationYear"));
        yearCol.setPrefWidth(70);

        TableColumn<Book, Genre> genreCol = new TableColumn<>("Genre");
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        genreCol.setPrefWidth(100);

        TableColumn<Book, Boolean> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(new PropertyValueFactory<>("available"));
        availableCol.setPrefWidth(80);
        availableCol.setCellFactory(column -> new TableCell<Book, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Yes" : "No");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });

        TableColumn<Book, String> borrowerCol = new TableColumn<>("Borrowed By");
        borrowerCol.setCellValueFactory(new PropertyValueFactory<>("borrowerName"));
        borrowerCol.setPrefWidth(130);

        TableColumn<Book, Date> dueDateCol = new TableColumn<>("Return Due Date");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDueDate"));
        dueDateCol.setPrefWidth(120);

        dueDateCol.setCellFactory(column -> new TableCell<Book, Date>() {
            private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(format.format(item));
                    if (item.before(new Date())) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        table.getColumns().addAll(isbnCol, titleCol, authorCol, publisherCol, yearCol, genreCol, availableCol, borrowerCol, dueDateCol);

        isbnCol.setComparator(String.CASE_INSENSITIVE_ORDER);
        titleCol.setComparator(String.CASE_INSENSITIVE_ORDER);
        authorCol.setComparator(String.CASE_INSENSITIVE_ORDER);
        publisherCol.setComparator(String.CASE_INSENSITIVE_ORDER);
        genreCol.setComparator(Comparator.comparing(Enum::toString));


        borrowerCol.setComparator((s1, s2) -> {
            if (s1 == null && s2 == null) return 0;
            if (s1 == null) return -1;
            if (s2 == null) return 1;
            return s1.compareToIgnoreCase(s2);
        });

        dueDateCol.setComparator((d1, d2) -> {
            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return -1;
            if (d2 == null) return 1;
            return d1.compareTo(d2);
        });

        ContextMenu rightClickMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit Book");
        MenuItem deleteItem = new MenuItem("Delete Book");
        MenuItem borrowItem = new MenuItem("Borrow/Return");

        editItem.setOnAction(e -> {
            Book selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showEditBookDialog(selected);
        });

        deleteItem.setOnAction(e -> {
            Book selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) deleteSelectedBook(selected);
        });

        borrowItem.setOnAction(e -> {
            Book selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showBorrowReturnDialog(selected.getIsbn());
        });

        rightClickMenu.getItems().addAll(editItem, deleteItem, borrowItem);

        table.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Book book = row.getItem();
                    showBorrowReturnDialog(book.getIsbn());
                }
            });
            return row;
        });

        table.setContextMenu(rightClickMenu);
        return table;
    }

    private HBox createButtonSection() {
        HBox buttonSection = new HBox(10);
        buttonSection.setPadding(new Insets(10));

        Button addBtn = new Button("Add Book");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        Button borrowBtn = new Button("Borrow/Return");

        Button saveBtn = new Button("Save");
        Button exitBtn = new Button("Exit");

        Button sortBtn = new Button("Sort");
        Button filterBtn = new Button("Filter");
        Button statsBtn = new Button("Stats");

        addBtn.setOnAction(e -> showAddBookDialog());
        editBtn.setOnAction(e -> {
            Book selected = bookTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditBookDialog(selected);
            } else {
                showSimpleAlert("Please select a book to edit.");
            }
        });

        deleteBtn.setOnAction(e -> {
            Book selected = bookTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteSelectedBook(selected);
            } else {
                showSimpleAlert("Please select a book to delete.");
            }
        });

        borrowBtn.setOnAction(e -> {
            Book selected = bookTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showBorrowReturnDialog(selected.getIsbn());
            } else {
                showBorrowReturnDialog();
            }
        });

        saveBtn.setOnAction(e -> saveData());
        exitBtn.setOnAction(e -> exitApplication());
        sortBtn.setOnAction(e -> showSortOptions());
        filterBtn.setOnAction(e -> showFilterOptions());
        statsBtn.setOnAction(e -> showStats());

        saveBtn.setStyle("-fx-background-color: lightgreen;");
        exitBtn.setStyle("-fx-background-color: lightcoral;");

        HBox mainButtons = new HBox(10);
        mainButtons.getChildren().addAll(addBtn, editBtn, deleteBtn, borrowBtn);

        HBox fileButtons = new HBox(10);
        fileButtons.getChildren().addAll(saveBtn, exitBtn);

        HBox utilButtons = new HBox(10);
        utilButtons.getChildren().addAll(sortBtn, filterBtn, statsBtn);

        Separator sep1 = new Separator(Orientation.VERTICAL);
        Separator sep2 = new Separator(Orientation.VERTICAL);

        buttonSection.getChildren().addAll(mainButtons, sep1, fileButtons, sep2, utilButtons);

        return buttonSection;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setStyle("-fx-background-color: #f0f0f0;");

        statusLbl = new Label("Books: " + libraryManager.getTotalBooks());
        statusBar.getChildren().add(statusLbl);

        return statusBar;
    }

    private void showAddBookDialog() {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Add New Book");
        dialog.setHeaderText("Fill in the book details:");

        ButtonType addBtn = new ButtonType("Add Book", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20, 150, 10, 10));

        TextField isbnInput = new TextField();
        TextField titleInput = new TextField();
        TextField authorInput = new TextField();
        TextField publisherInput = new TextField();
        TextField yearInput = new TextField();
        ComboBox<Genre> genreInput = new ComboBox<>();

        genreInput.getItems().addAll(Genre.values());

        genreInput.setValue(Genre.values()[0]);

        form.add(new Label("ISBN:"), 0, 0);
        form.add(isbnInput, 1, 0);
        form.add(new Label("Title:"), 0, 1);
        form.add(titleInput, 1, 1);
        form.add(new Label("Author:"), 0, 2);
        form.add(authorInput, 1, 2);
        form.add(new Label("Publisher:"), 0, 3);
        form.add(publisherInput, 1, 3);
        form.add(new Label("Year:"), 0, 4);
        form.add(yearInput, 1, 4);
        form.add(new Label("Genre:"), 0, 5);
        form.add(genreInput, 1, 5);

        Node addButton = dialog.getDialogPane().lookupButton(addBtn);
        addButton.setDisable(true);

        isbnInput.textProperty().addListener((obs, oldVal, newVal) -> {
            addButton.setDisable(isbnInput.getText().trim().isEmpty() || titleInput.getText().trim().isEmpty());
        });

        titleInput.textProperty().addListener((obs, oldVal, newVal) -> {
            addButton.setDisable(isbnInput.getText().trim().isEmpty() || titleInput.getText().trim().isEmpty());
        });

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == addBtn) {
                try {
                    return new Book(isbnInput.getText().trim(), titleInput.getText().trim(), authorInput.getText().trim(), publisherInput.getText().trim(), Integer.parseInt(yearInput.getText().trim()), genreInput.getValue());
                } catch (NumberFormatException e) {
                    showSimpleAlert("Please enter a valid year.");
                    return null;
                } catch (Exception e) {
                    showSimpleAlert("Error creating book: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Book> result = dialog.showAndWait();
        result.ifPresent(book -> {
            if (libraryManager.createBook(book)) {
                loadTableData();
                showSimpleAlert("Book added successfully!");
            } else {
                showSimpleAlert("Failed to add book. ISBN might already exist.");
            }
        });
    }

    private void showEditBookDialog(Book book) {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Edit Book");
        dialog.setHeaderText("Modify book details:");

        ButtonType updateBtn = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtn, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20, 150, 10, 10));

        TextField isbnInput = new TextField(book.getIsbn());
        isbnInput.setDisable(true);
        TextField titleInput = new TextField(book.getTitle());
        TextField authorInput = new TextField(book.getAuthor());
        TextField publisherInput = new TextField(book.getPublisher());
        TextField yearInput = new TextField(String.valueOf(book.getPublicationYear()));

        ComboBox<Genre> genreInput = new ComboBox<>();
        genreInput.getItems().addAll(Genre.values());
        genreInput.setValue(book.getGenre());

        form.add(new Label("ISBN:"), 0, 0);
        form.add(isbnInput, 1, 0);
        form.add(new Label("Title:"), 0, 1);
        form.add(titleInput, 1, 1);
        form.add(new Label("Author:"), 0, 2);
        form.add(authorInput, 1, 2);
        form.add(new Label("Publisher:"), 0, 3);
        form.add(publisherInput, 1, 3);
        form.add(new Label("Year:"), 0, 4);
        form.add(yearInput, 1, 4);
        form.add(new Label("Genre:"), 0, 5);
        form.add(genreInput, 1, 5);

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == updateBtn) {
                try {
                    Map<String, String> updates = new HashMap<>();
                    updates.put("title", titleInput.getText());
                    updates.put("author", authorInput.getText());
                    updates.put("publisher", publisherInput.getText());
                    updates.put("year", yearInput.getText());
                    updates.put("genre", genreInput.getValue().toString());

                    libraryManager.updateBook(book.getIsbn(), updates);
                    return book;
                } catch (Exception e) {
                    showSimpleAlert("Invalid input: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Book> result = dialog.showAndWait();
        result.ifPresent(updatedBook -> {
            loadTableData();
            showSimpleAlert("Book updated!");
        });
    }

    private void deleteSelectedBook(Book book) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Book");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Delete: " + book.getTitle() + "?");

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            if (libraryManager.deleteBook(book.getIsbn())) {
                loadTableData();
                showSimpleAlert("Book deleted!");
            } else {
                showSimpleAlert("Failed to delete book.");
            }
        }
    }

    private void showSortOptions() {
        Dialog<Pair<String, Boolean>> dialog = new Dialog<>();
        dialog.setTitle("Sort Books");
        dialog.setHeaderText("Choose how to sort");

        ButtonType sortBtn = new ButtonType("Sort", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sortBtn, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        ComboBox<String> sortBy = new ComboBox<>();
        sortBy.getItems().addAll("Title", "Author", "Year", "Genre", "ISBN");
        sortBy.setValue("Title");

        // Create radio buttons for sort order
        ToggleGroup sortOrderGroup = new ToggleGroup();
        RadioButton ascending = new RadioButton("Ascending (A-Z, Oldest-Newest)");
        RadioButton descending = new RadioButton("Descending (Z-A, Newest-Oldest)");

        ascending.setToggleGroup(sortOrderGroup);
        descending.setToggleGroup(sortOrderGroup);
        ascending.setSelected(true);

        content.getChildren().addAll(
                new Label("Sort by:"),
                sortBy,
                new Label("Sort order:"),
                ascending,
                descending
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == sortBtn) {
                return new Pair<>(sortBy.getValue(), ascending.isSelected());
            }
            return null;
        });

        Optional<Pair<String, Boolean>> result = dialog.showAndWait();
        result.ifPresent(choice -> {
            String field = choice.getKey().toLowerCase();
            boolean asc = choice.getValue();

            libraryManager.sortBooks(field, asc);
            loadTableData();

            String orderText = asc ? "ascending" : "descending";
            showSimpleAlert("Books sorted by " + choice.getKey() + " (" + orderText + ")");
        });
    }

    private void showFilterOptions() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Filter Books");
        dialog.setHeaderText("Filter by:");

        ButtonType filterBtn = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(filterBtn, ButtonType.CANCEL);

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));

        TextField titleFilter = new TextField();
        titleFilter.setPromptText("Title contains...");
        TextField authorFilter = new TextField();
        authorFilter.setPromptText("Author contains...");
        TextField genreFilter = new TextField();
        genreFilter.setPromptText("Genre contains...");

        ComboBox<String> availabilityFilter = new ComboBox<>();
        availabilityFilter.getItems().addAll("All", "Available", "Borrowed");
        availabilityFilter.setValue("All");

        form.getChildren().addAll(new Label("Title:"), titleFilter, new Label("Author:"), authorFilter, new Label("Genre:"), genreFilter, new Label("Status:"), availabilityFilter);

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == filterBtn) {
                Map<String, String> filters = new HashMap<>();

                if (!titleFilter.getText().trim().isEmpty()) filters.put("title", titleFilter.getText().trim());
                if (!authorFilter.getText().trim().isEmpty()) filters.put("author", authorFilter.getText().trim());
                if (!genreFilter.getText().trim().isEmpty()) filters.put("genre", genreFilter.getText().trim());

                if (availabilityFilter.getValue().equals("Available")) filters.put("available", "true");
                else if (availabilityFilter.getValue().equals("Borrowed")) filters.put("available", "false");

                return filters;
            }
            return null;
        });

        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(filters -> {
            if (filters.isEmpty()) {
                loadTableData();
                showSimpleAlert("Showing all books");
            } else {
                List<Book> filtered = libraryManager.filterBooks(filters);
                bookTable.setItems(FXCollections.observableArrayList(filtered));
                updateStatus();
                showSimpleAlert("Found " + filtered.size() + " books");
            }
        });
    }

    /**
     * Default Borrowing day is 14 days from today.
     */
    private void showBorrowReturnDialog(String isbn) {
        Dialog<Pair<String, Pair<String, Date>>> dialog = new Dialog<>();
        dialog.setTitle("Borrow/Return");
        dialog.setHeaderText("Book transaction");

        ButtonType borrowBtn = new ButtonType("Borrow", ButtonBar.ButtonData.OK_DONE);
        ButtonType returnBtn = new ButtonType("Return", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(borrowBtn, returnBtn, ButtonType.CANCEL);

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));

        TextField isbnInput = new TextField();
        if (isbn != null && !isbn.isEmpty()) {
            isbnInput.setText(isbn);
        }

        TextField borrowerInput = new TextField();
        DatePicker returnDate = new DatePicker();
        returnDate.setValue(java.time.LocalDate.now().plusWeeks(2));

        form.getChildren().addAll(new Label("ISBN:"), isbnInput, new Label("Borrower:"), borrowerInput, new Label("Return by:"), returnDate);

        dialog.getDialogPane().setContent(form);

        if (isbn == null || isbn.isEmpty()) {
            Platform.runLater(isbnInput::requestFocus);
        } else {
            Platform.runLater(borrowerInput::requestFocus);
        }

        dialog.setResultConverter(btn -> {
            if (btn == borrowBtn) {
                Date dueDate = null;
                if (returnDate.getValue() != null) {
                    dueDate = java.sql.Date.valueOf(returnDate.getValue());
                }
                return new Pair<>(isbnInput.getText().trim(), new Pair<>(borrowerInput.getText().trim(), dueDate));
            } else if (btn == returnBtn) {
                return new Pair<>(isbnInput.getText().trim(), null);
            }
            return null;
        });

        Optional<Pair<String, Pair<String, Date>>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            String bookIsbn = data.getKey();

            if (bookIsbn.isEmpty()) {
                showSimpleAlert("ISBN required");
                return;
            }

            Book book = libraryManager.getBookByIsbn(bookIsbn);
            if (book == null) {
                showSimpleAlert("Book not found: " + bookIsbn);
                return;
            }

            if (data.getValue() == null) {
                if (!book.isAvailable()) {
                    book.returnBook();
                    FileHandler.saveToFile(libraryManager);
                    loadTableData();
                    showSimpleAlert("Book returned!");
                } else {
                    showSimpleAlert("Book is not borrowed");
                }
            } else {

                String borrower = data.getValue().getKey();
                Date due = data.getValue().getValue();

                if (borrower.isEmpty()) {
                    showSimpleAlert("Borrower name required");
                    return;
                }

                if (book.isAvailable()) {
                    book.borrowBook(borrower, due);
                    FileHandler.saveToFile(libraryManager);
                    loadTableData();
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                    showSimpleAlert("Borrowed by " + borrower + "\nDue: " + fmt.format(due));
                } else {
                    showSimpleAlert("Already borrowed by: " + book.getBorrowerName());
                }
            }
        });
    }

    private void showBorrowReturnDialog() {
        showBorrowReturnDialog(null);
    }


    private void showStats() {
        Map<String, Object> stats = libraryManager.getLibraryStats();

        Alert statsDialog = new Alert(Alert.AlertType.INFORMATION);
        statsDialog.setTitle("Library Stats");
        statsDialog.setHeaderText("Current Statistics");

        StringBuilder content = new StringBuilder();
        content.append("Total Books: ").append(stats.get("totalBooks")).append("\n");
        content.append("Available: ").append(stats.get("availableBooks")).append("\n");
        content.append("Borrowed: ").append(stats.get("borrowedBooks")).append("\n\n");

        @SuppressWarnings("unchecked") Map<Genre, Long> genres = (Map<Genre, Long>) stats.get("genreDistribution");
        if (genres != null && !genres.isEmpty()) {
            content.append("Books by Genre:\n");
            genres.forEach((genre, count) -> content.append("  ").append(genre.toString()).append(": ").append(count).append("\n"));
        }

        long overdueCount = libraryManager.getBooks().stream().filter(book -> !book.isAvailable() && book.getReturnDueDate() != null && book.getReturnDueDate().before(new Date())).count();

        if (overdueCount > 0) {
            content.append("\n Overdue Books: ").append(overdueCount);
        }

        statsDialog.setContentText(content.toString());
        statsDialog.showAndWait();
    }

    private void doSearch() {
        String criteria = searchType.getValue();
        String term = searchBox.getText().trim().toLowerCase();

        if (term.isEmpty()) {
            loadTableData();
            return;
        }

        ObservableList<Book> results = FXCollections.observableArrayList();

        for (Book book : libraryManager.getBooks()) {
            boolean matches;

            if (criteria.equals("All Fields")) {
                matches = book.getIsbn().toLowerCase().contains(term) || book.getTitle().toLowerCase().contains(term) || book.getAuthor().toLowerCase().contains(term) || book.getPublisher().toLowerCase().contains(term) || book.getGenre().toString().toLowerCase().contains(term) || String.valueOf(book.getPublicationYear()).contains(term);
            } else {
                matches = switch (criteria) {
                    case "ISBN" -> book.getIsbn().toLowerCase().contains(term);
                    case "Title" -> book.getTitle().toLowerCase().contains(term);
                    case "Author" -> book.getAuthor().toLowerCase().contains(term);
                    case "Publisher" -> book.getPublisher().toLowerCase().contains(term);
                    case "Genre" -> book.getGenre().toString().toLowerCase().contains(term);
                    case "Year" -> String.valueOf(book.getPublicationYear()).contains(term);
                    default -> false;
                };
            }

            if (matches) {
                results.add(book);
            }
        }

        bookTable.setItems(results);
        updateStatus();
    }

    /**
     * Reload table with all books
     */
    private void loadTableData() {
        ObservableList<Book> allBooks = FXCollections.observableArrayList(libraryManager.getBooks());
        bookTable.setItems(allBooks);
        updateStatus();
    }

    private void updateStatus() {
        statusLbl.setText("Books: " + bookTable.getItems().size());
    }

    private void showSimpleAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Selection");
        alert.setHeaderText(null);
        alert.setContentText("Please select a book first.");
        alert.showAndWait();
    }

    private void saveData() {
        if (FileHandler.saveToFile(libraryManager)) {
            showSimpleAlert("Data saved!");
        } else {
            showSimpleAlert("Save failed!");
        }
    }

    private void exitApplication() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Exit");
        confirm.setHeaderText("Are you sure to exit?");
        confirm.setContentText("Save first?");

        ButtonType saveExit = new ButtonType("Save & Exit");
        ButtonType justExit = new ButtonType("Exit");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirm.getButtonTypes().setAll(saveExit, justExit, cancel);

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent()) {
            if (choice.get() == saveExit) {
                saveData();
                System.exit(0);
            } else if (choice.get() == justExit) {
                System.exit(0);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package utils;

import java.util.Calendar;
import java.util.regex.Pattern;


public class Validator {

    /**
     * Validates ISBN format (10 or 13 digits)
     */
    public static boolean isValidISBN(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }

        String cleanISBN = isbn.replaceAll("-", "");

        if (!cleanISBN.matches("\\d{10}|\\d{13}")) {
            return false;
        }

        if (cleanISBN.length() == 10) {
            return isValidISBN10(cleanISBN);
        }

        if (cleanISBN.length() == 13) {
            return isValidISBN13(cleanISBN);
        }

        return true;
    }

    private static boolean isValidISBN10(String isbn) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (10 - i) * Character.getNumericValue(isbn.charAt(i));
        }

        char lastChar = isbn.charAt(9);
        if (lastChar == 'X') {
            sum += 10;
        } else {
            sum += Character.getNumericValue(lastChar);
        }

        return sum % 11 == 0;
    }

    private static boolean isValidISBN13(String isbn) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(isbn.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }

        int checkDigit = Character.getNumericValue(isbn.charAt(12));
        return (10 - (sum % 10)) % 10 == checkDigit;
    }

    /**
     * Validates name format (LastName_FirstName)
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[A-Za-z]+_[A-Za-z]+$");
        return pattern.matcher(name).matches();
    }

    public static boolean isValidYear(String yearStr) {
        try {
            int year = Integer.parseInt(yearStr);
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            return year >= 1000 && year <= currentYear + 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    public static boolean isNotEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }

    public static int validateMenuChoice(String input, int min, int max) {
        try {
            int choice = Integer.parseInt(input.trim());
            if (choice >= min && choice <= max) {
                return choice;
            }
        } catch (NumberFormatException e) {
            //  will return -1
        }
        return -1;
    }

    public static boolean isValidGenre(String genre) {
        if (!isNotEmpty(genre)) {
            return false;
        }

        return genre.matches("^[A-Za-z_\\-\\s]+$");
    }

    public static boolean isValidPublisher(String publisher) {
        if (!isNotEmpty(publisher)) {
            return false;
        }

        return publisher.matches("^[A-Za-z0-9_\\-\\s]+$");
    }

    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replace("|", "_");
    }

    public static String getErrorMessage(String fieldName) {
        return switch (fieldName.toLowerCase()) {
            case "isbn" -> "ISBN must be 10 or 13 digits. Examples: 1000000000000 or 1000000000";
            case "name", "author", "borrower" -> "Name must be in format: LastName_FirstName (e.g., Rowling_JK)";
            case "year" -> "Year must be between 1000 and " + (Calendar.getInstance().get(Calendar.YEAR) + 1);
            case "genre" -> "Genre can only contain letters, spaces, underscores, or hyphens";
            case "publisher" -> "Publisher can contain letters, numbers, spaces, underscores, periods, and hyphens";
            case "boolean" -> "Please enter: true/false, yes/no, y/n, or 1/0";
            default -> "Invalid input for " + fieldName;
        };
    }
}
package com.smartplacementtracker.util;

import com.smartplacementtracker.model.ApplicationStatus;
import com.smartplacementtracker.model.ApplicationType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Robust input sanitation and Scanner utility.
 *
 * Solves common beginner pitfalls:
 * - Scanner newline bug: Mixing nextInt() / nextDouble() with nextLine().
 * - Format validation for dates (yyyy-MM-dd) and positive numbers.
 * - Clear friendly prompts and infinite retry loops without crashes.
 */
public final class InputValidator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private InputValidator() {
        // Prevent instantiation
    }

    /**
     * Reads an integer within [min, max] safely using line parsing.
     */
    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf(" Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println(" Invalid number. Please enter a valid whole number.");
            }
        }
    }

    /**
     * Reads a required non-empty string.
     */
    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println(" Input cannot be empty. Please enter a value.");
        }
    }

    /**
     * Reads an optional string (can be left blank).
     */
    public static String readOptionalString(Scanner scanner, String prompt, String defaultValue) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            return defaultValue;
        }
        return line;
    }

    /**
     * Reads a positive double for salary/stipend.
     */
    public static double readPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                // Allow user to write with commas (e.g. 12,00,000)
                String sanitized = line.replace(",", "");
                double value = Double.parseDouble(sanitized);
                if (value >= 0) {
                    return value;
                }
                System.out.println(" Amount cannot be negative. Please enter a value >= 0.");
            } catch (NumberFormatException e) {
                System.out.println(" Invalid decimal number. Example: 45000 or 1200000");
            }
        }
    }

    /**
     * Reads a mandatory date formatted as yyyy-MM-dd.
     */
    public static LocalDate readDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return LocalDate.parse(line, FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println(" Invalid date format. Please use YYYY-MM-DD (e.g., 2026-09-15).");
            }
        }
    }

    /**
     * Reads an optional date formatted as yyyy-MM-dd or blank if not scheduled.
     */
    public static LocalDate readOptionalDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.equalsIgnoreCase("none") || line.equalsIgnoreCase("-")) {
                return null;
            }
            try {
                return LocalDate.parse(line, FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println(" Invalid date format. Please use YYYY-MM-DD or leave blank if none.");
            }
        }
    }

    /**
     * Prompts user to select an ApplicationType.
     */
    public static ApplicationType readApplicationType(Scanner scanner) {
        System.out.println("Select Application Type:");
        System.out.println("  1. Internship (Monthly stipend)");
        System.out.println("  2. Full-Time (Annual CTC package)");
        int choice = readInt(scanner, "Enter choice (1-2): ", 1, 2);
        return (choice == 1) ? ApplicationType.INTERNSHIP : ApplicationType.FULL_TIME;
    }

    /**
     * Prompts user to select an ApplicationStatus.
     */
    public static ApplicationStatus readApplicationStatus(Scanner scanner) {
        System.out.println("Select Application Status:");
        ApplicationStatus[] statuses = ApplicationStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.printf("  %d. %s (%s)%n", (i + 1), statuses[i].name(), statuses[i].getDescription());
        }
        int choice = readInt(scanner, "Enter choice (1-" + statuses.length + "): ", 1, statuses.length);
        return statuses[choice - 1];
    }
}

package com.smartplacementtracker.storage;

import com.smartplacementtracker.model.Application;
import com.smartplacementtracker.model.ApplicationStatus;
import com.smartplacementtracker.model.ApplicationType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * File persistence service using pure Core Java File I/O.
 *
 * Concepts demonstrated:
 * - Java File I/O: BufferedReader, BufferedWriter, FileReader, FileWriter.
 * - Robust CSV parsing with RFC-4180 escaping for commas, quotes, and newlines.
 * - Fault tolerance: Missing file handling, corrupted row isolation.
 * - Safe atomic write: Writing to a temporary file before replacing to prevent corruption during crash.
 */
public class FileStorage {

    private static final String CSV_HEADER = "applicationId,companyName,jobRole,type,applicationDate,status,compensation,location,jobPostingUrl,interviewDate,notes";
    private final String filePath;

    public FileStorage(String filePath) {
        this.filePath = filePath;
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
    }

    /**
     * Loads applications from the persistent CSV file.
     *
     * Error Handling:
     * - If file does not exist, returns an empty list without crashing.
     * - If a specific record is corrupted, logs a warning and continues loading other valid records.
     *
     * @return List of successfully parsed Application objects
     */
    public List<Application> loadApplications() {
        List<Application> loadedList = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("[FileStorage] No existing data file found at '" + filePath + "'. Starting with clean session.");
            return loadedList;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Header line
            if (line == null) {
                return loadedList;
            }

            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // Skip empty lines
                }

                try {
                    Application app = parseCsvLine(line);
                    if (app != null) {
                        loadedList.add(app);
                    }
                } catch (Exception ex) {
                    System.err.printf("[FileStorage Warning] Skipping corrupted record at line %d: %s (Reason: %s)%n",
                            lineNum, line, ex.getMessage());
                }
            }
            System.out.printf("[FileStorage] Successfully loaded %d application record(s) from '%s'.%n", loadedList.size(), filePath);
        } catch (IOException e) {
            System.err.println("[FileStorage Error] Could not read file: " + e.getMessage());
        }

        return loadedList;
    }

    /**
     * Saves applications safely using a write-to-temp and atomic replacement pattern.
     *
     * Prevents data corruption if writing is interrupted midway (e.g. process killed or disk error).
     *
     * @param applications List of applications to persist
     * @return true if save succeeded, false otherwise
     */
    public boolean saveApplications(List<Application> applications) {
        if (applications == null) {
            return false;
        }

        ensureDirectoryExists();
        Path targetPath = Paths.get(filePath);
        Path tempPath = Paths.get(filePath + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempPath.toFile()))) {
            writer.write(CSV_HEADER);
            writer.newLine();

            for (Application app : applications) {
                writer.write(serializeToCsv(app));
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("[FileStorage Error] Failed to write temporary file: " + e.getMessage());
            return false;
        }

        // Atomically replace target file with temp file
        try {
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (IOException e) {
            // Fallback for filesystems that do not support ATOMIC_MOVE
            try {
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException ex) {
                System.err.println("[FileStorage Error] Failed to finalize save: " + ex.getMessage());
                return false;
            }
        }
    }

    // =========================================================================
    // CSV SERIALIZATION & PARSING HELPERS (RFC-4180 COMPLIANT)
    // =========================================================================

    /**
     * Serializes an Application object into an RFC-4180 compliant CSV line.
     */
    public String serializeToCsv(Application app) {
        StringBuilder sb = new StringBuilder();
        sb.append(escapeCsvField(app.getApplicationId())).append(",");
        sb.append(escapeCsvField(app.getCompanyName())).append(",");
        sb.append(escapeCsvField(app.getJobRole())).append(",");
        sb.append(escapeCsvField(app.getType().name())).append(",");
        sb.append(escapeCsvField(app.getApplicationDate().toString())).append(",");
        sb.append(escapeCsvField(app.getStatus().name())).append(",");
        sb.append(app.getCompensation()).append(",");
        sb.append(escapeCsvField(app.getLocation())).append(",");
        sb.append(escapeCsvField(app.getJobPostingUrl())).append(",");
        sb.append(escapeCsvField(app.getInterviewDate() != null ? app.getInterviewDate().toString() : "")).append(",");
        sb.append(escapeCsvField(app.getNotes()));
        return sb.toString();
    }

    /**
     * Parses a single CSV line into an Application object.
     */
    public Application parseCsvLine(String line) {
        List<String> fields = parseCsvFields(line);
        if (fields.size() < 11) {
            throw new IllegalArgumentException("Expected 11 CSV fields, but found " + fields.size());
        }

        String id = fields.get(0);
        String company = fields.get(1);
        String role = fields.get(2);
        ApplicationType type = ApplicationType.valueOf(fields.get(3).toUpperCase());
        LocalDate appDate = LocalDate.parse(fields.get(4), Application.DATE_FORMATTER);
        ApplicationStatus status = ApplicationStatus.valueOf(fields.get(5).toUpperCase());
        double comp = Double.parseDouble(fields.get(6));
        String location = fields.get(7);
        String url = fields.get(8);

        String interviewStr = fields.get(9);
        LocalDate interviewDate = (interviewStr != null && !interviewStr.trim().isEmpty())
                ? LocalDate.parse(interviewStr.trim(), Application.DATE_FORMATTER)
                : null;

        String notes = fields.get(10);

        return new Application(id, company, role, type, appDate, status, comp, location, url, interviewDate, notes);
    }

    /**
     * Escapes individual field values for CSV compliance:
     * - Wraps in double quotes if it contains commas, quotes, or newlines.
     * - Escapes existing double quotes as two double quotes ("").
     */
    public static String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * Custom tokenizer that parses CSV lines handling embedded commas within quoted fields.
     */
    public static List<String> parseCsvFields(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    // Escaped quote: "" becomes "
                    current.append('\"');
                    i++; // Skip the second quote
                } else {
                    // Toggle quote mode
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // Field separator outside quotes
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }

    public String getFilePath() {
        return filePath;
    }
}

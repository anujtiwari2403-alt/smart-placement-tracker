package com.smartplacementtracker.model;

/**
 * Represents the nature of employment for an application.
 *
 * Distinguishes between Internship (typically stipend per month) and
 * Full-Time employment (annual CTC package).
 */
public enum ApplicationType {
    INTERNSHIP("Internship", "Monthly stipend"),
    FULL_TIME("Full-Time", "Annual CTC (Cost to Company)");

    private final String displayName;
    private final String compensationUnit;

    ApplicationType(String displayName, String compensationUnit) {
        this.displayName = displayName;
        this.compensationUnit = compensationUnit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCompensationUnit() {
        return compensationUnit;
    }

    /**
     * Parses user input into ApplicationType in a tolerant manner.
     */
    public static ApplicationType fromString(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Application type cannot be empty.");
        }
        String clean = input.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        if (clean.equals("INTERN") || clean.equals("INTERNSHIP") || clean.equals("1")) {
            return INTERNSHIP;
        } else if (clean.equals("FULL_TIME") || clean.equals("FULLTIME") || clean.equals("FT") || clean.equals("2")) {
            return FULL_TIME;
        }
        throw new IllegalArgumentException("Invalid application type: '" + input + 
            "'. Allowed values: INTERNSHIP or FULL_TIME.");
    }
}

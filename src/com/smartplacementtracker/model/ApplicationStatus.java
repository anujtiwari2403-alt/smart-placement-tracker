package com.smartplacementtracker.model;

/**
 * Represents the current status/stage of a job or internship application.
 *
 * Concepts demonstrated:
 * - Java Enums: Type-safe, finite set of constants.
 * - Encapsulation of display strings and business logic within enum values.
 */
public enum ApplicationStatus {
    APPLIED("Applied", "Application submitted, awaiting initial review"),
    ASSESSMENT("Assessment", "Online assessment, coding test, or aptitude round"),
    INTERVIEW("Interview", "Technical, managerial, or HR interview stage"),
    OFFER("Offer", "Job or internship offer received"),
    REJECTED("Rejected", "Application not moving forward"),
    WITHDRAWN("Withdrawn", "Application cancelled or withdrawn by candidate");

    private final String displayName;
    private final String description;

    ApplicationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Case-insensitive parser helper for user input.
     *
     * @param input Raw text from user
     * @return Corresponding ApplicationStatus
     * @throws IllegalArgumentException if no match is found
     */
    public static ApplicationStatus fromString(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Status input cannot be empty.");
        }
        String clean = input.trim().toUpperCase().replace(" ", "_");
        for (ApplicationStatus status : values()) {
            if (status.name().equals(clean) || status.displayName.equalsIgnoreCase(clean)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid application status: '" + input + 
            "'. Allowed: APPLIED, ASSESSMENT, INTERVIEW, OFFER, REJECTED, WITHDRAWN.");
    }
}

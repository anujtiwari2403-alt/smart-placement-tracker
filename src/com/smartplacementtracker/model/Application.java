package com.smartplacementtracker.model;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Domain entity representing an individual Job or Internship Application.
 *
 * Concepts demonstrated:
 * - OOP Encapsulation: All fields are private with validated getters and setters.
 * - Defensive validation in constructor and setters.
 * - Java Date and Time API (java.time.LocalDate, java.time.format.DateTimeFormatter).
 * - Method overriding for equals(), hashCode(), and toString().
 * - Normalized business logic for fair salary/stipend comparison.
 */
public class Application {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Locale INDIA_LOCALE = Locale.of("en", "IN");

    private final String applicationId;
    private String companyName;
    private String jobRole;
    private ApplicationType type;
    private LocalDate applicationDate;
    private ApplicationStatus status;
    private double compensation; // monthly for INTERNSHIP, annual for FULL_TIME
    private String location;
    private String jobPostingUrl;
    private LocalDate interviewDate; // nullable
    private String notes;

    /**
     * Primary constructor with strict validation.
     */
    public Application(String applicationId,
                       String companyName,
                       String jobRole,
                       ApplicationType type,
                       LocalDate applicationDate,
                       ApplicationStatus status,
                       double compensation,
                       String location,
                       String jobPostingUrl,
                       LocalDate interviewDate,
                       String notes) {
        if (applicationId == null || applicationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Application ID cannot be null or empty.");
        }
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be null or empty.");
        }
        if (jobRole == null || jobRole.trim().isEmpty()) {
            throw new IllegalArgumentException("Job role cannot be null or empty.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Application type cannot be null.");
        }
        if (applicationDate == null) {
            throw new IllegalArgumentException("Application date cannot be null.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Application status cannot be null.");
        }
        if (compensation < 0) {
            throw new IllegalArgumentException("Compensation cannot be negative.");
        }

        this.applicationId = applicationId.trim().toUpperCase();
        this.companyName = companyName.trim();
        this.jobRole = jobRole.trim();
        this.type = type;
        this.applicationDate = applicationDate;
        this.status = status;
        this.compensation = compensation;
        this.location = (location == null || location.trim().isEmpty()) ? "Remote / Unspecified" : location.trim();
        this.jobPostingUrl = (jobPostingUrl == null || jobPostingUrl.trim().isEmpty()) ? "N/A" : jobPostingUrl.trim();
        this.interviewDate = interviewDate;
        this.notes = (notes == null || notes.trim().isEmpty()) ? "-" : notes.trim();
    }

    // --- Core Normalized Metric for DSA Comparisons ---

    /**
     * Normalizes compensation to an annual scale so internships and full-time
     * offers can be compared accurately without comparing monthly vs annual directly.
     *
     * Example:
     * - Internship at ₹50,000/month => Annualized = ₹6,00,000
     * - Full-Time at ₹12,00,000/year => Annualized = ₹12,00,000
     */
    public double getAnnualizedCompensation() {
        if (type == ApplicationType.INTERNSHIP) {
            return compensation * 12.0;
        }
        return compensation;
    }

    /**
     * Formats compensation cleanly in Indian currency format with appropriate unit.
     */
    public String getFormattedCompensation() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(INDIA_LOCALE);
        String formatted = currencyFormat.format(compensation);
        if (type == ApplicationType.INTERNSHIP) {
            return formatted + " / month (Stipend)";
        } else {
            return formatted + " / year (CTC)";
        }
    }

    public String getFormattedApplicationDate() {
        return applicationDate != null ? applicationDate.format(DATE_FORMATTER) : "N/A";
    }

    public String getFormattedInterviewDate() {
        return interviewDate != null ? interviewDate.format(DATE_FORMATTER) : "Not Scheduled";
    }

    // --- Getters and Setters ---

    public String getApplicationId() {
        return applicationId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be blank.");
        }
        this.companyName = companyName.trim();
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        if (jobRole == null || jobRole.trim().isEmpty()) {
            throw new IllegalArgumentException("Job role cannot be blank.");
        }
        this.jobRole = jobRole.trim();
    }

    public ApplicationType getType() {
        return type;
    }

    public void setType(ApplicationType type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }
        this.type = type;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        if (applicationDate == null) {
            throw new IllegalArgumentException("Application date cannot be null.");
        }
        this.applicationDate = applicationDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }
        this.status = status;
    }

    public double getCompensation() {
        return compensation;
    }

    public void setCompensation(double compensation) {
        if (compensation < 0) {
            throw new IllegalArgumentException("Compensation cannot be negative.");
        }
        this.compensation = compensation;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = (location == null || location.trim().isEmpty()) ? "Remote / Unspecified" : location.trim();
    }

    public String getJobPostingUrl() {
        return jobPostingUrl;
    }

    public void setJobPostingUrl(String jobPostingUrl) {
        this.jobPostingUrl = (jobPostingUrl == null || jobPostingUrl.trim().isEmpty()) ? "N/A" : jobPostingUrl.trim();
    }

    public LocalDate getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDate interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = (notes == null || notes.trim().isEmpty()) ? "-" : notes.trim();
    }

    // --- Object Contract: equals and hashCode ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Application that)) return false;
        return Objects.equals(applicationId, that.applicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId);
    }

    @Override
    public String toString() {
        return String.format(
            "[%s] %s - %s (%s) | Status: %s | Comp: %s | App Date: %s | Interview: %s | Location: %s",
            applicationId,
            companyName,
            jobRole,
            type.getDisplayName(),
            status.getDisplayName(),
            getFormattedCompensation(),
            getFormattedApplicationDate(),
            getFormattedInterviewDate(),
            location
        );
    }
}

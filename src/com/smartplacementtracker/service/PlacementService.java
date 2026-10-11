package com.smartplacementtracker.service;

import com.smartplacementtracker.model.Application;
import com.smartplacementtracker.model.ApplicationStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service class managing application records in memory.
 *
 * Concepts demonstrated:
 * - Java Collections: ArrayList for ordered, indexed storage.
 * - HashSet for O(1) duplicate ID detection and uniqueness enforcement.
 * - Defensive copying to protect internal state from accidental external modification.
 * - Clean CRUD API (Create, Read, Update, Delete).
 * - Exception Handling for domain business rules.
 */
public class PlacementService {

    // Primary in-memory list of applications
    private final List<Application> applications;

    // Set of active Application IDs for O(1) existence and uniqueness checks
    private final Set<String> activeIds;

    public PlacementService() {
        this.applications = new ArrayList<>();
        this.activeIds = new HashSet<>();
    }

    /**
     * Adds a new application to the repository.
     *
     * DSA Rationale:
     * - We check `activeIds.contains(...)` in O(1) average time using HashSet.
     *   Without HashSet, checking if an ID exists in an ArrayList would take O(n) linear time.
     * - `applications.add(...)` appends in amortized O(1) time.
     *
     * @param application Application to add
     * @return true if successfully added
     * @throws IllegalArgumentException if application is null or ID already exists
     */
    public boolean addApplication(Application application) {
        if (application == null) {
            throw new IllegalArgumentException("Cannot add null application.");
        }

        String id = application.getApplicationId().toUpperCase();
        if (activeIds.contains(id)) {
            throw new IllegalArgumentException("Application ID '" + id + "' already exists. Duplicate IDs are not allowed.");
        }

        applications.add(application);
        activeIds.add(id);
        return true;
    }

    /**
     * Returns an unmodifiable snapshot view of all applications.
     * Defensively prevents external callers from clearing or corrupting the list directly.
     */
    public List<Application> getAllApplications() {
        return Collections.unmodifiableList(new ArrayList<>(applications));
    }

    /**
     * Returns the total count of applications. O(1) complexity.
     */
    public int getApplicationCount() {
        return applications.size();
    }

    /**
     * Finds an application by its unique ID.
     *
     * @param applicationId ID to search
     * @return Optional containing Application if found, empty Optional otherwise
     */
    public Optional<Application> findById(String applicationId) {
        if (applicationId == null || applicationId.trim().isEmpty()) {
            return Optional.empty();
        }
        String searchId = applicationId.trim().toUpperCase();
        if (!activeIds.contains(searchId)) {
            return Optional.empty();
        }

        for (Application app : applications) {
            if (app.getApplicationId().equalsIgnoreCase(searchId)) {
                return Optional.of(app);
            }
        }
        return Optional.empty();
    }

    /**
     * Updates the status of an existing application.
     *
     * @param applicationId ID of application
     * @param newStatus New status to set
     * @return true if updated, false if application not found
     */
    public boolean updateStatus(String applicationId, ApplicationStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null.");
        }
        Optional<Application> optionalApp = findById(applicationId);
        if (optionalApp.isPresent()) {
            optionalApp.get().setStatus(newStatus);
            return true;
        }
        return false;
    }

    /**
     * Deletes an application by ID.
     *
     * DSA Rationale:
     * - We remove the ID from the HashSet in O(1).
     * - We remove from the ArrayList in O(n) because subsequent elements shift left.
     *
     * @param applicationId ID of application to delete
     * @return true if removed, false if not found
     */
    public boolean deleteApplication(String applicationId) {
        if (applicationId == null || applicationId.trim().isEmpty()) {
            return false;
        }
        String cleanId = applicationId.trim().toUpperCase();
        if (!activeIds.contains(cleanId)) {
            return false;
        }

        boolean removed = applications.removeIf(app -> app.getApplicationId().equalsIgnoreCase(cleanId));
        if (removed) {
            activeIds.remove(cleanId);
        }
        return removed;
    }

    /**
     * Filters applications by application status using ArrayList traversal.
     *
     * Complexity:
     * - Time: O(n) single pass over all applications.
     * - Space: O(m) where m is count of matching applications.
     *
     * @param status Status filter
     * @return List of matching applications
     */
    public List<Application> filterByStatus(ApplicationStatus status) {
        if (status == null) {
            return getAllApplications();
        }
        List<Application> result = new ArrayList<>();
        for (Application app : applications) {
            if (app.getStatus() == status) {
                result.add(app);
            }
        }
        return result;
    }

    /**
     * Clears all records and reloads from list (useful during file loading).
     */
    public void replaceAll(List<Application> newApplications) {
        this.applications.clear();
        this.activeIds.clear();
        if (newApplications != null) {
            for (Application app : newApplications) {
                addApplication(app);
            }
        }
    }
}

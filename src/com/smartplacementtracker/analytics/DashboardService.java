package com.smartplacementtracker.analytics;

import com.smartplacementtracker.model.Application;
import com.smartplacementtracker.model.ApplicationStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Analytics and dashboard computation service.
 *
 * Concepts demonstrated:
 * - HashMap: O(1) frequency count aggregation.
 * - Array Traversal: Linear scan O(n) to compute extrema (highest compensation).
 * - Date filtering: Detecting upcoming interviews using LocalDate comparisons.
 * - Separation of Concerns: Keeps analytics cleanly decoupled from UI and storage.
 */
public class DashboardService {

    /**
     * Calculates the distribution of applications by status using a Map.
     *
     * DSA Rationale:
     * - We initialize counts for all ApplicationStatus enum values to 0.
     * - In a single O(n) traversal pass over the application list, we increment the
     *   corresponding frequency counter in O(1) average time per element.
     *
     * Time Complexity: O(n) where n is the number of applications.
     * Space Complexity: O(k) where k is the constant number of statuses (k = 6).
     *
     * @param applications Current applications
     * @return Map of Status to count
     */
    public Map<ApplicationStatus, Integer> calculateStatusCounts(List<Application> applications) {
        Map<ApplicationStatus, Integer> counts = new EnumMap<>(ApplicationStatus.class);

        // Pre-populate with 0 so all statuses appear even if count is zero
        for (ApplicationStatus status : ApplicationStatus.values()) {
            counts.put(status, 0);
        }

        if (applications == null || applications.isEmpty()) {
            return counts;
        }

        for (Application app : applications) {
            counts.put(app.getStatus(), counts.get(app.getStatus()) + 1);
        }

        return counts;
    }

    /**
     * Calculates distribution of applications by company name using HashMap.
     *
     * Time Complexity: O(n)
     * Space Complexity: O(m) where m is the count of distinct companies.
     */
    public Map<String, Integer> calculateCompanyCounts(List<Application> applications) {
        Map<String, Integer> companyCounts = new HashMap<>();
        if (applications == null) {
            return companyCounts;
        }

        for (Application app : applications) {
            String company = app.getCompanyName();
            companyCounts.put(company, companyCounts.getOrDefault(company, 0) + 1);
        }

        return companyCounts;
    }

    /**
     * Finds the application with the highest annualized compensation using a single linear traversal scan.
     *
     * DSA Rationale:
     * - Traverses the array once, keeping a pointer to the current maximum.
     * - Compares annualized compensation so monthly stipends and yearly CTCs are evaluated fairly.
     *
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     *
     * @param applications Current applications
     * @return Optional containing the highest-paying application, or empty if list is empty
     */
    public Optional<Application> findHighestPayingOpportunity(List<Application> applications) {
        if (applications == null || applications.isEmpty()) {
            return Optional.empty();
        }

        Application highest = applications.get(0);
        for (int i = 1; i < applications.size(); i++) {
            Application current = applications.get(i);
            if (current.getAnnualizedCompensation() > highest.getAnnualizedCompensation()) {
                highest = current;
            }
        }
        return Optional.of(highest);
    }

    /**
     * Returns all applications with upcoming interview dates (scheduled on or after today).
     * Results are sorted chronologically by interview date.
     *
     * Time Complexity: O(n + m log m) where m is upcoming interviews count.
     * Space Complexity: O(m)
     */
    public List<Application> getUpcomingInterviews(List<Application> applications) {
        List<Application> upcoming = new ArrayList<>();
        if (applications == null || applications.isEmpty()) {
            return upcoming;
        }

        LocalDate today = LocalDate.now();
        for (Application app : applications) {
            if (app.getInterviewDate() != null && !app.getInterviewDate().isBefore(today)) {
                upcoming.add(app);
            }
        }

        // Sort upcoming interviews chronologically
        upcoming.sort(Comparator.comparing(Application::getInterviewDate));
        return upcoming;
    }

    /**
     * Generates the formatted dashboard string for console output.
     */
    public String renderDashboard(List<Application> applications) {
        int total = (applications != null) ? applications.size() : 0;
        Map<ApplicationStatus, Integer> statusCounts = calculateStatusCounts(applications);
        Optional<Application> highest = findHighestPayingOpportunity(applications);

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("        SMART PLACEMENT TRACKER\n");
        sb.append("========================================\n");
        sb.append(String.format("Total Applications: %d\n", total));
        sb.append(String.format("  - Applied     : %d\n", statusCounts.get(ApplicationStatus.APPLIED)));
        sb.append(String.format("  - Assessments : %d\n", statusCounts.get(ApplicationStatus.ASSESSMENT)));
        sb.append(String.format("  - Interviews  : %d\n", statusCounts.get(ApplicationStatus.INTERVIEW)));
        sb.append(String.format("  - Offers      : %d\n", statusCounts.get(ApplicationStatus.OFFER)));
        sb.append(String.format("  - Rejected    : %d\n", statusCounts.get(ApplicationStatus.REJECTED)));
        sb.append(String.format("  - Withdrawn   : %d\n", statusCounts.get(ApplicationStatus.WITHDRAWN)));
        sb.append("----------------------------------------\n");

        if (highest.isPresent()) {
            Application top = highest.get();
            sb.append(String.format("Highest Compensation: %s at %s (%s)\n",
                top.getFormattedCompensation(),
                top.getCompanyName(),
                top.getJobRole()
            ));
            sb.append(String.format("Annualized Equivalent: ₹%,.2f per year\n",
                top.getAnnualizedCompensation()
            ));
        } else {
            sb.append("Highest Compensation: No records available yet\n");
        }
        sb.append("========================================");

        return sb.toString();
    }
}

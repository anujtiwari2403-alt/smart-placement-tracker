package com.smartplacementtracker.algorithms;

import com.smartplacementtracker.model.Application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pure algorithms utility class implementing search techniques on Application lists.
 *
 * Concepts demonstrated:
 * - Linear Search: Sequential traversal of unsorted/partial-match collections.
 * - Binary Search: Divide-and-conquer logarithmic lookup on sorted collections.
 * - Algorithmic complexity trade-offs and preconditions.
 */
public final class SearchAlgorithms {

    private SearchAlgorithms() {
        // Utility class: prevent instantiation
    }

    // =========================================================================
    // 1. LINEAR SEARCH
    // =========================================================================

    /**
     * Performs Linear Search to find applications matching a company name keyword.
     * Supports substring and case-insensitive matching.
     *
     * Why Linear Search is appropriate here:
     * - The list does not need to be sorted.
     * - We want partial substring matches (e.g., "Goog" matches "Google").
     * - Binary Search cannot perform substring searches because substring ordering
     *   does not preserve total order across arbitrary prefixes.
     *
     * Complexity Analysis:
     * - Best-Case Time:    O(1)  - Target match found at first index.
     * - Average-Case Time: O(n)  - Target found midway or evenly distributed.
     * - Worst-Case Time:   O(n)  - Item at last index, or item not present (scans all n items).
     * - Auxiliary Space:   O(k)  - Memory for result list containing k matching elements.
     *
     * @param applications Input list of applications
     * @param companyQuery Search keyword
     * @return List of matching applications
     */
    public static List<Application> linearSearchByCompany(List<Application> applications, String companyQuery) {
        List<Application> matches = new ArrayList<>();
        if (applications == null || companyQuery == null || companyQuery.trim().isEmpty()) {
            return matches;
        }

        String query = companyQuery.trim().toLowerCase();
        for (Application app : applications) {
            if (app.getCompanyName().toLowerCase().contains(query)) {
                matches.add(app);
            }
        }
        return matches;
    }

    /**
     * Performs Linear Search to find applications matching a job role keyword.
     *
     * Complexity Analysis:
     * - Time Complexity:  O(n)
     * - Auxiliary Space:  O(k)
     *
     * @param applications Input list of applications
     * @param roleQuery Search keyword for role
     * @return List of matching applications
     */
    public static List<Application> linearSearchByRole(List<Application> applications, String roleQuery) {
        List<Application> matches = new ArrayList<>();
        if (applications == null || roleQuery == null || roleQuery.trim().isEmpty()) {
            return matches;
        }

        String query = roleQuery.trim().toLowerCase();
        for (Application app : applications) {
            if (app.getJobRole().toLowerCase().contains(query)) {
                matches.add(app);
            }
        }
        return matches;
    }

    // =========================================================================
    // 2. BINARY SEARCH
    // =========================================================================

    /**
     * Performs iterative Binary Search to find all applications for an EXACT company name.
     *
     * CRITICAL PREREQUISITE:
     * The input list MUST be sorted in ascending order of company name (case-insensitive).
     *
     * Why Binary Search requires sorted data:
     * - Binary search works by comparing the search key with the middle element.
     *   If the target is smaller than the middle element, we can GUARANTEE that the target
     *   cannot exist in the right half ONLY if the elements are sorted in non-decreasing order.
     * - This property allows us to discard half of the remaining search space in each step.
     * - On an unsorted array, no relationship exists between the middle element and the
     *   rest of the array, making it impossible to eliminate either half.
     *
     * How Duplicate Companies are Handled:
     * - Once a match index 'mid' is found via standard Binary Search, we expand outward
     *   (left and right) to gather all applications belonging to that exact company.
     *
     * Complexity Analysis:
     * - Best-Case Time:       O(1)       - Middle element matches target on first probe.
     * - Average-Case Time:    O(log n + d) - Logarithmic lookup to find pivot, plus d duplicate matches.
     * - Worst-Case Time:      O(log n + d) - Target at leaf level or absent (O(log n)).
     * - Auxiliary Space:      O(1)       - Iterative implementation uses constant memory (excluding results list).
     *
     * @param sortedList List sorted by companyName (case-insensitive)
     * @param targetCompany Exact company name to search
     * @return List of matching applications
     */
    public static List<Application> binarySearchByCompany(List<Application> sortedList, String targetCompany) {
        List<Application> results = new ArrayList<>();
        if (sortedList == null || sortedList.isEmpty() || targetCompany == null || targetCompany.trim().isEmpty()) {
            return results;
        }

        String target = targetCompany.trim();
        int low = 0;
        int high = sortedList.size() - 1;
        int matchIndex = -1;

        while (low <= high) {
            // Avoid integer overflow bug (low + high) / 2
            int mid = low + (high - low) / 2;
            String midCompany = sortedList.get(mid).getCompanyName();
            int cmp = midCompany.compareToIgnoreCase(target);

            if (cmp == 0) {
                matchIndex = mid;
                break; // Found one instance
            } else if (cmp < 0) {
                // midCompany comes alphabetically before target -> target is in right half
                low = mid + 1;
            } else {
                // midCompany comes alphabetically after target -> target is in left half
                high = mid - 1;
            }
        }

        // If target was found, collect all adjacent matches (duplicates for same company)
        if (matchIndex != -1) {
            // Scan left
            int left = matchIndex;
            while (left >= 0 && sortedList.get(left).getCompanyName().equalsIgnoreCase(target)) {
                left--;
            }

            // Scan right
            int right = matchIndex + 1;
            while (right < sortedList.size() && sortedList.get(right).getCompanyName().equalsIgnoreCase(target)) {
                right++;
            }

            // Add all items in range (left, right)
            for (int i = left + 1; i < right; i++) {
                results.add(sortedList.get(i));
            }
        }

        return results;
    }

    /**
     * Checks if a given list of applications is sorted by company name (case-insensitive).
     * Used as a precondition guard before invoking binary search.
     *
     * Complexity: O(n) time, O(1) space.
     */
    public static boolean isSortedByCompany(List<Application> list) {
        if (list == null || list.size() <= 1) {
            return true;
        }
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).getCompanyName().compareToIgnoreCase(list.get(i + 1).getCompanyName()) > 0) {
                return false;
            }
        }
        return true;
    }
}

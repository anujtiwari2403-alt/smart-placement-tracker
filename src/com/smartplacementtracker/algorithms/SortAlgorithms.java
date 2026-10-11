package com.smartplacementtracker.algorithms;

import com.smartplacementtracker.model.Application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Pure sorting algorithms utility class.
 *
 * Demonstrates:
 * - Manual Merge Sort implementation (Divide-and-Conquer).
 * - Algorithmic stability and complexity proofs.
 * - Standard Comparator patterns (comparing by company, annualized salary, application date).
 * - Clear contrast between manual Merge Sort and Java's built-in TimSort (List.sort()).
 */
public final class SortAlgorithms {

    private SortAlgorithms() {
        // Utility class: prevent instantiation
    }

    // =========================================================================
    // 1. COMPARATORS FOR APPLICATIONS
    // =========================================================================

    /**
     * Comparator for Company Name (Ascending, Case-Insensitive).
     */
    public static final Comparator<Application> BY_COMPANY_ASC = (a1, a2) ->
        a1.getCompanyName().compareToIgnoreCase(a2.getCompanyName());

    /**
     * Comparator for Annualized Compensation (Descending - Highest first).
     * Uses annualized comparison so internships (monthly * 12) and full-time (CTC)
     * are evaluated on an equivalent scale.
     */
    public static final Comparator<Application> BY_SALARY_DESC = (a1, a2) ->
        Double.compare(a2.getAnnualizedCompensation(), a1.getAnnualizedCompensation());

    /**
     * Comparator for Annualized Compensation (Ascending - Lowest first).
     */
    public static final Comparator<Application> BY_SALARY_ASC = (a1, a2) ->
        Double.compare(a1.getAnnualizedCompensation(), a2.getAnnualizedCompensation());

    /**
     * Comparator for Application Date (Descending - Newest first).
     */
    public static final Comparator<Application> BY_DATE_DESC = (a1, a2) ->
        a2.getApplicationDate().compareTo(a1.getApplicationDate());

    /**
     * Comparator for Application Date (Ascending - Oldest first).
     */
    public static final Comparator<Application> BY_DATE_ASC = (a1, a2) ->
        a1.getApplicationDate().compareTo(a2.getApplicationDate());

    // =========================================================================
    // 2. MANUAL MERGE SORT IMPLEMENTATION
    // =========================================================================

    /**
     * Sorts a copy of the input list using a manually implemented Merge Sort algorithm.
     *
     * How Merge Sort Works (Divide-and-Conquer):
     * 1. Divide: Recursively split the array into two halves until single-element sublists remain.
     * 2. Conquer: Recursively sort both sublists.
     * 3. Combine (Merge): Interleave elements from both sorted halves in order into an auxiliary buffer.
     *
     * Why Merge Sort is chosen:
     * - Stability: Preserves the original relative order of elements with identical keys.
     * - Guaranteed O(n log n): Unlike QuickSort (which degrades to O(n^2) on bad pivot choices),
     *   Merge Sort ALWAYS runs in O(n log n) time.
     *
     * Complexity Analysis:
     * - Best-Case Time:     O(n log n) - Even if already sorted, the divide & merge phases execute.
     * - Average-Case Time:  O(n log n) - Standard split tree depth is log2(n), each level takes O(n) merge work.
     * - Worst-Case Time:    O(n log n) - Guaranteed upper bound.
     * - Auxiliary Space:    O(n)       - Required for temporary sub-arrays during merging.
     *
     * @param inputList Original list (remains unmodified)
     * @param comparator Sorting criterion
     * @return A newly sorted ArrayList
     */
    public static List<Application> manualMergeSort(List<Application> inputList, Comparator<Application> comparator) {
        if (inputList == null) {
            return new ArrayList<>();
        }
        if (inputList.size() <= 1) {
            return new ArrayList<>(inputList);
        }

        // Create a working copy so the original collection is not mutated unexpectedly
        List<Application> list = new ArrayList<>(inputList);
        mergeSortInternal(list, 0, list.size() - 1, comparator);
        return list;
    }

    /**
     * Recursive helper for Divide-and-Conquer.
     */
    private static void mergeSortInternal(List<Application> list, int left, int right, Comparator<Application> comparator) {
        if (left < right) {
            int mid = left + (right - left) / 2; // Midpoint calculation avoiding overflow

            // Recursively divide left half
            mergeSortInternal(list, left, mid, comparator);

            // Recursively divide right half
            mergeSortInternal(list, mid + 1, right, comparator);

            // Merge both halves back together
            merge(list, left, mid, right, comparator);
        }
    }

    /**
     * Merges two adjacent sorted subarrays: list[left..mid] and list[mid+1..right].
     */
    private static void merge(List<Application> list, int left, int mid, int right, Comparator<Application> comparator) {
        int leftSize = mid - left + 1;
        int rightSize = right - mid;

        // Temporary auxiliary lists for left and right partitions
        List<Application> leftArray = new ArrayList<>(leftSize);
        List<Application> rightArray = new ArrayList<>(rightSize);

        for (int i = 0; i < leftSize; i++) {
            leftArray.add(list.get(left + i));
        }
        for (int j = 0; j < rightSize; j++) {
            rightArray.add(list.get(mid + 1 + j));
        }

        int i = 0; // Pointer in leftArray
        int j = 0; // Pointer in rightArray
        int k = left; // Target pointer in original list

        // Merge elements in sorted order based on comparator
        while (i < leftSize && j < rightSize) {
            // Note: '<= 0' ensures STABILITY (equal elements from leftArray maintain their earlier position)
            if (comparator.compare(leftArray.get(i), rightArray.get(j)) <= 0) {
                list.set(k, leftArray.get(i));
                i++;
            } else {
                list.set(k, rightArray.get(j));
                j++;
            }
            k++;
        }

        // Drain remaining elements from left partition
        while (i < leftSize) {
            list.set(k, leftArray.get(i));
            i++;
            k++;
        }

        // Drain remaining elements from right partition
        while (j < rightSize) {
            list.set(k, rightArray.get(j));
            j++;
            k++;
        }
    }

    // =========================================================================
    // 3. BUILT-IN SORTING FOR BENCHMARK / COMPARISON
    // =========================================================================

    /**
     * Sorts using Java's standard library (Collections.sort / List.sort).
     *
     * Concepts demonstrated:
     * - Java's built-in sorting uses TimSort (hybrid of Merge Sort and Insertion Sort).
     * - Time Complexity: Best O(n), Average O(n log n), Worst O(n log n).
     * - Space Complexity: O(n).
     *
     * @param inputList List to sort
     * @param comparator Sorting criterion
     * @return A newly sorted ArrayList
     */
    public static List<Application> builtinSort(List<Application> inputList, Comparator<Application> comparator) {
        if (inputList == null) {
            return new ArrayList<>();
        }
        List<Application> copy = new ArrayList<>(inputList);
        copy.sort(comparator);
        return copy;
    }
}

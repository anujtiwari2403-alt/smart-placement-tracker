package com.smartplacementtracker;

import com.smartplacementtracker.algorithms.SearchAlgorithms;
import com.smartplacementtracker.algorithms.SortAlgorithms;
import com.smartplacementtracker.analytics.DashboardService;
import com.smartplacementtracker.model.Application;
import com.smartplacementtracker.model.ApplicationStatus;
import com.smartplacementtracker.model.ApplicationType;
import com.smartplacementtracker.service.PlacementService;
import com.smartplacementtracker.storage.FileStorage;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Self-contained automated test suite for Smart Placement Tracker.
 * Runs without requiring third-party libraries (no external JUnit needed).
 */
public class AppSelfTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("          RUNNING AUTOMATED TEST SUITE: SMART PLACEMENT TRACKER          ");
        System.out.println("========================================================================");

        testModelValidation();
        testPlacementServiceCrudAndHashSet();
        testAnnualizedCompensationComparison();
        testLinearSearch();
        testMergeSortCorrectness();
        testBinarySearchCorrectness();
        testDashboardAnalyticsAndHighestPaying();
        testFileStorageRoundTripAndEscaping();
        testEmptyAndEdgeCases();

        System.out.println("\n========================================================================");
        System.out.printf("TEST SUMMARY: Total: %d | Passed: %d | Failed: %d%n", totalTests, passedTests, failedTests);
        System.out.println("========================================================================");

        if (failedTests > 0) {
            System.err.println("❌ SOME TESTS FAILED!");
            System.exit(1);
        } else {
            System.out.println("✅ ALL TESTS PASSED SUCCESSFULLY!");
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            failedTests++;
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void testModelValidation() {
        System.out.println("\n--- Testing Model Encapsulation & Validation ---");

        Application app = new Application(
            "APP-101", "Google", "SWE Intern", ApplicationType.INTERNSHIP,
            LocalDate.of(2026, 1, 15), ApplicationStatus.APPLIED,
            100000.0, "Bengaluru", "https://careers.google.com",
            LocalDate.of(2026, 2, 10), "Applied via referral"
        );

        assertTrue("Application getters match expected values",
            app.getApplicationId().equals("APP-101") &&
            app.getCompanyName().equals("Google") &&
            app.getType() == ApplicationType.INTERNSHIP &&
            app.getCompensation() == 100000.0
        );

        // Negative compensation rejection
        boolean caughtNegative = false;
        try {
            new Application("APP-ERR", "Amazon", "SDE", ApplicationType.FULL_TIME,
                LocalDate.now(), ApplicationStatus.APPLIED, -500.0, "Hyderabad", "", null, "");
        } catch (IllegalArgumentException e) {
            caughtNegative = true;
        }
        assertTrue("Rejects negative salary in constructor", caughtNegative);

        // Null company rejection
        boolean caughtNullCompany = false;
        try {
            new Application("APP-ERR2", "   ", "SDE", ApplicationType.FULL_TIME,
                LocalDate.now(), ApplicationStatus.APPLIED, 1200000.0, "Hyderabad", "", null, "");
        } catch (IllegalArgumentException e) {
            caughtNullCompany = true;
        }
        assertTrue("Rejects blank company name in constructor", caughtNullCompany);
    }

    private static void testPlacementServiceCrudAndHashSet() {
        System.out.println("\n--- Testing PlacementService (ArrayList & HashSet) ---");
        PlacementService service = new PlacementService();

        Application app1 = new Application(
            "APP-1", "Microsoft", "Software Engineer", ApplicationType.FULL_TIME,
            LocalDate.of(2026, 1, 10), ApplicationStatus.ASSESSMENT,
            1600000.0, "Hyderabad", "https://microsoft.com", null, "Online test passed"
        );
        Application app2 = new Application(
            "APP-2", "Adobe", "Product Intern", ApplicationType.INTERNSHIP,
            LocalDate.of(2026, 1, 12), ApplicationStatus.APPLIED,
            60000.0, "Noida", "https://adobe.com", null, ""
        );

        assertTrue("Add first application", service.addApplication(app1));
        assertTrue("Add second application", service.addApplication(app2));
        assertTrue("Application count equals 2", service.getApplicationCount() == 2);

        // Duplicate ID rejection using HashSet
        boolean caughtDup = false;
        try {
            Application duplicateApp = new Application(
                "APP-1", "Duplicate Company", "Dev", ApplicationType.FULL_TIME,
                LocalDate.now(), ApplicationStatus.APPLIED, 500000.0, "", "", null, ""
            );
            service.addApplication(duplicateApp);
        } catch (IllegalArgumentException e) {
            caughtDup = true;
        }
        assertTrue("HashSet blocks duplicate Application ID in O(1)", caughtDup);

        // Status update
        assertTrue("Update status of APP-1 to INTERVIEW", service.updateStatus("APP-1", ApplicationStatus.INTERVIEW));
        assertTrue("Verify updated status", service.findById("APP-1").get().getStatus() == ApplicationStatus.INTERVIEW);

        // Delete application
        assertTrue("Delete application APP-2", service.deleteApplication("APP-2"));
        assertTrue("Verify count reduced to 1", service.getApplicationCount() == 1);
        assertTrue("APP-2 no longer exists", service.findById("APP-2").isEmpty());

        // Can re-add APP-2 after deletion
        assertTrue("Can re-add APP-2 after deletion", service.addApplication(app2));
    }

    private static void testAnnualizedCompensationComparison() {
        System.out.println("\n--- Testing Annualized Compensation Logic ---");

        // ₹50,000 / month internship => ₹6,00,000 annualized
        Application intern = new Application("T1", "Startup", "Intern", ApplicationType.INTERNSHIP,
            LocalDate.now(), ApplicationStatus.OFFER, 50000.0, "Remote", "", null, "");

        // ₹5,00,000 / year full-time
        Application fullTime = new Application("T2", "Company B", "Associate", ApplicationType.FULL_TIME,
            LocalDate.now(), ApplicationStatus.OFFER, 500000.0, "Pune", "", null, "");

        assertTrue("Internship annualized calculation is 50,000 * 12 = 6,00,000",
            Math.abs(intern.getAnnualizedCompensation() - 600000.0) < 0.001);
        assertTrue("Full-time annualized is raw amount 5,00,000",
            Math.abs(fullTime.getAnnualizedCompensation() - 500000.0) < 0.001);
        assertTrue("Internship (₹50k/mo) has higher annualized comp than ₹5L CTC",
            intern.getAnnualizedCompensation() > fullTime.getAnnualizedCompensation());
    }

    private static void testLinearSearch() {
        System.out.println("\n--- Testing Linear Search ---");
        List<Application> list = createSampleData();

        List<Application> googleApps = SearchAlgorithms.linearSearchByCompany(list, "Google");
        assertTrue("Linear search finds Google", googleApps.size() == 1 && googleApps.get(0).getCompanyName().equals("Google"));

        List<Application> partialMatch = SearchAlgorithms.linearSearchByCompany(list, "micro");
        assertTrue("Linear search supports case-insensitive substring",
            partialMatch.size() == 1 && partialMatch.get(0).getCompanyName().equals("Microsoft"));

        List<Application> roleSearch = SearchAlgorithms.linearSearchByRole(list, "intern");
        assertTrue("Linear search finds multiple role matches", roleSearch.size() == 2);

        List<Application> notFound = SearchAlgorithms.linearSearchByCompany(list, "NonExistentCompany");
        assertTrue("Linear search returns empty list when no match", notFound.isEmpty());
    }

    private static void testMergeSortCorrectness() {
        System.out.println("\n--- Testing Manual Merge Sort Correctness & Stability ---");
        List<Application> list = createSampleData();

        // 1. Sort by Salary Descending
        List<Application> sortedBySalary = SortAlgorithms.manualMergeSort(list, SortAlgorithms.BY_SALARY_DESC);
        assertTrue("List size preserved after Merge Sort", sortedBySalary.size() == list.size());

        boolean isSortedDesc = true;
        for (int i = 0; i < sortedBySalary.size() - 1; i++) {
            if (sortedBySalary.get(i).getAnnualizedCompensation() < sortedBySalary.get(i + 1).getAnnualizedCompensation()) {
                isSortedDesc = false;
                break;
            }
        }
        assertTrue("Merge Sort correctly orders by annualized salary descending", isSortedDesc);

        // 2. Compare Merge Sort result with Java's TimSort
        List<Application> builtInSorted = SortAlgorithms.builtinSort(list, SortAlgorithms.BY_SALARY_DESC);
        boolean matchesTimSort = true;
        for (int i = 0; i < sortedBySalary.size(); i++) {
            if (sortedBySalary.get(i).getAnnualizedCompensation() != builtInSorted.get(i).getAnnualizedCompensation()) {
                matchesTimSort = false;
                break;
            }
        }
        assertTrue("Manual Merge Sort matches Java's built-in TimSort ordering", matchesTimSort);

        // 3. Sort by Company Name Ascending
        List<Application> sortedByCompany = SortAlgorithms.manualMergeSort(list, SortAlgorithms.BY_COMPANY_ASC);
        assertTrue("Sorted by company name ascends alphabetically", SearchAlgorithms.isSortedByCompany(sortedByCompany));
    }

    private static void testBinarySearchCorrectness() {
        System.out.println("\n--- Testing Binary Search (O(log n)) ---");
        List<Application> list = createSampleData();

        // Must sort by company name before binary search
        List<Application> sorted = SortAlgorithms.manualMergeSort(list, SortAlgorithms.BY_COMPANY_ASC);
        assertTrue("Precondition check: list is verified sorted", SearchAlgorithms.isSortedByCompany(sorted));

        // Binary search exact match
        List<Application> results = SearchAlgorithms.binarySearchByCompany(sorted, "Amazon");
        assertTrue("Binary search finds exact match Amazon", results.size() == 1 && results.get(0).getCompanyName().equals("Amazon"));

        // Binary search missing match
        List<Application> missing = SearchAlgorithms.binarySearchByCompany(sorted, "Netflix");
        assertTrue("Binary search returns empty list for absent target", missing.isEmpty());

        // Binary search duplicate companies
        List<Application> withDups = new ArrayList<>(sorted);
        withDups.add(new Application("DUP-1", "Amazon", "QA Intern", ApplicationType.INTERNSHIP,
            LocalDate.now(), ApplicationStatus.APPLIED, 35000.0, "Chennai", "", null, ""));
        List<Application> sortedWithDups = SortAlgorithms.manualMergeSort(withDups, SortAlgorithms.BY_COMPANY_ASC);

        List<Application> amazonDups = SearchAlgorithms.binarySearchByCompany(sortedWithDups, "Amazon");
        assertTrue("Binary search correctly gathers all duplicate company instances", amazonDups.size() == 2);
    }

    private static void testDashboardAnalyticsAndHighestPaying() {
        System.out.println("\n--- Testing Dashboard & Traversal Analytics ---");
        List<Application> list = createSampleData();
        DashboardService dashboard = new DashboardService();

        // Status counts using HashMap / EnumMap
        Map<ApplicationStatus, Integer> statusCounts = dashboard.calculateStatusCounts(list);
        assertTrue("Status count APPLIED matches sample data (2)", statusCounts.get(ApplicationStatus.APPLIED) == 2);
        assertTrue("Status count INTERVIEW matches sample data (1)", statusCounts.get(ApplicationStatus.INTERVIEW) == 1);
        assertTrue("Status count OFFER matches sample data (1)", statusCounts.get(ApplicationStatus.OFFER) == 1);

        // Highest paying opportunity traversal
        Optional<Application> highest = dashboard.findHighestPayingOpportunity(list);
        assertTrue("Highest paying opportunity exists", highest.isPresent());
        assertTrue("Highest paying role is Uber (₹24,00,000 CTC)",
            highest.get().getCompanyName().equals("Uber") && highest.get().getAnnualizedCompensation() == 2400000.0);

        // Upcoming interviews
        List<Application> upcoming = dashboard.getUpcomingInterviews(list);
        assertTrue("Identified upcoming interview", !upcoming.isEmpty() && upcoming.get(0).getCompanyName().equals("Microsoft"));
    }

    private static void testFileStorageRoundTripAndEscaping() {
        System.out.println("\n--- Testing File Storage & CSV Escaping (RFC-4180) ---");
        String testFile = "data/test_applications.csv";
        FileStorage storage = new FileStorage(testFile);

        List<Application> original = new ArrayList<>();
        // Record with commas, quotes, and complex notes to stress-test CSV escaping
        original.add(new Application(
            "TEST-1", "Complex, Inc.", "Software \"Lead\" Engineer", ApplicationType.FULL_TIME,
            LocalDate.of(2026, 3, 1), ApplicationStatus.APPLIED, 1800000.0,
            "Bengaluru, Karnataka", "https://careers.example.com?id=123&track=java",
            null, "Referral from senior; includes comma, and \"quoted words\""
        ));
        original.add(new Application(
            "TEST-2", "SimpleCorp", "Intern", ApplicationType.INTERNSHIP,
            LocalDate.of(2026, 3, 2), ApplicationStatus.OFFER, 40000.0,
            "Noida", "", LocalDate.of(2026, 3, 10), "Regular notes"
        ));

        // Save
        boolean saved = storage.saveApplications(original);
        assertTrue("Saved to test CSV file successfully", saved);

        // Reload
        List<Application> loaded = storage.loadApplications();
        assertTrue("Reloaded correct number of records", loaded.size() == 2);

        Application loaded1 = loaded.get(0);
        assertTrue("Preserved company with comma: 'Complex, Inc.'", loaded1.getCompanyName().equals("Complex, Inc."));
        assertTrue("Preserved role with quotes: 'Software \"Lead\" Engineer'", loaded1.getJobRole().equals("Software \"Lead\" Engineer"));
        assertTrue("Preserved notes with quotes and commas", loaded1.getNotes().contains("quoted words"));

        // Cleanup test file
        new File(testFile).delete();
        new File(testFile + ".tmp").delete();
    }

    private static void testEmptyAndEdgeCases() {
        System.out.println("\n--- Testing Edge Cases & Empty Collections ---");
        DashboardService dashboard = new DashboardService();

        assertTrue("Empty list highest paying returns empty Optional",
            dashboard.findHighestPayingOpportunity(new ArrayList<>()).isEmpty());

        assertTrue("Empty list binary search returns empty results",
            SearchAlgorithms.binarySearchByCompany(new ArrayList<>(), "Google").isEmpty());

        assertTrue("Empty list merge sort returns empty list without error",
            SortAlgorithms.manualMergeSort(new ArrayList<>(), SortAlgorithms.BY_COMPANY_ASC).isEmpty());

        assertTrue("Single element list merge sort returns single item",
            SortAlgorithms.manualMergeSort(List.of(createSampleData().get(0)), SortAlgorithms.BY_COMPANY_ASC).size() == 1);
    }

    private static List<Application> createSampleData() {
        List<Application> list = new ArrayList<>();
        LocalDate today = LocalDate.now();

        list.add(new Application("APP-001", "Google", "SWE Intern", ApplicationType.INTERNSHIP,
            LocalDate.of(2026, 1, 10), ApplicationStatus.APPLIED, 110000.0, "Bengaluru", "https://google.com", null, ""));

        list.add(new Application("APP-002", "Microsoft", "SDE 1", ApplicationType.FULL_TIME,
            LocalDate.of(2026, 1, 12), ApplicationStatus.INTERVIEW, 1800000.0, "Hyderabad", "https://microsoft.com",
            today.plusDays(3), "Round 2 scheduled"));

        list.add(new Application("APP-003", "Amazon", "SDE Intern", ApplicationType.INTERNSHIP,
            LocalDate.of(2026, 1, 15), ApplicationStatus.ASSESSMENT, 80000.0, "Bengaluru", "", null, ""));

        list.add(new Application("APP-004", "Uber", "Software Engineer", ApplicationType.FULL_TIME,
            LocalDate.of(2026, 1, 18), ApplicationStatus.OFFER, 2400000.0, "Bengaluru", "", null, "Offer letter received"));

        list.add(new Application("APP-005", "Adobe", "MTS 1", ApplicationType.FULL_TIME,
            LocalDate.of(2026, 1, 20), ApplicationStatus.APPLIED, 1700000.0, "Noida", "", null, ""));

        return list;
    }
}

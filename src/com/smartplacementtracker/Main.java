package com.smartplacementtracker;

import com.smartplacementtracker.algorithms.SearchAlgorithms;
import com.smartplacementtracker.algorithms.SortAlgorithms;
import com.smartplacementtracker.analytics.DashboardService;
import com.smartplacementtracker.model.Application;
import com.smartplacementtracker.model.ApplicationStatus;
import com.smartplacementtracker.model.ApplicationType;
import com.smartplacementtracker.service.PlacementService;
import com.smartplacementtracker.storage.FileStorage;
import com.smartplacementtracker.util.InputValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Main entry point and Console User Interface for Smart Placement Tracker.
 *
 * Implements a resilient menu loop with formatted tabular output,
 * input validation, and clear algorithm demonstrations.
 */
public class Main {

    private static final String DEFAULT_STORAGE_PATH = "data/applications.csv";
    private final PlacementService placementService;
    private final DashboardService dashboardService;
    private final FileStorage fileStorage;
    private final Scanner scanner;
    private boolean hasUnsavedChanges = false;

    public Main(String storagePath) {
        this.placementService = new PlacementService();
        this.dashboardService = new DashboardService();
        this.fileStorage = new FileStorage(storagePath);
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        String storagePath = (args.length > 0) ? args[0] : DEFAULT_STORAGE_PATH;
        Main app = new Main(storagePath);
        app.run();
    }

    /**
     * Main application loop.
     */
    public void run() {
        System.out.println("========================================================================");
        System.out.println("     WELCOME TO SMART PLACEMENT TRACKER - JAVA & DSA PORTFOLIO");
        System.out.println("========================================================================");

        // 1. Startup: Automatically load saved records
        List<Application> loaded = fileStorage.loadApplications();
        if (!loaded.isEmpty()) {
            placementService.replaceAll(loaded);
        }

        // 2. Display Dashboard
        System.out.println();
        System.out.println(dashboardService.renderDashboard(placementService.getAllApplications()));
        System.out.println();

        // 3. Menu Loop
        boolean running = true;
        while (running) {
            displayMenu();
            int choice = InputValidator.readInt(scanner, "Enter your choice (1-12): ", 1, 12);
            System.out.println();

            switch (choice) {
                case 1 -> handleAddApplication();
                case 2 -> handleViewAllApplications();
                case 3 -> handleSearchMenu();
                case 4 -> handleUpdateApplication();
                case 5 -> handleDeleteApplication();
                case 6 -> handleFilterByStatus();
                case 7 -> handleSortMenu();
                case 8 -> handleFindHighestPaying();
                case 9 -> handleViewUpcomingInterviews();
                case 10 -> handleViewDashboard();
                case 11 -> handleSaveData();
                case 12 -> running = handleExit();
            }
            System.out.println();
        }

        System.out.println("Thank you for using Smart Placement Tracker. Best wishes for your placements!");
    }

    private void displayMenu() {
        System.out.println("========================================");
        System.out.println("        SMART PLACEMENT TRACKER");
        System.out.println("========================================");
        System.out.println(" 1. Add Application");
        System.out.println(" 2. View All Applications");
        System.out.println(" 3. Search Application");
        System.out.println(" 4. Update Application");
        System.out.println(" 5. Delete Application");
        System.out.println(" 6. Filter Applications by Status");
        System.out.println(" 7. Sort Applications");
        System.out.println(" 8. Find Highest-Paying Opportunity");
        System.out.println(" 9. View Upcoming Interviews");
        System.out.println("10. View Dashboard Statistics");
        System.out.println("11. Save Data");
        System.out.println("12. Exit");
        System.out.println("========================================");
    }

    // =========================================================================
    // FEATURE IMPLEMENTATIONS
    // =========================================================================

    private void handleAddApplication() {
        System.out.println("--- [1] Add New Application ---");

        String id;
        while (true) {
            id = InputValidator.readNonEmptyString(scanner, "Enter Application ID (e.g., APP-101): ").toUpperCase();
            if (placementService.findById(id).isPresent()) {
                System.out.println(" Error: Application ID '" + id + "' already exists! IDs must be unique.");
            } else {
                break;
            }
        }

        String company = InputValidator.readNonEmptyString(scanner, "Enter Company Name: ");
        String role = InputValidator.readNonEmptyString(scanner, "Enter Job Role: ");
        ApplicationType type = InputValidator.readApplicationType(scanner);

        LocalDate appDate = InputValidator.readDate(scanner, "Enter Application Date (YYYY-MM-DD): ");
        ApplicationStatus status = InputValidator.readApplicationStatus(scanner);

        String compPrompt = (type == ApplicationType.INTERNSHIP)
                ? "Enter Monthly Stipend (in ₹): "
                : "Enter Annual CTC Salary (in ₹): ";
        double compensation = InputValidator.readPositiveDouble(scanner, compPrompt);

        String location = InputValidator.readOptionalString(scanner, "Enter Location (e.g., Bengaluru / Remote) [Press Enter for default]: ", "Remote / Unspecified");
        String url = InputValidator.readOptionalString(scanner, "Enter Job Posting URL [Press Enter for none]: ", "N/A");
        LocalDate interviewDate = InputValidator.readOptionalDate(scanner, "Enter Interview Date (YYYY-MM-DD) [Press Enter if not scheduled]: ");
        String notes = InputValidator.readOptionalString(scanner, "Enter Notes [Press Enter for none]: ", "-");

        try {
            Application newApp = new Application(id, company, role, type, appDate, status, compensation, location, url, interviewDate, notes);
            placementService.addApplication(newApp);
            hasUnsavedChanges = true;
            System.out.println("\n Successfully added application: " + id + " (" + company + ")");
        } catch (Exception e) {
            System.out.println(" Failed to create application: " + e.getMessage());
        }
    }

    private void handleViewAllApplications() {
        System.out.println("--- [2] All Applications ---");
        List<Application> apps = placementService.getAllApplications();
        printApplicationsTable(apps);
    }

    private void handleSearchMenu() {
        System.out.println("--- [3] Search Applications ---");
        System.out.println("  1. Linear Search by Company Name (Substring/Flexible)");
        System.out.println("  2. Binary Search by Company Name (O(log n) - Exact on Sorted Data)");
        System.out.println("  3. Linear Search by Job Role");
        System.out.println("  4. Search by Application ID (O(1) HashSet Lookup)");
        System.out.println("  5. Back to Main Menu");

        int choice = InputValidator.readInt(scanner, "Choose search method (1-5): ", 1, 5);
        List<Application> currentList = placementService.getAllApplications();

        switch (choice) {
            case 1 -> {
                String query = InputValidator.readNonEmptyString(scanner, "Enter company name/keyword: ");
                long start = System.nanoTime();
                List<Application> results = SearchAlgorithms.linearSearchByCompany(currentList, query);
                long duration = System.nanoTime() - start;
                System.out.printf(" [Linear Search complete in %d ns | Matches: %d]%n", duration, results.size());
                printApplicationsTable(results);
            }
            case 2 -> {
                System.out.println(" [DSA Note] Binary Search requires data to be sorted by company name.");
                List<Application> sorted = SortAlgorithms.manualMergeSort(currentList, SortAlgorithms.BY_COMPANY_ASC);
                String query = InputValidator.readNonEmptyString(scanner, "Enter exact company name to search: ");

                long start = System.nanoTime();
                List<Application> results = SearchAlgorithms.binarySearchByCompany(sorted, query);
                long duration = System.nanoTime() - start;
                System.out.printf(" [Binary Search complete in %d ns | Matches: %d]%n", duration, results.size());
                printApplicationsTable(results);
            }
            case 3 -> {
                String roleQuery = InputValidator.readNonEmptyString(scanner, "Enter job role keyword: ");
                long start = System.nanoTime();
                List<Application> results = SearchAlgorithms.linearSearchByRole(currentList, roleQuery);
                long duration = System.nanoTime() - start;
                System.out.printf(" [Linear Search complete in %d ns | Matches: %d]%n", duration, results.size());
                printApplicationsTable(results);
            }
            case 4 -> {
                String searchId = InputValidator.readNonEmptyString(scanner, "Enter Application ID: ");
                Optional<Application> app = placementService.findById(searchId);
                if (app.isPresent()) {
                    System.out.println(" Found Application:");
                    printApplicationsTable(List.of(app.get()));
                } else {
                    System.out.println(" No application found with ID: " + searchId);
                }
            }
            case 5 -> { /* return */ }
        }
    }

    private void handleUpdateApplication() {
        System.out.println("--- [4] Update Application ---");
        String id = InputValidator.readNonEmptyString(scanner, "Enter Application ID to update: ");
        Optional<Application> optionalApp = placementService.findById(id);

        if (optionalApp.isEmpty()) {
            System.out.println(" Application not found with ID: " + id);
            return;
        }

        Application app = optionalApp.get();
        System.out.println("Current details for " + id + ":");
        System.out.println(app);
        System.out.println();

        System.out.println("Select what to update:");
        System.out.println("  1. Change Status Only");
        System.out.println("  2. Update Compensation");
        System.out.println("  3. Schedule / Update Interview Date");
        System.out.println("  4. Update Location & Notes");
        System.out.println("  5. Cancel");

        int choice = InputValidator.readInt(scanner, "Enter choice (1-5): ", 1, 5);
        switch (choice) {
            case 1 -> {
                ApplicationStatus newStatus = InputValidator.readApplicationStatus(scanner);
                app.setStatus(newStatus);
                hasUnsavedChanges = true;
                System.out.println(" Status updated to " + newStatus.name());
            }
            case 2 -> {
                String prompt = (app.getType() == ApplicationType.INTERNSHIP)
                        ? "Enter new Monthly Stipend (in ₹): "
                        : "Enter new Annual CTC Salary (in ₹): ";
                double newComp = InputValidator.readPositiveDouble(scanner, prompt);
                app.setCompensation(newComp);
                hasUnsavedChanges = true;
                System.out.println(" Compensation updated.");
            }
            case 3 -> {
                LocalDate date = InputValidator.readOptionalDate(scanner, "Enter new Interview Date (YYYY-MM-DD) or blank to clear: ");
                app.setInterviewDate(date);
                hasUnsavedChanges = true;
                System.out.println(" Interview date updated to: " + app.getFormattedInterviewDate());
            }
            case 4 -> {
                String loc = InputValidator.readOptionalString(scanner, "Enter Location: ", app.getLocation());
                String notes = InputValidator.readOptionalString(scanner, "Enter Notes: ", app.getNotes());
                app.setLocation(loc);
                app.setNotes(notes);
                hasUnsavedChanges = true;
                System.out.println(" Location and notes updated.");
            }
            case 5 -> System.out.println("Update cancelled.");
        }
    }

    private void handleDeleteApplication() {
        System.out.println("--- [5] Delete Application ---");
        String id = InputValidator.readNonEmptyString(scanner, "Enter Application ID to delete: ");
        Optional<Application> optionalApp = placementService.findById(id);

        if (optionalApp.isEmpty()) {
            System.out.println(" Application with ID '" + id + "' not found.");
            return;
        }

        Application app = optionalApp.get();
        System.out.println("Target application: " + app.getApplicationId() + " (" + app.getCompanyName() + " - " + app.getJobRole() + ")");
        String confirm = InputValidator.readNonEmptyString(scanner, "Are you sure you want to delete this record? (y/n): ");

        if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
            boolean removed = placementService.deleteApplication(id);
            if (removed) {
                hasUnsavedChanges = true;
                System.out.println(" Application " + id + " successfully deleted.");
            } else {
                System.out.println(" Failed to delete application.");
            }
        } else {
            System.out.println(" Deletion cancelled.");
        }
    }

    private void handleFilterByStatus() {
        System.out.println("--- [6] Filter Applications by Status ---");
        ApplicationStatus status = InputValidator.readApplicationStatus(scanner);
        List<Application> filtered = placementService.filterByStatus(status);
        System.out.println("\n--- Applications with Status: " + status.name() + " (" + filtered.size() + " found) ---");
        printApplicationsTable(filtered);
    }

    private void handleSortMenu() {
        System.out.println("--- [7] Sort Applications ---");
        System.out.println("  1. Sort by Company Name (A to Z) [Manual Merge Sort]");
        System.out.println("  2. Sort by Salary / Stipend (Highest to Lowest) [Manual Merge Sort]");
        System.out.println("  3. Sort by Salary / Stipend (Highest to Lowest) [Java Built-in TimSort]");
        System.out.println("  4. Sort by Application Date (Newest First) [Manual Merge Sort]");
        System.out.println("  5. Sort by Application Date (Oldest First) [Manual Merge Sort]");
        System.out.println("  6. Back");

        int choice = InputValidator.readInt(scanner, "Enter choice (1-6): ", 1, 6);
        List<Application> all = placementService.getAllApplications();

        if (all.isEmpty()) {
            System.out.println(" No applications available to sort.");
            return;
        }

        List<Application> sorted = null;
        String algorithmUsed = "";

        switch (choice) {
            case 1 -> {
                algorithmUsed = "Manual Merge Sort (O(n log n)) by Company Name";
                sorted = SortAlgorithms.manualMergeSort(all, SortAlgorithms.BY_COMPANY_ASC);
            }
            case 2 -> {
                algorithmUsed = "Manual Merge Sort (O(n log n)) by Annualized Compensation (Desc)";
                sorted = SortAlgorithms.manualMergeSort(all, SortAlgorithms.BY_SALARY_DESC);
            }
            case 3 -> {
                algorithmUsed = "Java Built-in TimSort (List.sort()) by Annualized Compensation (Desc)";
                sorted = SortAlgorithms.builtinSort(all, SortAlgorithms.BY_SALARY_DESC);
            }
            case 4 -> {
                algorithmUsed = "Manual Merge Sort (O(n log n)) by Application Date (Newest First)";
                sorted = SortAlgorithms.manualMergeSort(all, SortAlgorithms.BY_DATE_DESC);
            }
            case 5 -> {
                algorithmUsed = "Manual Merge Sort (O(n log n)) by Application Date (Oldest First)";
                sorted = SortAlgorithms.manualMergeSort(all, SortAlgorithms.BY_DATE_ASC);
            }
            case 6 -> { return; }
        }

        if (sorted != null) {
            System.out.println("\n Sorted View using: " + algorithmUsed);
            printApplicationsTable(sorted);
        }
    }

    private void handleFindHighestPaying() {
        System.out.println("--- [8] Highest-Paying Opportunity ---");
        Optional<Application> highest = dashboardService.findHighestPayingOpportunity(placementService.getAllApplications());
        if (highest.isPresent()) {
            Application top = highest.get();
            System.out.println(" Top Opportunity Found via Linear Array Traversal O(n):");
            System.out.println(" Company     : " + top.getCompanyName());
            System.out.println(" Role        : " + top.getJobRole());
            System.out.println(" Type        : " + top.getType().getDisplayName());
            System.out.println(" Stated Comp : " + top.getFormattedCompensation());
            System.out.printf(" Annualized  : ₹%,.2f per year%n", top.getAnnualizedCompensation());
            System.out.println(" Status      : " + top.getStatus().getDisplayName());
            System.out.println(" Location    : " + top.getLocation());
        } else {
            System.out.println(" No applications on record.");
        }
    }

    private void handleViewUpcomingInterviews() {
        System.out.println("--- [9] Upcoming Interviews ---");
        List<Application> upcoming = dashboardService.getUpcomingInterviews(placementService.getAllApplications());
        if (upcoming.isEmpty()) {
            System.out.println(" No upcoming interviews scheduled.");
        } else {
            System.out.printf(" %d interview(s) scheduled on or after today:%n", upcoming.size());
            printApplicationsTable(upcoming);
        }
    }

    private void handleViewDashboard() {
        System.out.println("--- [10] Dashboard Statistics ---");
        System.out.println(dashboardService.renderDashboard(placementService.getAllApplications()));
    }

    private void handleSaveData() {
        System.out.println("--- [11] Save Data ---");
        boolean success = fileStorage.saveApplications(placementService.getAllApplications());
        if (success) {
            hasUnsavedChanges = false;
            System.out.println(" Data successfully saved to '" + fileStorage.getFilePath() + "'.");
        } else {
            System.out.println(" Failed to save data. Please check file permissions.");
        }
    }

    private boolean handleExit() {
        if (hasUnsavedChanges) {
            System.out.println(" You have unsaved changes.");
            String savePrompt = InputValidator.readNonEmptyString(scanner, "Would you like to save before exiting? (y/n): ");
            if (savePrompt.equalsIgnoreCase("y") || savePrompt.equalsIgnoreCase("yes")) {
                handleSaveData();
            }
        }
        return false; // Stop running
    }

    // =========================================================================
    // CONSOLE FORMATTING HELPER
    // =========================================================================

    private void printApplicationsTable(List<Application> list) {
        if (list == null || list.isEmpty()) {
            System.out.println(" (No matching applications found)");
            return;
        }

        String border = "+----------+----------------------+----------------------+------------+------------+--------------------+------------+------------+";
        System.out.println(border);
        System.out.printf("| %-8s | %-20s | %-20s | %-10s | %-10s | %-18s | %-10s | %-10s |%n",
                "ID", "Company", "Role", "Type", "Status", "Compensation", "App Date", "Interview");
        System.out.println(border);

        for (Application app : list) {
            String id = truncate(app.getApplicationId(), 8);
            String comp = truncate(app.getCompanyName(), 20);
            String role = truncate(app.getJobRole(), 20);
            String type = truncate(app.getType().getDisplayName(), 10);
            String status = truncate(app.getStatus().name(), 10);
            String salary = truncate(app.getFormattedCompensation(), 18);
            String appDate = app.getFormattedApplicationDate();
            String intDate = app.getFormattedInterviewDate();

            System.out.printf("| %-8s | %-20s | %-20s | %-10s | %-10s | %-18s | %-10s | %-10s |%n",
                    id, comp, role, type, status, salary, appDate, intDate);
        }
        System.out.println(border);
        System.out.printf("Total records displayed: %d%n", list.size());
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 2) + "..";
    }
}

# Smart Placement Tracker — Java & DSA Application Management System

A high-performance, menu-driven console application engineered in pure **Core Java (Java 21)** without any external frameworks, libraries, or external database engines. 

Designed for computer science students and aspiring software development engineers to organize internship and full-time job applications while demonstrating real-world mastery of **Data Structures and Algorithms (DSA)**, **Object-Oriented Programming (OOP)**, and robust **File I/O**.

---

## 📌 Problem Statement

During placement seasons, candidates apply to dozens of companies across multiple portals (LinkedIn, company career sites, college placement cells). Managing multiple application stages (OA, technical rounds, HR rounds, offers, rejections), tracking upcoming interview dates, and comparing compensation (monthly stipends vs. annual CTC packages) quickly becomes chaotic in unstructured spreadsheets.

**Smart Placement Tracker** solves this by providing a lightweight, persistent, and algorithmically efficient command-line management system with automated analytics, instant search, and sorting.

---

## ✨ Features

- **Application Lifecycle Management**: Add, update, delete, and view internship and full-time applications.
- **Type-Safe Stage Tracking**: Powered by Java enums (`APPLIED`, `ASSESSMENT`, `INTERVIEW`, `OFFER`, `REJECTED`, `WITHDRAWN`).
- **Normalized Compensation Evaluation**: Equates monthly stipends (internships) with annual CTCs (full-time) for fair ranking and analytics.
- **Sub-Millisecond Search**:
  - **Linear Search**: Substring and case-insensitive matching across companies and job roles ($O(n)$).
  - **Binary Search**: Logarithmic company lookup on sorted data ($O(\log n)$) with duplicate record resolution.
  - **Direct ID Lookup**: $O(1)$ unique identifier lookup powered by `HashSet`.
- **Manual Merge Sort**: Custom divide-and-conquer implementation with guaranteed $O(n \log n)$ time and stable ordering.
- **Real-Time Analytics Dashboard**:
  - Frequency distribution per application status via `HashMap`.
  - Linear scan traversal to find the highest-paying opportunity.
  - Date-based lookahead filter for upcoming scheduled interviews.
- **Resilient File Persistence**:
  - RFC-4180 compliant CSV storage with full escaping for quotes and commas.
  - Safe atomic write mechanism via temporary swap files to prevent corruption.
  - Automatic session load at startup.

---

## 🛠️ Technology Stack

- **Language**: Java 21 (Core Java)
- **Paradigm**: Object-Oriented Programming (OOP)
- **Data Structures**: `ArrayList`, `HashMap`, `HashSet`, Arrays
- **Algorithms**: Merge Sort (Divide & Conquer), Binary Search, Linear Search, Array Traversal
- **Standard APIs**: Java Collections Framework, Java Date and Time API (`java.time.LocalDate`), Java I/O (`BufferedReader`, `BufferedWriter`, `StandardCopyOption`)
- **Third-Party Dependencies**: **None (0% external libraries)**

---

## 🧩 Data Structures & Algorithms (DSA) Mapping

| Data Structure / Algorithm | Class & Location | Concrete Problem Solved | Best Case | Average Case | Worst Case | Space Complexity |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **`ArrayList<Application>`** | `PlacementService` | In-memory dynamic record management with index access. | $O(1)$ | $O(1)$ amortized append | $O(n)$ shift on delete | $O(n)$ |
| **`HashSet<String>`** | `PlacementService` | Real-time uniqueness enforcement to reject duplicate IDs. | $O(1)$ | $O(1)$ | $O(n)$ hash collision | $O(n)$ |
| **`HashMap<Status, Integer>`** | `DashboardService` | One-pass frequency aggregation for status metrics. | $O(1)$ | $O(1)$ per key | $O(1)$ ($k \le 6$ enums) | $O(k)$ |
| **Linear Search** | `SearchAlgorithms` | Case-insensitive substring matching on role and company. | $O(1)$ | $O(n)$ | $O(n)$ | $O(k)$ matches |
| **Binary Search** | `SearchAlgorithms` | Logarithmic search on sorted company names. | $O(1)$ | $O(\log n)$ | $O(\log n)$ | $O(1)$ iterative |
| **Merge Sort** (Manual) | `SortAlgorithms` | Stable sorting by compensation, company, or date. | $O(n \log n)$ | $O(n \log n)$ | $O(n \log n)$ | $O(n)$ auxiliary |
| **Array Traversal** | `DashboardService` | Single linear scan to find highest compensation & interviews. | $O(n)$ | $O(n)$ | $O(n)$ | $O(1)$ |

---

## 🧱 OOP Architecture & Principles

- **Encapsulation**: All fields in `Application.java` are strictly `private`. Modification is only permitted through validated setters and business domain methods.
- **Type Safety via Enums**: Eliminates string typo bugs for statuses and application types using `ApplicationStatus` and `ApplicationType`.
- **Single Responsibility Principle (SRP)**:
  - `Main.java`: User experience, menu display, and console formatting.
  - `PlacementService.java`: CRUD state management.
  - `SearchAlgorithms.java` & `SortAlgorithms.java`: Pure algorithmic functions without side effects.
  - `DashboardService.java`: Aggregations and metrics.
  - `FileStorage.java`: Serialization and disk I/O.
  - `InputValidator.java`: Defensive console reading (preventing Scanner newline pitfalls).

---

## 📂 Project Structure

```text
SmartPlacementTracker/
├── src/
│   └── com/
│       └── smartplacementtracker/
│           ├── Main.java                          # CLI Menu & Runner
│           ├── model/
│           │   ├── Application.java               # Core Model Entity
│           │   ├── ApplicationStatus.java         # Lifecycle Enum
│           │   └── ApplicationType.java           # Employment Type Enum
│           ├── service/
│           │   └── PlacementService.java          # ArrayList & HashSet CRUD
│           ├── algorithms/
│           │   ├── SearchAlgorithms.java          # Linear & Binary Search
│           │   └── SortAlgorithms.java            # Manual Merge Sort & Comparators
│           ├── analytics/
│           │   └── DashboardService.java          # HashMap Aggregations & Traversal
│           ├── storage/
│           │   └── FileStorage.java               # Atomic CSV File Persistence
│           └── util/
│               └── InputValidator.java            # Safe Scanner & Regex Parser
├── data/
│   └── applications.csv                           # Local CSV data store
├── test/
│   └── com/
│       └── smartplacementtracker/
│           └── AppSelfTest.java                   # Automated Test Suite (43 tests)
├── .gitignore
└── README.md
```

---

## ⚡ How to Compile & Run

### Prerequisites
- Java Development Kit (JDK 21 or later)
- Terminal / PowerShell / Bash

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/smart-placement-tracker.git
cd smart-placement-tracker
```

### 2. Compile the Application
On Windows PowerShell:
```powershell
javac -d bin (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
```
On Linux / macOS:
```bash
javac -d bin $(find src -name "*.java")
```

### 3. Run the Application
```bash
java -cp bin com.smartplacementtracker.Main
```

### 4. Run the Automated Test Suite
On Windows PowerShell:
```powershell
javac -d bin (Get-ChildItem -Recurse -Filter *.java src, test | ForEach-Object { $_.FullName })
java -cp bin com.smartplacementtracker.AppSelfTest
```
On Linux / macOS:
```bash
javac -d bin $(find src test -name "*.java")
java -cp bin com.smartplacementtracker.AppSelfTest
```

---

## 🖥️ Example Console Output

```text
========================================================================
     WELCOME TO SMART PLACEMENT TRACKER - JAVA & DSA PORTFOLIO
========================================================================
[FileStorage] Successfully loaded 7 application record(s) from 'data/applications.csv'.

========================================
        SMART PLACEMENT TRACKER
========================================
Total Applications: 7
  - Applied     : 1
  - Assessments : 2
  - Interviews  : 1
  - Offers      : 1
  - Rejected    : 1
  - Withdrawn   : 1
----------------------------------------
Highest Compensation: ₹26,00,000.00 / year (CTC) at Atlassian (Software Engineer)
Annualized Equivalent: ₹26,00,000.00 per year
========================================

========================================
        SMART PLACEMENT TRACKER
========================================
 1. Add Application
 2. View All Applications
 3. Search Application
 4. Update Application
 5. Delete Application
 6. Filter Applications by Status
 7. Sort Applications
 8. Find Highest-Paying Opportunity
 9. View Upcoming Interviews
10. View Dashboard Statistics
11. Save Data
12. Exit
========================================
Enter your choice (1-12):
```

---

## 🧪 Testing Verification

The project includes an in-depth standalone automated test suite (`AppSelfTest.java`) covering 43 test assertions:
- **Encapsulation & Validation**: Verifies negative salary rejection, blank company guards, and date formatting.
- **CRUD Operations**: Validates adding, updating, deleting, and re-inserting records.
- **Uniqueness Check**: Validates that `HashSet` denies duplicate application IDs in $O(1)$ time.
- **Merge Sort Verification**: Asserts that manual divide-and-conquer Merge Sort produces the identical sorted order as Java's native TimSort.
- **Binary Search Verification**: Confirms logarithmic exact matches, missing key handling, and duplicate company range extraction.
- **CSV Escaping**: Confirms RFC-4180 parsing with quotes, commas, and multiline values without data corruption.

---

## 🚀 GitHub Repository Metadata & Push Instructions

### Recommended Repository Description
> *"A professional, zero-dependency Java 21 console application for managing job and internship applications using OOP, Collections (ArrayList, HashSet, HashMap), and manual DSA algorithms (Merge Sort, Binary Search)."*

### Recommended Repository Topics (Tags)
`java`, `core-java`, `data-structures`, `algorithms`, `merge-sort`, `binary-search`, `oop`, `internship-project`, `placement-tracker`, `console-application`

### Git Push Commands
```bash
git init
git add .
git commit -m "feat: complete Smart Placement Tracker with Core Java and DSA algorithms"
git branch -M main
git remote add origin https://github.com/your-username/smart-placement-tracker.git
git push -u origin main
```

### Suggested Progressive Commit History
If you want to push multiple commits to demonstrate your development workflow:
1. `chore: initialize project architecture and directory structure`
2. `feat(model): implement Application entity, ApplicationStatus, and ApplicationType enums`
3. `feat(service): implement PlacementService using ArrayList and HashSet for O(1) deduplication`
4. `feat(algorithms): implement Linear Search and iterative Binary Search`
5. `feat(algorithms): implement manual Merge Sort with custom Comparators`
6. `feat(analytics): add DashboardService with HashMap frequency aggregation`
7. `feat(storage): implement RFC-4180 compliant CSV persistence with atomic file writes`
8. `feat(ui): implement interactive console menu and safe InputValidator`
9. `test: add AppSelfTest standalone test suite verifying 43 test cases`
10. `docs: add comprehensive README with architecture, DSA complexity table, and setup guide`

---

## 🔮 Future Enhancements

- Integration of a min-heap / priority queue to automatically surface the next closest interview deadlines.
- Exporting placement summary reports as formatted text or Markdown files.
- Command-line argument flags for automated batch imports and headless operation.

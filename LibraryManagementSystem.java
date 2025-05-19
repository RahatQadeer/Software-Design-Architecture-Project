package Liskow;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class LibraryManagementSystem {
    static Scanner scanner = new Scanner(System.in);

    // Data stores
    static List<Book> books = new ArrayList<>();
    static List<User> users = new ArrayList<>();
    static Map<Integer, Integer> issuedBooks = new HashMap<>(); // bookId -> userId
    static Map<Integer, LocalDate> dueDates = new HashMap<>(); // bookId -> dueDate
    static Map<Integer, Queue<Integer>> reservations = new HashMap<>(); // bookId -> queue of userIds
    static Map<Integer, List<Review>> bookReviews = new HashMap<>(); // bookId -> list of reviews

    static User currentUser = null;

    // Interfaces for ISP

    interface UserManager {
        void registerUser();

        void loginUser();

        void viewUsers();
    }

    interface BookManager {
        void addBook();

        void viewBooks();

        void searchBook();

        void updateBook();

        void deleteBook();

        void rateBook();

        void viewBookReviews();

        void recommendBooks();
    }

    interface IssueManager {
        void issueBook();

        void returnBook();

        void renewBook();

        void reserveBook();

        void viewIssuedBooks();

        void viewOverdueBooks();

        void viewReservedBooks();

        void calculateFine();
    }

    interface PersistenceManager {
        void saveData();

        void loadData();
    }

    interface ReportManager {
        void generateReports();

        void notifyDueDates();
    }

    interface AdminManager {
        boolean adminLogin();

        void adminMenu();
    }

    // Implementations

    static class DefaultUserManager implements UserManager {
        private int userIdCounter = 1;

        @Override
        public void registerUser() {
            System.out.print("Enter name: ");
            String name = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            System.out.print("Enter role (USER/LIBRARIAN): ");
            String role = scanner.nextLine().toUpperCase();

            if (!role.equals("USER") && !role.equals("LIBRARIAN")) {
                System.out.println("Invalid role.");
                return;
            }
            User user = new User(userIdCounter++, name, password, role, 5);
            users.add(user);
            System.out.println("User registered: " + user);
        }

        @Override
        public void loginUser() {
            System.out.print("Enter user ID: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            for (User u : users) {
                if (u.id == id && u.password.equals(password)) {
                    currentUser = u;
                    System.out.println("Login successful. Welcome " + u.name);
                    userMenu();
                    return;
                }
            }
            System.out.println("Invalid credentials.");
        }

        void userMenu() {
            int choice;
            BookManager bookManager = new DefaultBookManager();
            IssueManager issueManager = new DefaultIssueManager();

            do {
                System.out.println("\n--- User Menu ---");
                System.out.println("1. View Books");
                System.out.println("2. Search Book");
                System.out.println("3. Issue Book");
                System.out.println("4. Return Book");
                System.out.println("5. Renew Book");
                System.out.println("6. Reserve Book");
                System.out.println("7. View Issued Books");
                System.out.println("8. View Reserved Books");
                System.out.println("9. Calculate Fine");
                System.out.println("10. Rate Book");
                System.out.println("11. View Book Reviews");
                System.out.println("12. Recommend Books");
                System.out.println("0. Logout");
                System.out.print("Enter choice: ");
                choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> bookManager.viewBooks();
                    case 2 -> bookManager.searchBook();
                    case 3 -> issueManager.issueBook();
                    case 4 -> issueManager.returnBook();
                    case 5 -> issueManager.renewBook();
                    case 6 -> issueManager.reserveBook();
                    case 7 -> issueManager.viewIssuedBooks();
                    case 8 -> issueManager.viewReservedBooks();
                    case 9 -> issueManager.calculateFine();
                    case 10 -> bookManager.rateBook();
                    case 11 -> bookManager.viewBookReviews();
                    case 12 -> bookManager.recommendBooks();
                    case 0 -> {
                        System.out.println("Logged out.");
                        currentUser = null;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } while (choice != 0);
        }

        @Override
        public void viewUsers() {
            for (User u : users) {
                System.out.println(u);
            }
        }
    }

    static class DefaultBookManager implements BookManager {
        private int bookIdCounter = 1;

        @Override
        public void addBook() {
            System.out.print("Enter book title: ");
            String title = scanner.nextLine();
            System.out.print("Enter author: ");
            String author = scanner.nextLine();
            System.out.print("Enter category: ");
            String category = scanner.nextLine();

            Book book = new Book(bookIdCounter++, title, author, category);
            books.add(book);
            System.out.println("Book added: " + book);
        }

        @Override
        public void viewBooks() {
            if (books.isEmpty()) {
                System.out.println("No books available.");
                return;
            }
            for (Book b : books) {
                System.out.printf(
                        "ID: %d, Title: %s, Author: %s, Category: %s, Status: %s, Avg Rating: %.2f (%d ratings)%n",
                        b.id, b.title, b.author, b.category, b.status, b.avgRating, b.totalRatings);
            }
        }

        @Override
        public void searchBook() {
            System.out.print("Enter title or author keyword: ");
            String keyword = scanner.nextLine().toLowerCase();
            boolean found = false;
            for (Book b : books) {
                if (b.title.toLowerCase().contains(keyword) || b.author.toLowerCase().contains(keyword)) {
                    System.out.println(b);
                    found = true;
                }
            }
            if (!found)
                System.out.println("No matching books found.");
        }

        @Override
        public void updateBook() {
            System.out.print("Enter book ID to update: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            Book book = books.stream().filter(b -> b.id == id).findFirst().orElse(null);
            if (book == null) {
                System.out.println("Book not found.");
                return;
            }
            System.out.print("Enter new title (leave blank to keep): ");
            String title = scanner.nextLine();
            if (!title.isBlank())
                book.title = title;
            System.out.print("Enter new author (leave blank to keep): ");
            String author = scanner.nextLine();
            if (!author.isBlank())
                book.author = author;
            System.out.print("Enter new category (leave blank to keep): ");
            String category = scanner.nextLine();
            if (!category.isBlank())
                book.category = category;
            System.out.println("Book updated: " + book);
        }

        @Override
        public void deleteBook() {
            System.out.print("Enter book ID to delete: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            books.removeIf(b -> b.id == id);
            issuedBooks.remove(id);
            dueDates.remove(id);
            reservations.remove(id);
            bookReviews.remove(id);
            System.out.println("Book deleted if it existed.");
        }

        @Override
        public void rateBook() {
            System.out.print("Enter book ID to rate: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            Book book = books.stream().filter(b -> b.id == id).findFirst().orElse(null);
            if (book == null) {
                System.out.println("Book not found.");
                return;
            }
            System.out.print("Enter rating (1-5): ");
            int rating = scanner.nextInt();
            scanner.nextLine();
            if (rating < 1 || rating > 5) {
                System.out.println("Invalid rating.");
                return;
            }
            System.out.print("Enter review comment: ");
            String comment = scanner.nextLine();

            bookReviews.computeIfAbsent(id, k -> new ArrayList<>())
                    .add(new Review(currentUser.id, rating, comment));

            book.updateRatings();
            System.out.println("Thank you for your review.");
        }

        @Override
        public void viewBookReviews() {
            System.out.print("Enter book ID to view reviews: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            List<Review> reviews = bookReviews.get(id);
            if (reviews == null || reviews.isEmpty()) {
                System.out.println("No reviews for this book.");
                return;
            }
            System.out.println("Reviews:");
            for (Review r : reviews) {
                User reviewer = users.stream().filter(u -> u.id == r.userId).findFirst().orElse(null);
                String reviewerName = (reviewer == null) ? "Unknown" : reviewer.name;
                System.out.printf("User: %s, Rating: %d, Comment: %s%n", reviewerName, r.rating, r.comment);
            }
        }

        @Override
        public void recommendBooks() {
            // Simple recommendation by category of last rated book or top rated books
            System.out.print("Enter preferred category for recommendation: ");
            String category = scanner.nextLine().toLowerCase();
            List<Book> recommended = new ArrayList<>();
            for (Book b : books) {
                if (b.category.toLowerCase().contains(category) && b.avgRating >= 3) {
                    recommended.add(b);
                }
            }

            if (recommended.isEmpty()) {
                System.out.println("No recommendations available for this category.");
                return;
            }

            recommended.sort((a, b) -> Double.compare(b.avgRating, a.avgRating));
            System.out.println("Recommended books:");
            for (Book b : recommended) {
                System.out.printf("ID: %d, Title: %s, Avg Rating: %.2f%n", b.id, b.title, b.avgRating);
            }
        }
    }

    static class DefaultIssueManager implements IssueManager {
        private final int MAX_ISSUE_DAYS = 14;
        private final double FINE_PER_DAY = 1.0;

        @Override
        public void issueBook() {
            System.out.print("Enter book ID to issue: ");
            int bookId = scanner.nextInt();
            scanner.nextLine();
            Book book = books.stream().filter(b -> b.id == bookId).findFirst().orElse(null);

            if (book == null) {
                System.out.println("Book not found.");
                return;
            }
            if (!book.status.equals("AVAILABLE")) {
                System.out.println("Book is not available.");
                return;
            }

            // Check if user already has max allowed books
            long countIssuedByUser = issuedBooks.values().stream().filter(uid -> uid == currentUser.id).count();
            if (countIssuedByUser >= currentUser.maxBooksAllowed) {
                System.out.println("You have reached the maximum number of issued books.");
                return;
            }

            // Check if book reserved by others before current user
            Queue<Integer> reservationQueue = reservations.get(bookId);
            if (reservationQueue != null && !reservationQueue.isEmpty()) {
                if (!reservationQueue.peek().equals(currentUser.id)) {
                    System.out.println("Book is reserved by another user.");
                    return;
                } else {
                    // Remove current user from reservation queue
                    reservationQueue.poll();
                    if (reservationQueue.isEmpty()) {
                        reservations.remove(bookId);
                    }
                }
            }

            issuedBooks.put(bookId, currentUser.id);
            dueDates.put(bookId, LocalDate.now().plusDays(MAX_ISSUE_DAYS));
            book.status = "ISSUED";
            System.out.println("Book issued. Due date: " + dueDates.get(bookId));
        }

        @Override
        public void returnBook() {
            System.out.print("Enter book ID to return: ");
            int bookId = scanner.nextInt();
            scanner.nextLine();

            if (!issuedBooks.containsKey(bookId) || issuedBooks.get(bookId) != currentUser.id) {
                System.out.println("You have not issued this book.");
                return;
            }

            issuedBooks.remove(bookId);
            dueDates.remove(bookId);

            // Change status to AVAILABLE or RESERVED if reservations exist
            if (reservations.containsKey(bookId) && !reservations.get(bookId).isEmpty()) {
                books.stream().filter(b -> b.id == bookId).findFirst().ifPresent(b -> b.status = "RESERVED");
            } else {
                books.stream().filter(b -> b.id == bookId).findFirst().ifPresent(b -> b.status = "AVAILABLE");
            }

            System.out.println("Book returned successfully.");
        }

        @Override
        public void renewBook() {
            System.out.print("Enter book ID to renew: ");
            int bookId = scanner.nextInt();
            scanner.nextLine();

            if (!issuedBooks.containsKey(bookId) || issuedBooks.get(bookId) != currentUser.id) {
                System.out.println("You have not issued this book.");
                return;
            }
            if (reservations.containsKey(bookId) && !reservations.get(bookId).isEmpty()) {
                System.out.println("Book has reservations, cannot renew.");
                return;
            }

            LocalDate currentDue = dueDates.get(bookId);
            LocalDate newDue = currentDue.plusDays(MAX_ISSUE_DAYS);
            dueDates.put(bookId, newDue);
            System.out.println("Book renewed. New due date: " + newDue);
        }

        @Override
        public void reserveBook() {
            System.out.print("Enter book ID to reserve: ");
            int bookId = scanner.nextInt();
            scanner.nextLine();
            Book book = books.stream().filter(b -> b.id == bookId).findFirst().orElse(null);

            if (book == null) {
                System.out.println("Book not found.");
                return;
            }
            if (book.status.equals("AVAILABLE")) {
                System.out.println("Book is available. You can issue it instead of reserving.");
                return;
            }
            Queue<Integer> reservationQueue = reservations.computeIfAbsent(bookId, k -> new LinkedList<>());
            if (reservationQueue.contains(currentUser.id)) {
                System.out.println("You already reserved this book.");
                return;
            }
            reservationQueue.offer(currentUser.id);
            book.status = "RESERVED";
            System.out.println("Book reserved successfully.");
        }

        @Override
        public void viewIssuedBooks() {
            System.out.println("Your issued books:");
            boolean none = true;
            for (Map.Entry<Integer, Integer> entry : issuedBooks.entrySet()) {
                if (entry.getValue() == currentUser.id) {
                    int bookId = entry.getKey();
                    Book book = books.stream().filter(b -> b.id == bookId).findFirst().orElse(null);
                    LocalDate due = dueDates.get(bookId);
                    System.out.printf("ID: %d, Title: %s, Due Date: %s%n", bookId, book.title, due);
                    none = false;
                }
            }
            if (none)
                System.out.println("No issued books.");
        }

        @Override
        public void viewOverdueBooks() {
            System.out.println("Overdue books:");
            LocalDate today = LocalDate.now();
            boolean none = true;
            for (Map.Entry<Integer, LocalDate> entry : dueDates.entrySet()) {
                if (entry.getValue().isBefore(today)) {
                    Book book = books.stream().filter(b -> b.id == entry.getKey()).findFirst().orElse(null);
                    System.out.printf("ID: %d, Title: %s, Due Date: %s%n", entry.getKey(), book.title,
                            entry.getValue());
                    none = false;
                }
            }
            if (none)
                System.out.println("No overdue books.");
        }

        @Override
        public void viewReservedBooks() {
            System.out.println("Your reserved books:");
            boolean none = true;
            for (Map.Entry<Integer, Queue<Integer>> entry : reservations.entrySet()) {
                if (entry.getValue().contains(currentUser.id)) {
                    Book book = books.stream().filter(b -> b.id == entry.getKey()).findFirst().orElse(null);
                    System.out.println(book);
                    none = false;
                }
            }
            if (none)
                System.out.println("No reserved books.");
        }

        @Override
        public void calculateFine() {
            System.out.print("Enter book ID to calculate fine: ");
            int bookId = scanner.nextInt();
            scanner.nextLine();
            if (!issuedBooks.containsKey(bookId) || issuedBooks.get(bookId) != currentUser.id) {
                System.out.println("You have not issued this book.");
                return;
            }
            LocalDate due = dueDates.get(bookId);
            LocalDate today = LocalDate.now();
            long daysLate = ChronoUnit.DAYS.between(due, today);
            if (daysLate <= 0) {
                System.out.println("No fine. Book is not overdue.");
            } else {
                System.out.printf("Book is %d days overdue. Fine: $%.2f%n", daysLate, daysLate * FINE_PER_DAY);
            }
        }
    }

    static class DefaultPersistenceManager implements PersistenceManager {
        @Override
        public void saveData() {
            // Placeholder for saving data to file or DB
            System.out.println("Saving data... (not implemented)");
        }

        @Override
        public void loadData() {
            // Placeholder for loading data from file or DB
            System.out.println("Loading data... (not implemented)");
        }
    }

    static class DefaultReportManager implements ReportManager {
        @Override
        public void generateReports() {
            System.out.println("Generating reports...");

            // Example: total books, total users, books issued
            System.out.println("Total books: " + books.size());
            System.out.println("Total users: " + users.size());
            System.out.println("Books issued: " + issuedBooks.size());

            // Could add more detailed reports here
        }

        @Override
        public void notifyDueDates() {
            LocalDate today = LocalDate.now();
            System.out.println("Due date notifications:");
            for (Map.Entry<Integer, LocalDate> entry : dueDates.entrySet()) {
                long daysLeft = ChronoUnit.DAYS.between(today, entry.getValue());
                if (daysLeft >= 0 && daysLeft <= 3) { // Notify if due in 3 days
                    int bookId = entry.getKey();
                    int userId = issuedBooks.get(bookId);
                    User user = users.stream().filter(u -> u.id == userId).findFirst().orElse(null);
                    Book book = books.stream().filter(b -> b.id == bookId).findFirst().orElse(null);
                    if (user != null && book != null) {
                        System.out.printf("Reminder: User %s, book '%s' is due in %d days.%n",
                                user.name, book.title, daysLeft);
                    }
                }
            }
        }
    }

    static class DefaultAdminManager implements AdminManager {
        private final String adminUsername = "admin";
        private final String adminPassword = "admin123";

        @Override
        public boolean adminLogin() {
            System.out.print("Enter admin username: ");
            String username = scanner.nextLine();
            System.out.print("Enter admin password: ");
            String password = scanner.nextLine();
            if (adminUsername.equals(username) && adminPassword.equals(password)) {
                System.out.println("Admin login successful.");
                return true;
            }
            System.out.println("Invalid admin credentials.");
            return false;
        }

        @Override
        public void adminMenu() {
            int choice;
            UserManager userManager = new DefaultUserManager();
            BookManager bookManager = new DefaultBookManager();
            IssueManager issueManager = new DefaultIssueManager();
            ReportManager reportManager = new DefaultReportManager();

            do {
                System.out.println("\n--- Admin Menu ---");
                System.out.println("1. Add Book");
                System.out.println("2. View Books");
                System.out.println("3. Update Book");
                System.out.println("4. Delete Book");
                System.out.println("5. View Users");
                System.out.println("6. View Issued Books");
                System.out.println("7. View Overdue Books");
                System.out.println("8. Generate Reports");
                System.out.println("9. Notify Due Dates");
                System.out.println("0. Logout");
                System.out.print("Enter choice: ");
                choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> bookManager.addBook();
                    case 2 -> bookManager.viewBooks();
                    case 3 -> bookManager.updateBook();
                    case 4 -> bookManager.deleteBook();
                    case 5 -> userManager.viewUsers();
                    case 6 -> issueManager.viewIssuedBooks();
                    case 7 -> issueManager.viewOverdueBooks();
                    case 8 -> reportManager.generateReports();
                    case 9 -> reportManager.notifyDueDates();
                    case 0 -> System.out.println("Admin logged out.");
                    default -> System.out.println("Invalid choice.");
                }
            } while (choice != 0);
        }
    }

    // Models

    static class User {
        int id;
        String name;
        String password;
        String role; // USER or LIBRARIAN
        int maxBooksAllowed;

        public User(int id, String name, String password, String role, int maxBooksAllowed) {
            this.id = id;
            this.name = name;
            this.password = password;
            this.role = role;
            this.maxBooksAllowed = maxBooksAllowed;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", role='" + role + '\'' +
                    ", maxBooksAllowed=" + maxBooksAllowed +
                    '}';
        }
    }

    static class Book {
        int id;
        String title;
        String author;
        String category;
        String status; // AVAILABLE, ISSUED, RESERVED
        double avgRating;
        int totalRatings;

        public Book(int id, String title, String author, String category) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.category = category;
            this.status = "AVAILABLE";
            this.avgRating = 0.0;
            this.totalRatings = 0;
        }

        public void updateRatings() {
            List<Review> reviews = bookReviews.get(id);
            if (reviews == null || reviews.isEmpty()) {
                avgRating = 0.0;
                totalRatings = 0;
                return;
            }
            int sum = 0;
            for (Review r : reviews) {
                sum += r.rating;
            }
            totalRatings = reviews.size();
            avgRating = (double) sum / totalRatings;
        }

        @Override
        public String toString() {
            return "Book{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", author='" + author + '\'' +
                    ", category='" + category + '\'' +
                    ", status='" + status + '\'' +
                    ", avgRating=" + String.format("%.2f", avgRating) +
                    ", totalRatings=" + totalRatings +
                    '}';
        }
    }

    static class Review {
        int userId;
        int rating;
        String comment;

        public Review(int userId, int rating, String comment) {
            this.userId = userId;
            this.rating = rating;
            this.comment = comment;
        }
    }

    // Main method

    public static void main(String[] args) {
        DefaultUserManager userManager = new DefaultUserManager();
        DefaultAdminManager adminManager = new DefaultAdminManager();

        int mainChoice;
        do {
            System.out.println("\n--- Library Management System ---");
            System.out.println("1. Register User");
            System.out.println("2. User Login");
            System.out.println("3. Admin Login");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            mainChoice = scanner.nextInt();
            scanner.nextLine();

            switch (mainChoice) {
                case 1 -> userManager.registerUser();
                case 2 -> userManager.loginUser();
                case 3 -> {
                    if (adminManager.adminLogin()) {
                        adminManager.adminMenu();
                    }
                }
                case 0 -> System.out.println("Exiting system. Goodbye!");
                default -> System.out.println("Invalid choice.");
            }
        } while (mainChoice != 0);
    }

}

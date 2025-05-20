import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class LibraryManagementSystem {
    static Scanner scanner = new Scanner(System.in);
    static List<Book> books = new ArrayList<>();
    static List<User> users = new ArrayList<>();
    static Map<Integer, Integer> issuedBooks = new HashMap<>();
    static Map<Integer, LocalDate> dueDates = new HashMap<>();
    static Map<Integer, Queue<Integer>> reservations = new HashMap<>();
    static User currentUser = null;

    public static void main(String[] args) {
        BookManager bookManager = new DefaultBookManager();
        UserManager userManager = new DefaultUserManager();
        IssueManager issueManager = new DefaultIssueManager();
        AdminManager adminManager = new DefaultAdminManager(bookManager, userManager, issueManager);

        int choice;
        do {
            System.out.println("\n--- Library Management System ---");
            System.out.println("1. Admin Login");
            System.out.println("2. Create User Account");
            System.out.println("3. User Login");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> adminManager.adminMenu();
                case 2 -> userManager.createUserAccount();
                case 3 -> userManager.userLogin();
                case 0 -> System.out.println("Exiting...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    // --- Interfaces ---
    
    // Book Manager Interface
    interface BookManager {
        void addBook();
        void viewBooks();
        void searchBook();
        void updateBook();
        void deleteBook();
        void viewCategories();
    }
    
    // User Manager Interface
    interface UserManager {
        void createUserAccount();
        void userLogin();
        void viewUsers();
        void calculateFine();
    }
    
    // Issue Manager Interface
    interface IssueManager {
        void issueBook();
        void returnBook();
        void viewIssuedBooks();
        void viewOverdueBooks();
    }
    
    // Admin Manager Interface
    interface AdminManager {
        void adminMenu();
        boolean adminLogin();
    }

    // --- Default Implementations ---
    
    // Default Book Manager
    static class DefaultBookManager implements BookManager {
        @Override
        public void addBook() {
            System.out.print("Enter title: ");
            String title = scanner.nextLine();
            System.out.print("Enter author: ");
            String author = scanner.nextLine();
            System.out.print("Enter category: ");
            String category = scanner.nextLine();
            int id = books.size() + 1;
            books.add(new Book(id, title, author, category));
            System.out.println("Book added.");
        }

        @Override
        public void viewBooks() {
            if (books.isEmpty()) System.out.println("No books found.");
            books.forEach(b -> System.out.println(b + ", Status: " + b.status));
        }

        @Override
        public void searchBook() {
            System.out.print("Search keyword: ");
            String keyword = scanner.nextLine().toLowerCase();
            books.stream()
                 .filter(b -> b.title.toLowerCase().contains(keyword)
                        || b.author.toLowerCase().contains(keyword)
                        || b.category.toLowerCase().contains(keyword))
                 .forEach(System.out::println);
        }

        @Override
        public void updateBook() {
            System.out.print("Enter book ID: ");
            int id = scanner.nextInt(); scanner.nextLine();
            for (Book b : books) {
                if (b.id == id) {
                    System.out.print("New title: ");
                    b.title = scanner.nextLine();
                    System.out.print("New author: ");
                    b.author = scanner.nextLine();
                    System.out.print("New category: ");
                    b.category = scanner.nextLine();
                    System.out.println("Book updated.");
                    return;
                }
            }
            System.out.println("Book not found.");
        }

        @Override
        public void deleteBook() {
            System.out.print("Enter book ID: ");
            int id = scanner.nextInt(); scanner.nextLine();
            books.removeIf(b -> b.id == id);
            issuedBooks.remove(id);
            dueDates.remove(id);
            reservations.remove(id);
            System.out.println("Book deleted.");
        }

        @Override
        public void viewCategories() {
            Set<String> categories = new HashSet<>();
            books.forEach(b -> categories.add(b.category));
            System.out.println("Categories: " + categories);
        }
    }

    // Default User Manager
    static class DefaultUserManager implements UserManager {
        @Override
        public void createUserAccount() {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            System.out.print("Enter role (USER/LIBRARIAN): ");
            String role = scanner.nextLine();
            int id = users.size() + 1;
            users.add(new User(id, username, password, role, 0.0));
            System.out.println("Account created. ID: " + id);
        }

        @Override
        public void userLogin() {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            for (User u : users) {
                if (u.name.equals(username) && u.password.equals(password)) {
                    currentUser = u;
                    System.out.println("Welcome, " + u.name + "!");
                    return;
                }
            }
            System.out.println("Invalid credentials.");
        }

        @Override
        public void viewUsers() {
            if (users.isEmpty()) System.out.println("No users found.");
            users.forEach(u -> System.out.println(u + ", Fines: $" + u.fines));
        }

        @Override
        public void calculateFine() {
            users.forEach(u -> System.out.println("User: " + u.name + ", Fine: $" + u.fines));
        }
    }

    // Default Issue Manager
    static class DefaultIssueManager implements IssueManager {
        @Override
        public void issueBook() {
            System.out.print("Enter book ID: ");
            int bookId = scanner.nextInt();
            System.out.print("Enter user ID: ");
            int userId = scanner.nextInt();
            scanner.nextLine();

            Book book = books.stream().filter(b -> b.id == bookId).findFirst().orElse(null);
            User user = users.stream().filter(u -> u.id == userId).findFirst().orElse(null);

            if (book == null || user == null) {
                System.out.println("Invalid ID(s).");
                return;
            }

            if (book.status.equals("AVAILABLE")) {
                issuedBooks.put(bookId, userId);
                dueDates.put(bookId, LocalDate.now().plusWeeks(2));
                book.status = "ISSUED";
                System.out.println("Book issued. Due: " + dueDates.get(bookId));
            } else {
                System.out.println("Book unavailable.");
            }
        }

        @Override
        public void returnBook() {
            System.out.print("Enter book ID: ");
            int bookId = scanner.nextInt();
            scanner.nextLine();

            Book book = books.stream().filter(b -> b.id == bookId).findFirst().orElse(null);
            if (book == null) {
                System.out.println("Invalid book ID.");
                return;
            }

            if (issuedBooks.containsKey(bookId)) {
                User user = users.get(issuedBooks.get(bookId) - 1);
                LocalDate dueDate = dueDates.get(bookId);

                if (LocalDate.now().isAfter(dueDate)) {
                    long daysLate = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
                    double fine = daysLate * 0.50;
                    user.fines += fine;
                    System.out.printf("Late return! Fine: $%.2f%n", fine);
                }

                issuedBooks.remove(bookId);
                dueDates.remove(bookId);

                Queue<Integer> reservationQueue = reservations.get(bookId);
                if (reservationQueue != null && !reservationQueue.isEmpty()) {
                    int nextUserId = reservationQueue.poll();
                    issuedBooks.put(bookId, nextUserId);
                    dueDates.put(bookId, LocalDate.now().plusWeeks(2));
                    book.status = "ISSUED";
                    System.out.println("Book issued to reserved user ID: " + nextUserId);
                } else {
                    book.status = "AVAILABLE";
                    System.out.println("Book returned.");
                }
            } else {
                System.out.println("Book is not issued.");
            }
        }

        @Override
        public void viewIssuedBooks() {
            for (var entry : issuedBooks.entrySet()) {
                Book book = books.get(entry.getKey() - 1);
                User user = users.get(entry.getValue() - 1);
                System.out.println(book.title + " issued to " + user.name + ", Due: " + dueDates.get(entry.getKey()));
            }
        }

        @Override
        public void viewOverdueBooks() {
            LocalDate today = LocalDate.now();
            dueDates.forEach((bookId, due) -> {
                if (due.isBefore(today)) {
                    Book book = books.get(bookId - 1);
                    System.out.println("Overdue: " + book.title + ", Due: " + due);
                }
            });
        }
    }

    // Default Admin Manager
    static class DefaultAdminManager implements AdminManager {
        private BookManager bookManager;
        private UserManager userManager;
        private IssueManager issueManager;

        DefaultAdminManager(BookManager bm, UserManager um, IssueManager im) {
            this.bookManager = bm;
            this.userManager = um;
            this.issueManager = im;
        }

        @Override
        public void adminMenu() {
            if (!adminLogin()) return;
            int choice;
            do {
                System.out.println("\n--- Admin Menu ---");
                System.out.println("1. Add Book");
                System.out.println("2. View All Books");
                System.out.println("3. Search Books");
                System.out.println("4. Update Book");
                System.out.println("5. Delete Book");
                System.out.println("6. View Users");
                System.out.println("7. Issue Book");
                System.out.println("8. Return Book");
                System.out.println("9. View Issued Books");
                System.out.println("10. Calculate Fine");
                System.out.println("11. View Book Categories");
                System.out.println("12. View Overdue Books");
                System.out.println("0. Logout");
                System.out.print("Enter choice: ");
                choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1 -> bookManager.addBook();
                    case 2 -> bookManager.viewBooks();
                    case 3 -> bookManager.searchBook();
                    case 4 -> bookManager.updateBook();
                    case 5 -> bookManager.deleteBook();
                    case 6 -> userManager.viewUsers();
                    case 7 -> issueManager.issueBook();
                    case 8 -> issueManager.returnBook();
                    case 9 -> issueManager.viewIssuedBooks();
                    case 10 -> userManager.calculateFine();
                    case 11 -> bookManager.viewCategories();
                    case 12 -> issueManager.viewOverdueBooks();
                    case 0 -> System.out.println("Logging out...");
                    default -> System.out.println("Invalid choice.");
                }
            } while (choice != 0);
        }

        @Override
        public boolean adminLogin() {
            System.out.print("Enter admin username: ");
            String username = scanner.nextLine();
            System.out.print("Enter admin password: ");
            String password = scanner.nextLine();
            return username.equals("admin") && password.equals("admin123");
        }
    }

    // --- Supporting Classes ---
    static class Book {
        int id;
        String title, author, category;
        String status = "AVAILABLE";

        Book(int id, String title, String author, String category) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.category = category;
        }

        public String toString() {
            return "ID: " + id + ", " + title + " by " + author + " (" + category + ")";
        }
    }

    static class User {
        int id;
        String name, password, role;
        double fines;

        User(int id, String name, String password, String role, double fines) {
            this.id = id;
            this.name = name;
            this.password = password;
            this.role = role;
            this.fines = fines;
        }

        public String toString() {
            return "ID: " + id + ", Name: " + name + ", Role: " + role;
        }
    }
    
    // --- Example Extensions ---
    
    // Extended Book Manager with report generation functionality
    static class ReportingBookManager implements BookManager {
        private BookManager bookManager;
        
        public ReportingBookManager(BookManager bookManager) {
            this.bookManager = bookManager;
        }
        
        // Delegate all original methods
        @Override public void addBook() { bookManager.addBook(); }
        @Override public void viewBooks() { bookManager.viewBooks(); }
        @Override public void searchBook() { bookManager.searchBook(); }
        @Override public void updateBook() { bookManager.updateBook(); }
        @Override public void deleteBook() { bookManager.deleteBook(); }
        @Override public void viewCategories() { bookManager.viewCategories(); }
        
        // New functionality without modifying original code
        public void generateBookReport() {
            System.out.println("\n--- Book Report ---");
            System.out.println("Total books: " + books.size());
            
            Map<String, Integer> categoryCounts = new HashMap<>();
            for (Book book : books) {
                categoryCounts.put(book.category, categoryCounts.getOrDefault(book.category, 0) + 1);
            }
            
            System.out.println("\nBooks by category:");
            categoryCounts.forEach((category, count) -> 
                System.out.println(category + ": " + count + " books"));
        }
    }
    
    // Extended User Manager with premium user functionality
    static class PremiumUserManager implements UserManager {
        private UserManager userManager;
        
        public PremiumUserManager(UserManager userManager) {
            this.userManager = userManager;
        }
        
        // Delegate all original methods
        @Override public void createUserAccount() { userManager.createUserAccount(); }
        @Override public void userLogin() { userManager.userLogin(); }
        @Override public void viewUsers() { userManager.viewUsers(); }
        @Override public void calculateFine() { userManager.calculateFine(); }
        
        // New functionality without modifying original code
        public void createPremiumAccount() {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            int id = users.size() + 1;
            users.add(new PremiumUser(id, username, password, "PREMIUM", 0.0));
            System.out.println("Premium account created. ID: " + id);
        }
    }
    
    // Premium User type
    static class PremiumUser extends User {
        int extraBorrowDays = 14; // Premium users get extra 2 weeks
        
        PremiumUser(int id, String name, String password, String role, double fines) {
            super(id, name, password, role, fines);
        }
        
        @Override
        public String toString() {
            return "ID: " + id + ", Name: " + name + ", Role: " + role + " (Premium)";
        }
    }
    
    // Extended Issue Manager with reservation functionality
    static class ReservationIssueManager implements IssueManager {
        private IssueManager issueManager;
        
        public ReservationIssueManager(IssueManager issueManager) {
            this.issueManager = issueManager;
        }
        
        // Delegate all original methods
        @Override public void issueBook() { issueManager.issueBook(); }
        @Override public void returnBook() { issueManager.returnBook(); }
        @Override public void viewIssuedBooks() { issueManager.viewIssuedBooks(); }
        @Override public void viewOverdueBooks() { issueManager.viewOverdueBooks(); }
        
        // New functionality without modifying original code
        public void reserveBook() {
            System.out.print("Enter book ID to reserve: ");
            int bookId = scanner.nextInt();
            System.out.print("Enter your user ID: ");
            int userId = scanner.nextInt();
            scanner.nextLine();
            
            Book book = books.stream().filter(b -> b.id == bookId).findFirst().orElse(null);
            User user = users.stream().filter(u -> u.id == userId).findFirst().orElse(null);
            
            if (book == null || user == null) {
                System.out.println("Invalid ID(s).");
                return;
            }
            
            if (!book.status.equals("AVAILABLE")) {
                reservations.computeIfAbsent(bookId, k -> new LinkedList<>()).add(userId);
                System.out.println("Book reserved. You're in position " + reservations.get(bookId).size());
            } else {
                System.out.println("Book is available. You can issue it now.");
            }
        }
        
        public void viewReservations() {
            System.out.println("\n--- Current Reservations ---");
            if (reservations.isEmpty()) {
                System.out.println("No reservations found.");
                return;
            }
            
            for (var entry : reservations.entrySet()) {
                Book book = books.get(entry.getKey() - 1);
                Queue<Integer> queue = entry.getValue();
                System.out.println(book.title + " has " + queue.size() + " reservation(s):");
                int position = 1;
                for (Integer userId : queue) {
                    User user = users.get(userId - 1);
                    System.out.println("  " + position + ". " + user.name);
                    position++;
                }
            }
        }
    }
}
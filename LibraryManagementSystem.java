package Liskow;

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
        BookAdder bookAdder = new DefaultBookManager();
        BookViewer bookViewer = (BookViewer) bookAdder;
        BookSearcher bookSearcher = (BookSearcher) bookAdder;
        BookEditor bookEditor = (BookEditor) bookAdder;

        AccountCreator accountCreator = new DefaultUserManager();
        LoginService loginService = (LoginService) accountCreator;
        UserViewer userViewer = (UserViewer) accountCreator;
        FineCalculator fineCalculator = (FineCalculator) accountCreator;

        BookIssuer bookIssuer = new DefaultIssueManager();
        BookReturner bookReturner = (BookReturner) bookIssuer;
        IssuedBookViewer issuedBookViewer = (IssuedBookViewer) bookIssuer;

        AdminManager adminManager = new DefaultAdminManager(
                bookAdder, bookViewer, bookSearcher, bookEditor,
                accountCreator, loginService, userViewer, fineCalculator,
                bookIssuer, bookReturner, issuedBookViewer
        );

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
                case 2 -> accountCreator.createUserAccount();
                case 3 -> loginService.userLogin();
                case 0 -> System.out.println("Exiting...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    // --- Interface Segregation Applied ---

    interface BookAdder {
        void addBook();
    }

    interface BookViewer {
        void viewBooks();
        void viewCategories();
    }

    interface BookSearcher {
        void searchBook();
    }

    interface BookEditor {
        void updateBook();
        void deleteBook();
    }

    interface AccountCreator {
        void createUserAccount();
    }

    interface LoginService {
        void userLogin();
    }

    interface UserViewer {
        void viewUsers();
    }

    interface FineCalculator {
        void calculateFine();
    }

    interface BookIssuer {
        void issueBook();
    }

    interface BookReturner {
        void returnBook();
    }

    interface IssuedBookViewer {
        void viewIssuedBooks();
        void viewOverdueBooks();
    }

    interface AdminManager {
        void adminMenu();
        boolean adminLogin();
    }

    // --- Implementations ---

    static class DefaultBookManager implements BookAdder, BookViewer, BookSearcher, BookEditor {
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

    static class DefaultUserManager implements AccountCreator, LoginService, UserViewer, FineCalculator {
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

    static class DefaultIssueManager implements BookIssuer, BookReturner, IssuedBookViewer {
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

    static class DefaultAdminManager implements AdminManager {
        BookAdder bookAdder;
        BookViewer bookViewer;
        BookSearcher bookSearcher;
        BookEditor bookEditor;
        AccountCreator accountCreator;
        LoginService loginService;
        UserViewer userViewer;
        FineCalculator fineCalculator;
        BookIssuer bookIssuer;
        BookReturner bookReturner;
        IssuedBookViewer issuedBookViewer;

        DefaultAdminManager(
                BookAdder ba, BookViewer bv, BookSearcher bs, BookEditor be,
                AccountCreator ac, LoginService ls, UserViewer uv, FineCalculator fc,
                BookIssuer bi, BookReturner br, IssuedBookViewer ibv
        ) {
            this.bookAdder = ba;
            this.bookViewer = bv;
            this.bookSearcher = bs;
            this.bookEditor = be;
            this.accountCreator = ac;
            this.loginService = ls;
            this.userViewer = uv;
            this.fineCalculator = fc;
            this.bookIssuer = bi;
            this.bookReturner = br;
            this.issuedBookViewer = ibv;
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
                    case 1 -> bookAdder.addBook();
                    case 2 -> bookViewer.viewBooks();
                    case 3 -> bookSearcher.searchBook();
                    case 4 -> bookEditor.updateBook();
                    case 5 -> bookEditor.deleteBook();
                    case 6 -> userViewer.viewUsers();
                    case 7 -> bookIssuer.issueBook();
                    case 8 -> bookReturner.returnBook();
                    case 9 -> issuedBookViewer.viewIssuedBooks();
                    case 10 -> fineCalculator.calculateFine();
                    case 11 -> bookViewer.viewCategories();
                    case 12 -> issuedBookViewer.viewOverdueBooks();
                    case 0 -> System.out.println("Logging out...");
                    default -> System.out.println("Invalid choice.");
                }
            } while (choice != 0);
        }

        @Override
        public boolean adminLogin() {
            System.out.print("Enter admin password: ");
            String password = scanner.nextLine();
            return password.equals("admin123");
        }
    }

    // --- Data Classes ---

    static class Book {
        int id;
        String title, author, category, status = "AVAILABLE";

        Book(int id, String title, String author, String category) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.category = category;
        }

        @Override
        public String toString() {
            return "Book{" + "id=" + id + ", title='" + title + "', author='" + author + "', category='" + category + "'}";
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

        @Override
        public String toString() {
            return "User{" + "id=" + id + ", name='" + name + "', role='" + role + "'}";
        }
    }
}

import java.util.*;
import java.io.*;

// --- Main Class ---
public class LibraryManagementSystem {
    static Scanner scanner = new Scanner(System.in);
    static BookManager bookManager = new BookManager();
    static UserManager userManager = new UserManager();
    static IssueManager issueManager = new IssueManager(bookManager, userManager);
    static DataManager dataManager = new DataManager(bookManager, userManager, issueManager);

    public static void main(String[] args) {
        dataManager.loadFromFiles();

        int choice;
        do {
            System.out.println("\n--- Welcome to Library System ---");
            System.out.println("1. Login as Admin");
            System.out.println("2. Create User Account");
            System.out.println("3. Login as User");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> adminMenu();
                case 2 -> userManager.createUserAccount(scanner);
                case 3 -> userManager.userLogin(scanner, bookManager);
                case 0 -> dataManager.saveToFiles();
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    static void adminMenu() {
        if (!userManager.adminLogin(scanner)) return;

        int choice;
        do {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add Book");
            System.out.println("2. View Books");
            System.out.println("3. Search Book");
            System.out.println("4. Update Book");
            System.out.println("5. Delete Book");
            System.out.println("6. View Users");
            System.out.println("7. Issue Book");
            System.out.println("8. Return Book");
            System.out.println("9. View Issued Books");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> bookManager.addBook(scanner);
                case 2 -> bookManager.viewBooks();
                case 3 -> bookManager.searchBook(scanner);
                case 4 -> bookManager.updateBook(scanner);
                case 5 -> bookManager.deleteBook(scanner);
                case 6 -> userManager.viewUsers();
                case 7 -> issueManager.issueBook(scanner);
                case 8 -> issueManager.returnBook(scanner);
                case 9 -> issueManager.viewIssuedBooks();
                case 0 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }
}

// --- Book Class ---
class Book implements Serializable {
    int id;
    String title;
    String author;

    Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
    }

    public String toString() {
        return "Book ID: " + id + ", Title: " + title + ", Author: " + author;
    }
}

// --- User Class ---
class User implements Serializable {
    int id;
    String name;
    String password;

    User(int id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public String toString() {
        return "User ID: " + id + ", Name: " + name;
    }
}

// --- BookManager: Handles all book-related responsibilities (SRP) ---
class BookManager {
    List<Book> books = new ArrayList<>();

    void addBook(Scanner scanner) {
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        System.out.print("Enter author: ");
        String author = scanner.nextLine();
        int id = books.size() + 1;
        books.add(new Book(id, title, author));
        System.out.println("Book added.");
    }

    void viewBooks() {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        books.forEach(System.out::println);
    }

    void searchBook(Scanner scanner) {
        System.out.print("Enter title or author to search: ");
        String keyword = scanner.nextLine().toLowerCase();
        books.stream()
            .filter(b -> b.title.toLowerCase().contains(keyword) || b.author.toLowerCase().contains(keyword))
            .forEach(System.out::println);
    }

    void updateBook(Scanner scanner) {
        System.out.print("Enter book ID to update: ");
        int id = scanner.nextInt(); scanner.nextLine();
        for (Book b : books) {
            if (b.id == id) {
                System.out.print("Enter new title: ");
                b.title = scanner.nextLine();
                System.out.print("Enter new author: ");
                b.author = scanner.nextLine();
                System.out.println("Book updated.");
                return;
            }
        }
        System.out.println("Book not found.");
    }

    void deleteBook(Scanner scanner) {
        System.out.print("Enter book ID to delete: ");
        int id = scanner.nextInt(); scanner.nextLine();
        books.removeIf(b -> b.id == id);
        System.out.println("Book deleted.");
    }
}

// --- UserManager: Handles user accounts and login (SRP) ---
class UserManager {
    List<User> users = new ArrayList<>();
    User currentUser = null;

    void createUserAccount(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        int id = users.size() + 1;
        users.add(new User(id, username, password));
        System.out.println("User account created. ID: " + id);
    }

    void userLogin(Scanner scanner, BookManager bookManager) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        for (User u : users) {
            if (u.name.equals(username) && u.password.equals(password)) {
                currentUser = u;
                System.out.println("Login successful. Welcome, " + u.name + "!");
                viewUserMenu(scanner, bookManager);
                return;
            }
        }
        System.out.println("Invalid credentials.");
    }

    void viewUserMenu(Scanner scanner, BookManager bookManager) {
        int choice;
        do {
            System.out.println("\n--- User Menu ---");
            System.out.println("1. View Available Books");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> bookManager.viewBooks();
                case 0 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    boolean adminLogin(Scanner scanner) {
        System.out.print("Enter admin username: ");
        String username = scanner.nextLine();
        System.out.print("Enter admin password: ");
        String password = scanner.nextLine();
        return username.equals("admin") && password.equals("1234");
    }

    void viewUsers() {
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        users.forEach(System.out::println);
    }
}

// --- IssueManager: Handles book issuing/returning (SRP) ---
class IssueManager {
    Map<Integer, Integer> issuedBooks = new HashMap<>();
    BookManager bookManager;
    UserManager userManager;

    IssueManager(BookManager bookManager, UserManager userManager) {
        this.bookManager = bookManager;
        this.userManager = userManager;
    }

    void issueBook(Scanner scanner) {
        System.out.print("Enter book ID: ");
        int bookId = scanner.nextInt();
        System.out.print("Enter user ID: ");
        int userId = scanner.nextInt();
        scanner.nextLine();
        if (issuedBooks.containsKey(bookId)) {
            System.out.println("Book already issued.");
        } else {
            issuedBooks.put(bookId, userId);
            System.out.println("Book issued.");
        }
    }

    void returnBook(Scanner scanner) {
        System.out.print("Enter book ID to return: ");
        int bookId = scanner.nextInt();
        scanner.nextLine();
        if (issuedBooks.remove(bookId) != null) {
            System.out.println("Book returned.");
        } else {
            System.out.println("Book was not issued.");
        }
    }

    void viewIssuedBooks() {
        if (issuedBooks.isEmpty()) {
            System.out.println("No books issued.");
            return;
        }
        issuedBooks.forEach((bookId, userId) -> {
            Book book = bookManager.books.stream().filter(b -> b.id == bookId).findFirst().orElse(null);
            User user = userManager.users.stream().filter(u -> u.id == userId).findFirst().orElse(null);
            if (book != null && user != null) {
                System.out.println("Book: " + book.title + " | Issued to: " + user.name);
            }
        });
    }
}

// --- DataManager: Handles loading/saving from files (SRP) ---
class DataManager {
    BookManager bookManager;
    UserManager userManager;
    IssueManager issueManager;

    DataManager(BookManager bookManager, UserManager userManager, IssueManager issueManager) {
        this.bookManager = bookManager;
        this.userManager = userManager;
        this.issueManager = issueManager;
    }

    void saveToFiles() {
        try (ObjectOutputStream out1 = new ObjectOutputStream(new FileOutputStream("books.dat"));
             ObjectOutputStream out2 = new ObjectOutputStream(new FileOutputStream("users.dat"));
             ObjectOutputStream out3 = new ObjectOutputStream(new FileOutputStream("issued.dat"))) {
            out1.writeObject(bookManager.books);
            out2.writeObject(userManager.users);
            out3.writeObject(issueManager.issuedBooks);
        } catch (IOException e) {
            System.out.println("Error saving data.");
        }
    }

    void loadFromFiles() {
        try (ObjectInputStream in1 = new ObjectInputStream(new FileInputStream("books.dat"));
             ObjectInputStream in2 = new ObjectInputStream(new FileInputStream("users.dat"));
             ObjectInputStream in3 = new ObjectInputStream(new FileInputStream("issued.dat"))) {
            bookManager.books = (List<Book>) in1.readObject();
            userManager.users = (List<User>) in2.readObject();
            issueManager.issuedBooks = (Map<Integer, Integer>) in3.readObject();
        } catch (Exception e) {
        }
    }
}

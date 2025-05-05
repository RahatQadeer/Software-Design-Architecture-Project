import java.util.*;
import java.io.*;

public class LibraryManagementSystem {
    static Scanner scanner = new Scanner(System.in);
    static List<Book> books = new ArrayList<>();
    static List<User> users = new ArrayList<>();
    static Map<Integer, Integer> issuedBooks = new HashMap<>(); // bookId -> userId
    static User currentUser = null;

    public static void main(String[] args) {
        loadFromFiles();
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
                case 2 -> createUserAccount();
                case 3 -> userLogin();
                case 0 -> saveToFiles();
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    static void adminMenu() {
        if (!adminLogin()) return;
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
                case 1 -> addBook();
                case 2 -> viewBooks();
                case 3 -> searchBook();
                case 4 -> updateBook();
                case 5 -> deleteBook();
                case 6 -> viewUsers();
                case 7 -> issueBook();
                case 8 -> returnBook();
                case 9 -> viewIssuedBooks();
                case 0 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    static void userMenu() {
        int choice;
        do {
            System.out.println("\n--- User Menu ---");
            System.out.println("1. View Available Books");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> viewBooks();
                case 0 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    static boolean adminLogin() {
        System.out.print("Enter admin username: ");
        String username = scanner.nextLine();
        System.out.print("Enter admin password: ");
        String password = scanner.nextLine();
        return username.equals("admin") && password.equals("1234");
    }

    static void createUserAccount() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        int id = users.size() + 1;
        users.add(new User(id, username, password));
        System.out.println("User account created successfully. Your user ID is: " + id);
    }

    static void userLogin() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        for (User u : users) {
            if (u.name.equals(username) && u.password.equals(password)) {
                currentUser = u;
                System.out.println("Login successful. Welcome, " + u.name + "!");
                userMenu();
                return;
            }
        }
        System.out.println("Invalid credentials.");
    }

    static void addBook() {
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        System.out.print("Enter author: ");
        String author = scanner.nextLine();
        int id = books.size() + 1;
        books.add(new Book(id, title, author));
        System.out.println("Book added.");
    }

    static void viewBooks() {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        books.forEach(System.out::println);
    }

    static void searchBook() {
        System.out.print("Enter title or author to search: ");
        String keyword = scanner.nextLine().toLowerCase();
        books.stream()
             .filter(b -> b.title.toLowerCase().contains(keyword) || b.author.toLowerCase().contains(keyword))
             .forEach(System.out::println);
    }

    static void updateBook() {
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

    static void deleteBook() {
        System.out.print("Enter book ID to delete: ");
        int id = scanner.nextInt(); scanner.nextLine();
        books.removeIf(b -> b.id == id);
        issuedBooks.remove(id);
        System.out.println("Book deleted.");
    }

    static void viewUsers() {
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        users.forEach(System.out::println);
    }

    static void issueBook() {
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

    static void returnBook() {
        System.out.print("Enter book ID to return: ");
        int bookId = scanner.nextInt();
        scanner.nextLine();
        if (issuedBooks.remove(bookId) != null) {
            System.out.println("Book returned.");
        } else {
            System.out.println("Book was not issued.");
        }
    }

    static void viewIssuedBooks() {
        if (issuedBooks.isEmpty()) {
            System.out.println("No books issued.");
            return;
        }
        issuedBooks.forEach((bookId, userId) -> {
            Book book = books.stream().filter(b -> b.id == bookId).findFirst().orElse(null);
            User user = users.stream().filter(u -> u.id == userId).findFirst().orElse(null);
            if (book != null && user != null) {
                System.out.println("Book: " + book.title + " | Issued to: " + user.name);
            }
        });
    }

    static void saveToFiles() {
        try (ObjectOutputStream out1 = new ObjectOutputStream(new FileOutputStream("books.dat"));
             ObjectOutputStream out2 = new ObjectOutputStream(new FileOutputStream("users.dat"));
             ObjectOutputStream out3 = new ObjectOutputStream(new FileOutputStream("issued.dat"))) {
            out1.writeObject(books);
            out2.writeObject(users);
            out3.writeObject(issuedBooks);
        } catch (IOException e) {
            System.out.println("Error saving data.");
        }
    }

    static void loadFromFiles() {
        try (ObjectInputStream in1 = new ObjectInputStream(new FileInputStream("books.dat"));
             ObjectInputStream in2 = new ObjectInputStream(new FileInputStream("users.dat"));
             ObjectInputStream in3 = new ObjectInputStream(new FileInputStream("issued.dat"))) {
            books = (List<Book>) in1.readObject();
            users = (List<User>) in2.readObject();
            issuedBooks = (Map<Integer, Integer>) in3.readObject();
        } catch (Exception e) {
            // ignore if files not found (first run)
        }
    }
}

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
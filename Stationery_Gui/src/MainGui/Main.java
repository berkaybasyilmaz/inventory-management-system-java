package MainGui;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) {
        // Initialize database
        initializeDatabase();

        JFrame mainFrame = new JFrame("Management System");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);

        JMenuBar menuBar = new JMenuBar();

        JMenu inventoryMenu = new JMenu("Inventory");
        JMenu supplierMenu = new JMenu("Suppliers");
        JMenu salesMenu = new JMenu("Sales");

        menuBar.add(inventoryMenu);
        menuBar.add(supplierMenu);
        menuBar.add(salesMenu);

        JMenuItem inventoryPage = new JMenuItem("Inventory Page");
        JMenuItem supplierPage = new JMenuItem("Supplier Page");
        JMenuItem salesPage = new JMenuItem("Sales Page");

        inventoryMenu.add(inventoryPage);
        supplierMenu.add(supplierPage);
        salesMenu.add(salesPage);

        mainFrame.setJMenuBar(menuBar);

        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        mainPanel.add(new InventoryPanel(), "Inventory");
        mainPanel.add(new SupplierPanel(), "Suppliers");
        mainPanel.add(new SalesPanel(), "Sales");

        inventoryPage.addActionListener(e -> cardLayout.show(mainPanel, "Inventory"));
        supplierPage.addActionListener(e -> cardLayout.show(mainPanel, "Suppliers"));
        salesPage.addActionListener(e -> cardLayout.show(mainPanel, "Sales"));

        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);
    }

    private static void initializeDatabase() {
        String url = "jdbc:sqlite:inventory_system.db";

        // SQL statements to create tables
        String createInventoryTable = "CREATE TABLE IF NOT EXISTS inventory (" +
                                       "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                       "item TEXT NOT NULL, " +
                                       "stock INTEGER NOT NULL);";

        String createSupplierTable = "CREATE TABLE IF NOT EXISTS suppliers (" +
                                      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                      "name TEXT NOT NULL, " +
                                      "contact TEXT NOT NULL);";

        String createSalesTable = "CREATE TABLE IF NOT EXISTS sales (" +
                                  "sale_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                  "item_id INTEGER NOT NULL, " +
                                  "id_name TEXT NOT NULL, " +
                                  "date TEXT NOT NULL, " +
                                  "quantity INTEGER NOT NULL, " +
                                  "FOREIGN KEY (item_id) REFERENCES inventory(id) ON DELETE CASCADE);";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
             
            // Enable WAL mode for better concurrency
            stmt.execute("PRAGMA journal_mode=WAL;");
            System.out.println("WAL mode enabled.");

            // Create tables
            stmt.execute(createInventoryTable);
            stmt.execute(createSupplierTable);
            stmt.execute(createSalesTable);

            System.out.println("Database and tables initialized.");
        } catch (SQLException e) {
            System.out.println("Database initialization failed: " + e.getMessage());
        }
    }
}
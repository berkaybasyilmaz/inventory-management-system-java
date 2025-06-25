package MainGui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class InventoryPanel extends JPanel {
    private Connection conn;
    private DefaultTableModel tableModel;
    private JTable inventoryTable;
    private String highlightedItem = null;

    public InventoryPanel() {
        String url = "jdbc:sqlite:inventory_system.db";
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Inventory Management", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        add(label, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Item", "Stock"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        inventoryTable = new JTable(tableModel);
        inventoryTable.setRowHeight(25);
        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String itemName = (String) table.getValueAt(row, 0);
                if (highlightedItem != null && highlightedItem.equalsIgnoreCase(itemName)) {
                    cell.setBackground(Color.YELLOW);
                } else {
                    cell.setBackground(Color.WHITE);
                }
                return cell;
            }
        });

        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel manageItemPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel itemLabel1 = new JLabel("Item:");
        JTextField itemTextField = new JTextField(20);
        JLabel itemLabel2 = new JLabel("Stock:");
        JTextField itemTextField2 = new JTextField(20);

        JButton addButton = new JButton("Add Item");
        JButton updateButton = new JButton("Update Stock");
        JButton deleteButton = new JButton("Delete Item");
        JButton showAllButton = new JButton("Show All Items");

        gbc.gridx = 0; gbc.gridy = 0;
        manageItemPanel.add(itemLabel1, gbc);
        gbc.gridx = 1;
        manageItemPanel.add(itemTextField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        manageItemPanel.add(itemLabel2, gbc);
        gbc.gridx = 1;
        manageItemPanel.add(itemTextField2, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        manageItemPanel.add(addButton, gbc);
        gbc.gridy = 3;
        manageItemPanel.add(updateButton, gbc);
        gbc.gridy = 4;
        manageItemPanel.add(deleteButton, gbc);
        gbc.gridy = 5;
        manageItemPanel.add(showAllButton, gbc);

        add(manageItemPanel, BorderLayout.EAST);

        // Action Listeners
        addButton.addActionListener(e -> {
            String newItem = itemTextField.getText().trim();
            String newStock = itemTextField2.getText().trim();
            if (!newItem.isEmpty() && !newStock.isEmpty()) {
                try {
                    String checkSql = "SELECT COUNT(*) FROM inventory WHERE LOWER(item) = LOWER(?);";
                    PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                    checkStmt.setString(1, newItem);
                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "This item already exists.", "Duplicate", JOptionPane.WARNING_MESSAGE);
                    } else {
                        String sql = "INSERT INTO inventory (item, stock) VALUES (?, ?);";
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, newItem);
                        pstmt.setInt(2, Integer.parseInt(newStock));
                        pstmt.executeUpdate();
                        highlightedItem = newItem;
                        JOptionPane.showMessageDialog(this, "Item added successfully!");
                        itemTextField.setText("");
                        itemTextField2.setText("");
                        showAllItems();
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Stock must be a number.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Fill all fields.");
            }
        });

        updateButton.addActionListener(e -> {
            String updateItem = itemTextField.getText().trim();
            String updateStock = itemTextField2.getText().trim();
            if (!updateItem.isEmpty() && !updateStock.isEmpty()) {
                try {
                    String sql = "UPDATE inventory SET stock = ? WHERE LOWER(TRIM(item)) = LOWER(TRIM(?));";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, Integer.parseInt(updateStock));
                    pstmt.setString(2, updateItem);
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        highlightedItem = updateItem;
                        JOptionPane.showMessageDialog(this, "Item updated.");
                        itemTextField.setText("");
                        itemTextField2.setText("");
                        showAllItems();
                    } else {
                        JOptionPane.showMessageDialog(this, "Item not found.");
                    }
                } catch (SQLException | NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Fill all fields.");
            }
        });

        deleteButton.addActionListener(e -> {
            String deleteItem = itemTextField.getText().trim();
            if (!deleteItem.isEmpty()) {
                try {
                    String sql = "DELETE FROM inventory WHERE LOWER(TRIM(item)) = LOWER(TRIM(?));";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, deleteItem);
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        highlightedItem = null;
                        JOptionPane.showMessageDialog(this, "Item deleted.");
                        itemTextField.setText("");
                        itemTextField2.setText("");
                        showAllItems();
                    } else {
                        JOptionPane.showMessageDialog(this, "Item not found.");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Enter an item.");
            }
        });

        showAllButton.addActionListener(e -> {
            highlightedItem = null;
            showAllItems();
        });

        showAllItems();
    }

    public void showAllItems() {
        try {
            String sql = "SELECT item, stock FROM inventory;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getString("item"), rs.getInt("stock")});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching items: " + ex.getMessage());
        }
    }
}
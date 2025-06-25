package MainGui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SalesPanel extends JPanel {
    private Connection conn;
    private DefaultTableModel tableModel;
    private JTable salesTable;
    private JComboBox<String> saleComboBox; // JComboBox for selecting item from inventory
    private String highlightedItem;

    public SalesPanel() {
        String url = "jdbc:sqlite:inventory_system.db";
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Sales Management", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        add(label, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Item", "Quantity", "Date"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        salesTable = new JTable(tableModel);
        salesTable.setRowHeight(25);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String itemName = (String) table.getValueAt(row, 0);
                int quantity = (int) table.getValueAt(row, 1);
                String date = (String) table.getValueAt(row, 2);

                if (highlightedItem != null && highlightedItem.equals(itemName + ":" + quantity + ":" + date)) {
                    cell.setBackground(Color.YELLOW);
                } else {
                    cell.setBackground(Color.WHITE);
                }
                return cell;
            }
        };

        for (int i = 0; i < salesTable.getColumnCount(); i++) {
            salesTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        add(new JScrollPane(salesTable), BorderLayout.CENTER);

        JPanel manageSalesPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel saleLabel = new JLabel("Item:");
        saleComboBox = new JComboBox<>();
        loadInventoryItems(); // Load inventory items into combo box

        JLabel saleLabel2 = new JLabel("Quantity:");
        JTextField saleTextField2 = new JTextField(20);

        JLabel saleLabel3 = new JLabel("Date:");
        JTextField saleTextField3 = new JTextField(20);

        JButton addButton = new JButton("Add Sale");
        JButton updateButton = new JButton("Update Sale");
        JButton deleteButton = new JButton("Delete Sale");
        JButton showAllSales = new JButton("Show All");

        gbc.gridx = 0; gbc.gridy = 0;
        manageSalesPanel.add(saleLabel, gbc);
        gbc.gridx = 1;
        manageSalesPanel.add(saleComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        manageSalesPanel.add(saleLabel2, gbc);
        gbc.gridx = 1;
        manageSalesPanel.add(saleTextField2, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        manageSalesPanel.add(saleLabel3, gbc);
        gbc.gridx = 1;
        manageSalesPanel.add(saleTextField3, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        manageSalesPanel.add(addButton, gbc);
        gbc.gridy = 4;
        manageSalesPanel.add(updateButton, gbc);
        gbc.gridy = 5;
        manageSalesPanel.add(deleteButton, gbc);
        gbc.gridy = 6;
        manageSalesPanel.add(showAllSales, gbc);

        add(manageSalesPanel, BorderLayout.EAST);

        // Add Sale
        addButton.addActionListener(e -> {
            String itemName = (String) saleComboBox.getSelectedItem();
            String quantityStr = saleTextField2.getText().trim();
            String date = saleTextField3.getText().trim();

            if (itemName != null && !quantityStr.isEmpty() && !date.isEmpty()) {
                try {
                    int quantity = Integer.parseInt(quantityStr);

                    String insertSql = "INSERT INTO sales (id_name, quantity, date) VALUES (?, ?, ?);";
                    PreparedStatement pstmt = conn.prepareStatement(insertSql);
                    pstmt.setString(1, itemName);
                    pstmt.setInt(2, quantity);
                    pstmt.setString(3, date);
                    pstmt.executeUpdate();

                    highlightedItem = itemName + ":" + quantity + ":" + date;

                    saleTextField2.setText("");
                    saleTextField3.setText("");

                    showAllSales();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error adding sale: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Quantity must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "All fields must be filled out.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Update Sale
        updateButton.addActionListener(e -> {
            String oldItemName = (String) saleComboBox.getSelectedItem();
            String oldQuantityStr = saleTextField2.getText().trim();
            String oldDate = saleTextField3.getText().trim();

            if (oldItemName == null || oldQuantityStr.isEmpty() || oldDate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill all fields to match a record.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int oldQuantity = Integer.parseInt(oldQuantityStr);

                String checkSql = "SELECT COUNT(*) FROM sales WHERE id_name = ? AND quantity = ? AND date = ?;";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, oldItemName);
                checkStmt.setInt(2, oldQuantity);
                checkStmt.setString(3, oldDate);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                if (count == 0) {
                    JOptionPane.showMessageDialog(this, "No matching sale record found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String newItemName = (String) JOptionPane.showInputDialog(this, "Enter new item name:", "Update Item", JOptionPane.PLAIN_MESSAGE, null, null, oldItemName);
                if (newItemName == null || newItemName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "New item name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String newQuantityStr = JOptionPane.showInputDialog(this, "Enter new quantity:", oldQuantity);
                if (newQuantityStr == null || newQuantityStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "New quantity cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String newDate = JOptionPane.showInputDialog(this, "Enter new date:", oldDate);
                if (newDate == null || newDate.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "New date cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int newQuantity = Integer.parseInt(newQuantityStr);

                String updateSql = "UPDATE sales SET id_name = ?, quantity = ?, date = ? WHERE id_name = ? AND quantity = ? AND date = ?;";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, newItemName);
                updateStmt.setInt(2, newQuantity);
                updateStmt.setString(3, newDate);
                updateStmt.setString(4, oldItemName);
                updateStmt.setInt(5, oldQuantity);
                updateStmt.setString(6, oldDate);

                int updatedRows = updateStmt.executeUpdate();
                if (updatedRows > 0) {
                    highlightedItem = newItemName + ":" + newQuantity + ":" + newDate;
                    JOptionPane.showMessageDialog(this, "Sale updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    saleTextField2.setText("");
                    saleTextField3.setText("");
                    showAllSales();
                } else {
                    JOptionPane.showMessageDialog(this, "No rows updated.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error updating sale: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Delete Sale
        deleteButton.addActionListener(e -> {
            String itemName = (String) saleComboBox.getSelectedItem();
            String quantityStr = saleTextField2.getText().trim();
            String date = saleTextField3.getText().trim();

            if (itemName == null || quantityStr.isEmpty() || date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill all fields to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityStr);

                String deleteSql = "DELETE FROM sales WHERE id_name = ? AND quantity = ? AND date = ?;";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                deleteStmt.setString(1, itemName);
                deleteStmt.setInt(2, quantity);
                deleteStmt.setString(3, date);

                int deletedRows = deleteStmt.executeUpdate();
                if (deletedRows > 0) {
                    highlightedItem = null;
                    JOptionPane.showMessageDialog(this, "Sale deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    saleTextField2.setText("");
                    saleTextField3.setText("");
                    showAllSales();
                } else {
                    JOptionPane.showMessageDialog(this, "No matching sale found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting sale: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        showAllSales.addActionListener(e -> {
            highlightedItem = null;
            showAllSales();
        });

        showAllSales();
    }

    private void loadInventoryItems() {
        try {
            String sql = "SELECT item FROM inventory ORDER BY item ASC;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            saleComboBox.removeAllItems();
            while (rs.next()) {
                saleComboBox.addItem(rs.getString("item"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading inventory items: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAllSales() {
        try {
            String sql = "SELECT id_name, quantity, date FROM sales;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("id_name"),
                        rs.getInt("quantity"),
                        rs.getString("date")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching sales: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

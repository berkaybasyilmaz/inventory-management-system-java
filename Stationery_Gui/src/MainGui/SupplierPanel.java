package MainGui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import javax.swing.Timer;

class SupplierPanel extends JPanel {
    private Connection conn;
    private JTable supplierTable;
    private DefaultTableModel tableModel;

    public SupplierPanel() {
        // Initialize database connection
        String url = "jdbc:sqlite:inventory_system.db";
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        // Header Label
        JLabel label = new JLabel("Supplier Management", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        add(label, BorderLayout.NORTH);

        // Table for displaying supplier details
        String[] columnNames = {"Supplier Name", "Selling"};
        tableModel = new DefaultTableModel(columnNames, 0);
        supplierTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(supplierTable);
        add(tableScrollPane, BorderLayout.CENTER);

        // South Panel for managing suppliers (East side in BorderLayout)
        JPanel manageSupplierPanel = new JPanel();
        manageSupplierPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel supplierLabel = new JLabel("Supplier:");
        JTextField supplierTextField = new JTextField(20);
        JLabel supplierLabel2 = new JLabel("Selling:");
        JTextField supplierTextField2 = new JTextField(20);
        JButton addButton = new JButton("Add Supplier");
        JButton updateButton = new JButton("Update Supplier");
        JButton deleteButton = new JButton("Delete Supplier");
        JButton showAllButton = new JButton("Show All");

        // Add components to manageSupplierPanel using GridBagConstraints
        gbc.gridx = 0;
        gbc.gridy = 0;
        manageSupplierPanel.add(supplierLabel, gbc);

        gbc.gridx = 1;
        manageSupplierPanel.add(supplierTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        manageSupplierPanel.add(supplierLabel2, gbc);

        gbc.gridx = 1;
        manageSupplierPanel.add(supplierTextField2, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        manageSupplierPanel.add(addButton, gbc);

        gbc.gridy = 3;
        manageSupplierPanel.add(updateButton, gbc);

        gbc.gridy = 4;
        manageSupplierPanel.add(deleteButton, gbc);

        gbc.gridy = 5;
        manageSupplierPanel.add(showAllButton, gbc);

        add(manageSupplierPanel, BorderLayout.EAST);

        // Add Supplier Button
        addButton.addActionListener(e -> {
            String supplier = supplierTextField.getText();
            String selling = supplierTextField2.getText();

            if (!supplier.isEmpty() && !selling.isEmpty()) {
                try {
                    String sql = "INSERT INTO suppliers (name, contact) VALUES (?, ?);";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, supplier);
                    pstmt.setString(2, selling);
                    pstmt.executeUpdate();
                    // Update table with new supplier and show all items
                    showAllSuppliers();
                    // Highlight the newly added row (last row in the table)
                    highlightRow(tableModel.getRowCount() - 1);
                    supplierTextField.setText("");
                    supplierTextField2.setText("");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error adding supplier: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "All fields must be filled out.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Update Supplier Button
        updateButton.addActionListener(e -> {
            String supplierName = supplierTextField.getText();
            String currentSelling = supplierTextField2.getText();

            if (!supplierName.isEmpty() && !currentSelling.isEmpty()) {
                String newSelling = JOptionPane.showInputDialog(this, "Enter the new item to update:");

                if (newSelling != null && !newSelling.isEmpty()) {
                    try {
                        String sql = "UPDATE suppliers SET contact = ? WHERE name = ? AND contact = ?;";
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, newSelling);
                        pstmt.setString(2, supplierName);
                        pstmt.setString(3, currentSelling);
                        int rowsUpdated = pstmt.executeUpdate();

                        if (rowsUpdated > 0) {
                            // Update the table view
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                if (tableModel.getValueAt(i, 0).equals(supplierName) &&
                                        tableModel.getValueAt(i, 1).equals(currentSelling)) {
                                    tableModel.setValueAt(newSelling, i, 1);
                                    // Highlight the updated row
                                    highlightRow(i);
                                    break;
                                }
                            }
                            supplierTextField.setText("");
                            supplierTextField2.setText("");
                        } else {
                            JOptionPane.showMessageDialog(this, "No matching supplier/item combination found.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error updating supplier: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "New selling item cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Both fields must be filled out to update a supplier.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Delete Supplier Button
        deleteButton.addActionListener(e -> {
            String supplierName = supplierTextField.getText();
            String selling = supplierTextField2.getText();

            if (!supplierName.isEmpty() && !selling.isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete " + selling + " sold by " + supplierName + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        String sql = "DELETE FROM suppliers WHERE name = ? AND contact = ?;";
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, supplierName);
                        pstmt.setString(2, selling);
                        int rowsDeleted = pstmt.executeUpdate();

                        if (rowsDeleted > 0) {
                            // Remove from the table
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                if (tableModel.getValueAt(i, 0).equals(supplierName) &&
                                        tableModel.getValueAt(i, 1).equals(selling)) {
                                    tableModel.removeRow(i);
                                    break;
                                }
                            }
                            supplierTextField.setText("");
                            supplierTextField2.setText("");
                        } else {
                            JOptionPane.showMessageDialog(this, "No matching supplier/item combination found.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error deleting supplier: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Both fields must be filled out to delete a supplier.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Show All Suppliers Button
        showAllButton.addActionListener(e -> showAllSuppliers());

        setPreferredSize(new Dimension(1200, 900));
    }

    private void highlightRow(int rowIndex) {
        // Set a fixed highlight color for the selected row
        supplierTable.setRowSelectionInterval(rowIndex, rowIndex);
        supplierTable.setSelectionBackground(Color.YELLOW);
    }

    private void showAllSuppliers() {
        try {
            String sql = "SELECT name, contact FROM suppliers;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            // Clear the table before adding updated data
            tableModel.setRowCount(0);
               
            while (rs.next()) {
                String name = rs.getString("name");
                String contact = rs.getString("contact");
                tableModel.addRow(new Object[]{name, contact});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching suppliers: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


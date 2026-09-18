package com.tourist.ui.gui;

import com.tourist.engine.TripCostEstimator;
import com.tourist.model.Place;
import com.tourist.repository.WishlistRepository;
import com.tourist.util.FileStorageHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Wishlist management panel for reviewing saved dream destinations,
 * planning target trip seasons, adding custom notes, and exporting itineraries.
 */
public class WishlistPanel extends JPanel {

    private final Frame parentFrame;
    private final WishlistRepository wishlistRepo;
    private final TripCostEstimator costEstimator;

    private JTable wishlistTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private List<WishlistRepository.WishlistItem> currentItems;

    public WishlistPanel(Frame parent, WishlistRepository wishlistRepo, TripCostEstimator costEstimator) {
        this.parentFrame = parent;
        this.wishlistRepo = wishlistRepo;
        this.costEstimator = costEstimator;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 225, 235), 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel title = new JLabel("⭐ My Travel Wishlist & Trip Planner");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(24, 43, 73));
        header.add(title, BorderLayout.NORTH);

        statusLabel = new JLabel("Keep track of your dream vacation spots and export your customized itinerary.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(90, 100, 115));
        header.add(statusLabel, BorderLayout.SOUTH);

        return header;
    }

    private JPanel buildTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new LineBorder(new Color(220, 225, 235), 1, true));

        String[] cols = {"Destination", "Location", "Theme", "Est. Daily ($)", "Target Travel Time", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        wishlistTable = new JTable(tableModel);
        wishlistTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        wishlistTable.setRowHeight(28);
        wishlistTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wishlistTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        wishlistTable.getTableHeader().setBackground(new Color(240, 243, 248));

        wishlistTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        wishlistTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        wishlistTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        wishlistTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        wishlistTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        wishlistTable.getColumnModel().getColumn(5).setPreferredWidth(220);

        JScrollPane scroll = new JScrollPane(wishlistTable);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bar.setBackground(new Color(245, 247, 250));

        JButton btnOpen = new JButton("📖 View Dossier");
        btnOpen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOpen.addActionListener(e -> openSelectedDossier());

        JButton btnEditNotes = new JButton("✏ Edit Notes");
        btnEditNotes.addActionListener(e -> editSelectedNotes());

        JButton btnRemove = new JButton("🗑 Remove");
        btnRemove.addActionListener(e -> removeSelected());

        JButton btnExport = new JButton("📤 Export Itinerary to File");
        btnExport.setBackground(new Color(25, 118, 210));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExport.addActionListener(e -> exportWishlist());

        bar.add(btnRemove);
        bar.add(btnEditNotes);
        bar.add(btnOpen);
        bar.add(btnExport);

        return bar;
    }

    private WishlistRepository.WishlistItem getSelectedItem() {
        int row = wishlistTable.getSelectedRow();
        if (row != -1 && currentItems != null && row < currentItems.size()) {
            return currentItems.get(row);
        }
        return null;
    }

    private void openSelectedDossier() {
        WishlistRepository.WishlistItem item = getSelectedItem();
        if (item != null) {
            PlaceDetailDialog dlg = new PlaceDetailDialog(parentFrame, item.getPlace(), wishlistRepo, costEstimator);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item from your wishlist first.", "Selection Needed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void editSelectedNotes() {
        WishlistRepository.WishlistItem item = getSelectedItem();
        if (item != null) {
            String newNotes = JOptionPane.showInputDialog(this, "Update personal trip notes for " + item.getPlace().getName() + ":", item.getNotes());
            if (newNotes != null) {
                item.setNotes(newNotes);
                String newSeason = JOptionPane.showInputDialog(this, "Target travel season or month:", item.getTargetSeasonOrMonth());
                if (newSeason != null && !newSeason.trim().isEmpty()) {
                    item.setTargetSeasonOrMonth(newSeason.trim());
                }
                refreshTable();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to edit.", "Selection Needed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removeSelected() {
        WishlistRepository.WishlistItem item = getSelectedItem();
        if (item != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Remove " + item.getPlace().getName() + " from your wishlist?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                wishlistRepo.remove(item.getPlace().getId());
                refreshTable();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.", "Selection Needed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void exportWishlist() {
        if (wishlistRepo.size() == 0) {
            JOptionPane.showMessageDialog(this, "Your wishlist is empty. Add places before exporting.", "Empty Wishlist", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("My_Travel_Wishlist.txt"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try {
                String summary = wishlistRepo.exportSummary();
                FileStorageHelper.writeAllLines(target, Collections.singletonList(summary));
                JOptionPane.showMessageDialog(this, "Wishlist and itinerary successfully exported to:\n" + target.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshTable() {
        currentItems = wishlistRepo.getAll();
        tableModel.setRowCount(0);

        for (WishlistRepository.WishlistItem item : currentItems) {
            Place p = item.getPlace();
            tableModel.addRow(new Object[]{
                    p.getName(),
                    p.getFullLocation(),
                    p.getPrimaryCategory().getDisplayName(),
                    String.format("$%.0f", p.getEstimatedDailyCostUSD()),
                    item.getTargetSeasonOrMonth(),
                    item.getNotes()
            });
        }

        statusLabel.setText(String.format("You have %d saved destination%s in your travel wishlist.",
                currentItems.size(), currentItems.size() == 1 ? "" : "s"));
    }
}

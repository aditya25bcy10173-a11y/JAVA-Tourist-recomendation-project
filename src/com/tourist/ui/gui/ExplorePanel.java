package com.tourist.ui.gui;

import com.tourist.engine.TripCostEstimator;
import com.tourist.model.BudgetTier;
import com.tourist.model.Category;
import com.tourist.model.Place;
import com.tourist.repository.PlaceRepository;
import com.tourist.repository.WishlistRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;

/**
 * Explore panel allowing travelers to browse all available destinations,
 * filter by theme and budget, search keywords, sort, and inspect dossiers.
 */
public class ExplorePanel extends JPanel {

    private final Frame parentFrame;
    private final PlaceRepository placeRepo;
    private final WishlistRepository wishlistRepo;
    private final TripCostEstimator costEstimator;

    private JTextField txtSearch;
    private JComboBox<String> comboCategory;
    private JComboBox<String> comboBudget;
    private JComboBox<String> comboSort;
    private JTable placesTable;
    private DefaultTableModel tableModel;
    private JLabel countLabel;

    private List<Place> currentDisplayedPlaces;

    public ExplorePanel(Frame parent, PlaceRepository placeRepo, WishlistRepository wishlistRepo, TripCostEstimator costEstimator) {
        this.parentFrame = parent;
        this.placeRepo = placeRepo;
        this.wishlistRepo = wishlistRepo;
        this.costEstimator = costEstimator;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTableView(), BorderLayout.CENTER);
        add(buildBottomActionBar(), BorderLayout.SOUTH);

        refreshTableData();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 10));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 225, 235), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel filtersRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        filtersRow.setBackground(Color.WHITE);

        // Search text
        filtersRow.add(new JLabel("🔍 Search:"));
        txtSearch = new JTextField(15);
        txtSearch.setToolTipText("Search place name, country, sight, or keyword");
        txtSearch.addActionListener(e -> refreshTableData());
        filtersRow.add(txtSearch);

        // Category filter
        filtersRow.add(new JLabel("Theme:"));
        comboCategory = new JComboBox<>();
        comboCategory.addItem("All Categories");
        for (Category c : Category.values()) comboCategory.addItem(c.getDisplayName());
        comboCategory.addActionListener(e -> refreshTableData());
        filtersRow.add(comboCategory);

        // Budget filter
        filtersRow.add(new JLabel("Budget:"));
        comboBudget = new JComboBox<>();
        comboBudget.addItem("All Budgets");
        for (BudgetTier b : BudgetTier.values()) comboBudget.addItem(b.getLabel());
        comboBudget.addActionListener(e -> refreshTableData());
        filtersRow.add(comboBudget);

        // Sort option
        filtersRow.add(new JLabel("Sort By:"));
        comboSort = new JComboBox<>(new String[]{"Rating (High to Low)", "Cost (Low to High)", "Cost (High to Low)", "Name (A-Z)"});
        comboSort.addActionListener(e -> refreshTableData());
        filtersRow.add(comboSort);

        JButton btnFilter = new JButton("Apply");
        btnFilter.setBackground(new Color(25, 118, 210));
        btnFilter.setForeground(Color.WHITE);
        btnFilter.addActionListener(e -> refreshTableData());
        filtersRow.add(btnFilter);

        JButton btnReset = new JButton("Clear");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            comboCategory.setSelectedIndex(0);
            comboBudget.setSelectedIndex(0);
            comboSort.setSelectedIndex(0);
            refreshTableData();
        });
        filtersRow.add(btnReset);

        bar.add(filtersRow, BorderLayout.CENTER);

        countLabel = new JLabel("Showing 0 destinations");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countLabel.setForeground(new Color(90, 100, 115));
        bar.add(countLabel, BorderLayout.SOUTH);

        return bar;
    }

    private JPanel buildTableView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new LineBorder(new Color(220, 225, 235), 1, true));

        String[] cols = {"ID", "Destination", "Country / Region", "Theme", "Budget Tier", "Daily Cost", "Duration", "Rating"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        placesTable = new JTable(tableModel);
        placesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        placesTable.setRowHeight(28);
        placesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        placesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        placesTable.getTableHeader().setBackground(new Color(240, 243, 248));

        // Column widths
        placesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        placesTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        placesTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        placesTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        placesTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        placesTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        placesTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        placesTable.getColumnModel().getColumn(7).setPreferredWidth(70);

        placesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && placesTable.getSelectedRow() != -1) {
                    openSelectedDossier();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(placesTable);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildBottomActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        bar.setBackground(new Color(245, 247, 250));

        JButton btnView = new JButton("📖 Open Destination Dossier");
        btnView.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnView.addActionListener(e -> openSelectedDossier());

        JButton btnWishlist = new JButton("⭐ Save to Wishlist");
        btnWishlist.addActionListener(e -> {
            Place p = getSelectedPlace();
            if (p != null) {
                if (!wishlistRepo.contains(p.getId())) {
                    wishlistRepo.add(p, "", "Flexible");
                    JOptionPane.showMessageDialog(this, p.getName() + " saved to your Wishlist!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, p.getName() + " is already in your Wishlist.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a destination from the table first.", "Selection Needed", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnAddPlace = new JButton("➕ Add New Destination");
        btnAddPlace.addActionListener(e -> {
            AddPlaceDialog dlg = new AddPlaceDialog(parentFrame, placeRepo);
            dlg.setVisible(true);
            refreshTableData();
        });

        bar.add(btnAddPlace);
        bar.add(btnWishlist);
        bar.add(btnView);
        return bar;
    }

    private Place getSelectedPlace() {
        int row = placesTable.getSelectedRow();
        if (row != -1 && currentDisplayedPlaces != null && row < currentDisplayedPlaces.size()) {
            return currentDisplayedPlaces.get(row);
        }
        return null;
    }

    private void openSelectedDossier() {
        Place p = getSelectedPlace();
        if (p != null) {
            PlaceDetailDialog dlg = new PlaceDetailDialog(parentFrame, p, wishlistRepo, costEstimator);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a destination from the table to view its dossier.", "Selection Needed", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void refreshTableData() {
        String query = txtSearch.getText().trim();
        List<Place> list = placeRepo.search(query);

        // Filter Category
        if (comboCategory.getSelectedIndex() > 0) {
            Category cat = Category.values()[comboCategory.getSelectedIndex() - 1];
            list.removeIf(p -> !p.matchesCategory(cat));
        }

        // Filter Budget
        if (comboBudget.getSelectedIndex() > 0) {
            BudgetTier tier = BudgetTier.values()[comboBudget.getSelectedIndex() - 1];
            list.removeIf(p -> p.getBudgetTier() != tier);
        }

        // Sort
        int sortIdx = comboSort.getSelectedIndex();
        switch (sortIdx) {
            case 0: // Rating High to Low
                list.sort(Comparator.comparingDouble(Place::getRating).reversed());
                break;
            case 1: // Cost Low to High
                list.sort(Comparator.comparingDouble(Place::getEstimatedDailyCostUSD));
                break;
            case 2: // Cost High to Low
                list.sort(Comparator.comparingDouble(Place::getEstimatedDailyCostUSD).reversed());
                break;
            case 3: // Name A-Z
                list.sort(Comparator.comparing(Place::getName));
                break;
        }

        this.currentDisplayedPlaces = list;
        tableModel.setRowCount(0);

        for (Place p : list) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getFullLocation(),
                    p.getPrimaryCategory().getDisplayName(),
                    p.getBudgetTier().getLabel(),
                    String.format("$%.0f", p.getEstimatedDailyCostUSD()),
                    p.getMinRecommendedDays() + " - " + p.getMaxRecommendedDays() + " d",
                    String.format("%.1f★", p.getRating())
            });
        }

        countLabel.setText(String.format("Showing %d destinations", list.size()));
    }
}

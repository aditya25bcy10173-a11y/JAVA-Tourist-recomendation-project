package com.tourist.ui.gui;

import com.tourist.engine.TripCostEstimator;
import com.tourist.model.BudgetTier;
import com.tourist.model.Place;
import com.tourist.model.TripCostEstimate;
import com.tourist.repository.PlaceRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * Interactive budget & expense estimator panel.
 */
public class CostEstimatorPanel extends JPanel {

    private final PlaceRepository placeRepo;
    private final TripCostEstimator costEstimator;

    private JComboBox<PlaceItem> comboPlaces;
    private JSpinner spinDays;
    private JSpinner spinTravelers;
    private JComboBox<String> comboComfort;

    private JLabel lblTotal;
    private JLabel lblPerPerson;
    private JLabel lblLodging;
    private JLabel lblFood;
    private JLabel lblActivities;
    private JLabel lblTransit;
    private JLabel lblMisc;
    private JTextArea txtNotes;

    private static class PlaceItem {
        private final Place place;
        public PlaceItem(Place place) { this.place = place; }
        public Place getPlace() { return place; }
        @Override
        public String toString() {
            return place.getName() + " (" + place.getFullLocation() + ") - " + place.getBudgetTier().getLabel();
        }
    }

    public CostEstimatorPanel(PlaceRepository placeRepo, TripCostEstimator costEstimator) {
        this.placeRepo = placeRepo;
        this.costEstimator = costEstimator;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        add(buildControlsCard(), BorderLayout.WEST);
        add(buildBreakdownCard(), BorderLayout.CENTER);

        calculateCost();
    }

    private JPanel buildControlsCard() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(340, 500));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 225, 235), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel title = new JLabel("💰 Trip Parameters");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(24, 43, 73));
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        // Destination Dropdown
        panel.add(createLabel("Choose Destination:"));
        comboPlaces = new JComboBox<>();
        List<Place> places = placeRepo.getAllPlaces();
        for (Place p : places) {
            comboPlaces.addItem(new PlaceItem(p));
        }
        comboPlaces.addActionListener(e -> {
            PlaceItem item = (PlaceItem) comboPlaces.getSelectedItem();
            if (item != null) {
                spinDays.setValue(item.getPlace().getMinRecommendedDays());
                comboComfort.setSelectedIndex(item.getPlace().getBudgetTier().getLevel() - 1);
            }
            calculateCost();
        });
        panel.add(comboPlaces);
        panel.add(Box.createVerticalStrut(12));

        // Number of Days
        panel.add(createLabel("Number of Days:"));
        spinDays = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        spinDays.addChangeListener(e -> calculateCost());
        panel.add(spinDays);
        panel.add(Box.createVerticalStrut(12));

        // Number of Travelers
        panel.add(createLabel("Number of Travelers:"));
        spinTravelers = new JSpinner(new SpinnerNumberModel(2, 1, 30, 1));
        spinTravelers.addChangeListener(e -> calculateCost());
        panel.add(spinTravelers);
        panel.add(Box.createVerticalStrut(12));

        // Comfort Tier
        panel.add(createLabel("Comfort & Travel Style:"));
        comboComfort = new JComboBox<>(new String[]{
                "Budget Friendly ($30-$70/day)",
                "Moderate Comfort ($70-$180/day)",
                "Luxury & Premium ($180+/day)"
        });
        comboComfort.setSelectedIndex(1); // Default to Moderate
        comboComfort.addActionListener(e -> calculateCost());
        panel.add(comboComfort);
        panel.add(Box.createVerticalStrut(20));

        JButton btnRecalc = new JButton("🔄 Recalculate");
        btnRecalc.setBackground(new Color(25, 118, 210));
        btnRecalc.setForeground(Color.WHITE);
        btnRecalc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRecalc.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRecalc.addActionListener(e -> calculateCost());
        panel.add(btnRecalc);

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildBreakdownCard() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 225, 235), 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        // Top Highlight Header
        JPanel topHeader = new JPanel(new GridLayout(1, 2, 15, 0));
        topHeader.setBackground(new Color(245, 248, 255));
        topHeader.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 220, 250), 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel totalBox = new JPanel(new BorderLayout());
        totalBox.setOpaque(false);
        JLabel lblTotalTitle = new JLabel("TOTAL ESTIMATED COST");
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTotalTitle.setForeground(new Color(70, 90, 120));
        lblTotal = new JLabel("$0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTotal.setForeground(new Color(20, 80, 180));
        totalBox.add(lblTotalTitle, BorderLayout.NORTH);
        totalBox.add(lblTotal, BorderLayout.CENTER);

        JPanel perPersonBox = new JPanel(new BorderLayout());
        perPersonBox.setOpaque(false);
        JLabel lblPerPersonTitle = new JLabel("PER TRAVELER");
        lblPerPersonTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPerPersonTitle.setForeground(new Color(70, 90, 120));
        lblPerPerson = new JLabel("$0.00");
        lblPerPerson.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblPerPerson.setForeground(new Color(25, 135, 60));
        perPersonBox.add(lblPerPersonTitle, BorderLayout.NORTH);
        perPersonBox.add(lblPerPerson, BorderLayout.CENTER);

        topHeader.add(totalBox);
        topHeader.add(perPersonBox);
        panel.add(topHeader, BorderLayout.NORTH);

        // Center Itemized Table / Rows
        JPanel rows = new JPanel(new GridLayout(5, 1, 8, 12));
        rows.setBackground(Color.WHITE);
        rows.setBorder(new EmptyBorder(15, 0, 15, 0));

        lblLodging = new JLabel();
        lblFood = new JLabel();
        lblActivities = new JLabel();
        lblTransit = new JLabel();
        lblMisc = new JLabel();

        rows.add(createItemRow("🏨 Accommodation & Lodging (Rooms & Stays)", lblLodging, new Color(40, 90, 180)));
        rows.add(createItemRow("🍽 Dining, Food & Beverages", lblFood, new Color(200, 100, 20)));
        rows.add(createItemRow("🎟 Activities, Sightseeing & Entry Tickets", lblActivities, new Color(130, 40, 160)));
        rows.add(createItemRow("🚕 Local Transit (Taxis, Shuttles, Metro)", lblTransit, new Color(30, 140, 100)));
        rows.add(createItemRow("🛡 Miscellaneous & Contingency Buffer (8%)", lblMisc, new Color(100, 110, 120)));

        panel.add(rows, BorderLayout.CENTER);

        // Bottom Notes
        txtNotes = new JTextArea();
        txtNotes.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        txtNotes.setForeground(new Color(110, 120, 135));
        txtNotes.setEditable(false);
        txtNotes.setBackground(Color.WHITE);
        txtNotes.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 240)));
        panel.add(txtNotes, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createItemRow(String label, JLabel valueLabel, Color barColor) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(new Color(250, 252, 255));
        row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(235, 240, 248), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel titleLbl = new JLabel(label);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLbl.setForeground(new Color(40, 50, 65));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(barColor);

        row.add(titleLbl, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(70, 80, 95));
        lbl.setBorder(new EmptyBorder(2, 0, 2, 0));
        return lbl;
    }

    private void calculateCost() {
        if (comboPlaces == null || comboPlaces.getSelectedItem() == null) return;
        PlaceItem item = (PlaceItem) comboPlaces.getSelectedItem();
        Place place = item.getPlace();

        int days = (Integer) spinDays.getValue();
        int travelers = (Integer) spinTravelers.getValue();
        BudgetTier tier = BudgetTier.values()[comboComfort.getSelectedIndex()];

        TripCostEstimate est = costEstimator.estimate(place, days, travelers, tier);

        lblTotal.setText(String.format("$%,.2f", est.getTotalEstimatedUSD()));
        lblPerPerson.setText(String.format("$%,.2f", est.getPerPersonCostUSD()));

        lblLodging.setText(String.format("$%,.2f", est.getLodgingCost()));
        lblFood.setText(String.format("$%,.2f", est.getFoodCost()));
        lblActivities.setText(String.format("$%,.2f", est.getActivitiesCost()));
        lblTransit.setText(String.format("$%,.2f", est.getLocalTransitCost()));
        lblMisc.setText(String.format("$%,.2f", est.getMiscellaneousCost()));

        int rooms = (int) Math.ceil(travelers / 2.0);
        txtNotes.setText(String.format("Calculated for %d traveler%s sharing %d room%s for %d days in %s under %s comfort profile.\n* Does not include long-haul international flight tickets.",
                travelers, travelers > 1 ? "s" : "", rooms, rooms > 1 ? "s" : "", days, place.getName(), tier.getLabel()));
    }
}

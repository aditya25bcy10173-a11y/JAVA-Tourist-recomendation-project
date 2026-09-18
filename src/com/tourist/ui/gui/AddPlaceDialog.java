package com.tourist.ui.gui;

import com.tourist.model.BudgetTier;
import com.tourist.model.Category;
import com.tourist.model.CompanionType;
import com.tourist.model.Place;
import com.tourist.model.Season;
import com.tourist.repository.PlaceRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

/**
 * Dialog form for adding custom tourist destinations dynamically.
 */
public class AddPlaceDialog extends JDialog {

    private final PlaceRepository placeRepo;

    private JTextField txtName;
    private JTextField txtCountry;
    private JTextField txtRegion;
    private JComboBox<Category> comboCategory;
    private JComboBox<BudgetTier> comboBudget;
    private JSpinner spinDailyCost;
    private JSpinner spinRating;
    private JSpinner spinMinDays;
    private JSpinner spinMaxDays;
    private JTextArea txtDesc;
    private JTextField txtAttractions;
    private JTextField txtActivities;
    private JTextField txtDelicacies;
    private JTextField txtTips;
    private JTextArea txtItinerary;

    public AddPlaceDialog(Frame parent, PlaceRepository placeRepo) {
        super(parent, "Add New Tourist Destination", true);
        this.placeRepo = placeRepo;

        setSize(600, 680);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(24, 43, 73));
        header.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel title = new JLabel("➕ Add New Tourist Destination");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Form fields
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(15, 20, 15, 20));
        form.setBackground(Color.WHITE);

        txtName = new JTextField();
        txtCountry = new JTextField();
        txtRegion = new JTextField();
        comboCategory = new JComboBox<>(Category.values());
        comboBudget = new JComboBox<>(BudgetTier.values());
        spinDailyCost = new JSpinner(new SpinnerNumberModel(50.0, 10.0, 2000.0, 5.0));
        spinRating = new JSpinner(new SpinnerNumberModel(4.5, 1.0, 5.0, 0.1));
        spinMinDays = new JSpinner(new SpinnerNumberModel(3, 1, 30, 1));
        spinMaxDays = new JSpinner(new SpinnerNumberModel(6, 1, 30, 1));

        txtDesc = new JTextArea(3, 20);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);

        txtAttractions = new JTextField();
        txtActivities = new JTextField();
        txtDelicacies = new JTextField();
        txtTips = new JTextField();

        txtItinerary = new JTextArea(4, 20);
        txtItinerary.setLineWrap(true);
        txtItinerary.setWrapStyleWord(true);

        form.add(createRow("Destination Name *:", txtName));
        form.add(createRow("Country *:", txtCountry));
        form.add(createRow("State / Region:", txtRegion));
        form.add(createRow("Primary Theme *:", comboCategory));
        form.add(createRow("Budget Comfort *:", comboBudget));
        form.add(createRow("Est. Daily Cost ($/person) *:", spinDailyCost));
        form.add(createRow("Rating (1.0 - 5.0):", spinRating));
        form.add(createRow("Min Recommended Days:", spinMinDays));
        form.add(createRow("Max Recommended Days:", spinMaxDays));

        form.add(createTextAreaRow("Short Description *:", new JScrollPane(txtDesc)));
        form.add(createRow("Top Attractions (comma separated):", txtAttractions));
        form.add(createRow("Activities (comma separated):", txtActivities));
        form.add(createRow("Local Delicacies (comma separated):", txtDelicacies));
        form.add(createRow("Pro Travel Tip:", txtTips));
        form.add(createTextAreaRow("Sample Itinerary Outline:", new JScrollPane(txtItinerary)));

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Save Destination");
        btnSave.setBackground(new Color(25, 118, 210));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSave.addActionListener(e -> saveDestination());

        bottom.add(btnCancel);
        bottom.add(btnSave);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel createRow(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(200, 24));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(lbl, BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(3, 0, 3, 0));
        return p;
    }

    private JPanel createTextAreaRow(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(8, 4));
        p.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(4, 0, 4, 0));
        return p;
    }

    private void saveDestination() {
        String name = txtName.getText().trim();
        String country = txtCountry.getText().trim();
        String desc = txtDesc.getText().trim();

        if (name.isEmpty() || country.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Destination Name and Country are required.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = "P" + (System.currentTimeMillis() % 100000);
        Place p = new Place(
                id,
                name,
                country,
                txtRegion.getText().trim(),
                (Category) comboCategory.getSelectedItem(),
                (BudgetTier) comboBudget.getSelectedItem(),
                (Double) spinDailyCost.getValue(),
                (Double) spinRating.getValue(),
                (Integer) spinMinDays.getValue(),
                (Integer) spinMaxDays.getValue(),
                desc.isEmpty() ? "Wonderful travel destination offering diverse attractions and scenic beauty." : desc
        );

        String att = txtAttractions.getText().trim();
        if (!att.isEmpty()) {
            for (String a : att.split(",")) p.addTopAttraction(a.trim());
        }

        String act = txtActivities.getText().trim();
        if (!act.isEmpty()) {
            for (String a : act.split(",")) p.addActivity(a.trim());
        }

        String food = txtDelicacies.getText().trim();
        if (!food.isEmpty()) {
            for (String f : food.split(",")) p.addLocalDelicacy(f.trim());
        }

        p.setTravelTips(txtTips.getText().trim());
        p.setSampleItinerary(txtItinerary.getText().trim());

        // Defaults
        p.setBestSeasons(Arrays.asList(Season.ALL_YEAR));
        p.setSuitableCompanions(Arrays.asList(CompanionType.COUPLE, CompanionType.FAMILY, CompanionType.FRIENDS, CompanionType.SOLO));

        placeRepo.addPlace(p);
        JOptionPane.showMessageDialog(this, "'" + name + "' was successfully added!", "Destination Added", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}

package com.tourist.ui.gui;

import com.tourist.engine.SuggestionEngine;
import com.tourist.engine.TripCostEstimator;
import com.tourist.model.BudgetTier;
import com.tourist.model.Category;
import com.tourist.model.CompanionType;
import com.tourist.model.Place;
import com.tourist.model.RecommendationResult;
import com.tourist.model.Season;
import com.tourist.model.UserPreference;
import com.tourist.repository.PlaceRepository;
import com.tourist.repository.WishlistRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * Visual recommendation wizard panel allowing travelers to adjust preferences
 * and immediately receive dynamically scored destination suggestions.
 */
public class RecommendationPanel extends JPanel {

    private final PlaceRepository placeRepo;
    private final WishlistRepository wishlistRepo;
    private final SuggestionEngine suggestionEngine;
    private final TripCostEstimator costEstimator;
    private final Frame parentFrame;

    private JComboBox<String> comboCategory;
    private JComboBox<String> comboBudget;
    private JTextField txtMaxDailyBudget;
    private JCheckBox chkStrictBudget;
    private JComboBox<String> comboSeason;
    private JComboBox<String> comboCompanion;
    private JSpinner spinDuration;
    private JTextField txtKeyword;

    private JPanel resultsContainer;
    private JLabel statusLabel;

    public RecommendationPanel(Frame parent, PlaceRepository placeRepo, WishlistRepository wishlistRepo,
                               SuggestionEngine suggestionEngine, TripCostEstimator costEstimator) {
        this.parentFrame = parent;
        this.placeRepo = placeRepo;
        this.wishlistRepo = wishlistRepo;
        this.suggestionEngine = suggestionEngine;
        this.costEstimator = costEstimator;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        add(buildPreferenceForm(), BorderLayout.NORTH);
        add(buildResultsView(), BorderLayout.CENTER);

        // Run default recommendation on launch
        triggerRecommendation();
    }

    private JPanel buildPreferenceForm() {
        JPanel formCard = new JPanel(new BorderLayout(10, 10));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 225, 235), 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel title = new JLabel("🎯 Travel Preference Questionnaire");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(24, 43, 73));
        formCard.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 4, 12, 10));
        grid.setBackground(Color.WHITE);

        // 1. Theme
        comboCategory = new JComboBox<>();
        comboCategory.addItem("Any Theme / All");
        for (Category c : Category.values()) comboCategory.addItem(c.getDisplayName());
        grid.add(createFormField("Travel Theme:", comboCategory));

        // 2. Budget Level
        comboBudget = new JComboBox<>();
        comboBudget.addItem("Any Budget Level");
        for (BudgetTier b : BudgetTier.values()) comboBudget.addItem(b.getLabel());
        grid.add(createFormField("Budget Comfort:", comboBudget));

        // 3. Travel Season
        comboSeason = new JComboBox<>();
        comboSeason.addItem("Any Season / Time");
        for (Season s : Season.values()) comboSeason.addItem(s.getDisplayName());
        grid.add(createFormField("Travel Season:", comboSeason));

        // 4. Companions
        comboCompanion = new JComboBox<>();
        comboCompanion.addItem("Any Party Type");
        for (CompanionType c : CompanionType.values()) comboCompanion.addItem(c.getDisplayName());
        grid.add(createFormField("Companions:", comboCompanion));

        // 5. Trip Duration
        spinDuration = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
        grid.add(createFormField("Trip Duration (Days):", spinDuration));

        // 6. Max Daily Budget
        txtMaxDailyBudget = new JTextField();
        grid.add(createFormField("Max Budget ($/day):", txtMaxDailyBudget));

        // 7. Keyword / Region
        txtKeyword = new JTextField();
        grid.add(createFormField("Search City/Country:", txtKeyword));

        // 8. Strict Budget Checkbox
        chkStrictBudget = new JCheckBox("Strict Budget Cap");
        chkStrictBudget.setBackground(Color.WHITE);
        grid.add(createFormField("Budget Filter:", chkStrictBudget));

        formCard.add(grid, BorderLayout.CENTER);

        // Buttons row
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setBackground(Color.WHITE);
        btnBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(e -> resetFilters());

        JButton btnFind = new JButton("✨ Find Matching Places");
        btnFind.setBackground(new Color(25, 118, 210));
        btnFind.setForeground(Color.WHITE);
        btnFind.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnFind.setFocusPainted(false);
        btnFind.addActionListener(e -> triggerRecommendation());

        btnBar.add(btnReset);
        btnBar.add(btnFind);
        formCard.add(btnBar, BorderLayout.SOUTH);

        return formCard;
    }

    private JPanel createFormField(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(80, 90, 105));
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildResultsView() {
        JPanel resultsPanel = new JPanel(new BorderLayout(5, 5));
        resultsPanel.setBackground(new Color(245, 247, 250));

        statusLabel = new JLabel("Top suggestions based on your criteria:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(new Color(50, 60, 75));
        statusLabel.setBorder(new EmptyBorder(8, 4, 4, 4));
        resultsPanel.add(statusLabel, BorderLayout.NORTH);

        resultsContainer = new JPanel();
        resultsContainer.setLayout(new BoxLayout(resultsContainer, BoxLayout.Y_AXIS));
        resultsContainer.setBackground(new Color(245, 247, 250));

        JScrollPane scroll = new JScrollPane(resultsContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        resultsPanel.add(scroll, BorderLayout.CENTER);

        return resultsPanel;
    }

    private void resetFilters() {
        comboCategory.setSelectedIndex(0);
        comboBudget.setSelectedIndex(0);
        comboSeason.setSelectedIndex(0);
        comboCompanion.setSelectedIndex(0);
        spinDuration.setValue(5);
        txtMaxDailyBudget.setText("");
        txtKeyword.setText("");
        chkStrictBudget.setSelected(false);
        triggerRecommendation();
    }

    private void triggerRecommendation() {
        UserPreference prefs = new UserPreference();

        if (comboCategory.getSelectedIndex() > 0) {
            prefs.setPreferredCategory(Category.values()[comboCategory.getSelectedIndex() - 1]);
        }
        if (comboBudget.getSelectedIndex() > 0) {
            prefs.setBudgetTier(BudgetTier.values()[comboBudget.getSelectedIndex() - 1]);
        }
        if (comboSeason.getSelectedIndex() > 0) {
            prefs.setTravelSeason(Season.values()[comboSeason.getSelectedIndex() - 1]);
        }
        if (comboCompanion.getSelectedIndex() > 0) {
            prefs.setCompanionType(CompanionType.values()[comboCompanion.getSelectedIndex() - 1]);
        }
        prefs.setDurationDays((Integer) spinDuration.getValue());

        String maxDaily = txtMaxDailyBudget.getText().trim();
        if (!maxDaily.isEmpty()) {
            try {
                prefs.setMaxDailyBudgetUSD(Double.parseDouble(maxDaily));
            } catch (NumberFormatException ignored) {}
        }
        prefs.setStrictBudget(chkStrictBudget.isSelected());

        String kw = txtKeyword.getText().trim();
        if (!kw.isEmpty()) {
            prefs.setDestinationQuery(kw);
        }

        List<RecommendationResult> results = suggestionEngine.recommend(prefs, placeRepo.getAllPlaces());
        displayResults(results);
    }

    private void displayResults(List<RecommendationResult> results) {
        resultsContainer.removeAll();

        if (results.isEmpty()) {
            statusLabel.setText("No matching destinations found for your filter criteria. Try relaxing constraints.");
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(new Color(245, 247, 250));
            emptyPanel.add(new JLabel("No matching destinations. Try clearing filters or setting a higher budget."));
            resultsContainer.add(emptyPanel);
        } else {
            statusLabel.setText(String.format("Found %d matching destinations (ranked by best fit):", results.size()));
            for (RecommendationResult res : results) {
                resultsContainer.add(createResultCard(res));
                resultsContainer.add(Box.createVerticalStrut(10));
            }
        }

        resultsContainer.revalidate();
        resultsContainer.repaint();
    }

    private JPanel createResultCard(RecommendationResult res) {
        Place place = res.getPlace();

        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 230, 240), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Left Score Badge
        JPanel scoreBadge = new JPanel(new BorderLayout());
        int score = res.getRoundedScore();
        Color badgeBg = score >= 80 ? new Color(230, 248, 235) : (score >= 60 ? new Color(235, 243, 255) : new Color(255, 248, 230));
        Color badgeFg = score >= 80 ? new Color(20, 130, 50) : (score >= 60 ? new Color(25, 90, 180) : new Color(180, 110, 10));

        scoreBadge.setBackground(badgeBg);
        scoreBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(badgeFg, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel scoreNum = new JLabel(score + "%", SwingConstants.CENTER);
        scoreNum.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreNum.setForeground(badgeFg);

        JLabel scoreText = new JLabel("MATCH", SwingConstants.CENTER);
        scoreText.setFont(new Font("Segoe UI", Font.BOLD, 10));
        scoreText.setForeground(badgeFg);

        scoreBadge.add(scoreNum, BorderLayout.CENTER);
        scoreBadge.add(scoreText, BorderLayout.SOUTH);
        card.add(scoreBadge, BorderLayout.WEST);

        // Center Content
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setBackground(Color.WHITE);

        JLabel nameLbl = new JLabel(place.getName() + " (" + place.getFullLocation() + ")");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLbl.setForeground(new Color(24, 43, 73));
        titleRow.add(nameLbl);

        JLabel ratingLbl = new JLabel("⭐ " + place.getRating());
        ratingLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ratingLbl.setForeground(new Color(210, 140, 0));
        titleRow.add(ratingLbl);

        JLabel costLbl = new JLabel("•  " + place.getBudgetTier().getLabel() + " (~$" + (int)place.getEstimatedDailyCostUSD() + "/day)");
        costLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        costLbl.setForeground(new Color(100, 110, 125));
        titleRow.add(costLbl);
        center.add(titleRow);

        JLabel descLbl = new JLabel("<html>" + place.getShortDescription() + "</html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLbl.setForeground(new Color(70, 75, 85));
        descLbl.setBorder(new EmptyBorder(4, 0, 4, 0));
        center.add(descLbl);

        // Reasons
        if (!res.getMatchReasons().isEmpty()) {
            JLabel reasonsLbl = new JLabel("<html><font color='#1b7e36'><b>Fits your criteria:</b> " + String.join(" &bull; ", res.getMatchReasons()) + "</font></html>");
            reasonsLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            center.add(reasonsLbl);
        }
        if (!res.getCautions().isEmpty()) {
            JLabel cautionLbl = new JLabel("<html><font color='#b06000'><b>Note:</b> " + String.join(" &bull; ", res.getCautions()) + "</font></html>");
            cautionLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            center.add(cautionLbl);
        }

        card.add(center, BorderLayout.CENTER);

        // Right Actions
        JPanel rightActions = new JPanel(new GridLayout(2, 1, 0, 6));
        rightActions.setBackground(Color.WHITE);

        JButton btnView = new JButton("View Dossier");
        btnView.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnView.addActionListener(e -> {
            PlaceDetailDialog dlg = new PlaceDetailDialog(parentFrame, place, wishlistRepo, costEstimator);
            dlg.setVisible(true);
        });

        JButton btnWishlist = new JButton(wishlistRepo.contains(place.getId()) ? "⭐ Saved" : "⭐ Save");
        btnWishlist.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnWishlist.addActionListener(e -> {
            if (!wishlistRepo.contains(place.getId())) {
                wishlistRepo.add(place, "", "Flexible");
                btnWishlist.setText("⭐ Saved");
            }
        });

        rightActions.add(btnView);
        rightActions.add(btnWishlist);
        card.add(rightActions, BorderLayout.EAST);

        return card;
    }
}

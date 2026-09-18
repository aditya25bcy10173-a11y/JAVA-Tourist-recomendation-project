package com.tourist.ui.gui;

import com.tourist.engine.TripCostEstimator;
import com.tourist.model.Place;
import com.tourist.model.TripCostEstimate;
import com.tourist.repository.WishlistRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Detailed modal dialog displaying complete destination information,
 * attractions, local food, tips, sample itinerary, and quick actions.
 */
public class PlaceDetailDialog extends JDialog {

    public PlaceDetailDialog(Frame parent, Place place, WishlistRepository wishlistRepo, TripCostEstimator costEstimator) {
        super(parent, place.getName() + " - Destination Dossier", true);
        setSize(720, 680);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel titleLabel = new JLabel(place.getName() + ", " + place.getCountry());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel(place.getStateOrRegion() + "  •  " + place.getPrimaryCategory().getDisplayName() + "  •  ⭐ " + place.getRating() + " / 5.0");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(210, 225, 245));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Content Panel (Scrollable)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        contentPanel.setBackground(Color.WHITE);

        // Badges / Metrics Row
        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        badgesPanel.setBackground(Color.WHITE);
        badgesPanel.add(createBadge("Budget: " + place.getBudgetTier().getLabel() + " (~$" + (int)place.getEstimatedDailyCostUSD() + "/day)", new Color(230, 245, 235), new Color(25, 120, 50)));
        badgesPanel.add(createBadge("Duration: " + place.getMinRecommendedDays() + " - " + place.getMaxRecommendedDays() + " Days", new Color(235, 240, 255), new Color(30, 80, 180)));
        badgesPanel.add(createBadge("Seasons: " + place.getBestSeasons(), new Color(255, 245, 230), new Color(180, 90, 10)));
        contentPanel.add(badgesPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // Description
        addSectionHeader(contentPanel, "Overview");
        JTextArea descArea = createReadOnlyTextArea(place.getShortDescription());
        contentPanel.add(descArea);
        contentPanel.add(Box.createVerticalStrut(12));

        // Attractions
        addSectionHeader(contentPanel, "🏛 Top Sights & Attractions");
        StringBuilder sights = new StringBuilder();
        for (String sight : place.getTopAttractions()) {
            sights.append("• ").append(sight).append("\n");
        }
        contentPanel.add(createReadOnlyTextArea(sights.toString().trim()));
        contentPanel.add(Box.createVerticalStrut(12));

        // Activities
        addSectionHeader(contentPanel, "🏄 Popular Activities & Experiences");
        StringBuilder activities = new StringBuilder();
        for (String act : place.getActivities()) {
            activities.append("• ").append(act).append("\n");
        }
        contentPanel.add(createReadOnlyTextArea(activities.toString().trim()));
        contentPanel.add(Box.createVerticalStrut(12));

        // Delicacies
        addSectionHeader(contentPanel, "🍲 Must-Try Local Delicacies");
        StringBuilder food = new StringBuilder();
        for (String d : place.getLocalDelicacies()) {
            food.append("• ").append(d).append("\n");
        }
        contentPanel.add(createReadOnlyTextArea(food.toString().trim()));
        contentPanel.add(Box.createVerticalStrut(12));

        // Travel Tips
        if (place.getTravelTips() != null && !place.getTravelTips().isEmpty()) {
            addSectionHeader(contentPanel, "💡 Pro Travel Tip");
            contentPanel.add(createReadOnlyTextArea(place.getTravelTips()));
            contentPanel.add(Box.createVerticalStrut(12));
        }

        // Suggested Itinerary
        if (place.getSampleItinerary() != null && !place.getSampleItinerary().isEmpty()) {
            addSectionHeader(contentPanel, "📅 Suggested Itinerary Outline");
            contentPanel.add(createReadOnlyTextArea(place.getSampleItinerary()));
            contentPanel.add(Box.createVerticalStrut(12));
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomBar.setBackground(new Color(245, 247, 250));

        JButton btnWishlist = new JButton(wishlistRepo.contains(place.getId()) ? "⭐ In Wishlist" : "⭐ Add to Wishlist");
        btnWishlist.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnWishlist.addActionListener(e -> {
            if (!wishlistRepo.contains(place.getId())) {
                String notes = JOptionPane.showInputDialog(this, "Add personal notes for this destination (optional):", "Save to Wishlist", JOptionPane.PLAIN_MESSAGE);
                wishlistRepo.add(place, notes != null ? notes : "", "Flexible");
                btnWishlist.setText("⭐ In Wishlist");
                JOptionPane.showMessageDialog(this, place.getName() + " added to your Wishlist!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, place.getName() + " is already in your Wishlist.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnCost = new JButton("💰 Quick Cost Estimate");
        btnCost.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCost.addActionListener(e -> {
            TripCostEstimate est = costEstimator.estimate(place, place.getMinRecommendedDays(), 2);
            JOptionPane.showMessageDialog(this, est.getFormattedSummary(), "Cost Estimate for " + place.getName(), JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());

        bottomBar.add(btnWishlist);
        bottomBar.add(btnCost);
        bottomBar.add(btnClose);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void addSectionHeader(JPanel panel, String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(24, 43, 73));
        lbl.setBorder(new EmptyBorder(4, 0, 4, 0));
        panel.add(lbl);
    }

    private JTextArea createReadOnlyTextArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setBackground(Color.WHITE);
        area.setBorder(null);
        return area;
    }

    private JPanel createBadge(String text, Color bg, Color fg) {
        JPanel badge = new JPanel();
        badge.setBackground(bg);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                new EmptyBorder(3, 8, 3, 8)
        ));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(fg);
        badge.add(lbl);
        return badge;
    }
}

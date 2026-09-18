package com.tourist.ui.gui;

import com.tourist.engine.SuggestionEngine;
import com.tourist.engine.TripCostEstimator;
import com.tourist.repository.PlaceRepository;
import com.tourist.repository.WishlistRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * Main application window hosting tabbed navigation between
 * recommendation wizard, destination explorer, budget calculator, and wishlist.
 */
public class MainFrame extends JFrame {

    private final PlaceRepository placeRepo;
    private final WishlistRepository wishlistRepo;
    private final SuggestionEngine suggestionEngine;
    private final TripCostEstimator costEstimator;

    private JTabbedPane tabbedPane;
    private RecommendationPanel recommendationPanel;
    private ExplorePanel explorePanel;
    private CostEstimatorPanel costEstimatorPanel;
    private WishlistPanel wishlistPanel;

    public MainFrame(PlaceRepository placeRepo, WishlistRepository wishlistRepo,
                     SuggestionEngine suggestionEngine, TripCostEstimator costEstimator) {
        super("Tourist Places Suggestion System");
        this.placeRepo = placeRepo;
        this.wishlistRepo = wishlistRepo;
        this.suggestionEngine = suggestionEngine;
        this.costEstimator = costEstimator;

        initLookAndFeel();
        initComponents();
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1140, 780);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setJMenuBar(createAppMenuBar());
        add(createHeaderPanel(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        recommendationPanel = new RecommendationPanel(this, placeRepo, wishlistRepo, suggestionEngine, costEstimator);
        explorePanel = new ExplorePanel(this, placeRepo, wishlistRepo, costEstimator);
        costEstimatorPanel = new CostEstimatorPanel(placeRepo, costEstimator);
        wishlistPanel = new WishlistPanel(this, wishlistRepo, costEstimator);

        tabbedPane.addTab("🎯 Recommendation Wizard", recommendationPanel);
        tabbedPane.addTab("🔍 Explore Destinations", explorePanel);
        tabbedPane.addTab("💰 Trip Cost Estimator", costEstimatorPanel);
        tabbedPane.addTab("⭐ My Travel Wishlist", wishlistPanel);

        tabbedPane.addChangeListener(e -> {
            int selected = tabbedPane.getSelectedIndex();
            if (selected == 1) {
                explorePanel.refreshTableData();
            } else if (selected == 3) {
                wishlistPanel.refreshTable();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 36, 62));
        header.setBorder(new EmptyBorder(14, 22, 14, 22));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("🌍 TOURIST PLACES SUGGESTION SYSTEM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Intelligent travel recommendations based on budget, season, group type & preferences");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(185, 205, 235));

        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.WEST);

        // Stats badge on right
        JPanel statsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        statsBox.setOpaque(false);

        JLabel stats = new JLabel("📦 " + placeRepo.getAllPlaces().size() + " Destinations Loaded");
        stats.setFont(new Font("Segoe UI", Font.BOLD, 12));
        stats.setForeground(new Color(220, 235, 255));
        statsBox.add(stats);

        header.add(statsBox, BorderLayout.EAST);
        return header;
    }

    private JMenuBar createAppMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        JMenuItem itemAdd = new JMenuItem("➕ Add New Destination...");
        itemAdd.addActionListener(e -> {
            AddPlaceDialog dlg = new AddPlaceDialog(this, placeRepo);
            dlg.setVisible(true);
            explorePanel.refreshTableData();
        });

        JMenuItem itemExportCsv = new JMenuItem("📤 Export Destinations to CSV...");
        itemExportCsv.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("tourist_places_export.csv"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                placeRepo.exportToCsv(chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Destinations exported to: " + chooser.getSelectedFile().getName());
            }
        });

        JMenuItem itemExit = new JMenuItem("Exit");
        itemExit.addActionListener(e -> System.exit(0));

        menuFile.add(itemAdd);
        menuFile.add(itemExportCsv);
        menuFile.addSeparator();
        menuFile.add(itemExit);

        JMenu menuHelp = new JMenu("Help");
        JMenuItem itemAbout = new JMenuItem("About");
        itemAbout.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Tourist Places Suggestion System\n" +
                    "Version 1.0\n\n" +
                    "A multi-criteria travel recommendation application built in pure Java.\n" +
                    "Features:\n" +
                    "• Multi-factor weighted recommendation algorithm\n" +
                    "• 25+ curated domestic and international destinations\n" +
                    "• Itemized trip cost & budget estimator\n" +
                    "• Travel wishlist & itinerary exporter\n" +
                    "• Dynamic destination editor & search system",
                    "About Tourist Places Suggestion System",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        menuHelp.add(itemAbout);

        bar.add(menuFile);
        bar.add(menuHelp);
        return bar;
    }
}

package com.tourist;

import com.tourist.engine.SuggestionEngine;
import com.tourist.engine.TripCostEstimator;
import com.tourist.repository.PlaceRepository;
import com.tourist.repository.WishlistRepository;
import com.tourist.ui.cli.ConsoleUI;
import com.tourist.ui.gui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Main application entrypoint.
 * Automatically selects GUI desktop interface or CLI interactive console mode based on arguments.
 */
public class Main {

    public static void main(String[] args) {
        boolean forceCli = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "--console".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                forceCli = true;
                break;
            }
        }

        // Initialize Data Folders & Repositories
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        File placesCsv = new File(dataDir, "default_places.csv");
        PlaceRepository placeRepo = new PlaceRepository();

        // If CSV exists, optionally load external records, or seed and save if not present
        if (placesCsv.exists()) {
            placeRepo.importFromCsv(placesCsv);
        } else {
            placeRepo.exportToCsv(placesCsv);
        }

        File wishlistFile = new File(dataDir, "user_wishlist.txt");
        WishlistRepository wishlistRepo = new WishlistRepository(wishlistFile, placeRepo);

        SuggestionEngine suggestionEngine = new SuggestionEngine();
        TripCostEstimator costEstimator = new TripCostEstimator();

        // Check if headless or forced CLI
        if (forceCli || GraphicsEnvironment.isHeadless()) {
            ConsoleUI cli = new ConsoleUI(placeRepo, wishlistRepo, suggestionEngine, costEstimator);
            cli.start();
        } else {
            try {
                SwingUtilities.invokeLater(() -> {
                    try {
                        MainFrame frame = new MainFrame(placeRepo, wishlistRepo, suggestionEngine, costEstimator);
                        frame.setVisible(true);
                    } catch (Exception e) {
                        System.err.println("GUI failed to start: " + e.getMessage() + ". Falling back to console mode.");
                        ConsoleUI cli = new ConsoleUI(placeRepo, wishlistRepo, suggestionEngine, costEstimator);
                        cli.start();
                    }
                });
            } catch (Exception e) {
                ConsoleUI cli = new ConsoleUI(placeRepo, wishlistRepo, suggestionEngine, costEstimator);
                cli.start();
            }
        }
    }
}

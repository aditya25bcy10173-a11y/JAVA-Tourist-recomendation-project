package com.tourist.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight, zero-dependency helper for CSV and text file storage.
 * Properly handles escaped commas, quotes, and UTF-8 encoding.
 */
public class FileStorageHelper {

    /**
     * Reads all lines from a file using UTF-8 encoding.
     */
    public static List<String> readAllLines(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        if (!file.exists()) return lines;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * Writes lines to a file using UTF-8 encoding.
     */
    public static void writeAllLines(File file, List<String> lines) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Appends text lines to an existing or new file.
     */
    public static void appendLines(File file, List<String> lines) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Parses a CSV line respecting quoted columns and escaped quotes.
     */
    public static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        if (line == null) return tokens;

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++; // Skip the escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }

    /**
     * Encodes a list of column values into a CSV line with appropriate escaping.
     */
    public static String toCsvLine(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(",");
            String val = values.get(i);
            if (val == null) val = "";
            boolean needsQuotes = val.contains(",") || val.contains("\"") || val.contains("\n") || val.contains("\r");
            if (needsQuotes) {
                sb.append('"').append(val.replace("\"", "\"\"")).append('"');
            } else {
                sb.append(val);
            }
        }
        return sb.toString();
    }
}

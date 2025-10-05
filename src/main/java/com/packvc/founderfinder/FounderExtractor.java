package com.packvc.founderfinder;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Extracts founder names from Wikipedia infoboxes.
 * Handles various formats and normalizes names.
 */
public class FounderExtractor {
    
    // Patterns for founder-related headers
    private static final String[] FOUNDER_HEADERS = {"Founder(s)", "Founders", "Founder"};
    
    // Pattern to match person-like names: 2-4 capitalized tokens with optional hyphens/apostrophes
    private static final Pattern PERSON_NAME_PATTERN = Pattern.compile(
        "^[A-Z][a-z]+(?:[-'][A-Z][a-z]+)*(?:\\s+[A-Z][a-z]+(?:[-'][A-Z][a-z]+)*){1,3}$"
    );
    
    /**
     * Extracts founders from a Wikipedia article document.
     * 
     * @param doc The Wikipedia article document
     * @param companyName Name of the company for logging
     * @return List of founder names (may be empty)
     */
    public static List<String> extractFounders(Document doc, String companyName) {
        System.out.println("Extracting founders for: " + companyName);
        
        // Find the infobox
        Elements infoboxes = doc.select("table.infobox");
        if (infoboxes.isEmpty()) {
            System.out.println("  No infobox found");
            return new ArrayList<>();
        }
        
        Element infobox = infoboxes.first();
        System.out.println("  Found infobox, searching for founder rows...");
        
        // Look for founder-related rows
        Elements rows = infobox.select("tr");
        for (Element row : rows) {
            Elements headers = row.select("th");
            Elements dataCells = row.select("td");
            
            if (headers.isEmpty() || dataCells.isEmpty()) {
                continue;
            }
            
            String headerText = headers.first().text().trim();
            System.out.println("  Checking header: '" + headerText + "'");
            
            // Check if this is a founder-related header
            if (isFounderHeader(headerText)) {
                System.out.println("  ✓ Found founder header: '" + headerText + "'");
                Element dataCell = dataCells.first();
                List<String> founders = extractNamesFromCell(dataCell);
                System.out.println("  Extracted " + founders.size() + " founders: " + founders);
                return founders;
            }
        }
        
        System.out.println("  No founder information found in infobox");
        return new ArrayList<>();
    }
    
    /**
     * Checks if a header text matches founder-related patterns.
     */
    private static boolean isFounderHeader(String headerText) {
        for (String founderHeader : FOUNDER_HEADERS) {
            if (headerText.equals(founderHeader)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Extracts and normalizes names from a data cell.
     */
    private static List<String> extractNamesFromCell(Element cell) {
        // Get all text content, handling <br> tags as separators
        String cellText = cell.html();
        
        // Replace <br> tags with newlines for splitting
        cellText = cellText.replaceAll("(?i)<br\\s*/?>", "\n");
        
        // Remove HTML tags but preserve text content
        cellText = cellText.replaceAll("<[^>]+>", "");
        
        // Split by newlines, commas, and semicolons
        String[] nameParts = cellText.split("[\n,;]+");
        
        Set<String> uniqueNames = new LinkedHashSet<>();
        
        for (String part : nameParts) {
            String normalizedName = normalizeName(part.trim());
            if (isValidPersonName(normalizedName)) {
                uniqueNames.add(normalizedName);
                System.out.println("    Added founder: '" + normalizedName + "'");
            } else if (!normalizedName.isEmpty()) {
                System.out.println("    Skipped invalid name: '" + normalizedName + "'");
            }
        }
        
        return new ArrayList<>(uniqueNames);
    }
    
    /**
     * Normalizes a name by cleaning up spacing and Unicode characters.
     */
    private static String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        
        // Remove extra whitespace
        name = name.trim().replaceAll("\\s+", " ");
        
        // Normalize Unicode apostrophes and hyphens
        name = name.replaceAll("[''`]", "'");  // Normalize apostrophes
        name = name.replaceAll("[-–—]", "-");  // Normalize hyphens
        
        // Remove common prefixes/suffixes that aren't part of names
        name = name.replaceAll("^(Mr\\.?|Mrs\\.?|Ms\\.?|Dr\\.?)\\s+", "");
        name = name.replaceAll("\\s+(Jr\\.?|Sr\\.?|III|IV)$", "");
        
        return name.trim();
    }
    
    /**
     * Validates if a string looks like a person's name.
     */
    private static boolean isValidPersonName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Must match person name pattern (2-4 capitalized tokens)
        if (!PERSON_NAME_PATTERN.matcher(name).matches()) {
            return false;
        }
        
        // Additional filters for common non-person text
        String lowerName = name.toLowerCase();
        
        // Skip if it contains common non-person words
        String[] skipWords = {
            "company", "corporation", "inc", "llc", "ltd", "group", "systems",
            "technologies", "software", "services", "solutions", "ventures",
            "capital", "partners", "associates", "holdings", "enterprises"
        };
        
        for (String skipWord : skipWords) {
            if (lowerName.contains(skipWord)) {
                return false;
            }
        }
        
        // Skip if it's too short or too long
        if (name.length() < 3 || name.length() > 100) {
            return false;
        }
        
        return true;
    }
}

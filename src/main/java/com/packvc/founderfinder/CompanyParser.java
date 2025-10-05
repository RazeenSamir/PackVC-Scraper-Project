package com.packvc.founderfinder;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses companies.txt file into Company objects.
 * Handles lines in format: "CompanyName (https://example.com/)"
 */
public class CompanyParser {
    
    // Regex to match: Name (URL) or just Name
    private static final Pattern COMPANY_PATTERN = Pattern.compile("^(.+?)\\s*\\(([^)]+)\\)\\s*$");
    
    /**
     * Parses a companies.txt file and returns a list of Company objects.
     * 
     * @param filePath Path to the companies.txt file
     * @return List of Company objects
     * @throws IOException if file cannot be read
     */
    public static List<Company> parseFile(String filePath) throws IOException {
        List<Company> companies = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                // Skip blank lines
                if (line.isEmpty()) {
                    continue;
                }
                
                Company company = parseLine(line, lineNumber);
                if (company != null) {
                    companies.add(company);
                }
            }
        }
        
        return companies;
    }
    
    /**
     * Parses a single line into a Company object.
     * 
     * @param line The line to parse
     * @param lineNumber Line number for error reporting
     * @return Company object or null if line is invalid
     */
    private static Company parseLine(String line, int lineNumber) {
        // Try to match pattern: Name (URL)
        Matcher matcher = COMPANY_PATTERN.matcher(line);
        
        if (matcher.matches()) {
            String name = matcher.group(1).trim();
            String url = matcher.group(2).trim();
            
            // Validate URL format (basic check)
            if (isValidUrl(url)) {
                return new Company(name, url);
            } else {
                System.err.println("Warning: Invalid URL format on line " + lineNumber + ": " + url);
                return new Company(name, null);
            }
        } else {
            // No URL found, treat entire line as company name
            return new Company(line, null);
        }
    }
    
    /**
     * Basic URL validation.
     * 
     * @param url URL to validate
     * @return true if URL appears valid
     */
    private static boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }
}

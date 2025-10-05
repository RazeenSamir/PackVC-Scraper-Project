package com.packvc.founderfinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.jsoup.nodes.Document;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Main entry point for the Founder Finder CLI tool.
 * Scrapes Wikipedia for company founders and outputs to JSON.
 */
public class Main {
    
    public static void main(String[] args) {
        // Validate command line arguments
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }
        
        if (args.length > 2) {
            System.err.println("Error: Too many arguments provided.");
            printUsage();
            System.exit(1);
        }
        
        // Parse arguments
        String inputFile = args[0];
        String outputFile = args.length == 2 ? args[1] : "founders.json";
        
        System.out.println("Founder Finder - Pack Ventures Technical Screening");
        System.out.println("Input file: " + inputFile);
        System.out.println("Output file: " + outputFile);
        
        try {
            // Parse companies from input file
            List<Company> companies = CompanyParser.parseFile(inputFile);
            
            System.out.println("\nParsed " + companies.size() + " companies:");
            for (Company company : companies) {
                System.out.println("  " + company.toString());
            }
            
            System.out.println("\nPhase 2 complete - Company parsing ready");
            
            // Phase 5: Complete orchestration - process all companies
            System.out.println("\n=== PHASE 5: Complete Processing ===");
            
            Map<String, List<String>> foundersMap = new LinkedHashMap<>();
            
            for (int i = 0; i < companies.size(); i++) {
                Company company = companies.get(i);
                String companyName = company.getName();
                
                System.out.println("\n--- Processing " + (i + 1) + "/" + companies.size() + ": " + companyName + " ---");
                
                try {
                    // Step 1: Resolve Wikipedia page
                    System.out.println("Resolving Wikipedia page for: " + companyName);
                    Optional<String> wikipediaUrl = WikipediaFetcher.resolveWikipediaPage(companyName);
                    
                    if (wikipediaUrl.isPresent()) {
                        System.out.println("✓ Found Wikipedia page: " + wikipediaUrl.get());
                        
                        // Step 2: Extract founders
                        System.out.println("Extracting founders from Wikipedia page...");
                        Document doc = WikipediaFetcher.fetch(wikipediaUrl.get());
                        List<String> founders = FounderExtractor.extractFounders(doc, companyName);
                        
                        if (!founders.isEmpty()) {
                            System.out.println("✓ Found " + founders.size() + " founders for " + companyName + ":");
                            for (int j = 0; j < founders.size(); j++) {
                                System.out.println("  " + (j + 1) + ". " + founders.get(j));
                            }
                            foundersMap.put(companyName, founders);
                        } else {
                            System.out.println("✗ No founders found for " + companyName);
                            foundersMap.put(companyName, new ArrayList<>());
                        }
                        
                    } else {
                        System.out.println("✗ No Wikipedia page found for " + companyName);
                        foundersMap.put(companyName, new ArrayList<>());
                    }
                    
                } catch (Exception e) {
                    System.err.println("✗ Error processing " + companyName + ": " + e.getMessage());
                    foundersMap.put(companyName, new ArrayList<>());
                }
                
                // Add delay between companies (except for the last one)
                if (i < companies.size() - 1) {
                    try {
                        System.out.println("Waiting 400ms before next company...");
                        Thread.sleep(400);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        System.err.println("Interrupted during delay");
                        break;
                    }
                }
            }
            
            // Print final results
            System.out.println("\n=== FINAL RESULTS ===");
            System.out.println("Founders Map:");
            for (Map.Entry<String, List<String>> entry : foundersMap.entrySet()) {
                String company = entry.getKey();
                List<String> founders = entry.getValue();
                System.out.println("  " + company + ": " + founders);
            }
            
            // Phase 6: Write JSON output
            System.out.println("\n=== PHASE 6: JSON Output ===");
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(foundersMap);
                
                Files.write(Paths.get(outputFile), json.getBytes());
                System.out.println("✓ Successfully wrote founders to: " + outputFile);
                System.out.println("JSON content:");
                System.out.println(json);
                
            } catch (IOException e) {
                System.err.println("✗ Error writing JSON file: " + e.getMessage());
                System.exit(1);
            }
            
            System.out.println("\nPhase 6 complete - JSON output written");
            
        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Prints usage information for the CLI tool.
     */
    private static void printUsage() {
        System.out.println("Usage: java -jar founder-finder.jar <companies.txt> [founders.json]");
        System.out.println("  companies.txt  - Input file with company names and URLs");
        System.out.println("  founders.json  - Output file (default: founders.json)");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java -jar founder-finder.jar companies.txt");
        System.out.println("  java -jar founder-finder.jar companies.txt output.json");
    }
}


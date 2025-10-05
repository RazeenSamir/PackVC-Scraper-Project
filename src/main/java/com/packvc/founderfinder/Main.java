package com.packvc.founderfinder;

import java.io.IOException;
import java.util.List;

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


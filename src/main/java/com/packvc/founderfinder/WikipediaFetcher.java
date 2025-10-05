package com.packvc.founderfinder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Fetches Wikipedia pages for company names.
 * Uses only Wikipedia's on-site search and direct URL access.
 */
public class WikipediaFetcher {
    
    private static final String USER_AGENT = "PackVenturesFounderFinder/1.0 (+contact)";
    private static final int TIMEOUT = 12000; // 12 seconds
    private static final int MAX_RETRIES = 3;
    private static final int DELAY_MS = 350; // 350ms delay between requests
    
    /**
     * Fetches a document from the given URL with retry logic.
     * 
     * @param url URL to fetch
     * @return Document object
     * @throws IOException if all retries fail
     */
    public static Document fetch(String url) throws IOException {
        IOException lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                System.out.println("  Fetching: " + url + " (attempt " + attempt + ")");
                
                Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .followRedirects(true)
                    .get();
                
                // Add delay after successful request
                if (attempt < MAX_RETRIES) {
                    Thread.sleep(DELAY_MS);
                }
                
                return doc;
                
            } catch (IOException e) {
                lastException = e;
                System.err.println("  Attempt " + attempt + " failed: " + e.getMessage());
                
                if (attempt < MAX_RETRIES) {
                    try {
                        // Exponential backoff: 500ms, 1000ms, 2000ms
                        int backoffMs = 500 * (int) Math.pow(2, attempt - 1);
                        System.out.println("  Retrying in " + backoffMs + "ms...");
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry", ie);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted during request", e);
            }
        }
        
        throw new IOException("Failed to fetch " + url + " after " + MAX_RETRIES + " attempts", lastException);
    }
    
    /**
     * Resolves a company name to its Wikipedia article URL.
     * 
     * @param companyName Name of the company
     * @return Optional containing the Wikipedia URL if found
     */
    public static Optional<String> resolveWikipediaPage(String companyName) {
        try {
            // Step 1: Try direct slug approach
            String directUrl = buildDirectUrl(companyName);
            System.out.println("Trying direct URL for '" + companyName + "': " + directUrl);
            
            try {
                Document doc = fetch(directUrl);
                
                // Check if we got a valid article (not a disambiguation or search page)
                if (isValidArticle(doc, companyName)) {
                    System.out.println("  ✓ Direct URL works: " + directUrl);
                    return Optional.of(directUrl);
                } else {
                    System.out.println("  ✗ Direct URL not suitable (disambiguation or search page)");
                }
                
            } catch (IOException e) {
                System.out.println("  ✗ Direct URL failed: " + e.getMessage());
            }
            
            // Step 2: Use Wikipedia search
            System.out.println("Trying Wikipedia search for '" + companyName + "'");
            String searchUrl = buildSearchUrl(companyName);
            System.out.println("Search URL: " + searchUrl);
            
            Document searchDoc = fetch(searchUrl);
            String articleUrl = extractFirstArticleLink(searchDoc);
            
            if (articleUrl != null) {
                System.out.println("  ✓ Found article via search: " + articleUrl);
                return Optional.of(articleUrl);
            } else {
                System.out.println("  ✗ No suitable article found in search results");
                return Optional.empty();
            }
            
        } catch (Exception e) {
            System.err.println("Error resolving Wikipedia page for '" + companyName + "': " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Builds a direct Wikipedia URL from company name.
     */
    private static String buildDirectUrl(String companyName) {
        String slug = companyName.replaceAll("\\s+", "_");
        return "https://en.wikipedia.org/wiki/" + URLEncoder.encode(slug, StandardCharsets.UTF_8);
    }
    
    /**
     * Builds a Wikipedia search URL.
     */
    private static String buildSearchUrl(String companyName) {
        String encodedName = URLEncoder.encode(companyName, StandardCharsets.UTF_8);
        return "https://en.wikipedia.org/w/index.php?search=" + encodedName;
    }
    
    /**
     * Checks if a document represents a valid article (not disambiguation/search).
     */
    private static boolean isValidArticle(Document doc, String companyName) {
        // Check for disambiguation page indicators
        if (doc.select("div#disambig").size() > 0) {
            return false;
        }
        
        // Check for search page indicators
        if (doc.select("div.searchresults").size() > 0) {
            return false;
        }
        
        // Check if we have a proper article title
        String title = doc.title();
        if (title.contains("Search results") || title.contains("Wikipedia:")) {
            return false;
        }
        
        // Check if the page has an infobox (good indicator of a company article)
        Elements infoboxes = doc.select("table.infobox");
        if (infoboxes.size() > 0) {
            return true;
        }
        
        // If no infobox, at least check it's not a redirect or special page
        return !title.contains("(disambiguation)") && 
               !title.contains("Wikipedia:") &&
               !title.contains("Special:");
    }
    
    /**
     * Extracts the first valid article link from search results.
     */
    private static String extractFirstArticleLink(Document searchDoc) {
        // Look for article links in search results
        Elements links = searchDoc.select("div.searchresults a[href^='/wiki/']");
        
        for (Element link : links) {
            String href = link.attr("href");
            String title = link.attr("title");
            
            // Skip special pages and namespaces
            if (href.contains(":") && !href.contains("File:")) {
                continue;
            }
            
            // Skip disambiguation pages
            if (title.contains("(disambiguation)")) {
                continue;
            }
            
            // This looks like a valid article
            String fullUrl = "https://en.wikipedia.org" + href;
            System.out.println("  Found candidate: " + title + " -> " + fullUrl);
            return fullUrl;
        }
        
        return null;
    }
}

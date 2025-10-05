package com.packvc.founderfinder;

/**
 * Represents a company with its name and optional URL.
 */
public class Company {
    private final String name;
    private final String url;
    
    public Company(String name, String url) {
        this.name = name;
        this.url = url;
    }
    
    public String getName() {
        return name;
    }
    
    public String getUrl() {
        return url;
    }
    
    public boolean hasUrl() {
        return url != null && !url.isEmpty();
    }
    
    @Override
    public String toString() {
        if (hasUrl()) {
            return String.format("%s (%s)", name, url);
        } else {
            return name;
        }
    }
}

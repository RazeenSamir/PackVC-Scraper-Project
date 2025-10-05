# Founder Finder

A Java CLI tool for Pack Ventures' technical screening that scrapes Wikipedia for company founders.

## Overview

This tool reads a list of companies from an input file and outputs a JSON mapping of company names to their founders, scraped from Wikipedia pages. It uses only Wikipedia as the data source and implements polite web scraping practices.

## How to Run

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

### Building
```bash
mvn clean package
```

This creates a fat JAR at `target/founder-finder-1.0.0.jar` with all dependencies included.

### Running
```bash
# Basic usage (outputs to founders.json)
java -jar target/founder-finder-1.0.0.jar companies.txt

# Specify output file
java -jar target/founder-finder-1.0.0.jar companies.txt output.json
```

### Example Usage
```bash
# Create input file
echo "Airbnb (https://www.airbnb.com/)" > companies.txt
echo "Dropbox (https://www.dropbox.com/)" >> companies.txt

# Run the tool
java -jar target/founder-finder-1.0.0.jar companies.txt

# Check results
cat founders.json
```

## Input Format

The input file should contain one company per line in the format:
```
CompanyName (https://www.company.com/)
```

Example `companies.txt`:
```
Airbnb (https://www.airbnb.com/)
Dropbox (https://www.dropbox.com/)
Tesla (https://www.tesla.com/)
Apple
Google (https://www.google.com/)
```

**Note:** URLs are optional. Companies without URLs will still be processed.

## Output Format

The tool outputs a JSON file mapping company names to arrays of founder names:

```json
{
  "Airbnb": [
    "Brian Chesky",
    "Joe Gebbia", 
    "Nathan Blecharczyk"
  ],
  "Dropbox": [
    "Drew Houston",
    "Arash Ferdowsi"
  ],
  "Tesla": []
}
```

## Approach & Assumptions

### Data Source
- **Wikipedia Only:** Uses only en.wikipedia.org as the data source
- **HTML Parsing:** Scrapes HTML pages using Jsoup (no JavaScript execution)
- **No External APIs:** Does not use search engines or other APIs

### Founder Extraction
- **Infobox Parsing:** Extracts founders from Wikipedia infoboxes
- **Header Matching:** Looks for "Founder(s)", "Founders", or "Founder" headers
- **Name Normalization:** Handles various name formats, Unicode characters, and HTML tags
- **Validation:** Filters to person-like names (2-4 capitalized tokens)

### Politeness & Rate Limiting
- **Custom User-Agent:** `PackVenturesFounderFinder/1.0 (+contact)`
- **Request Delays:** 400ms delay between companies
- **Retry Logic:** 3 attempts with exponential backoff for failed requests
- **Timeout:** 12-second timeout per request

### Error Handling
- **Graceful Failures:** Companies without founders get empty arrays `[]`
- **No Crashes:** Individual company failures don't stop processing
- **Comprehensive Logging:** Detailed progress and error reporting

## Future Improvements

- **Caching System:** Implement local caching to avoid re-scraping the same companies
- **Confidence Scoring:** Add confidence scores for founder matches based on Wikipedia page quality
- **Disambiguation Handling:** Better handling of Wikipedia disambiguation pages and multiple company matches
- **Parallel Processing:** Add controlled parallelism with rate limiting for faster processing of large company lists
- **Enhanced Parsing:** Support for Selenium-based scraping if JavaScript-rendered content becomes necessary
- **Multiple Languages:** Support for non-English Wikipedia versions
- **Progress Indicators:** Real-time progress bars for large company lists
- **Output Formats:** Support for CSV, XML, and other output formats


# Founder Finder

A Java CLI tool for Pack Ventures' technical screening that scrapes Wikipedia for company founders.

## Overview

This tool reads a list of companies from an input file and outputs a JSON mapping of company names to their founders, scraped from Wikipedia pages.

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Building

```bash
mvn clean package
```

This creates a fat JAR at `target/founder-finder-1.0.0.jar`.

## Running

```bash
# Basic usage (outputs to founders.json)
java -jar target/founder-finder-1.0.0.jar companies.txt

# Specify output file
java -jar target/founder-finder-1.0.0.jar companies.txt output.json
```

## Input Format

The input file should contain one company per line in the format:
```
CompanyName (https://www.company.com/)
```

Example `companies.txt`:
```
Airbnb (https://www.airbnb.com/)
Tesla (https://www.tesla.com/)
```

## Output Format

The tool outputs a JSON file mapping company names to arrays of founder names:

```json
{
  "Airbnb": ["Brian Chesky", "Nathan Blecharczyk", "Joe Gebbia"],
  "Tesla": ["Elon Musk", "Martin Eberhard", "Marc Tarpenning"]
}
```

## Assumptions

- Only Wikipedia (en.wikipedia.org) is used as the data source
- If no founders are found, the company maps to an empty array
- The tool includes polite delays between requests to avoid overwhelming Wikipedia

## Future Improvements

- Add support for multiple Wikipedia language versions
- Implement caching to avoid re-scraping the same companies
- Add progress indicators for large company lists
- Support for different output formats (CSV, XML)
- Enhanced error handling and retry logic


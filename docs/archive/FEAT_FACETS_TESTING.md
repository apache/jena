# Testing the Faceting Implementation

This guide describes how to build, deploy, and test the new faceting functionality in Apache Jena's text index module.

---

## Prerequisites

- Java 21+ (Java 25 recommended)
- Maven 3.9+
- curl (for HTTP requests)

Verify your environment:
```bash
java -version    # Should show 21+
mvn --version    # Should show 3.9+
```

---

## Step 1: Build the Project

### Build jena-text with Dependencies

```bash
cd /Users/hjohns/workspace/kurrawong/fuseki/jena

# Build jena-text and all its dependencies
mvn clean install -pl jena-text -am -DskipTests

# Verify the build - run all tests
mvn test -pl jena-text
```

Expected output:
```
Tests run: 303, Failures: 0, Errors: 0, Skipped: 8
BUILD SUCCESS
```

### Run New Faceting Tests Only

```bash
# Run the new API tests
mvn test -pl jena-text -Dtest="TestSearchExecution,TestUpdateDocumentFacets,TestTextFacetPF,TestTextQueryPFFilters,TestNativeFacetCounts,TestFacetedResults"
```

Expected output:
```
Tests run: 34, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Build Fuseki Server

```bash
# Build the Fuseki server (executable jar with jena-text included)
mvn clean install -pl jena-fuseki2/jena-fuseki-server -am -DskipTests
```

This builds `jena-fuseki-server` which is an uber-jar containing all dependencies including `jena-text`.

---

## Step 2: Create Test Configuration

### Create a Test Directory

```bash
mkdir -p ~/fuseki-facet-test
cd ~/fuseki-facet-test
```

### Create Fuseki Configuration File

Create `config.ttl`:

```turtle
PREFIX fuseki:  <http://jena.apache.org/fuseki#>
PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>
PREFIX ja:      <http://jena.hpl.hp.com/2005/11/Assembler#>
PREFIX text:    <http://jena.apache.org/text#>
PREFIX tdb2:    <http://jena.apache.org/2016/tdb#>

[] rdf:type fuseki:Server ;
   fuseki:services (
     <#service>
   ) .

<#service> rdf:type fuseki:Service ;
    fuseki:name "ds" ;
    fuseki:endpoint [ fuseki:operation fuseki:query ] ;
    fuseki:endpoint [ fuseki:operation fuseki:update ] ;
    fuseki:endpoint [ fuseki:operation fuseki:gsp-rw ] ;
    fuseki:dataset <#text_dataset> .

## Text-indexed dataset
<#text_dataset> rdf:type text:TextDataset ;
    text:dataset <#base_dataset> ;
    text:index <#indexLucene> .

## Base dataset (in-memory for testing)
<#base_dataset> rdf:type ja:MemoryDataset .

## Lucene index configuration WITH NATIVE FACETING
<#indexLucene> rdf:type text:TextIndexLucene ;
    text:directory "mem" ;           # In-memory index for testing
    text:storeValues true ;          # Store field values
    text:facetFields ("category" "author" "year") ;  # Enable native faceting on these fields
    text:entityMap <#entMap> .

## Entity mapping - defines indexed fields
<#entMap> rdf:type text:EntityMap ;
    text:entityField "uri" ;
    text:defaultField "text" ;
    text:map (
        [ text:field "text" ;     text:predicate rdfs:label ]
        [ text:field "comment" ;  text:predicate rdfs:comment ]
        [ text:field "category" ; text:predicate <http://example.org/category> ]
        [ text:field "author" ;   text:predicate <http://example.org/author> ]
        [ text:field "year" ;     text:predicate <http://example.org/year> ]
    ) .
```

**Important:** The `text:facetFields` property enables native Lucene faceting on the specified fields. This uses SortedSetDocValues for O(1) facet counting.

---

## Step 3: Start Fuseki Server

### Run the Fuseki Server

```bash
# Navigate to fuseki-server module
cd /Users/hjohns/workspace/kurrawong/fuseki/jena/jena-fuseki2/jena-fuseki-server

# Run Fuseki with the test configuration
java -jar target/jena-fuseki-server-6.0.0-SNAPSHOT.jar \
    --config ~/fuseki-facet-test/config.ttl
```

### Alternative: Run from project root

```bash
cd /Users/hjohns/workspace/kurrawong/fuseki/jena

java -jar jena-fuseki2/jena-fuseki-server/target/jena-fuseki-server-6.0.0-SNAPSHOT.jar \
    --config ~/fuseki-facet-test/config.ttl
```

The server will start on `http://localhost:3030/`

---

## Step 4: Load Test Data

### Create Test Data File

Create `test-data.ttl`:

```turtle
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix ex: <http://example.org/> .

# Technology documents
ex:doc1 rdfs:label "Introduction to Machine Learning" ;
        ex:category "technology" ;
        ex:author "Smith" ;
        ex:year "2023" .

ex:doc2 rdfs:label "Deep Learning Neural Networks" ;
        ex:category "technology" ;
        ex:author "Jones" ;
        ex:year "2023" .

ex:doc3 rdfs:label "Machine Learning for Beginners" ;
        ex:category "technology" ;
        ex:author "Smith" ;
        ex:year "2024" .

ex:doc4 rdfs:label "Advanced Machine Learning Techniques" ;
        ex:category "technology" ;
        ex:author "Brown" ;
        ex:year "2024" .

# Science documents
ex:doc5 rdfs:label "Learning About Quantum Physics" ;
        ex:category "science" ;
        ex:author "Wilson" ;
        ex:year "2023" .

ex:doc6 rdfs:label "Machine Learning in Biology" ;
        ex:category "science" ;
        ex:author "Smith" ;
        ex:year "2024" .

# Cooking documents
ex:doc7 rdfs:label "Learning to Cook Italian" ;
        ex:category "cooking" ;
        ex:author "Garcia" ;
        ex:year "2022" .

ex:doc8 rdfs:label "Learning Baking Fundamentals" ;
        ex:category "cooking" ;
        ex:author "Taylor" ;
        ex:year "2023" .
```

### Load Data via HTTP

```bash
# Load the test data (POST to default graph)
curl -X POST "http://localhost:3030/ds?default" \
    -H "Content-Type: text/turtle" \
    --data-binary @test-data.ttl

# Verify data loaded
curl -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -H "Accept: application/json" \
    -d "SELECT (COUNT(*) AS ?count) WHERE { ?s ?p ?o }"
```

**Note:** With unnamed endpoints in the config, all operations go to `/ds`:
- GSP (load data): `/ds?default` or `/ds?graph=<uri>`
- SPARQL Query: POST to `/ds` with `Content-Type: application/sparql-query`
- SPARQL Update: POST to `/ds` with `Content-Type: application/sparql-update`

---

## Step 5: Test Facet Counts (text:facet)

> **Note:** The `text:facetCounts` PF has been replaced by `text:facet` as of 2026-02-17.
> The `text:facet` PF uses JSON array syntax for facet fields and JSON object syntax for filters.

The `text:facetCounts` property function provides native Lucene faceting with O(1) counting - no document iteration required.

### Test 1: Open Facets (No Search Query)

Get facet counts for all documents without a search filter:

```bash
curl -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -H "Accept: application/json" \
    -d '
PREFIX text: <http://jena.apache.org/text#>

SELECT ?field ?value ?count
WHERE {
  (?field ?value ?count) text:facetCounts ("category" 10)
}
ORDER BY DESC(?count)
'
```

Expected output:
```json
{
  "results": {
    "bindings": [
      { "field": {"value": "category"}, "value": {"value": "technology"}, "count": {"value": "4"} },
      { "field": {"value": "category"}, "value": {"value": "science"}, "count": {"value": "2"} },
      { "field": {"value": "category"}, "value": {"value": "cooking"}, "count": {"value": "2"} }
    ]
  }
}
```

### Test 2: Multiple Facet Fields

Get counts for multiple fields at once:

```bash
curl -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -H "Accept: application/json" \
    -d '
PREFIX text: <http://jena.apache.org/text#>

SELECT ?field ?value ?count
WHERE {
  (?field ?value ?count) text:facetCounts ("category" "author" "year" 10)
}
ORDER BY ?field DESC(?count)
'
```

### Test 3: Facet Counts with Author Field

```bash
curl -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -H "Accept: application/json" \
    -d '
PREFIX text: <http://jena.apache.org/text#>

SELECT ?field ?value ?count
WHERE {
  (?field ?value ?count) text:facetCounts ("author" 10)
}
ORDER BY DESC(?count)
'
```

Expected: Smith should have count 3 (appears in doc1, doc3, doc6).

### Test 4: Filtered Facets - Counts for Search Results

Get facet counts only for documents matching a search query:

```bash
curl -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -H "Accept: application/json" \
    -d '
PREFIX text: <http://jena.apache.org/text#>

# Get category counts only for documents containing "machine AND learning"
SELECT ?field ?value ?count
WHERE {
  (?field ?value ?count) text:facetCounts ("machine AND learning" "category" 10)
}
ORDER BY DESC(?count)
'
```

Expected output:
```json
{
  "results": {
    "bindings": [
      { "field": {"value": "category"}, "value": {"value": "technology"}, "count": {"value": "4"} },
      { "field": {"value": "category"}, "value": {"value": "science"}, "count": {"value": "1"} }
    ]
  }
}
```

Note: Only technology and science categories appear because only documents with "machine learning" are counted.

### Test 5: Filtered Facets with Single Word Query

```bash
curl -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -H "Accept: application/json" \
    -d '
PREFIX text: <http://jena.apache.org/text#>

# Get author counts for documents containing "learning"
SELECT ?field ?value ?count
WHERE {
  (?field ?value ?count) text:facetCounts ("learning" "author" 10)
}
ORDER BY DESC(?count)
'
```

Expected: All authors should appear since all documents contain "learning".

**Query Detection Notes:**
- The first argument is treated as a search query if it is NOT a configured facet field name AND NOT a number
- Use `AND` for conjunction: `"machine AND learning"`
- Use quotes for phrase: `"\"machine learning\""`

---

## Step 6: Test Faceted Search (text:queryWithFacets)

### Test 1: Basic Text Search with Facets

```bash
curl -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -H "Accept: application/json" \
    -d '
PREFIX text: <http://jena.apache.org/text#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?doc ?score
WHERE {
  (?doc ?score) text:queryWithFacets ("learning") .
}
ORDER BY DESC(?score)
'
```

Expected: Returns all 8 documents containing "learning" with scores.

### Test 2: Search with Specific Property

```bash
curl -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -H "Accept: application/json" \
    -d '
PREFIX text: <http://jena.apache.org/text#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?doc ?score ?label
WHERE {
  (?doc ?score) text:queryWithFacets (rdfs:label "machine learning") .
  ?doc rdfs:label ?label .
}
ORDER BY DESC(?score)
'
```

Expected: Returns documents with "machine learning" in their label.

### Test 3: Combine Text Search with SPARQL Filtering

```bash
curl -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -H "Accept: application/json" \
    -d '
PREFIX text: <http://jena.apache.org/text#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX ex: <http://example.org/>

SELECT ?doc ?label ?category
WHERE {
  (?doc ?score) text:queryWithFacets ("learning") .
  ?doc rdfs:label ?label ;
       ex:category ?category .
  FILTER(?category = "technology")
}
ORDER BY DESC(?score)
'
```

Expected: Returns only technology documents containing "learning".

### Test 4: Aggregate Facet Counts with SPARQL

```bash
curl -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -H "Accept: application/json" \
    -d '
PREFIX text: <http://jena.apache.org/text#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX ex: <http://example.org/>

SELECT ?category (COUNT(?doc) AS ?count)
WHERE {
  (?doc ?score) text:queryWithFacets ("learning") .
  ?doc ex:category ?category .
}
GROUP BY ?category
ORDER BY DESC(?count)
'
```

Expected output:
```json
{
  "results": {
    "bindings": [
      { "category": {"value": "technology"}, "count": {"value": "4"} },
      { "category": {"value": "science"}, "count": {"value": "2"} },
      { "category": {"value": "cooking"}, "count": {"value": "2"} }
    ]
  }
}
```

---

## Step 7: Test via Java API

### Native Facet Counts API

```java
import org.apache.jena.query.text.*;
import java.util.*;

// Get facet counts directly (no document iteration)
TextIndexLucene index = ...;
List<String> facetFields = Arrays.asList("category", "author");

// Open facets - all counts
Map<String, List<FacetValue>> counts = index.getFacetCounts(facetFields, 10);

// Filtered facets - counts for matching documents
Map<String, List<FacetValue>> filtered =
    index.getFacetCounts("machine learning", facetFields, 10);

for (FacetValue fv : counts.get("category")) {
    System.out.printf("%s: %d%n", fv.getValue(), fv.getCount());
}
```

### Full Example with Dataset

Create `FacetingApiTest.java`:

```java
import org.apache.jena.query.*;
import org.apache.jena.query.text.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;
import org.apache.lucene.store.ByteBuffersDirectory;
import java.util.*;

public class FacetingApiTest {
    public static void main(String[] args) {
        // Create in-memory dataset with text index
        Dataset baseDs = DatasetFactory.create();

        // Configure text index with facet fields
        EntityDefinition entDef = new EntityDefinition("uri", "text");
        entDef.setPrimaryPredicate(RDFS.label);
        entDef.set("category",
            ResourceFactory.createProperty("http://example.org/category").asNode());
        entDef.set("author",
            ResourceFactory.createProperty("http://example.org/author").asNode());

        TextIndexConfig config = new TextIndexConfig(entDef);
        config.setValueStored(true);
        config.setFacetFields(Arrays.asList("category", "author")); // Enable native faceting

        // Create text-enabled dataset
        Dataset ds = TextDatasetFactory.createLucene(
            baseDs, new ByteBuffersDirectory(), config);

        // Load test data
        ds.begin(ReadWrite.WRITE);
        Model m = ds.getDefaultModel();
        m.read("test-data.ttl", "TURTLE");
        ds.commit();

        // Get native facet counts via Java API
        TextIndex textIndex = (TextIndex) ds.getContext().get(TextQuery.textIndex);
        if (textIndex instanceof TextIndexLucene luceneIndex) {
            System.out.println("=== Native Facet Counts (Open Facets) ===");
            Map<String, List<FacetValue>> counts =
                luceneIndex.getFacetCounts(Arrays.asList("category", "author"), 10);

            for (Map.Entry<String, List<FacetValue>> entry : counts.entrySet()) {
                System.out.println("\n" + entry.getKey() + ":");
                for (FacetValue fv : entry.getValue()) {
                    System.out.printf("  %s: %d%n", fv.getValue(), fv.getCount());
                }
            }
        }

        // Execute SPARQL query with text:facetCounts
        String queryStr = """
            PREFIX text: <http://jena.apache.org/text#>

            SELECT ?field ?value ?count
            WHERE {
              (?field ?value ?count) text:facetCounts ("category" 10)
            }
            ORDER BY DESC(?count)
            """;

        ds.begin(ReadWrite.READ);
        try (QueryExecution qe = QueryExecutionFactory.create(queryStr, ds)) {
            ResultSet rs = qe.execSelect();
            System.out.println("\n=== SPARQL Facet Results ===");
            while (rs.hasNext()) {
                QuerySolution sol = rs.next();
                System.out.printf("%s - %s: %d%n",
                    sol.getLiteral("field").getString(),
                    sol.getLiteral("value").getString(),
                    sol.getLiteral("count").getLong());
            }
        }
        ds.end();

        ds.close();
    }
}
```

---

## Step 8: Verify with Unit Tests

### Run All Faceting Tests

```bash
cd /Users/hjohns/workspace/kurrawong/fuseki/jena/jena-text

# Run all faceting-related tests
mvn test -Dtest="*Facet*"
```

Expected output:
```
Tests run: 36, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Run Individual Test Classes

```bash
# Unit tests for data structures
mvn test -Dtest=TestFacetedResults

# Integration tests with Lucene
mvn test -Dtest=TestFacetedSearchIntegration

# Performance benchmarks
mvn test -Dtest=TestFacetedSearchPerformance

# SPARQL property function tests (text:queryWithFacets)
mvn test -Dtest=TestTextQueryFacetsPF

# Native faceting tests (Java API)
mvn test -Dtest=TestNativeFacetCounts

# SPARQL property function tests (text:facetCounts)
mvn test -Dtest=TestTextFacetCountsPF
```

---

## Troubleshooting

### Server Won't Start

```bash
# Check if port 3030 is in use
lsof -i :3030

# Use alternative port
java -jar target/jena-fuseki-server-6.0.0-SNAPSHOT.jar \
    --port 3031 \
    --config ~/fuseki-facet-test/config.ttl
```

### Native Faceting Not Working

1. **Check `text:facetFields` is configured:**
```turtle
<#indexLucene> rdf:type text:TextIndexLucene ;
    text:facetFields ("category" "author" "year") ;  # Required!
    ...
```

2. **Verify field names match the entity map:**
```turtle
text:facetFields ("category") ;  # This name...
text:map (
    [ text:field "category" ; ... ]  # ...must match this name
)
```

3. **Rebuild index** if faceting was added after data was loaded

### Text Index Not Working

Verify configuration includes:
```turtle
text:storeValues true ;  # Recommended for faceting
```

### No Search Results

Check that data is loaded:
```bash
curl -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -d "SELECT * WHERE { ?s ?p ?o } LIMIT 10"
```

### "No Fuseki dispatch" Error

If you see `No Fuseki dispatch /ds/query` in the server logs, you're using the wrong endpoint.

**Problem:** Using `/ds/query` instead of `/ds`

**Solution:** With unnamed endpoints (as in the config above), all operations go to `/ds`:
```bash
# Correct:
curl -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -d "SELECT * WHERE { ?s ?p ?o }"

# Incorrect - will fail:
curl -X POST "http://localhost:3030/ds/query" ...
```

To use named endpoints like `/ds/query`, modify the config:
```turtle
fuseki:endpoint [
    fuseki:operation fuseki:query ;
    fuseki:name "query"   # <-- Add this for /ds/query endpoint
] ;
```

### Facet Counts Return Empty

1. Ensure fields are in `text:facetFields`
2. Ensure fields are mapped in `text:map`
3. Verify data has values for those fields

---

## Performance Testing

### Load Large Dataset

```bash
# Generate test data (example with 10,000 documents)
for i in $(seq 1 10000); do
    cat << EOF
ex:doc$i rdfs:label "Document about learning topic $i" ;
         ex:category "cat$((i % 10))" ;
         ex:author "author$((i % 50))" .
EOF
done > large-test-data.ttl

# Load into Fuseki
curl -X POST "http://localhost:3030/ds?default" \
    -H "Content-Type: text/turtle" \
    --data-binary @large-test-data.ttl
```

### Benchmark Native Facet Counts

```bash
# Time native facet counts (should be very fast)
time curl -s -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -d 'PREFIX text: <http://jena.apache.org/text#>
        SELECT ?field ?value ?count
        WHERE { (?field ?value ?count) text:facetCounts ("category" 10) }' \
    > /dev/null
```

### Benchmark Faceted Search

```bash
# Time a faceted query
time curl -s -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -d 'PREFIX text: <http://jena.apache.org/text#>
        SELECT ?doc WHERE { (?doc) text:queryWithFacets ("learning") }' \
    > /dev/null
```

---

## Expected Test Results Summary

| Test | Expected Result |
|------|-----------------|
| text:facetCounts (open) | All facet counts returned without search |
| text:facetCounts (filtered) | Facet counts only for matching documents |
| text:facetCounts (multiple fields) | Counts for all specified fields |
| text:queryWithFacets (basic) | Documents + scores returned |
| text:queryWithFacets (filtered) | Only matching documents |
| Category faceting | Correct counts per category |
| Author faceting | Correct counts per author |
| Year faceting | Correct counts per year |
| Empty results | No matches, empty result set |
| Performance (1K docs) | < 50ms for native facets |
| Performance (10K docs) | < 100ms for native facets |

---

## Property Function Reference

### text:facetCounts

Native Lucene faceting - O(1) counting, no document iteration. Supports both open and filtered facets.

**Syntax:**
```sparql
# Open facets (all documents)
(?field ?value ?count) text:facetCounts (field1 field2 ... maxValues)

# Filtered facets (documents matching search query)
(?field ?value ?count) text:facetCounts ("search query" field1 field2 ... maxValues)
```

**Examples:**
```sparql
# Single field - open facets
(?f ?v ?c) text:facetCounts ("category" 10)

# Multiple fields - open facets
(?f ?v ?c) text:facetCounts ("category" "author" "year" 20)

# Filtered facets - counts only for matching documents
(?f ?v ?c) text:facetCounts ("machine AND learning" "category" 10)

# Filtered facets - single word query
(?f ?v ?c) text:facetCounts ("technology" "author" "year" 10)
```

**Query Detection:**
- First argument is treated as search query if NOT a configured facet field name AND NOT a number
- Use `AND` for conjunction, standard Lucene query syntax supported

### text:queryWithFacets

Text search with facet data available via SPARQL aggregation.

**Syntax:**
```sparql
(?doc ?score) text:queryWithFacets ("query string")
(?doc ?score) text:queryWithFacets (property "query string")
```

**Examples:**
```sparql
# Basic search
(?doc ?score) text:queryWithFacets ("machine learning")

# With specific property
(?doc ?score) text:queryWithFacets (rdfs:label "machine learning")
```

---

## Clean Up

```bash
# Stop Fuseki server (Ctrl+C)

# Remove test directory
rm -rf ~/fuseki-facet-test
```

---

**Last Updated:** 2026-01-19

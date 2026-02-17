# Apache Jena Text Index - Faceting Feature Specification

This document specifies the native Lucene faceting functionality in Apache Jena's text index module.

---

## Overview

The jena-text module supports native Lucene faceting using `SortedSetDocValuesFacetCounts`, which provides:

- **Open Facets**: Get facet counts for all indexed documents (no search query required)
- **Filtered Facets**: Get facet counts constrained by a text search query
- **Structured Filters**: Filter by facet field values using JSON syntax (AND across fields, OR within)
- **Efficient Counting**: O(1) facet counting using pre-built DocValues (no document iteration)
- **SPARQL Integration**: Two property functions for facet access
- **Java API**: Direct access to faceting via `TextIndexLucene` methods

### SPARQL Property Functions

| Function | Purpose |
|----------|---------|
| `text:query` | Text search with optional JSON filter support |
| `text:facet` | Get facet counts with JSON facet fields and optional filters |

### Java API Methods

| Method | Purpose |
|--------|---------|
| `getFacetCounts(List<String> fields, int max)` | Open facets - counts for all documents |
| `getFacetCounts(String query, List<String> fields, int max)` | Filtered facets - counts for matching documents |
| `getFacetCountsWithFilters(String query, List<String> fields, Map<String,List<String>> filters, int max)` | Facets with structured filter constraints |
| `queryWithFilters(List<Resource> props, String qs, Map<String,List<String>> filters, ...)` | Text query with structured filters |
| `isFacetingEnabled()` | Check if faceting is configured |

---

## Index Configuration

### Enabling Faceting

To enable faceting, you must configure which fields support faceting in your text index definition. This is done using the `text:facetFields` property.

### Assembler Configuration (TTL)

```turtle
PREFIX fuseki:  <http://jena.apache.org/fuseki#>
PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>
PREFIX ja:      <http://jena.hpl.hp.com/2005/11/Assembler#>
PREFIX text:    <http://jena.apache.org/text#>

## Text-indexed dataset
<#text_dataset> rdf:type text:TextDataset ;
    text:dataset <#base_dataset> ;
    text:index <#indexLucene> .

## Base dataset
<#base_dataset> rdf:type ja:MemoryDataset .

## Lucene index configuration with faceting
<#indexLucene> rdf:type text:TextIndexLucene ;
    text:directory "mem" ;              # Use "mem" for in-memory, or a file path
    text:storeValues true ;             # Store field values (recommended for faceting)
    text:facetFields ("category" "author" "year") ;  # Fields to enable faceting on
    text:entityMap <#entMap> .

## Entity mapping - defines indexed fields
<#entMap> rdf:type text:EntityMap ;
    text:entityField "uri" ;
    text:defaultField "text" ;
    text:map (
        [ text:field "text" ;     text:predicate rdfs:label ]
        [ text:field "category" ; text:predicate <http://example.org/category> ]
        [ text:field "author" ;   text:predicate <http://example.org/author> ]
        [ text:field "year" ;     text:predicate <http://example.org/year> ]
    ) .
```

### Key Configuration Properties

| Property | Required | Description |
|----------|----------|-------------|
| `text:facetFields` | Yes (for faceting) | RDF list of field names to enable faceting on |
| `text:storeValues` | Recommended | Set to `true` to store field values |
| `text:entityMap` | Yes | Defines the mapping between predicates and index fields |

### Important Notes

1. **Field names in `text:facetFields` must match field names in `text:map`**
2. Fields must be indexed for faceting to work
3. Faceting uses SortedSetDocValues, which are created during indexing
4. **Existing indexes need to be rebuilt** after enabling faceting on new fields

---

## SPARQL Usage

### text:facet - Get Facet Counts

Use this when you want facet counts. Supports open facets, filtered facets, and structured filter constraints.

**Syntax:**
```sparql
# Basic: query + facet fields
(?field ?value ?count) text:facet ("query" '["field1","field2"]' maxValues)

# With filters: query + facet fields + filter constraints
(?field ?value ?count) text:facet ("query" '["field1","field2"]' '{"field":"[val]"}' maxValues)

# With property:
(?field ?value ?count) text:facet (rdfs:label "query" '["field1"]' 10)
```

**Parameters:**
- Property URI(s) - Optional, restricts which indexed fields to search
- `"query"` - Text query string
- `'["field1","field2"]'` - JSON array of facet field names to count
- `'{"field": ["value1","value2"]}'` - Optional JSON object for filter constraints
- `maxValues` - Maximum values per field (integer, optional, default 10)

**Filter semantics:**
- Multiple values within a field are **OR'd** (e.g., category=Technology OR category=Science)
- Multiple fields are **AND'd** (e.g., category=X AND author=Y)

**Returns:** Bindings for each facet value:
- `?field` - The facet field name (literal)
- `?value` - The facet value (literal)
- `?count` - Number of documents with this value (xsd:long)

**Example: Basic facet counts**
```sparql
PREFIX text: <http://jena.apache.org/text#>

SELECT ?field ?value ?count
WHERE {
    (?field ?value ?count) text:facet ("learning" '["category"]' 10)
}
ORDER BY DESC(?count)
```

**Example: Multiple facet fields**
```sparql
PREFIX text: <http://jena.apache.org/text#>

SELECT ?field ?value ?count
WHERE {
    (?field ?value ?count) text:facet ("learning" '["category", "author"]' 20)
}
ORDER BY ?field DESC(?count)
```

**Example: Facets with filter constraints**
```sparql
PREFIX text: <http://jena.apache.org/text#>

# Get author facets, filtered to only technology documents
SELECT ?field ?value ?count
WHERE {
    (?field ?value ?count) text:facet ("learning" '["author"]' '{"category": ["technology"]}' 10)
}
ORDER BY DESC(?count)
```

**Example: Multi-value filter (OR within field)**
```sparql
PREFIX text: <http://jena.apache.org/text#>

# Get author facets for technology OR science documents
SELECT ?field ?value ?count
WHERE {
    (?field ?value ?count) text:facet ("learning" '["author"]' '{"category": ["technology", "science"]}' 10)
}
ORDER BY DESC(?count)
```

### text:query - Text Search (with optional filters)

Use this for text search. Now supports optional JSON filter constraints.

**Syntax:**
```sparql
# Standard (unchanged)
(?s ?score) text:query ("query string" limit)
(?s ?score) text:query (rdfs:label "query string" limit)

# With JSON filters (new)
(?s ?score) text:query ("query string" '{"category": ["Technology"]}' limit)
(?s ?score) text:query (rdfs:label "query string" '{"category": ["Technology"], "author": ["Smith"]}' limit)
```

**Example: Standard text search (backward compatible)**
```sparql
PREFIX text: <http://jena.apache.org/text#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?doc ?score ?label
WHERE {
    (?doc ?score) text:query ("machine learning" 20) .
    ?doc rdfs:label ?label .
}
ORDER BY DESC(?score)
```

**Example: Text search filtered by category**
```sparql
PREFIX text: <http://jena.apache.org/text#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?doc ?score ?label
WHERE {
    (?doc ?score) text:query ("learning" '{"category": ["technology"]}' 20) .
    ?doc rdfs:label ?label .
}
ORDER BY DESC(?score)
```

**Example: Combining text:query and text:facet with shared execution**
```sparql
PREFIX text: <http://jena.apache.org/text#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

# When both PFs use the same query string and filters,
# they share a single Lucene execution via SearchExecution
SELECT ?doc ?label ?field ?value ?count
WHERE {
    (?doc ?score) text:query ("learning" '{"category": ["technology"]}' 20) .
    ?doc rdfs:label ?label .
    (?field ?value ?count) text:facet ("learning" '["author"]' '{"category": ["technology"]}' 10)
}
ORDER BY DESC(?score)
```

---

## Java API

### Using TextIndexLucene Directly

```java
import org.apache.jena.query.text.*;
import java.util.*;

// Get the text index from your dataset
TextIndexLucene index = ...;

// Check if faceting is enabled
if (index.isFacetingEnabled()) {
    List<String> facetFields = Arrays.asList("category", "author");

    // Open facets (all documents)
    Map<String, List<FacetValue>> counts = index.getFacetCounts(facetFields, 10);

    // Filtered by query
    Map<String, List<FacetValue>> filtered =
        index.getFacetCounts("machine learning", facetFields, 10);

    // Filtered by query AND structured filters
    Map<String, List<String>> filters = new LinkedHashMap<>();
    filters.put("category", Arrays.asList("technology", "science"));
    Map<String, List<FacetValue>> drillDown =
        index.getFacetCountsWithFilters("learning", facetFields, filters, 10);

    // Query with filters
    List<TextHit> hits = index.queryWithFilters(
        props, "learning", filters, null, null, 20, null);
}
```

---

## Architecture

### Key Classes

| Class | Purpose |
|-------|---------|
| `FacetValue` | Immutable (value, count) pair |
| `TextIndexLucene` | Core index implementation with faceting methods |
| `TextFacetPF` | SPARQL property function for `text:facet` |
| `TextQueryPF` | SPARQL property function for `text:query` (extended with filter support) |
| `SearchExecution` | Shared execution state between PFs (lazy hits + facet counts) |
| `TextIndexConfig` | Configuration including facet fields |

### Shared Execution

When `text:query` and `text:facet` appear in the same BGP with matching parameters (same properties, query string, and filters), they share a `SearchExecution` instance stored in the `ExecutionContext`. This avoids executing the Lucene query twice.

### Triple-Based Document Model

The jena-text module uses a triples-based indexing model where each triple creates a separate Lucene document. Filter queries use a two-pass approach:

1. Execute text query to find matching entity URIs
2. For each filter field, query for documents matching those URIs + filter values
3. Intersect entity URIs across all filter fields
4. Return hits/facets only for the surviving entity URIs

---

## Performance Considerations

### SortedSetDocValues Faceting

The implementation uses Lucene's `SortedSetDocValuesFacetCounts` which:

- **Does NOT iterate through documents** for counting
- Uses pre-built DocValues structure for O(1) lookups
- Requires ~25% more indexing time vs non-faceted
- Adds memory overhead for DocValues (~10-20 bytes per unique value)

### Best Practices

1. **Limit facet fields**: Only enable faceting on fields you'll actually facet on
2. **Use maxValues**: Don't request more facet values than needed
3. **Index rebuild required**: Enable faceting before loading data, or rebuild index

---

## Troubleshooting

### No Facet Results

1. **Check facet fields are configured:**
   ```turtle
   text:facetFields ("category" "author") ;
   ```

2. **Verify field names match entity map:**
   ```turtle
   text:map (
       [ text:field "category" ; text:predicate ex:category ]
   )
   ```

3. **Rebuild index** if faceting was added after data was loaded

### Errors

| Error | Cause | Solution |
|-------|-------|----------|
| "Faceting not enabled" | No `text:facetFields` configured | Add facet fields to config |
| "No facet data for field" | Field not indexed with DocValues | Rebuild index |
| "TextIndex is not a TextIndexLucene" | Using non-Lucene implementation | Use Lucene-based index |

---

**Last Updated:** 2026-02-17

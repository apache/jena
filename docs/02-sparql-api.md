# SPARQL API Reference

## Namespace Overview

| Prefix | Namespace | Mode | Description |
|--------|-----------|------|-------------|
| `text:` | `http://jena.apache.org/text#` | Classic | Upstream Jena `text:query` — no filters, no facets |
| `luc:` | `urn:jena:lucene:index#` | SHACL | `luc:query` (with filters) and `luc:facet` (facet counts) |

Classic mode (`text:entityMap`) uses `text:query` only — this is the unmodified upstream Jena text search.

SHACL mode (`text:shapes`) uses `luc:query` and `luc:facet` — these are new property functions with filter and faceting support.

---

## text:query — Text Search (Classic Mode)

The upstream Jena text search property function. Works with `text:entityMap` configuration.

### Syntax

```
(?s ?score ?literal ?graph ?prop) text:query (property* queryString limit?)
```

### Arguments (positional, left to right)

| Position | Type | Required | Description |
|----------|------|----------|-------------|
| property | URI(s) | No | RDF predicate(s) to search. If omitted, searches the default field |
| queryString | String literal | Yes | Lucene query string |
| limit | Integer | No | Max results. Negative = no limit |

### Return bindings

| Variable | Required | Type | Description |
|----------|----------|------|-------------|
| ?s | Yes | URI | Matched entity |
| ?score | No | float | Lucene relevance score |
| ?literal | No | Literal | The matched text value |
| ?graph | No | URI | Named graph of the match |
| ?prop | No | URI | Which predicate matched |

### Examples

```sparql
PREFIX text: <http://jena.apache.org/text#>

# Simple search
(?s ?score) text:query ("machine learning") .

# Search a specific property
(?s ?score) text:query (rdfs:label "machine learning") .

# Search with limit
(?s ?score) text:query ("machine learning" 20) .
```

---

## luc:query — Text Search with Filters (SHACL Mode)

Extended search property function for SHACL-mode datasets. Supports JSON filter arguments for faceted navigation.

### Syntax

```
(?s ?score ?literal ?graph ?prop) luc:query (property* queryString filter? limit?)
```

### Arguments (positional, left to right)

| Position | Type | Required | Description |
|----------|------|----------|-------------|
| property | URI(s) | No | RDF predicate(s) to search. If omitted, searches the default field |
| queryString | String literal | Yes | Lucene query string |
| filter | JSON object literal | No | Structured filter: `'{"field": ["val1", "val2"]}'` |
| limit | Integer | No | Max results. Negative = no limit |

### Return bindings

| Variable | Required | Type | Description |
|----------|----------|------|-------------|
| ?s | Yes | URI | Matched entity |
| ?score | No | float | Lucene relevance score |
| ?literal | No | Literal | The matched text value |
| ?graph | No | URI | Named graph of the match |
| ?prop | No | URI | Which predicate matched |

### Examples

```sparql
PREFIX luc: <urn:jena:lucene:index#>

# Simple search
(?s ?score) luc:query ("machine learning") .

# Search a specific property
(?s ?score) luc:query (rdfs:label "machine learning") .

# Search with limit
(?s ?score) luc:query ("machine learning" 20) .

# Search with filter (only Technology books)
(?s ?score) luc:query ("learning" '{"category": ["Technology"]}' 20) .

# Search with multi-value filter (Technology OR Science)
(?s ?score) luc:query ("learning" '{"category": ["Technology", "Science"]}') .

# Search with multi-field filter (AND across fields)
(?s ?score) luc:query ("learning" '{"category": ["Technology"], "author": ["Smith"]}') .
```

### Filter JSON format

```json
{
  "fieldName": ["value1", "value2"],
  "otherField": ["value3"]
}
```

- Values within a field: OR (matches value1 OR value2)
- Across fields: AND (must match fieldName AND otherField)
- The JSON string must start with `{` to be recognised as a filter

---

## luc:facet — Facet Counts (SHACL Mode)

### Syntax

```
(?field ?value ?count) luc:facet (queryString facetFields filter? maxValues? minCount?)
```

### Arguments (positional, left to right)

| Position | Type | Required | Description |
|----------|------|----------|-------------|
| queryString | String literal | Yes | Lucene query string |
| facetFields | JSON array literal | Yes | Fields to facet on: `'["category", "author"]'` |
| filter | JSON object literal | No | Structured filter (same format as luc:query) |
| maxValues | Integer | No | Max facet values per field. Default: 10. `0` = all values |
| minCount | Integer | No | Exclude values with count below this. Default: 0 |

### Return bindings

| Variable | Required | Type | Description |
|----------|----------|------|-------------|
| ?field | Yes | xsd:string | Facet field name |
| ?value | No | xsd:string | Facet value |
| ?count | No | xsd:long | Number of matching documents |

### Examples

```sparql
PREFIX luc: <urn:jena:lucene:index#>

# Basic facet counts
(?f ?v ?c) luc:facet ("learning" '["category"]' 10) .

# Multiple facet fields
(?f ?v ?c) luc:facet ("learning" '["category", "author"]' 10) .

# With filter applied
(?f ?v ?c) luc:facet ("learning" '["author"]' '{"category": ["Technology"]}' 10) .

# Return all facet values (maxValues=0)
(?f ?v ?c) luc:facet ("learning" '["category"]' 0) .

# With minCount threshold (exclude rare values)
(?f ?v ?c) luc:facet ("learning" '["author"]' 10 2) .

# Combine maxValues=0 with minCount
(?f ?v ?c) luc:facet ("learning" '["author"]' 0 2) .
```

---

## Shared Execution

When `luc:query` and `luc:facet` appear in the same query with matching parameters (same query string, same properties, same filters), they share a single Lucene execution:

```sparql
PREFIX luc: <urn:jena:lucene:index#>

SELECT ?s ?score ?field ?value ?count WHERE {
    (?s ?score) luc:query ("learning") .
    (?field ?value ?count) luc:facet ("learning" '["category"]' 10) .
}
```

This is efficient: one Lucene query, one index reader snapshot, consistent results.

The match is based on normalised keys — property URIs are sorted, filter maps are sorted by field name. If parameters differ, each PF executes independently.

---

## Lucene Query Syntax

The query string argument in both `luc:query` and `text:query` uses the standard Lucene query parser. Key syntax:

| Syntax | Meaning | Example |
|--------|---------|---------|
| `word1 word2` | OR — matches either term | `"machine learning"` matches "machine" OR "learning" |
| `word1 AND word2` | AND — matches both terms | `"machine AND learning"` |
| `"exact phrase"` | Phrase match | `"\"machine learning\""` (escaped quotes in SPARQL string) |
| `field:value` | Field-scoped query | `"title:learning"` |
| `wild*` | Wildcard | `"learn*"` matches "learning", "learned", etc. |
| `~` | Fuzzy match | `"learninh~"` matches "learning" |
| `-term` | Exclusion | `"learning -neural"` |

These are Lucene query parser conventions — not specific to Jena. Refer to the [Lucene Classic Query Parser documentation](https://lucene.apache.org/core/9_0_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html) for full syntax.

---

## Constructing Filters Dynamically (CDT)

The JSON filter arguments in `luc:query` and `luc:facet` can be constructed dynamically in SPARQL using Jena's CDT (Composite Datatype) extension, avoiding hardcoded filter strings:

```sparql
PREFIX cdt: <http://w3id.org/awslabs/neptune/SPARQL-CDTs/>

SELECT (FOLD(?filter, ?vals) AS ?filters)
WHERE {
  {
    SELECT ?filter (FOLD(?value) AS ?vals)
    WHERE {
      VALUES (?filter ?value) {
        ("category" "Technology")
        ("category" "Science")
        ("author"   "Smith")
      }
    }
    GROUP BY ?filter
  }
}
```

This produces a `cdt:Map` value that serializes as `{"category": ["Technology", "Science"], "author": ["Smith"]}`, suitable for passing as a filter argument. CDT `FOLD` is a Jena extension (not standard SPARQL).

---

## Java API

For programmatic access via `TextIndexLucene` (SHACL mode):

```java
// Open facets (all documents)
Map<String, List<FacetValue>> counts =
    textIndex.getFacetCounts(Arrays.asList("category"), 10);

// Filtered by query
Map<String, List<FacetValue>> filtered =
    textIndex.getFacetCounts("machine learning", Arrays.asList("category"), 10);

// With minCount
Map<String, List<FacetValue>> rare =
    textIndex.getFacetCounts("learning", Arrays.asList("author"), 10, 2);

// With structured filters
Map<String, List<String>> filters = new HashMap<>();
filters.put("category", Arrays.asList("Technology"));
Map<String, List<FacetValue>> drilled =
    textIndex.getFacetCountsWithFilters("learning", Arrays.asList("author"), filters, 10);

// Text query with filters
List<TextHit> hits =
    textIndex.queryWithFilters(props, "learning", filters, null, null, 20, null);
```

`FacetValue` is an immutable pair:

```java
facetValue.getValue()   // "Technology"
facetValue.getCount()   // 42
```

### Checking Facet Support

```java
if (textIndex.isFacetingEnabled()) {
    // Facet methods are available
}
```

`isFacetingEnabled()` returns `true` when the index has facetable fields configured (SHACL mode with `idx:facetable true` fields).

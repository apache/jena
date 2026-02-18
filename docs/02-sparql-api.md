# SPARQL API Reference

## text:query — Text Search

### Syntax

```
(?s ?score ?literal ?graph ?prop) text:query (property* queryString filter? limit?)
```

### Arguments (positional, left to right)

| Position | Type | Required | Description |
|----------|------|----------|-------------|
| property | URI(s) | No | RDF predicate(s) to search. If omitted, searches the default field |
| queryString | String literal | Yes | Lucene query string |
| filter | JSON object literal | No | Structured filter: `'{"field": ["val1", "val2"]}'` |
| limit | Integer | No | Max results. Negative = no limit |

### Return bindings (subject list)

| Variable | Required | Type | Description |
|----------|----------|------|-------------|
| ?s | Yes | URI | Matched entity |
| ?score | No | float | Lucene relevance score |
| ?literal | No | Literal | The matched text value |
| ?graph | No | URI | Named graph of the match |
| ?prop | No | URI | Which predicate matched |

### Examples

```sparql
# Simple search
(?s ?score) text:query ("machine learning") .

# Search a specific property
(?s ?score) text:query (rdfs:label "machine learning") .

# Search with limit
(?s ?score) text:query ("machine learning" 20) .

# Search with filter (only Technology books)
(?s ?score) text:query ("learning" '{"category": ["Technology"]}' 20) .

# Search with multi-value filter (Technology OR Science)
(?s ?score) text:query ("learning" '{"category": ["Technology", "Science"]}') .

# Search with multi-field filter (AND across fields)
(?s ?score) text:query ("learning" '{"category": ["Technology"], "author": ["Smith"]}') .
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

## text:facet — Facet Counts

### Syntax

```
(?field ?value ?count) text:facet (property* queryString facetFields filter? maxValues? minCount?)
```

### Arguments (positional, left to right)

| Position | Type | Required | Description |
|----------|------|----------|-------------|
| property | URI(s) | No | RDF predicate(s) constraining the text search |
| queryString | String literal | Yes | Lucene query string |
| facetFields | JSON array literal | Yes | Fields to facet on: `'["category", "author"]'` |
| filter | JSON object literal | No | Structured filter (same format as text:query) |
| maxValues | Integer | No | Max facet values per field. Default: 10. `0` = all values |
| minCount | Integer | No | Exclude values with count below this. Default: 0 |

### Return bindings (subject list)

| Variable | Required | Type | Description |
|----------|----------|------|-------------|
| ?field | Yes | xsd:string | Facet field name |
| ?value | No | xsd:string | Facet value |
| ?count | No | xsd:long | Number of matching documents |

### Examples

```sparql
# Basic facet counts
(?f ?v ?c) text:facet ("learning" '["category"]' 10) .

# Multiple facet fields
(?f ?v ?c) text:facet ("learning" '["category", "author"]' 10) .

# With filter applied
(?f ?v ?c) text:facet ("learning" '["author"]' '{"category": ["Technology"]}' 10) .

# Return all facet values (maxValues=0)
(?f ?v ?c) text:facet ("learning" '["category"]' 0) .

# With minCount threshold (exclude rare values)
(?f ?v ?c) text:facet ("learning" '["author"]' 10 2) .

# Combine maxValues=0 with minCount
(?f ?v ?c) text:facet ("learning" '["author"]' 0 2) .

# With a specific property
(?f ?v ?c) text:facet (rdfs:label "learning" '["category"]' 10) .
```

---

## Shared Execution

When `text:query` and `text:facet` appear in the same query with matching parameters (same query string, same properties, same filters), they share a single Lucene execution:

```sparql
SELECT ?s ?score ?field ?value ?count WHERE {
    (?s ?score) text:query ("learning") .
    (?field ?value ?count) text:facet ("learning" '["category"]' 10) .
}
```

This is efficient: one Lucene query, one index reader snapshot, consistent results.

The match is based on normalised keys — property URIs are sorted, filter maps are sorted by field name. If parameters differ, each PF executes independently.

---

## Java API

For programmatic access via `TextIndexLucene`:

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

# SPARQL API Reference

All property functions use the `luc:` namespace (`urn:jena:lucene:index#`).

> **Note:** The upstream Jena `text:query` property function is unchanged and still available for classic mode (`text:entityMap`). See the [Apache Jena documentation](https://jena.apache.org/documentation/query/text-query.html) for its syntax. This reference covers only the SHACL mode property functions.

---

## luc:query — Text Search with Filters

Supports field-scoped queries, CQL2-JSON filter arguments, sort pushdown, and faceted navigation.

### Syntax

```
(?s ?score ?literal ?totalHits ?graph ?field) luc:query (fieldSpec queryString filter? sort? limit? highlight?)
```

### Arguments (positional, left to right)

| Position | Type | Required | Description |
|----------|------|----------|-------------|
| fieldSpec | String literal | No | Which indexed fields to search (see below). Default: `"default"` |
| queryString | String literal | Yes | Lucene query string |
| filter | JSON object literal | No | CQL filter: `'{"op":"=","args":[{"property":"urn:jena:lucene:field#category"},"Technology"]}'` |
| sort | JSON literal | No | Sort spec: `'{"field":"urn:jena:lucene:field#year","order":"desc"}'` |
| limit | Integer | No | Max results. Negative = no limit |
| highlight | String literal | No | Highlight options: `"highlight:m:3\|z:128\|s:→\|e:←\|f:÷"` |

### Field specification

The `fieldSpec` argument controls which Lucene fields are searched. Field IRIs are required — the `idx:fieldName` (Lucene field name) is internal and not accepted here.

| Value | Meaning |
|-------|---------|
| `"default"` | Search all fields marked `idx:defaultSearch true` in the index configuration |
| `"urn:jena:lucene:field#title"` | Search only the field with this IRI |
| `'["urn:jena:lucene:field#title","urn:jena:lucene:field#description"]'` | Search multiple specific fields (JSON array of IRIs) |
| *(omitted)* | Same as `"default"` |

Field IRIs correspond to the named resource IRIs in the SHACL index configuration. They are validated at query time — an unknown IRI produces an error.

### Return bindings

| Variable | Required | Type | Description |
|----------|----------|------|-------------|
| ?s | Yes | URI | Matched entity |
| ?score | No | float | Lucene relevance score |
| ?literal | No | Node | Stored value for the matched field. KEYWORD fields return an IRI, TEXT fields return a string literal, numeric fields return typed literals |
| ?totalHits | No | xsd:long | Total matching documents (same value on every row) |
| ?graph | No | URI | Named graph of the match |
| ?field | No | URI | Field IRI identifying the matched field (bound for single-field queries, unbound for multi-field) |

The `?totalHits` binding returns the total number of documents matching the query and filters, regardless of the `limit` parameter. This is useful for displaying "Showing X of Y results" in search UIs. The value is computed efficiently using `IndexSearcher.count()` and is only evaluated when the variable is present in the subject.

The `?field` binding returns the IRI of the Lucene field that was searched. For fields defined as named resources in the configuration, the resource's own IRI is used. For fields defined on blank nodes, an auto-generated IRI of the form `urn:jena:lucene:field#{fieldName}` is used. For single-field queries, this is always bound. For multi-field queries, it is unbound.

### Examples

```sparql
PREFIX luc: <urn:jena:lucene:index#>
PREFIX field: <urn:jena:lucene:field#>

# Simple search (all default fields)
(?s ?score) luc:query ("machine learning") .

# Search with explicit "default"
(?s ?score) luc:query ("default" "machine learning") .

# Search a specific field (by IRI)
(?s ?score) luc:query ("urn:jena:lucene:field#title" "machine learning") .

# Search multiple fields (JSON array of IRIs)
(?s ?score) luc:query ('["urn:jena:lucene:field#title", "urn:jena:lucene:field#description"]' "machine learning") .

# Search with limit
(?s ?score) luc:query ("urn:jena:lucene:field#title" "machine learning" 20) .

# Search with total hit count
(?s ?score ?literal ?totalHits) luc:query ("machine learning" 20) .

# Search with field binding
(?s ?score ?lit ?totalHits ?g ?field) luc:query ("urn:jena:lucene:field#title" "machine learning" 20) .

# Search with CQL filter (only Technology books)
(?s ?score) luc:query ("default" "learning" '{"op":"=","args":[{"property":"urn:jena:lucene:field#category"},"Technology"]}' 20) .

# Search with filter and total hit count
(?s ?score ?_lit ?totalHits) luc:query ("default" "learning" '{"op":"=","args":[{"property":"urn:jena:lucene:field#category"},"Technology"]}' 20) .

# Search with sort (by field IRI)
(?s ?score) luc:query ("default" "learning" '{"field":"urn:jena:lucene:field#year","order":"desc"}' 10) .
```

### Filter JSON format (CQL2-JSON)

Filters use CQL2-JSON syntax. The `property` value is a field IRI:

```json
{"op": "=", "args": [{"property": "urn:jena:lucene:field#category"}, "Technology"]}
```

- `"="` — exact match on KEYWORD fields
- `"and"` / `"or"` — boolean combinators
- `"s_intersects"` — spatial intersection (LATLON fields)
- Numeric comparisons (`">"`, `"<"`, `">="`, `"<="`) for INT/LONG/DOUBLE fields

---

## luc:facet — Facet Counts
### Syntax

```
(?field ?value ?count) luc:facet (fieldSpec queryString facetFields filter? maxValues? minCount?)
```

### Arguments (positional, left to right)

| Position | Type | Required | Description |
|----------|------|----------|-------------|
| fieldSpec | String literal | No | Which indexed fields to scope the text query (same as luc:query). Default: `"default"` |
| queryString | String literal | Yes | Lucene query string |
| facetFields | JSON array literal | Yes | Field IRIs to facet on: `'["urn:jena:lucene:field#category"]'` |
| filter | JSON object literal | No | CQL filter (same format as luc:query) |
| maxValues | Integer | No | Max facet values per field. Default: 10. `0` = all values |
| minCount | Integer | No | Exclude values with count below this. Default: 0 |

### Return bindings

| Variable | Required | Type | Description |
|----------|----------|------|-------------|
| ?field | Yes | URI | Field IRI identifying the facet field (auto-generated `urn:jena:lucene:field#{fieldName}` for blank node fields) |
| ?value | No | Node | Facet value. KEYWORD fields return IRIs, TEXT fields return string literals |
| ?count | No | xsd:long | Number of matching documents |

### Examples

```sparql
PREFIX luc: <urn:jena:lucene:index#>

# Basic facet counts
(?f ?v ?c) luc:facet ("default" "learning" '["urn:jena:lucene:field#category"]' 10) .

# Multiple facet fields
(?f ?v ?c) luc:facet ("default" "learning" '["urn:jena:lucene:field#category", "urn:jena:lucene:field#author"]' 10) .

# With CQL filter applied
(?f ?v ?c) luc:facet ("default" "learning" '["urn:jena:lucene:field#author"]' '{"op":"=","args":[{"property":"urn:jena:lucene:field#category"},"Technology"]}' 10) .

# Return all facet values (maxValues=0)
(?f ?v ?c) luc:facet ("default" "learning" '["urn:jena:lucene:field#category"]' 0) .

# With minCount threshold (exclude rare values)
(?f ?v ?c) luc:facet ("default" "learning" '["urn:jena:lucene:field#author"]' 10 2) .

# Combine maxValues=0 with minCount
(?f ?v ?c) luc:facet ("default" "learning" '["urn:jena:lucene:field#author"]' 0 2) .
```

---

## Combining Search and Facets

Search hits and facet counts are two fundamentally different result shapes — hits are entities with scores, facets are (field, value, count) aggregations. SPARQL's tabular result model requires care when combining them.

### Recommended: Separate queries

Use one query for hits, another for facets. Each returns a clean result shape with no wasted rows.

```sparql
PREFIX luc: <urn:jena:lucene:index#>

# Query 1: search results
SELECT ?s ?score WHERE {
    (?s ?score) luc:query ("learning") .
}

# Query 2: facet counts
SELECT ?field ?value ?count WHERE {
    (?field ?value ?count) luc:facet ("default" "learning"
        '["urn:jena:lucene:field#category", "urn:jena:lucene:field#author"]' 10) .
}
```

This is the pattern used by search UIs (Elasticsearch, Solr) — one request for results, one for facets. Each result set has exactly the rows the consumer needs.

### Alternative: UNION in a single query

If a single SPARQL request is preferred, use `UNION` to return both result sets without a cartesian product:

```sparql
PREFIX luc: <urn:jena:lucene:index#>

SELECT ?s ?score ?totalHits ?field ?value ?count WHERE {
    { (?s ?score ?_lit ?totalHits) luc:query ("default" "learning" 10) . }
    UNION
    { (?field ?value ?count) luc:facet ("default" "learning"
        '["urn:jena:lucene:field#category"]' 10) . }
}
```

This returns N + M rows (not N × M). Hit rows have `?field`, `?value`, `?count` unbound; facet rows have `?s`, `?score`, `?totalHits` unbound. The consumer splits results by checking which columns are present. `?totalHits` appears on every hit row with the same value — read it from the first row. Both PFs share a single Lucene execution via `SearchExecution` (see below).

### Avoid: Combined BGP (cartesian product)

Placing both PFs in the same basic graph pattern produces a cartesian product:

```sparql
# WARNING: produces N × M rows
SELECT ?s ?score ?field ?value ?count WHERE {
    (?s ?score) luc:query ("learning") .
    (?field ?value ?count) luc:facet ("default" "learning"
        '["urn:jena:lucene:field#category"]' 10) .
}
```

With 100 hits and 10 facet values, this returns 1,000 rows — every hit paired with every facet value. The shared execution avoids redundant Lucene work, but the result set still explodes. This is a consequence of SPARQL's join semantics: two patterns with no shared variables produce a cross join.

---

## Shared Execution

When `luc:query` and `luc:facet` appear in the same SPARQL query (whether in a BGP, UNION, or subquery) with matching parameters, they share a single Lucene execution internally. One Lucene query, one index reader snapshot, consistent results.

The match is based on normalised keys — search field IRIs are sorted, CQL filter maps are sorted by field. If parameters differ, each PF executes independently.

This optimisation is transparent. It reduces Lucene index access but does not change SPARQL result semantics — the cartesian product concern (above) is a SPARQL join issue, not a Lucene execution issue.

---

## Lucene Query Syntax

The query string argument in `luc:query` uses the standard Lucene query parser. Key syntax:

| Syntax | Meaning | Example |
|--------|---------|---------|
| `word1 word2` | OR — matches either term | `"machine learning"` matches "machine" OR "learning" |
| `word1 AND word2` | AND — matches both terms | `"machine AND learning"` |
| `"exact phrase"` | Phrase match | `"\"machine learning\""` (escaped quotes in SPARQL string) |
| `field:value` | Field-scoped query | `"title:learning"` |
| `wild*` | Wildcard | `"learn*"` matches "learning", "learned", etc. |
| `~` | Fuzzy match | `"learninh~"` matches "learning" |
| `-term` | Exclusion | `"learning -neural"` |

These are Lucene query parser conventions — not specific to Jena. Refer to the [Lucene Classic Query Parser documentation](https://lucene.apache.org/core/10_3_1/queryparser/org/apache/lucene/queryparser/classic/package-summary.html) for full syntax.

---

## Java API

For programmatic access via `ShaclTextIndexLucene`. The Java API accepts Lucene field names directly (these are the `idx:fieldName` values from the configuration):

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

// With search fields scoping
Map<String, List<FacetValue>> scoped =
    textIndex.getFacetCounts("learning", List.of("title"),
        Arrays.asList("author"), 10);

// Text query with field scoping
List<TextHit> hits =
    textIndex.queryByFields(List.of("title"), "learning", null, null, 20, null);

// Count total matching documents (efficient — uses IndexSearcher.count())
long total = textIndex.countQueryWithCql("learning", null, null);
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

`isFacetingEnabled()` returns `true` when the index has facetable fields configured (`idx:facetable true` on one or more fields).

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

Extended search property function for SHACL-mode datasets. Supports field-scoped queries, JSON filter arguments, sort pushdown, and faceted navigation.

### Syntax

```
(?s ?score ?literal ?totalHits ?graph ?field) luc:query (fieldSpec queryString filter? sort? limit? highlight?)
```

### Arguments (positional, left to right)

| Position | Type | Required | Description |
|----------|------|----------|-------------|
| fieldSpec | String literal | No | Which indexed fields to search (see below). Default: `"default"` |
| queryString | String literal | Yes | Lucene query string |
| filter | JSON object literal | No | CQL filter: `'{"op":"=","args":[{"property":"category"},"Technology"]}'` |
| sort | JSON literal | No | Sort spec: `'{"field":"year","order":"desc"}'` or `'[{"field":"year"},{"field":"title"}]'` |
| limit | Integer | No | Max results. Negative = no limit |
| highlight | String literal | No | Highlight options: `"highlight:m:3\|z:128\|s:→\|e:←\|f:÷"` |

### Field specification

The `fieldSpec` argument controls which Lucene fields are searched:

| Value | Meaning |
|-------|---------|
| `"default"` | Search all fields marked `idx:defaultSearch true` in the index configuration |
| `"title"` | Search only the `title` field (must be a valid `idx:fieldName`) |
| `'["title","description"]'` | Search multiple specific fields (JSON array) |
| *(omitted)* | Same as `"default"` |

Field names correspond to `idx:fieldName` values in the SHACL index configuration. They are validated at query time — an unknown field name produces an error.

### Return bindings

| Variable | Required | Type | Description |
|----------|----------|------|-------------|
| ?s | Yes | URI | Matched entity |
| ?score | No | float | Lucene relevance score |
| ?literal | No | Literal | The matched text value |
| ?totalHits | No | xsd:long | Total matching documents (same value on every row) |
| ?graph | No | URI | Named graph of the match |
| ?field | No | xsd:string | Which field matched (bound for single-field queries, unbound for multi-field) |

The `?totalHits` binding returns the total number of documents matching the query and filters, regardless of the `limit` parameter. This is useful for displaying "Showing X of Y results" in search UIs. The value is computed efficiently using `IndexSearcher.count()` and is only evaluated when the variable is present in the subject.

The `?field` binding returns the name of the Lucene field that was searched. For single-field queries (e.g., `"title"`), this is always bound to that field name as a literal. For multi-field queries (e.g., `"default"` resolving to multiple fields), it is unbound.

### Examples

```sparql
PREFIX luc: <urn:jena:lucene:index#>

# Simple search (all default fields)
(?s ?score) luc:query ("machine learning") .

# Search with explicit "default"
(?s ?score) luc:query ("default" "machine learning") .

# Search a specific field
(?s ?score) luc:query ("title" "machine learning") .

# Search multiple fields
(?s ?score) luc:query ('["title", "description"]' "machine learning") .

# Search with limit
(?s ?score) luc:query ("title" "machine learning" 20) .

# Search with total hit count
(?s ?score ?literal ?totalHits) luc:query ("machine learning" 20) .

# Search with field binding
(?s ?score ?lit ?totalHits ?g ?field) luc:query ("title" "machine learning" 20) .

# Search with CQL filter (only Technology books)
(?s ?score) luc:query ("default" "learning" '{"op":"=","args":[{"property":"category"},"Technology"]}' 20) .

# Search with filter and total hit count
(?s ?score ?_lit ?totalHits) luc:query ("default" "learning" '{"op":"=","args":[{"property":"category"},"Technology"]}' 20) .

# Search with sort
(?s ?score) luc:query ("default" "learning" '{"field":"year","order":"desc"}' 10) .
```

### Filter JSON format (CQL2-JSON)

Filters use CQL2-JSON syntax:

```json
{"op": "=", "args": [{"property": "category"}, "Technology"]}
```

- `"="` — exact match on KEYWORD fields
- `"and"` / `"or"` — boolean combinators
- `"s_intersects"` — spatial intersection (LATLON fields)
- Numeric comparisons (`">"`, `"<"`, `">="`, `"<="`) for INT/LONG/DOUBLE fields

---

## luc:facet — Facet Counts (SHACL Mode)

### Syntax

```
(?field ?value ?count) luc:facet (fieldSpec queryString facetFields filter? maxValues? minCount?)
```

### Arguments (positional, left to right)

| Position | Type | Required | Description |
|----------|------|----------|-------------|
| fieldSpec | String literal | No | Which indexed fields to scope the text query (same as luc:query). Default: `"default"` |
| queryString | String literal | Yes | Lucene query string |
| facetFields | JSON array literal | Yes | Fields to facet on: `'["category", "author"]'` |
| filter | JSON object literal | No | CQL filter (same format as luc:query) |
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
(?f ?v ?c) luc:facet ("default" "learning" '["category"]' 10) .

# Multiple facet fields
(?f ?v ?c) luc:facet ("default" "learning" '["category", "author"]' 10) .

# With CQL filter applied
(?f ?v ?c) luc:facet ("default" "learning" '["author"]' '{"op":"=","args":[{"property":"category"},"Technology"]}' 10) .

# Return all facet values (maxValues=0)
(?f ?v ?c) luc:facet ("default" "learning" '["category"]' 0) .

# With minCount threshold (exclude rare values)
(?f ?v ?c) luc:facet ("default" "learning" '["author"]' 10 2) .

# Combine maxValues=0 with minCount
(?f ?v ?c) luc:facet ("default" "learning" '["author"]' 0 2) .
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
    (?field ?value ?count) luc:facet ("default" "learning" '["category", "author"]' 10) .
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
    { (?field ?value ?count) luc:facet ("default" "learning" '["category"]' 10) . }
}
```

This returns N + M rows (not N × M). Hit rows have `?field`, `?value`, `?count` unbound; facet rows have `?s`, `?score`, `?totalHits` unbound. The consumer splits results by checking which columns are present. `?totalHits` appears on every hit row with the same value — read it from the first row. Both PFs share a single Lucene execution via `SearchExecution` (see below).

### Avoid: Combined BGP (cartesian product)

Placing both PFs in the same basic graph pattern produces a cartesian product:

```sparql
# WARNING: produces N × M rows
SELECT ?s ?score ?field ?value ?count WHERE {
    (?s ?score) luc:query ("learning") .
    (?field ?value ?count) luc:facet ("default" "learning" '["category"]' 10) .
}
```

With 100 hits and 10 facet values, this returns 1,000 rows — every hit paired with every facet value. The shared execution avoids redundant Lucene work, but the result set still explodes. This is a consequence of SPARQL's join semantics: two patterns with no shared variables produce a cross join.

---

## Shared Execution

When `luc:query` and `luc:facet` appear in the same SPARQL query (whether in a BGP, UNION, or subquery) with matching parameters, they share a single Lucene execution internally. One Lucene query, one index reader snapshot, consistent results.

The match is based on normalised keys — search field names are sorted, CQL filter maps are sorted by field name. If parameters differ, each PF executes independently.

This optimisation is transparent. It reduces Lucene index access but does not change SPARQL result semantics — the cartesian product concern (above) is a SPARQL join issue, not a Lucene execution issue.

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

These are Lucene query parser conventions — not specific to Jena. Refer to the [Lucene Classic Query Parser documentation](https://lucene.apache.org/core/10_3_1/queryparser/org/apache/lucene/queryparser/classic/package-summary.html) for full syntax.

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

For programmatic access via `ShaclTextIndexLucene` (SHACL mode):

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

`isFacetingEnabled()` returns `true` when the index has facetable fields configured (SHACL mode with `idx:facetable true` fields).

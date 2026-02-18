# User Guide: Faceted Search in Jena Text

## Overview

The `jena-text` module provides full-text search over RDF data using Apache Lucene. This fork adds **native faceted search** — the ability to get categorised counts alongside text search results, the same pattern used by e-commerce sites, library catalogues, and data portals.

Two indexing modes are available:

| Mode | Config property | SPARQL prefix | Document model | Best for |
|------|----------------|---------------|---------------|----------|
| **Classic** | `text:entityMap` | `text:` | One Lucene doc per RDF triple | Backward compatible, simple text search |
| **SHACL** | `text:shapes` | `luc:` | One Lucene doc per entity | Faceted navigation, numeric fields, filtering |

Classic mode uses `text:query` (upstream Jena, unchanged). SHACL mode uses `luc:query` and `luc:facet` for search with filters and facet counts.

---

## Getting Started

### 1. Define your index configuration

**Classic mode** — standard Jena text search:

```turtle
@prefix text: <http://jena.apache.org/text#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .

<#index> a text:TextIndexLucene ;
    text:directory "mem" ;
    text:entityMap <#entMap> ;
    text:storeValues true ;
    .

<#entMap> a text:EntityMap ;
    text:entityField "uri" ;
    text:defaultField "text" ;
    text:map (
        [ text:field "text" ; text:predicate rdfs:label ]
        [ text:field "category" ; text:predicate <http://example.org/category> ]
        [ text:field "author" ; text:predicate <http://example.org/author> ]
    ) .
```

**SHACL mode** — entity-per-document with faceting and typed fields:

```turtle
@prefix text:  <http://jena.apache.org/text#> .
@prefix idx:   <urn:jena:lucene:index#> .
@prefix sh:    <http://www.w3.org/ns/shacl#> .
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
@prefix ex:    <http://example.org/> .

<#index> a text:TextIndexLucene ;
    text:directory "mem" ;
    text:shapes ( <#BookShape> ) ;
    text:storeValues true ;
    .

<#BookShape>
    sh:targetClass ex:Book ;
    sh:property [
        idx:fieldName "title" ;
        idx:fieldType idx:TextField ;
        idx:defaultSearch true ;
        sh:path rdfs:label ;
    ] ;
    sh:property [
        idx:fieldName "category" ;
        idx:fieldType idx:KeywordField ;
        idx:facetable true ;
        idx:multiValued true ;
        sh:path ex:category ;
    ] ;
    sh:property [
        idx:fieldName "author" ;
        idx:fieldType idx:KeywordField ;
        idx:facetable true ;
        sh:path ex:author ;
    ] ;
    sh:property [
        idx:fieldName "year" ;
        idx:fieldType idx:IntField ;
        idx:sortable true ;
        sh:path ex:year ;
    ] .
```

### 2. Load data

Data is indexed automatically when triples are added to a text-indexed dataset:

```sparql
PREFIX ex: <http://example.org/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

INSERT DATA {
    ex:book1 a ex:Book ;
        rdfs:label "Introduction to Machine Learning" ;
        ex:category "Technology" ;
        ex:author "Smith" ;
        ex:year 2024 .

    ex:book2 a ex:Book ;
        rdfs:label "Quantum Physics Basics" ;
        ex:category "Science" ;
        ex:author "Wilson" ;
        ex:year 2023 .
}
```

### 3. Search

**Classic mode — `text:query`:**

```sparql
PREFIX text: <http://jena.apache.org/text#>

SELECT ?s ?score WHERE {
    (?s ?score) text:query ("machine learning") .
}
```

**SHACL mode — `luc:query`:**

```sparql
PREFIX luc: <urn:jena:lucene:index#>

SELECT ?s ?score WHERE {
    (?s ?score) luc:query ("machine learning") .
}
```

### 4. Get facet counts (SHACL mode only)

```sparql
PREFIX luc: <urn:jena:lucene:index#>

SELECT ?field ?value ?count WHERE {
    (?field ?value ?count) luc:facet ("machine learning" '["category", "author"]' 10) .
}
```

Returns rows like:

| ?field | ?value | ?count |
|--------|--------|--------|
| "category" | "Technology" | 3 |
| "category" | "Science" | 1 |
| "author" | "Smith" | 2 |
| "author" | "Jones" | 1 |

### 5. Search with facet filtering (SHACL mode only)

```sparql
PREFIX luc: <urn:jena:lucene:index#>

# Only return results where category is "Technology"
SELECT ?s ?score WHERE {
    (?s ?score) luc:query ("learning" '{"category": ["Technology"]}' 20) .
}
```

**Facets with filters applied:**

```sparql
PREFIX luc: <urn:jena:lucene:index#>

# Get author counts, but only for Technology books
SELECT ?field ?value ?count WHERE {
    (?field ?value ?count) luc:facet (
        "learning"
        '["author"]'
        '{"category": ["Technology"]}'
        10
    ) .
}
```

### 6. Combine search and facets in one query

When `luc:query` and `luc:facet` appear in the same query with matching parameters, they automatically share execution (one Lucene query, not two):

```sparql
PREFIX luc: <urn:jena:lucene:index#>

SELECT ?s ?score ?field ?value ?count WHERE {
    (?s ?score) luc:query ("learning") .
    (?field ?value ?count) luc:facet ("learning" '["category"]' 10) .
}
```

---

## Filter Semantics

Filters use JSON syntax:

```json
{"category": ["Technology", "Science"], "author": ["Smith"]}
```

- **Within a field:** values are OR'd — matches Technology OR Science
- **Across fields:** fields are AND'd — must match category AND author
- This is the standard faceted navigation pattern

---

## Key Options

| Option | Where | Effect |
|--------|-------|--------|
| `maxValues` | `luc:facet` arg | Max facet values per field. `0` = return all values |
| `minCount` | `luc:facet` arg | Exclude values with count below this threshold |
| `text:maxFacetHits` | Assembler config (SHACL only) | Limit internal Lucene search for facet collection. `0` = unlimited |
| `text:storeValues` | Assembler config | Store literal values for retrieval in results |

---

## Deploying with Fuseki

### Prerequisites

- Java 21+ (Java 25 recommended)
- Maven 3.9+

### Build the Fuseki Server

```bash
cd jena

# Build jena-text and all dependencies first
mvn clean install -pl jena-text -am -DskipTests

# Build the Fuseki server (uber-jar including jena-text)
mvn clean install -pl jena-fuseki2/jena-fuseki-server -am -DskipTests
```

### Create a Fuseki Configuration File

Create `config.ttl` for a SHACL-mode text-indexed dataset:

```turtle
PREFIX fuseki:  <http://jena.apache.org/fuseki#>
PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>
PREFIX ja:      <http://jena.hpl.hp.com/2005/11/Assembler#>
PREFIX text:    <http://jena.apache.org/text#>
PREFIX idx:     <urn:jena:lucene:index#>
PREFIX sh:      <http://www.w3.org/ns/shacl#>
PREFIX ex:      <http://example.org/>

[] rdf:type fuseki:Server ;
   fuseki:services ( <#service> ) .

<#service> rdf:type fuseki:Service ;
    fuseki:name "ds" ;
    fuseki:endpoint [ fuseki:operation fuseki:query ] ;
    fuseki:endpoint [ fuseki:operation fuseki:update ] ;
    fuseki:endpoint [ fuseki:operation fuseki:gsp-rw ] ;
    fuseki:dataset <#text_dataset> .

<#text_dataset> rdf:type text:TextDataset ;
    text:dataset <#base_dataset> ;
    text:index <#index> .

<#base_dataset> rdf:type ja:MemoryDataset .

<#index> rdf:type text:TextIndexLucene ;
    text:directory "mem" ;
    text:shapes ( <#BookShape> ) ;
    text:storeValues true ;
    .

<#BookShape>
    sh:targetClass ex:Book ;
    sh:property [
        idx:fieldName "title" ;
        idx:fieldType idx:TextField ;
        idx:defaultSearch true ;
        sh:path rdfs:label ;
    ] ;
    sh:property [
        idx:fieldName "category" ;
        idx:fieldType idx:KeywordField ;
        idx:facetable true ;
        sh:path ex:category ;
    ] ;
    sh:property [
        idx:fieldName "author" ;
        idx:fieldType idx:KeywordField ;
        idx:facetable true ;
        sh:path ex:author ;
    ] .
```

### Start the Server

```bash
java -jar jena-fuseki2/jena-fuseki-server/target/jena-fuseki-server-*.jar \
    --config config.ttl
```

The server starts on `http://localhost:3030/`. Use `--port 3031` for an alternative port.

### Load Data

```bash
curl -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-update" \
    -d '
PREFIX ex: <http://example.org/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

INSERT DATA {
    ex:book1 a ex:Book ; rdfs:label "Introduction to Machine Learning" ;
        ex:category "Technology" ; ex:author "Smith" .
    ex:book2 a ex:Book ; rdfs:label "Quantum Physics Basics" ;
        ex:category "Science" ; ex:author "Wilson" .
    ex:book3 a ex:Book ; rdfs:label "Deep Learning Neural Networks" ;
        ex:category "Technology" ; ex:author "Jones" .
}'
```

### Test Queries

```bash
# Search
curl -s -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -H "Accept: application/json" \
    -d 'PREFIX luc: <urn:jena:lucene:index#>
SELECT ?s ?score WHERE {
  (?s ?score) luc:query ("learning") .
} ORDER BY DESC(?score)'

# Facets
curl -s -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -H "Accept: application/json" \
    -d 'PREFIX luc: <urn:jena:lucene:index#>
SELECT ?f ?v ?c WHERE {
  (?f ?v ?c) luc:facet ("learning" '\''["category", "author"]'\'' 10)
} ORDER BY ?f DESC(?c)'

# Search with filter
curl -s -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -H "Accept: application/json" \
    -d 'PREFIX luc: <urn:jena:lucene:index#>
SELECT ?s ?score WHERE {
  (?s ?score) luc:query ("learning" '\''{"category": ["Technology"]}'\'' 20)
} ORDER BY DESC(?score)'
```

**Note:** With unnamed endpoints (as in the config above), all operations go to `/ds`. Do not use `/ds/query` or `/ds/update` — those require named endpoints in the config.

---

## Troubleshooting

### No Search Results

1. **Verify data is loaded:**
   ```sparql
   SELECT (COUNT(*) AS ?count) WHERE { ?s ?p ?o }
   ```
2. **Check that entities have the correct `rdf:type`** — SHACL mode only indexes entities matching a shape's `sh:targetClass`
3. **Verify `text:storeValues true`** is set in the assembler config

### No Facet Results

1. **Check that fields have `idx:facetable true`** in the shape definition
2. **Verify field names match** between the shape config and the `luc:facet` JSON array argument
3. **Rebuild the index** if faceting was enabled after data was loaded — SortedSetDocValues are built at write time

### "No Fuseki dispatch" Error

Using `/ds/query` or `/ds/update` with unnamed endpoints will fail. Either:
- Use `/ds` for all operations (content type determines the operation), or
- Add named endpoints in the config:
  ```turtle
  fuseki:endpoint [
      fuseki:operation fuseki:query ;
      fuseki:name "query"    # enables /ds/query
  ] ;
  ```

### Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| `text:shapes and text:entityMap are mutually exclusive` | Both specified on the same index | Use one or the other |
| `TextIndexException: no shapes defined` | `text:shapes` list is empty | Add at least one shape resource |
| No facet data for a field | Field not marked `idx:facetable true`, or index not rebuilt | Check config and reindex |
| Port already in use | Another process on port 3030 | Use `--port 3031` or stop the other process |

### Performance Issues

- Set `text:maxFacetHits` in the assembler config to limit facet collection scope for large indexes
- Use `maxValues` and `minCount` arguments in `luc:facet` to reduce result size
- See [Architecture — Performance Characteristics](04-architecture.md#performance-characteristics) for tuning guidance

---

## Classic vs SHACL Mode

| Aspect | Classic (`text:entityMap`) | SHACL (`text:shapes`) |
|--------|--------------------------|----------------------|
| SPARQL prefix | `text:query` | `luc:query`, `luc:facet` |
| Document model | One Lucene doc per triple | One Lucene doc per entity |
| Faceting | Not supported | Native facet counts via `luc:facet` |
| Filters | Not supported | JSON filter arg on `luc:query` |
| Field types | Text only | Text, Keyword, Int, Long, Double |
| Numeric fields | No | Yes |
| Per-field config | Limited (analyzer only) | Full (stored, indexed, facetable, sortable, multiValued) |
| Existing data | Works with existing indexes | Requires reindex |
| Backward compat | Full (upstream Jena) | New feature |

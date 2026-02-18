# User Guide: Faceted Search in Jena Text

## Overview

The `jena-text` module provides full-text search over RDF data using Apache Lucene. This fork adds **native faceted search** — the ability to get categorised counts alongside text search results, the same pattern used by e-commerce sites, library catalogues, and data portals.

Two indexing modes are available:

| Mode | Config property | Document model | Best for |
|------|----------------|---------------|----------|
| **Classic** | `text:entityMap` | One Lucene doc per RDF triple | Backward compatible, simple text search |
| **SHACL** | `text:shapes` | One Lucene doc per entity | Faceted navigation, numeric fields, filtering |

Both modes support `text:query` for search and `text:facet` for facet counts.

---

## Getting Started

### 1. Define your index configuration

**Classic mode** — add `text:facetFields` to enable faceting on specific fields:

```turtle
@prefix text: <http://jena.apache.org/text#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .

<#index> a text:TextIndexLucene ;
    text:directory "mem" ;
    text:entityMap <#entMap> ;
    text:facetFields ("category" "author") ;
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

**SHACL mode** — define shapes for entity-per-document indexing:

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

### 3. Search and get facets

**Basic text search:**

```sparql
PREFIX text: <http://jena.apache.org/text#>

SELECT ?s ?score WHERE {
    (?s ?score) text:query ("machine learning") .
}
```

**Get facet counts:**

```sparql
PREFIX text: <http://jena.apache.org/text#>

SELECT ?field ?value ?count WHERE {
    (?field ?value ?count) text:facet ("machine learning" '["category", "author"]' 10) .
}
```

Returns rows like:

| ?field | ?value | ?count |
|--------|--------|--------|
| "category" | "Technology" | 3 |
| "category" | "Science" | 1 |
| "author" | "Smith" | 2 |
| "author" | "Jones" | 1 |

**Search with facet filtering:**

```sparql
PREFIX text: <http://jena.apache.org/text#>

# Only return results where category is "Technology"
SELECT ?s ?score WHERE {
    (?s ?score) text:query ("learning" '{"category": ["Technology"]}' 20) .
}
```

**Facets with filters applied:**

```sparql
PREFIX text: <http://jena.apache.org/text#>

# Get author counts, but only for Technology books
SELECT ?field ?value ?count WHERE {
    (?field ?value ?count) text:facet (
        "learning"
        '["author"]'
        '{"category": ["Technology"]}'
        10
    ) .
}
```

### 4. Combine search and facets in one query

When `text:query` and `text:facet` appear in the same query with matching parameters, they automatically share execution (one Lucene query, not two):

```sparql
PREFIX text: <http://jena.apache.org/text#>

SELECT ?s ?score ?field ?value ?count WHERE {
    (?s ?score) text:query ("learning") .
    (?field ?value ?count) text:facet ("learning" '["category"]' 10) .
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
| `maxValues` | `text:facet` arg | Max facet values per field. `0` = return all values |
| `minCount` | `text:facet` arg | Exclude values with count below this threshold |
| `text:maxFacetHits` | Assembler config | Limit internal Lucene search for facet collection. `0` = unlimited |
| `text:storeValues` | Assembler config | Store literal values for retrieval in results |

---

## Classic vs SHACL Mode

| Aspect | Classic (`text:entityMap`) | SHACL (`text:shapes`) |
|--------|--------------------------|----------------------|
| Document model | One Lucene doc per triple | One Lucene doc per entity |
| Facet accuracy | Requires URI-join (two-pass) | Single-pass (all fields colocated) |
| Field types | Text only | Text, Keyword, Int, Long, Double |
| Numeric fields | No | Yes |
| Per-field config | Limited (analyzer only) | Full (stored, indexed, facetable, sortable, multiValued) |
| Existing data | Works with existing indexes | Requires reindex |
| Backward compat | Full | New feature |

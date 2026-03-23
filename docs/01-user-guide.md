# User Guide: Faceted Search in Jena Text

## Overview

The `jena-text` module provides full-text search over RDF data using Apache Lucene. This fork adds **entity-per-document indexing with native faceted search** — the ability to get categorised counts alongside text search results, the same pattern used by e-commerce sites, library catalogues, and data portals.

SHACL shapes define entity types with typed fields. Each entity matching a shape's `sh:targetClass` gets one Lucene document containing all its fields. Search uses `luc:query` (with CQL2-JSON filters) and `luc:facet` (for facet counts).

> **Note:** The upstream Jena `text:query` / `text:entityMap` (classic mode) is unchanged and still available. This documentation covers only the SHACL mode added by this fork.

---

## Getting Started

### 1. Define your index configuration

```turtle
@prefix text:  <http://jena.apache.org/text#> .
@prefix idx:   <urn:jena:lucene:index#> .
@prefix field: <urn:jena:lucene:field#> .
@prefix sh:    <http://www.w3.org/ns/shacl#> .
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
@prefix ex:    <http://example.org/> .

<#index> a text:TextIndexLucene ;
    text:directory "mem" ;
    text:shapes ( <#BookShape> ) ;
    text:storeValues true ;
    .

## Named field resources — their IRIs identify fields in SPARQL queries
field:title
    idx:fieldName "title" ;
    idx:fieldType idx:TextField ;
    idx:defaultSearch true ;
    sh:path rdfs:label .

field:category
    idx:fieldName "category" ;
    idx:fieldType idx:KeywordField ;
    idx:facetable true ;
    idx:multiValued true ;
    sh:path ex:category .

field:author
    idx:fieldName "author" ;
    idx:fieldType idx:KeywordField ;
    idx:facetable true ;
    sh:path ex:author .

field:authorName
    idx:fieldName "authorName" ;
    idx:fieldType idx:KeywordField ;
    idx:facetable true ;
    sh:path ( ex:writtenBy ex:name ) .  ## sequence path — indexes author name on the book

field:year
    idx:fieldName "year" ;
    idx:fieldType idx:IntField ;
    idx:sortable true ;
    sh:path ex:year .

<#BookShape>
    sh:targetClass ex:Book ;
    sh:property field:title ;
    sh:property field:category ;
    sh:property field:author ;
    sh:property field:authorName ;
    sh:property field:year .
```

Each field is a **named resource** with a stable, absolute IRI (e.g., `urn:jena:lucene:field#category`). This IRI is used in SPARQL queries to identify fields — in `luc:query` field specs, `luc:facet` facet field arrays, CQL2-JSON filter properties, and sort specs. The `idx:fieldName` property defines the internal Lucene field name and is not used in SPARQL.

Fields defined as blank nodes get auto-generated IRIs (`urn:jena:lucene:field#{fieldName}`). Named resources are recommended — they enable field reuse across multiple shapes and support sequence/inverse paths for cross-entity indexing without forward chaining (see [Configuration Reference](03-configuration.md)).

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

```sparql
PREFIX luc: <urn:jena:lucene:index#>

SELECT ?s ?score WHERE {
    (?s ?score) luc:query ("machine learning") .
}
```

### 4. Get facet counts

```sparql
PREFIX luc: <urn:jena:lucene:index#>

SELECT ?field ?value ?count WHERE {
    (?field ?value ?count) luc:facet ("default" "machine learning"
        '["urn:jena:lucene:field#category", "urn:jena:lucene:field#author"]' 10) .
}
```

Returns rows like:

| ?field | ?value | ?count |
|--------|--------|--------|
| `<urn:jena:lucene:field#category>` | `<http://example.org/Technology>` | 3 |
| `<urn:jena:lucene:field#category>` | `<http://example.org/Science>` | 1 |
| `<urn:jena:lucene:field#author>` | `<http://example.org/Smith>` | 2 |
| `<urn:jena:lucene:field#author>` | `<http://example.org/Jones>` | 1 |

`?field` returns the field IRI (the named resource from config). `?value` returns IRIs for KEYWORD fields, string literals for TEXT fields.

The `facetFields` array requires field IRIs — the full IRI of each field resource as defined in the configuration.

### 5. Search with facet filtering
Filters use CQL2-JSON syntax. The `property` value must be a field IRI:

```sparql
PREFIX luc: <urn:jena:lucene:index#>

# Only return results where category is "Technology"
SELECT ?s ?score WHERE {
    (?s ?score) luc:query ("default" "learning"
        '{"op":"=","args":[{"property":"urn:jena:lucene:field#category"},"Technology"]}'
        20) .
}
```

**Facets with filters applied:**

```sparql
PREFIX luc: <urn:jena:lucene:index#>

# Get author counts, but only for Technology books
SELECT ?field ?value ?count WHERE {
    (?field ?value ?count) luc:facet (
        "default"
        "learning"
        '["urn:jena:lucene:field#author"]'
        '{"op":"=","args":[{"property":"urn:jena:lucene:field#category"},"Technology"]}'
        10
    ) .
}
```

### 6. Get total hit count

Add `?totalHits` as the 4th subject variable to get the total number of matching documents. This is useful for "Showing X of Y results" UI patterns:

```sparql
PREFIX luc: <urn:jena:lucene:index#>

SELECT ?s ?score ?totalHits WHERE {
    (?s ?score ?_lit ?totalHits) luc:query ("learning" 10) .
}
```

`?totalHits` is the same value on every row — read it from the first result. The count is computed efficiently using `IndexSearcher.count()` and only runs when the variable is present.

### 7. Combine search and facets in one query

When `luc:query` and `luc:facet` appear in the same query with matching parameters, they automatically share execution (one Lucene query, not two):

```sparql
PREFIX luc: <urn:jena:lucene:index#>

SELECT ?s ?score ?totalHits ?field ?value ?count WHERE {
    { (?s ?score ?_lit ?totalHits) luc:query ("learning" 10) }
    UNION
    { (?field ?value ?count) luc:facet ("learning"
        '["urn:jena:lucene:field#category"]' 10) }
}
```

---

## Filter Semantics

Filters use CQL2-JSON syntax. The `property` value is a field IRI.

Single equality:

```json
{"op": "=", "args": [{"property": "urn:jena:lucene:field#category"}, "Technology"]}
```

Multiple values (OR within a field):

```json
{"op": "or", "args": [
    {"op": "=", "args": [{"property": "urn:jena:lucene:field#category"}, "Technology"]},
    {"op": "=", "args": [{"property": "urn:jena:lucene:field#category"}, "Science"]}
]}
```

Multiple fields (AND across fields):

```json
{"op": "and", "args": [
    {"op": "=", "args": [{"property": "urn:jena:lucene:field#category"}, "Technology"]},
    {"op": "=", "args": [{"property": "urn:jena:lucene:field#author"}, "Smith"]}
]}
```

See [SPARQL API Reference](02-sparql-api.md) for the full CQL2-JSON syntax.

---

## Key Options

| Option | Where | Effect |
|--------|-------|--------|
| `maxValues` | `luc:facet` arg | Max facet values per field. `0` = return all values |
| `minCount` | `luc:facet` arg | Exclude values with count below this threshold |
| `text:maxFacetHits` | Assembler config | Limit internal Lucene search for facet collection. `0` = unlimited |
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
PREFIX field:   <urn:jena:lucene:field#>
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

field:title
    idx:fieldName "title" ;
    idx:fieldType idx:TextField ;
    idx:defaultSearch true ;
    sh:path rdfs:label .

field:category
    idx:fieldName "category" ;
    idx:fieldType idx:KeywordField ;
    idx:facetable true ;
    sh:path ex:category .

field:author
    idx:fieldName "author" ;
    idx:fieldType idx:KeywordField ;
    idx:facetable true ;
    sh:path ex:author .

<#BookShape>
    sh:targetClass ex:Book ;
    sh:property field:title ;
    sh:property field:category ;
    sh:property field:author .
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
  (?f ?v ?c) luc:facet ("learning" '\''["urn:jena:lucene:field#category", "urn:jena:lucene:field#author"]'\'' 10)
} ORDER BY ?f DESC(?c)'

# Search with CQL2-JSON filter
curl -s -X POST "http://localhost:3030/ds" \
    -H "Content-Type: application/sparql-query" \
    -H "Accept: application/json" \
    -d 'PREFIX luc: <urn:jena:lucene:index#>
SELECT ?s ?score WHERE {
  (?s ?score) luc:query ("default" "learning" '\''{"op":"=","args":[{"property":"urn:jena:lucene:field#category"},"Technology"]}'\'' 20)
} ORDER BY DESC(?score)'
```

**Note:** With unnamed endpoints (as in the config above), all operations go to `/ds`. Do not use `/ds/query` or `/ds/update` — those require named endpoints in the config.

---

## Troubleshooting

### Named Graph Data Not Indexed

If data is loaded into named graphs (e.g. N-Quads), the SHACL indexer reads from a combined view of all graphs (`MultiUnion` of default + named). This works automatically. For SPARQL queries to also see named graph data, add `tdb2:unionDefaultGraph true` to the TDB2 dataset config:

```turtle
:base_dataset rdf:type tdb2:DatasetTDB ;
    tdb2:location "DB" ;
    tdb2:unionDefaultGraph true .
```

### No Search Results

1. **Verify data is loaded:**
   ```sparql
   SELECT (COUNT(*) AS ?count) WHERE { ?s ?p ?o }
   ```
2. **Check that entities have the correct `rdf:type`** — SHACL mode only indexes entities matching a shape's `sh:targetClass`
3. **Verify `text:storeValues true`** is set in the assembler config

### No Facet Results

1. **Check that fields have `idx:facetable true`** in the shape definition
2. **Verify field IRIs match** — the `luc:facet` JSON array requires field IRIs (the full IRI of each field resource)
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

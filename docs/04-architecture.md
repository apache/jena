# Architecture

## Document Models

### Classic: Triple-Per-Document

The original `jena-text` model. Each RDF triple matching the entity map creates a separate Lucene document:

```
Triple: ex:book1 rdfs:label "Machine Learning"
  → Lucene doc: {uri: "ex:book1", text: "Machine Learning", lang: "en"}

Triple: ex:book1 ex:category "Technology"
  → Lucene doc: {uri: "ex:book1", category: "Technology"}
```

**Consequence for faceting:** A text search finds documents with the `text` field. Facet values live on *different* documents (those with the `category` field). To connect them, a two-pass URI-join is required:

1. Search text field → collect entity URIs
2. Build `TermInSetQuery` for those URIs → collect facets from matching docs

This works but is inherently limited — it cannot use Lucene's `DrillSideways` and requires materialising the full URI set in memory.

### SHACL: Entity-Per-Document

Introduced in Phase 2. Each entity (identified by `rdf:type` matching a shape's `sh:targetClass`) gets **one** Lucene document with all its fields:

```
Entity: ex:book1 (type ex:Book)
  → Lucene doc: {
      uri: "ex:book1",
      docType: "Book",
      title: "Machine Learning",
      category: "Technology",
      author: "Smith",
      year: 2024
    }
```

**Advantages:**
- Single-pass faceting (text and facet fields on same document)
- Enables `DrillSideways` (future optimisation)
- Supports typed fields (int, long, double) for range queries
- Per-field configuration (stored, indexed, facetable, sortable)
- No overcounting from duplicate documents

---

## Key Classes

### Core

| Class | Role |
|-------|------|
| `TextIndexLucene` | Central index implementation. Manages Lucene `IndexWriter`, `FacetsConfig`. Contains all query, facet, and document-building methods |
| `EntityDefinition` | Maps RDF predicates to Lucene field names. Used by both modes |
| `Entity` | Represents a single indexable entity with field→value map. `addValue()` supports multi-valued fields |
| `TextIndexConfig` | Configuration holder passed to `TextIndexLucene` constructor |

### SHACL Mode

| Class | Role |
|-------|------|
| `ShaclIndexMapping` | Parsed data model: `IndexProfile` (shape), `FieldDef` (field), `FieldType` enum. Pure data, no RDF/Lucene dependencies beyond `Node` and `Analyzer` |
| `ShaclTextDocProducer` | Change listener. On triple add/delete, reads entity state from base dataset, builds Entity, calls `updateEntityForProfile()` |
| `ShaclIndexAssembler` | Parses `text:shapes` RDF config into `ShaclIndexMapping`. Reads `sh:targetClass`, `sh:path`, `sh:alternativePath`. No jena-shacl dependency |
| `IndexVocab` | `urn:jena:lucene:index#` namespace constants |

### Property Functions

| Class | Role |
|-------|------|
| `TextQueryPF` | Implements `text:query`. Parses args, handles JSON filter detection, delegates to `TextIndexLucene` |
| `TextFacetPF` | Implements `text:facet`. Parses facet field list, maxValues, minCount. Returns (field, value, count) bindings |
| `SearchExecution` | Shared execution state. Stored in `ExecutionContext` keyed by normalised query params. Lazy-computes hits and facet counts |

### Assembler

| Class | Role |
|-------|------|
| `TextIndexLuceneAssembler` | Builds `TextIndexLucene` from TTL config. Detects `text:shapes` vs `text:entityMap` |
| `TextDatasetAssembler` | Builds text-indexed dataset. Auto-creates `ShaclTextDocProducer` in SHACL mode |

---

## Shared Execution Flow

When `text:query` and `text:facet` appear in the same SPARQL query:

```
SPARQL query parsed
  ├── TextQueryPF.exec()
  │     ├── Parse args: queryString, props, filters, limit
  │     ├── Build lookup key: "props=...|qs=...|filters=..."
  │     ├── SearchExecution.getOrCreate(execCxt, key, ...)
  │     │     └── Creates new SearchExecution, stores in ExecutionContext
  │     └── searchExec.getHits(limit, highlight)
  │           └── Executes Lucene query (lazy, first access)
  │
  └── TextFacetPF.exec()
        ├── Parse args: queryString, props, filters, facetFields, maxValues, minCount
        ├── Build lookup key: "props=...|qs=...|filters=..." (same key!)
        ├── SearchExecution.getOrCreate(execCxt, key, ...)
        │     └── Returns EXISTING SearchExecution from context
        └── searchExec.getFacetCounts(facetFields, maxValues, minCount)
              └── Reuses same reader snapshot
```

Key normalisation: property URIs are sorted, filter map keys are sorted, filter values within each key are sorted. This ensures the same logical query always produces the same key regardless of argument ordering.

---

## SHACL Change Listener Flow

`ShaclTextDocProducer` handles all triple changes:

```
DatasetGraphTextMonitor.add(g, s, p, o)
  ├── super.add(g, s, p, o)        ← base dataset updated FIRST
  └── record() → change(ADD, g, s, p, o)
        │
        ShaclTextDocProducer.change()
        ├── p == rdf:type?
        │     └── handleTypeChange()
        │           └── rebuildEntityDocuments(s)
        ├── mapping.isRelevantPredicate(p)?
        │     └── rebuildEntityDocuments(s)
        └── else: ignore (irrelevant predicate)

rebuildEntityDocuments(subject)
  ├── Read rdf:type values from base dataset
  ├── Find matching IndexProfiles via classLookup
  ├── If no profiles match → deleteEntityByUri()
  └── For each matching profile:
        ├── Read all relevant triples from base dataset
        ├── Build Entity with addValue() for each field
        └── indexer.updateEntityForProfile(entity, profile)
              ├── docFromMapping() → builds typed Lucene Document
              ├── Delete existing doc by (uri + docType) composite query
              └── Add new document
```

The base dataset is always up-to-date when `change()` fires because `DatasetGraphTextMonitor` calls `super.add()` before `record()`.

---

## Lucene Field Mapping (SHACL Mode)

| FieldType | Lucene indexed field | Lucene stored field | Lucene DocValues |
|-----------|---------------------|--------------------|--------------------|
| TEXT | `TextField` | (via `TYPE_STORED`) | — |
| KEYWORD | `StringField` | (via `Store.YES`) | `SortedSetDocValuesFacetField` (facetable), `SortedDocValuesField` (sortable) |
| INT | `IntPoint` | `StoredField(int)` | `NumericDocValuesField` (sortable) |
| LONG | `LongPoint` | `StoredField(long)` | `NumericDocValuesField` (sortable) |
| DOUBLE | `DoublePoint` | `StoredField(double)` | `NumericDocValuesField` (sortable) |

Each entity document also gets:
- **URI field** (`ftIRI` type) — tokenized=false, stored=true
- **Discriminator field** — `StringField` with the target class local name (e.g., "Book")

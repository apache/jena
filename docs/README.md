# Jena Text Index: Faceting & Entity-Per-Document

This documentation covers the faceted search and entity-per-document indexing features added to Apache Jena's `jena-text` module.

## Documents

| Document | Audience | Description |
|----------|----------|-------------|
| [User Guide](01-user-guide.md) | Users / Integrators | Configure, query, deploy with Fuseki, troubleshoot |
| [SPARQL API Reference](02-sparql-api.md) | Users / Developers | `text:query`, `luc:query`, `luc:facet` syntax, Lucene query syntax, Java API |
| [Configuration Reference](03-configuration.md) | Admins / Integrators | Assembler TTL config for both indexing modes |
| [Architecture](04-architecture.md) | Developers | Internal design, document models, shared execution |
| [Testing](05-testing.md) | Developers / QA | Test coverage, how to run tests |
| [Design Decisions](06-design-decisions.md) | Developers / Reviewers | Why things are the way they are |
| [Known Limitations & Future Work](07-future-work.md) | All | What's deferred, what needs attention |

## Quick Start

```turtle
# Classic mode — upstream Jena text search (text:query)
text:entityMap <#entMap> ;

# SHACL mode — entity-per-document with faceting (luc:query, luc:facet)
text:shapes ( <#BookShape> ) ;
```

```sparql
# Classic: text search
PREFIX text: <http://jena.apache.org/text#>
(?s ?sc) text:query ("machine learning") .

# SHACL: search with filters + facets
PREFIX luc: <urn:jena:lucene:index#>
(?s ?sc) luc:query ("machine learning") .
(?f ?v ?c) luc:facet ("machine learning" '["category"]' 10) .
```

## Build & Test

```bash
mvn test -pl jena-text                    # 366 tests
mvn clean install -pl jena-fuseki2/jena-fuseki-server -am -DskipTests  # build Fuseki
```

## Archive

Previous working documents, design reviews, and phase summaries are in [archive/](archive/).

## What Changed

Summary of additions to the upstream Apache Jena codebase as part of this work.

### Java Source (`jena-text/src/main/java`) — 16 files, +2,594 lines

**9 new classes:**

| Class | Lines | Role |
|-------|-------|------|
| `TextIndexLucene` (extended) | +646 | SHACL faceting methods added to central index |
| `ShaclTextQueryPF` | +340 | `luc:query` property function with JSON filter support |
| `TextFacetPF` | +354 | `luc:facet` property function for facet counts |
| `ShaclIndexAssembler` | +303 | Parses `text:shapes` RDF config into `ShaclIndexMapping` |
| `ShaclTextDocProducer` | +191 | Change listener — rebuilds entity Lucene docs on triple changes |
| `SearchExecution` | +188 | Shared execution state between `luc:query` and `luc:facet` |
| `ShaclIndexMapping` | +186 | Data model: `IndexProfile`, `FieldDef`, `FieldType` enum |
| `FacetedTextResults` | +106 | Result container for faceted search |
| `FacetValue` | +73 | Immutable (value, count) pair |
| `IndexVocab` | +67 | `urn:jena:lucene:index#` namespace constants |

**7 modified classes** (additive only): `TextIndexConfig`, `Entity`, `TextQuery`, `TextVocab`, `TextDatasetAssembler`, `TextIndexLuceneAssembler`, `TextVocab`

### Java Tests (`jena-text/src/test/java`) — 12 new files, +2,329 lines

| Test Class | Tests | Coverage |
|------------|-------|----------|
| `TestTextFacetPF` | 7 | SPARQL `luc:facet` property function |
| `TestNativeFacetCounts` | 10 | Java API facet operations |
| `TestShaclEntityPerDocument` | 7 | End-to-end text search and facets |
| `TestTextQueryPFFilters` | 6 | `luc:query` with JSON filters |
| `TestShaclDocumentBuilding` | 11 | Lucene doc building, all field types |
| `TestShaclTextDocProducer` | 5 | Change listener lifecycle |
| `TestShaclIndexMapping` | 8 | Data model, predicate/class lookup |
| `TestSearchExecution` | 6 | Shared execution key normalisation |
| `TestShaclAssembler` | 3 | Config parsing |
| `TestFacetedResults` | — | Faceted result container |
| `TestUpdateDocumentFacets` | — | Document update with facets |
| `TS_Text` (modified) | — | Suite registration for new tests |

### Documentation (`docs/`) — 25 new files, +5,373 lines

8 core docs (user guide, SPARQL API, configuration, architecture, testing, design decisions, future work) and 17 archive documents (design specs, review notes, phase summaries).

### Totals

| | Files | Lines |
|---|---|---|
| Java source | 16 (9 new, 7 modified) | +2,594 |
| Java tests | 12 (all new) | +2,329 |
| Documentation | 25 (all new) | +5,373 |
| **Total** | **54** | **+10,299** |

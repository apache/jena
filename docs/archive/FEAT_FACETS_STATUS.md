# Apache Jena Text Index - Faceting Implementation Project

## Project Goal

**Add Lucene Faceting and Grouping capabilities to Apache Jena's text index module.**

Enable Fuseki to perform:
- Full-text search (FTS) - Already exists
- Spatial queries - Already exists
- **Faceting** - COMPLETE (Phases 1-7)
- **Grouping** - PLANNED (Future work)

All working together natively in SPARQL queries.

---

## Current Status: API REDESIGN COMPLETE

### All Tests Passing

```
Tests run: 303, Failures: 0, Errors: 0, Skipped: 8
BUILD SUCCESS
```

### Test Summary

| Test Class | Tests | Status |
|------------|-------|--------|
| TestFacetedResults | 6 | PASS |
| TestNativeFacetCounts | 8 | PASS |
| TestUpdateDocumentFacets | 2 | PASS |
| TestSearchExecution | 6 | PASS |
| TestTextFacetPF | 5 | PASS |
| TestTextQueryPFFilters | 7 | PASS |
| **New tests total** | **34** | **PASS** |
| **Full jena-text suite** | **303** | **PASS** |

---

## API Redesign (2026-02-17)

Replaced 3 property functions with 2:

| Old API | New API |
|---------|---------|
| `text:query` | `text:query` (extended with JSON filter support) |
| `text:queryWithFacets` | Removed (use `text:query` + `text:facet`) |
| `text:facetCounts` | `text:facet` (new, with JSON array/object syntax) |

### New Syntax

```sparql
# text:query - hits with optional JSON filters
(?s ?score) text:query (rdfs:label "climate" 20)
(?s ?score) text:query (rdfs:label "climate" '{"category": ["Technology"]}' 20)

# text:facet - counts with facet fields and optional JSON filters
(?f ?v ?c) text:facet (rdfs:label "climate" '["category", "author"]' 10)
(?f ?v ?c) text:facet (rdfs:label "climate" '["category"]' '{"author": ["Smith"]}' 10)
```

### Bug Fixes

1. **updateDocument missing facetsConfig.build()** - Fixed in TextIndexLucene
2. **getFacetCounts TooManyClauses risk** - Replaced BooleanQuery with TermInSetQuery

### New Classes

| Class | Purpose |
|-------|---------|
| `TextFacetPF` | SPARQL property function for `text:facet` |
| `SearchExecution` | Shared execution state between PFs |

### Deleted Classes

| Class | Reason |
|-------|--------|
| `TextQueryFacetsPF` | Replaced by `text:query` + `text:facet` |
| `TextFacetCountsPF` | Replaced by `text:facet` |
| `TestTextQueryFacetsPF` | Tests deleted PF |
| `TestTextFacetCountsPF` | Tests deleted PF |
| `TestFacetedSearchIntegration` | Used deleted `queryWithFacets$` method |
| `TestFacetedSearchPerformance` | Used deleted `queryWithFacets$` method |

### Modified Classes

| Class | Change |
|-------|--------|
| `TextIndexLucene` | Bug fixes + `queryWithFilters()` + `getFacetCountsWithFilters()` |
| `TextQueryPF` | JSON filter parsing in `objectToStruct()`, `SearchExecution` in `prepareQuery()` |
| `TextQuery` | Updated PF registrations |
| `TextVocab` | Added `pfFacet` constant |
| `TextIndex` | Removed `queryWithFacets()` interface method |

---

## Build Commands

```bash
# Run all tests
mvn test -pl jena-text

# Run new faceting tests
mvn test -pl jena-text -Dtest="TestSearchExecution,TestUpdateDocumentFacets,TestTextFacetPF,TestTextQueryPFFilters,TestNativeFacetCounts,TestFacetedResults"

# Build Fuseki server
mvn clean install -pl jena-fuseki2/jena-fuseki-server -am -DskipTests
```

---

**Last Updated:** 2026-02-17
**Status:** COMPLETE - All 303 tests passing

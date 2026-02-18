# Phase 1.5: Faceting Enhancements - Execution Summary

**Date:** 2026-02-18
**Status:** Complete
**Tests:** 327 passing (was 303), 0 failures, 8 skipped

---

## Changes Made

### 1. TextIndexConfig (`TextIndexConfig.java`)

- Added `int maxFacetHits = 0` field (0 = unlimited)
- Added `getMaxFacetHits()` / `setMaxFacetHits(int)` following existing pattern

### 2. TextVocab (`TextVocab.java`)

- Added `pMaxFacetHits = Vocab.property(NS, "maxFacetHits")`

### 3. TextIndexLuceneAssembler (`TextIndexLuceneAssembler.java`)

- Parses `text:maxFacetHits` as integer (default 0), following `maxBasicQueries` pattern
- Sets `config.setMaxFacetHits(maxFacetHits)` before index creation

### 4. TextIndexLucene (`TextIndexLucene.java`)

**4a. Replaced static `MAX_N` with configurable limit:**

| Before | After |
|--------|-------|
| `private static int MAX_N = 10000` | `private static final int DEFAULT_MAX_RESULTS = 10000` |
| — | `private final int maxFacetHits` (instance field) |
| — | `facetSearchLimit()` helper: returns `maxFacetHits > 0 ? maxFacetHits : Integer.MAX_VALUE` |

**4b. Updated all 6 usage sites:**

| Location | Context | Change |
|----------|---------|--------|
| Line ~494 | `query(property, qs, ...)` | `MAX_N` → `DEFAULT_MAX_RESULTS` |
| Line ~815 | `limit = MAX_N` | `MAX_N` → `DEFAULT_MAX_RESULTS` |
| Line ~899 | `searcher.search(query, 10000)` | → `searcher.search(query, facetSearchLimit())` |
| Line ~993 | `limit > 0 ? limit * 10 : MAX_N` | `MAX_N` → `DEFAULT_MAX_RESULTS` |
| Line ~1059 | `searcher.search(builder.build(), MAX_N)` | → `facetSearchLimit()` |
| Line ~1169 | `searcher.search(filterBuilder.build(), MAX_N)` | → `facetSearchLimit()` |

Two hardcoded `10000` values were also replaced with `facetSearchLimit()`.

**4c. Added `minCount` overloads:**

```java
// Existing signatures delegate with minCount=0:
getFacetCounts(List<String> fields, int maxValues)
  → getFacetCounts(null, fields, maxValues, 0)

getFacetCounts(String qs, List<String> fields, int maxValues)
  → getFacetCounts(qs, fields, maxValues, 0)

// New primary:
getFacetCounts(String qs, List<String> fields, int maxValues, int minCount)

// Same for WithFilters:
getFacetCountsWithFilters(String qs, List<String> fields, Map filters, int maxValues)
  → getFacetCountsWithFilters(qs, fields, filters, maxValues, 0)

getFacetCountsWithFilters(String qs, List<String> fields, Map filters, int maxValues, int minCount)
```

**4d. `getAllChildren` when `maxValues <= 0`:**

Both `getFacetCounts` and `getFacetCountsWithFilters` now use:
```java
FacetResult facetResult = (maxValues <= 0)
    ? facets.getAllChildren(field)
    : facets.getTopChildren(maxValues, field);
```

**4e. `minCount` filtering in LabelAndValue loops:**

```java
for (LabelAndValue lv : facetResult.labelValues) {
    if (minCount <= 0 || lv.value.longValue() >= minCount) {
        fieldFacets.add(new FacetValue(lv.label, lv.value.longValue()));
    }
}
```

### 5. SearchExecution (`SearchExecution.java`)

- Added `getFacetCounts(List<String>, int, int)` overload with `minCount` param
- Existing two-arg method delegates with `minCount=0`
- Passes `minCount` through to `textIndex.getFacetCounts()` / `getFacetCountsWithFilters()`

### 6. TextFacetPF (`TextFacetPF.java`)

- **FacetArgs:** Added `final int minCount` field
- **parseObjectArgs:** First integer = `maxValues`, second integer = `minCount`
- **exec():** Passes `args.minCount` through to both SearchExecution and direct textIndex calls

### 7. TS_Text (`TS_Text.java`)

- Added `TestNativeFacetCounts`, `TestTextFacetPF`, `TestSearchExecution` to suite
  (these were previously not discovered by surefire's `**/TS_*.java` include pattern)

### 8. Tests

**TestTextFacetPF (3 new tests):**

| Test | SPARQL | Assertion |
|------|--------|-----------|
| `testFacetCountsWithMinCount` | `text:facet ("learning" '["author"]' 10 2)` | Only Smith (count=3) returned |
| `testFacetCountsWithMaxValuesZero` | `text:facet ("learning" '["author"]' 0)` | All 3 authors returned |
| `testFacetCountsWithMinCountAndMaxValues` | `text:facet ("learning" '["author"]' 0 2)` | Only Smith returned |

**TestNativeFacetCounts (2 new tests):**

| Test | API Call | Assertion |
|------|----------|-----------|
| `testGetAllChildrenWhenMaxValuesZero` | `getFacetCounts("learning", fields, 0)` | All 6 authors returned |
| `testMinCountFiltering` | `getFacetCounts("learning", fields, 10, 2)` | Only Smith (count=3) returned |

---

## Syntax Reference

```sparql
# Basic facet counts
(?f ?v ?c) text:facet ("query" '["field1","field2"]')

# With maxValues
(?f ?v ?c) text:facet ("query" '["field1"]' 10)

# With maxValues=0 (return all values)
(?f ?v ?c) text:facet ("query" '["field1"]' 0)

# With maxValues and minCount
(?f ?v ?c) text:facet ("query" '["field1"]' 10 2)

# With filters, maxValues, and minCount
(?f ?v ?c) text:facet ("query" '["field1"]' '{"field2":["val"]}' 10 2)
```

---

## Assembler Configuration

```turtle
# Optional: limit the number of hits in facet search queries (default: 0 = unlimited)
text:maxFacetHits 50000 ;
```

---

## Verification

```
$ mvn test -pl jena-text
Tests run: 327, Failures: 0, Errors: 0, Skipped: 8
BUILD SUCCESS
```

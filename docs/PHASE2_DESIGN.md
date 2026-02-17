# Phase 2 Design: SHACL-Driven Document Model

**Date:** 2026-02-17
**Updated:** 2026-02-17

---

## 1. Design Decisions

| Question | Decision |
|----------|----------|
| Hit limit in filtered faceting | Configurable, default unlimited. Also expose `minCount` threshold. |
| Open facets (no query) | Yes, supported. Optimize later if needed. |
| SHACL change listener | Custom `TextDocProducer` using existing `DatasetChanges` wiring. |
| Incremental reindexing | MVP: full document reindex per entity. Partial deferred. |
| Config format | Inline SHACL shapes in assembler TTL. |
| Coexistence | New SHACL model is an alternative alongside existing `text:entityMap`. No existing code removed. |
| Suggest | Optional per-field config using Lucene's `SuggestField`. |

---

## 2. Faceting API Enhancements (Phase 1.5)

Improvements to the current codebase, before SHACL work.

### 2.1 Remove Hardcoded 10,000 Hit Limit

The current `getFacetCounts()` and `getFacetCountsWithFilters()` use `searcher.search(query, 10000)`. Replace with a configurable assembler property:

```turtle
<#indexLucene> rdf:type text:TextIndexLucene ;
    text:maxFacetHits 0 ;           # 0 = unlimited (default)
    ...
```

When `maxFacetHits == 0`, use `FacetsCollector` as a direct `Collector` (collects all matching docs, no cap). Otherwise use `searcher.search(query, maxFacetHits)`.

### 2.2 Top-N and Get-All Facets

Already supported via `maxValues` parameter. Lucene provides:
- `getTopChildren(topN, dim)` — top N by count (current behavior)
- `getAllChildren(dim)` — all non-zero values, no ordering guarantee

When `maxValues` is omitted or 0, use `getAllChildren()`.

### 2.3 Minimum Count Threshold

No built-in Lucene API — the standard approach is post-filtering `LabelAndValue[]`. Expose as optional parameter in `text:facet`:

```sparql
(?f ?v ?c) text:facet ("query" '["category"]' 10 2)
#                                              ^topN ^minCount
```

Implementation: filter `LabelAndValue[]` after retrieval, keeping only entries where `value.longValue() >= minCount`.

### 2.4 DrillSideways (Phase 2 only)

Lucene's `DrillSideways` computes faceted navigation in a single pass — hits + facet counts together, with "sideways" semantics (clicking a category filter still shows counts for other categories).

**Blocked by triple-per-document model.** DrillSideways requires all fields on the same Lucene document. Phase 2's entity-per-document model enables this:

```java
DrillSideways ds = new DrillSideways(searcher, facetsConfig, sortedSetState);
DrillDownQuery ddq = new DrillDownQuery(facetsConfig, baseQuery);
ddq.add("category", "Science");
DrillSidewaysResult result = ds.search(ddq, limit);
// result.hits + result.facets in one pass, no URI join
```

This is the main architectural payoff of the SHACL document model.

---

## 3. Coexistence Strategy

The SHACL-driven model is a **new alternative** alongside the existing implementation. No existing code is removed or deprecated.

### 3.1 Principle

- Existing `text:entityMap` path continues to work exactly as today
- New `text:shapes` path activates the SHACL model
- Both use the same `TextIndexLucene` for index operations
- The difference is in the **document producer** and **config parsing**

### 3.2 Detection

```
text:shapes present     → ShaclTextDocProducer + SHACL-based config
text:entityMap present  → TextDocProducerTriples + EntityDefinition (existing)
both present            → Assembler error
neither present         → Assembler error
```

### 3.3 What's Shared vs Separate

| Component | Shared | Notes |
|-----------|--------|-------|
| `TextIndexLucene` | Yes | Core index operations, extended for new field types |
| `TextQueryPF` / `TextFacetPF` | Yes | SPARQL property functions work with both models |
| `SearchExecution` | Yes | Shared execution state |
| `DatasetGraphText` | Yes | Transaction coordination |
| `DatasetGraphTextMonitor` | Yes | Change event delivery |
| Document producer | No | `TextDocProducerTriples` vs `ShaclTextDocProducer` |
| Config parsing | No | `EntityDefinitionAssembler` vs new SHACL assembler |
| Filtered faceting strategy | Conditional | URI-join (triple model) vs DrillSideways (SHACL model) |

### 3.4 Deprecation Path

1. Phase 2: SHACL model ships as alternative. Both models documented.
2. Future release: if community is happy, `text:entityMap` can be deprecated.
3. Later release: `text:entityMap` removed.

No timeline pressure. The existing model continues to work indefinitely.

---

## 4. SHACL Change Listener: Custom TextDocProducer

### 4.1 Analysis

Jena's SHACL library (`jena-shacl`) provides:
- Shape parsing: `Shapes.parse(graph)` → `NodeShape` / `PropertyShape`
- Target resolution: `TargetOps.focusTargetClass()`, `FocusNodes.focusNodes()`
- Path evaluation: `PropertyShape.getPath()` for `sh:path` values

It does **not** provide:
- Data change listeners / reactive validation on triple changes
- "Which triples match this shape" utilities

### 4.2 Approach

Implement `TextDocProducer` (same interface as `TextDocProducerTriples`). The existing `DatasetGraphTextMonitor` wiring delivers triple changes — no transaction coordination changes needed.

**On startup:**
1. Parse shapes from config
2. Build predicate → list of (NodeShape, Field) lookup
3. Build class URI → list of NodeShapes lookup (for `sh:targetClass`)

**On each triple change (`change(action, g, s, p, o)`):**
1. Check if predicate `p` is in the predicate lookup
2. If yes, subject `s` is a potential focus node — re-read all properties, rebuild entire Lucene document, call `updateDocument()`
3. If `p == rdf:type` and `o` matches a `sh:targetClass` — new entity, build full document
4. If `p == rdf:type` and action is DELETE — check if entity still matches any shape, delete document if not

**Initial scope — simple paths only:**
- `sh:path ex:predicate` (single predicate)
- `sh:alternativePath` (union of predicates — extract each predicate for the lookup)
- Sequence paths, inverse paths deferred

### 4.3 Reindexing Strategy

**MVP: Full document reindex per entity.** Lucene's `updateDocument()` replaces the whole document anyway — no partial field update at the Lucene level. Simple to implement.

**Future enhancement:** Skip reindex when the changed predicate isn't covered by any shape (cheap predicate lookup check).

---

## 5. Configuration Model

### 5.1 Three-Layer Architecture

Following the model from `shacl_config_design.md`:

```
NodeShape (idx:IndexProfile)
  → defines which RDF nodes become Lucene documents
  → links to Field definitions via idx:field

Field (idx:Field)
  → defines a single Lucene field
  → has fieldName, fieldType, analyzer, facetable, suggestable, etc.
  → links to one or more paths via idx:path

Path (sh:path / sh:alternativePath)
  → defines how to extract values from a focus node
  → multiple paths can feed one Field (e.g., "all_text" from rdfs:label + skos:prefLabel + ...)
```

This is more expressive than a flat PropertyShape model because:
- Multiple RDF predicates can feed a single Lucene field (`all_text`)
- Same predicate can feed multiple fields with different analyzers (exact + ngram)
- Field config is cleanly separated from path extraction

### 5.2 Vocabulary

Using `idx:` namespace (`urn:jena:lucene:index#`) as proposed in `shacl_config_design.md`, keeping it distinct from the existing `text:` namespace. The `text:` namespace connects the index to the dataset; `idx:` configures Lucene field behavior.

**Index-level (on TextIndexLucene resource):**

| Property | Range | Default | Description |
|----------|-------|---------|-------------|
| `text:shapes` | `rdf:List` | — | NodeShapes defining the document model |
| `text:maxFacetHits` | `xsd:integer` | `0` (unlimited) | Max hits for filtered faceting |

**NodeShape-level (idx:IndexProfile):**

| Property | Range | Default | Description |
|----------|-------|---------|-------------|
| `sh:targetClass` | URI(s) | — | RDF classes whose instances become documents |
| `idx:docIdField` | `xsd:string` | `"uri"` | Lucene field name for entity URI |
| `idx:discriminatorField` | `xsd:string` | `"docType"` | Lucene field for shape/type discriminator |
| `idx:field` | `idx:Field` | — | Field definitions for this document type |

**Field-level (idx:Field):**

| Property | Range | Default | Description |
|----------|-------|---------|-------------|
| `idx:fieldName` | `xsd:string` | required | Lucene field name |
| `idx:fieldType` | resource | `idx:TextField` | Lucene field type (see below) |
| `idx:analyzer` | resource | index default | Field-specific analyzer |
| `idx:stored` | `xsd:boolean` | `true` | Store field values for retrieval |
| `idx:indexed` | `xsd:boolean` | `true` | Index field for searching |
| `idx:facetable` | `xsd:boolean` | `false` | Add `SortedSetDocValuesFacetField` |
| `idx:sortable` | `xsd:boolean` | `false` | Add DocValues for sorting |
| `idx:suggestable` | `xsd:boolean` | `false` | Add `SuggestField` for autocomplete |
| `idx:suggestWeight` | `xsd:integer` | `1` | Weight for suggest ranking |
| `idx:multiValued` | `xsd:boolean` | `false` | Expect multiple values per entity |
| `idx:defaultSearch` | `xsd:boolean` | `false` | Default search field |
| `idx:path` | blank node | — | SHACL path(s) that feed this field |

**Field type resources:**

| Resource | Lucene Type | Analyzed | Notes |
|----------|------------|----------|-------|
| `idx:TextField` | `TextField` | Yes | Full-text searchable, tokenized |
| `idx:KeywordField` | `StringField` | No | Exact match, not tokenized |
| `idx:IntField` | `IntPoint` + `NumericDocValuesField` | No | Integer range queries |
| `idx:LongField` | `LongPoint` + `NumericDocValuesField` | No | Long range queries |
| `idx:DoubleField` | `DoublePoint` + `NumericDocValuesField` | No | Double range queries |

**Analyzer resources** (reuse existing `text:` namespace):

| Resource | Lucene Analyzer |
|----------|----------------|
| `text:StandardAnalyzer` | `StandardAnalyzer` |
| `text:KeywordAnalyzer` | `KeywordAnalyzer` |
| `text:SimpleAnalyzer` | `SimpleAnalyzer` |
| `text:ConfigurableAnalyzer` | Custom analyzer with tokenizer + filters |

Custom analyzers (e.g., EdgeNGram) use the existing `text:ConfigurableAnalyzer` mechanism with `text:defineAnalyzers`.

### 5.3 Example Configuration

```turtle
@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix ja:   <http://jena.hpl.hp.com/2005/11/Assembler#> .
@prefix text: <http://jena.apache.org/text#> .
@prefix sh:   <http://www.w3.org/ns/shacl#> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix sdo:  <https://schema.org/> .
@prefix dct:  <http://purl.org/dc/terms/> .
@prefix idx:  <urn:jena:lucene:index#> .
@prefix ex:   <http://example.org/> .

## ── Dataset ──────────────────────────────────────────────

<#text_dataset> rdf:type text:TextDataset ;
    text:dataset <#base_dataset> ;
    text:index <#indexLucene> .

<#base_dataset> rdf:type ja:MemoryDataset .

## ── Lucene index (SHACL-driven) ─────────────────────────

<#indexLucene> rdf:type text:TextIndexLucene ;
    text:directory "mem" ;
    text:storeValues true ;
    text:shapes ( <#BookShape> <#PersonShape> ) ;
    text:analyzer [ rdf:type text:StandardAnalyzer ] .

## ── Book document shape ─────────────────────────────────

<#BookShape>
    a sh:NodeShape, idx:IndexProfile ;
    sh:targetClass ex:Book ;
    idx:docIdField "uri" ;
    idx:discriminatorField "docType" ;

    idx:field <#Book_allText> ;
    idx:field <#Book_title> ;
    idx:field <#Book_category> ;
    idx:field <#Book_author> ;
    idx:field <#Book_year> .

# Union field: global search across multiple predicates
<#Book_allText>
    a idx:Field ;
    idx:fieldName "all_text" ;
    idx:fieldType idx:TextField ;
    idx:defaultSearch true ;
    idx:suggestable true ;
    idx:suggestWeight 1 ;
    idx:path [
        sh:alternativePath (
            rdfs:label
            dct:description
            ex:abstract
        )
    ] .

# Targeted label field (can be boosted at query time)
<#Book_title>
    a idx:Field ;
    idx:fieldName "title" ;
    idx:fieldType idx:TextField ;
    idx:stored true ;
    idx:suggestable true ;
    idx:suggestWeight 5 ;
    idx:path [ sh:path rdfs:label ] .

# Facetable keyword fields
<#Book_category>
    a idx:Field ;
    idx:fieldName "category" ;
    idx:fieldType idx:KeywordField ;
    idx:facetable true ;
    idx:path [ sh:path ex:category ] .

<#Book_author>
    a idx:Field ;
    idx:fieldName "author" ;
    idx:fieldType idx:KeywordField ;
    idx:facetable true ;
    idx:multiValued true ;
    idx:path [ sh:path ex:author ] .

# Numeric field with faceting and sorting
<#Book_year>
    a idx:Field ;
    idx:fieldName "year" ;
    idx:fieldType idx:IntField ;
    idx:facetable true ;
    idx:sortable true ;
    idx:path [ sh:path ex:year ] .

## ── Person document shape ───────────────────────────────

<#PersonShape>
    a sh:NodeShape, idx:IndexProfile ;
    sh:targetClass ex:Person ;
    idx:field <#Person_allText> ;
    idx:field <#Person_name> ;
    idx:field <#Person_affiliation> .

<#Person_allText>
    a idx:Field ;
    idx:fieldName "all_text" ;
    idx:fieldType idx:TextField ;
    idx:defaultSearch true ;
    idx:suggestable true ;
    idx:path [
        sh:alternativePath (
            rdfs:label
            skos:prefLabel
            sdo:name
        )
    ] .

<#Person_name>
    a idx:Field ;
    idx:fieldName "name" ;
    idx:fieldType idx:TextField ;
    idx:stored true ;
    idx:path [
        sh:alternativePath (
            rdfs:label
            skos:prefLabel
        )
    ] .

<#Person_affiliation>
    a idx:Field ;
    idx:fieldName "affiliation" ;
    idx:fieldType idx:KeywordField ;
    idx:facetable true ;
    idx:path [ sh:path ex:affiliation ] .
```

### 5.4 How It Maps to Lucene

For a Book entity:
```turtle
ex:book1 a ex:Book ;
    rdfs:label "Machine Learning Fundamentals" ;
    dct:description "An introduction to ML concepts" ;
    ex:category "technology" ;
    ex:author "Smith" ;
    ex:author "Jones" ;
    ex:year 2024 .
```

Produces **one** Lucene document:
```
{
  uri:       "http://example.org/book1"                          [StringField, stored]
  docType:   "Book"                                              [StringField, stored]
  all_text:  "Machine Learning Fundamentals An introduction..."  [TextField]
  title:     "Machine Learning Fundamentals"                     [TextField, stored]
  category:  "technology"                                        [StringField + SortedSetDocValuesFacetField]
  author:    ["Smith", "Jones"]                                  [StringField + SortedSetDocValuesFacetField]
  year:      2024                                                [IntPoint + NumericDocValuesField]
  suggest:   "Machine Learning Fundamentals" (weight=5)          [SuggestField]
  suggest:   "An introduction to ML concepts" (weight=1)         [SuggestField]
}
```

### 5.5 Discriminator Auto-Detection

Per `shacl_config_design.md`:
- One `sh:targetClass` → `docType` = that class (local name or qname)
- Multiple target classes → multi-valued `docType` from the entity's actual `rdf:type` values that match shape targets
- Non-class targets (`sh:targetSubjectsOf` etc.) → stable label like `shape:<shapeIRI>` unless overridden via `idx:discriminatorField`

---

## 6. Suggest Support (`text:suggest`)

### 6.1 Approach: Lucene `SuggestField`

Lucene 10.x provides `SuggestField` — a field type that indexes directly into regular documents alongside `TextField`, `StringField`, and facet fields. No separate index or build step.

**Why `SuggestField` over `AnalyzingInfixSuggester`:**

| Aspect | SuggestField | AnalyzingInfixSuggester |
|--------|-------------|------------------------|
| Separate index? | No — same documents | Yes — private index required |
| NRT updates? | Automatic | Manual `refresh()` |
| Coexists with other fields? | Yes | N/A |
| Build lifecycle? | None | `build()` + `add()` + `refresh()` |
| Context filtering? | Yes (`ContextSuggestField`) | Yes |
| Fuzzy/typo tolerance? | Yes (`FuzzyCompletionQuery`) | No |

### 6.2 Configuration

Per-field opt-in via `idx:suggestable true`:

```turtle
<#Book_title>
    a idx:Field ;
    idx:fieldName "title" ;
    idx:fieldType idx:TextField ;
    idx:suggestable true ;       # enable suggest for this field
    idx:suggestWeight 5 ;        # higher weight = ranked higher in suggestions
    idx:path [ sh:path rdfs:label ] .
```

All suggestable fields feed into a single `suggest` Lucene field on the document. The weight controls ranking. Multiple suggestable fields per shape are allowed — each value becomes a separate `SuggestField` entry.

### 6.3 Codec Requirement

`SuggestField` requires a custom codec that routes the `suggest` field to `Completion101PostingsFormat` while all other fields use the default format. This is configured on `IndexWriterConfig`:

```java
Codec filterCodec = new FilterCodec(Codec.getDefault().getName(), Codec.getDefault()) {
    final PostingsFormat completionFormat = new Completion101PostingsFormat();
    @Override
    public PostingsFormat postingsFormat() {
        return new PerFieldPostingsFormat() {
            @Override
            public PostingsFormat getPostingsFormatForField(String field) {
                if ("suggest".equals(field)) return completionFormat;
                return delegate.postingsFormat().getPostingsFormatForField(field);
            }
        };
    }
};
iwc.setCodec(filterCodec);
```

Only needed when any field has `idx:suggestable true`. The codec config is added to `TextIndexLucene.openIndexWriter()`.

### 6.4 Query API

New `SuggestIndexSearcher` wraps the regular `IndexSearcher`:

```java
SuggestIndexSearcher searcher = new SuggestIndexSearcher(reader);
CompletionQuery query = new FuzzyCompletionQuery(analyzer, new Term("suggest", prefix));
TopSuggestDocs results = searcher.suggest(query, maxResults, true);
```

Returns `TopSuggestDocs` with suggestion text + score + document ID (for fetching entity URI and other stored fields).

### 6.5 SPARQL Property Function

```sparql
# text:suggest — autocomplete suggestions
(?entity ?label ?score) text:suggest ("aus" 10)
(?entity ?label ?score) text:suggest ("aus" 10 true)   # fuzzy=true for typo tolerance
```

Returns:
- `?entity` — entity URI (from stored `uri` field)
- `?label` — suggested text (from `SuggestScoreDoc.key`)
- `?score` — suggestion weight/score

### 6.6 Context-Aware Suggestions

If `idx:facetable true` fields exist on the same shape, their values can be used as suggestion contexts via `ContextSuggestField`:

```sparql
# Suggest within a category
(?entity ?label ?score) text:suggest ("aus" '{"category": ["Organization"]}' 10)
```

This reuses the same JSON filter syntax as `text:query` and `text:facet`. Implementation uses `ContextQuery` wrapping a `PrefixCompletionQuery` or `FuzzyCompletionQuery`.

### 6.7 Dependency

Requires adding `lucene-suggest` to `jena-text/pom.xml`:

```xml
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-suggest</artifactId>
    <version>${ver.lucene}</version>
</dependency>
```

---

## 7. Implementation Plan

### Phase 1.5: Faceting Enhancements (current model)

| Step | Change |
|------|--------|
| 1 | Add `text:maxFacetHits` assembler property and `TextIndexConfig` field |
| 2 | Replace hardcoded `10000` with configurable limit |
| 3 | Add `minCount` parameter to `text:facet` argument parsing |
| 4 | Post-filter `LabelAndValue[]` by minCount |
| 5 | Support `maxValues=0` → `getAllChildren()` |

### Phase 2: SHACL Document Model

| Step | Change |
|------|--------|
| 1 | Add `idx:` vocabulary constants to new `IndexVocab.java` |
| 2 | Create `ShaclIndexMapping.java` — parses NodeShape → Fields → paths |
| 3 | Create `ShaclTextDocProducer.java` — entity-per-document, predicate lookup |
| 4 | Extend `TextIndexLucene.java` — numeric fields, per-field options |
| 5 | Create `ShaclIndexAssembler.java` — parse `text:shapes` config |
| 6 | Update `TextIndexLuceneAssembler.java` — detect shapes vs entityMap |
| 7 | Add DrillSideways path in `TextIndexLucene` for SHACL model |
| 8 | Tests |

### Phase 3: Suggest

| Step | Change |
|------|--------|
| 1 | Add `lucene-suggest` dependency |
| 2 | Codec config in `TextIndexLucene.openIndexWriter()` |
| 3 | Add `SuggestField` to document building when `idx:suggestable` |
| 4 | Create `TextSuggestPF.java` — `text:suggest` property function |
| 5 | Register `text:suggest` in `TextQuery.init()` |
| 6 | Tests |

### Files Overview

| File | Action | Phase |
|------|--------|-------|
| `IndexVocab.java` | New — `idx:` namespace vocabulary | 2 |
| `ShaclIndexMapping.java` | New — shape→field mapping | 2 |
| `ShaclTextDocProducer.java` | New — entity-per-document producer | 2 |
| `ShaclIndexAssembler.java` | New — SHACL config assembler | 2 |
| `TextSuggestPF.java` | New — `text:suggest` property function | 3 |
| `TextIndexLucene.java` | Modify — numeric fields, DrillSideways, suggest codec | 2+3 |
| `TextIndexLuceneAssembler.java` | Modify — shapes vs entityMap detection | 2 |
| `TextVocab.java` | Modify — `text:shapes`, `text:maxFacetHits`, `pfSuggest` | 1.5+2+3 |
| `TextIndexConfig.java` | Modify — maxFacetHits, shape-based config | 1.5+2 |
| `TextQuery.java` | Modify — register `text:suggest` | 3 |

**No files deleted.** Existing `TextDocProducerTriples`, `EntityDefinition`, `EntityDefinitionAssembler` remain untouched.

---

## 8. Open Questions

1. **Multiple shapes matching same entity:** If `ex:book1` matches both `BookShape` and `PublicationShape`, produce two documents or merge?
   - **Proposed:** Two separate documents. Each shape is independent.

2. **Field name collisions across shapes:** If `BookShape` and `PersonShape` both define `all_text`, do they share the same Lucene field?
   - **Proposed:** Yes. Same field name = same Lucene field. Queries hit both. Use `docType` filter to scope.

3. **`sh:alternativePath` predicate extraction:** For the change listener's predicate lookup, `sh:alternativePath (rdfs:label skos:prefLabel)` needs to be decomposed into individual predicates. Only flat alternative paths (list of IRIs) are supported initially.

4. **Suggest weight strategy:** `idx:suggestWeight` is static per field. Should we support dynamic weights (e.g., based on triple count, PageRank, or a numeric property)?
   - **Proposed:** Static weights for MVP. Dynamic weights are a future enhancement.

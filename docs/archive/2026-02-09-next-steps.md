# 2026-02-09 Design Assessment and Next Steps

**Date:** 2026-02-17
**Updated:** 2026-02-17 (David's feedback incorporated)
**Scope:** Assessment of the 2026-02-09 text:query / text:facet split design against the current implementation and David's review findings.

---

## 1. What the Design Gets Right

The 2026-02-09 design makes three sound architectural choices:

**Split API.** Separating `text:query` (hits) from `text:facet` (counts) is correct. The current `text:queryWithFacets` creates a cartesian product when facet variables are requested (hits x fields x values), which is both semantically confusing and expensive. A split avoids that binding explosion entirely.

**Shared execution via lookup key.** Using a normalized request signature stored in `ExecutionContext` is the right mechanism. Jena's `ExecutionContext` already supports this pattern: `TextQueryPF` uses `Symbol.create("TextQueryPF.cache")` for query caching (line 76). The existing `FACETED_RESULTS` symbol in `TextQueryFacetsPF` (line 87) was a step in this direction but is set without ever being consumed.

**Consistency requirement.** Identifying that both PFs must share the same reader snapshot is important. Currently `getFacetCounts()` opens its own `DirectoryReader` (TextIndexLucene.java:1004) completely independent of the reader used by `query$()`. If an update lands between the two calls, hit results and facet counts will disagree.

---

## 2. Holes and Gaps

### 2.1 Known Bugs Must Be Fixed First

David's review identified two **High** severity bugs. These are implementation fixes, not design changes, but must be resolved before any API work.

**Bug 1: `updateDocument` skips `facetsConfig.build()`.**
`addDocument()` (line 315) wraps the document correctly:
```java
Document indexDoc = facetFields.isEmpty() ? doc : facetsConfig.build(doc);
```
But `updateDocument()` (line 284) passes the raw document:
```java
indexWriter.updateDocument(term, doc);
```
Updated documents silently lose all facet DocValues. Facet counts drift after any update.

**Fix:** Add `facetsConfig.build(doc)` to `updateDocument()` (one-liner, see Section 4).

**Bug 2: Filtered faceting can hit `TooManyClauses`.**
The URI join approach (line 1042-1046) builds a BooleanQuery with one SHOULD clause per unique matched entity URI. With the 10,000 hit limit (line 1026), this can exceed Lucene's default `maxClauseCount` of 1024.

**Fix:** Replace the BooleanQuery with `TermInSetQuery`, which is specifically designed for large term sets and uses a more efficient data structure internally (see Section 4).

### 2.2 Document Model: SHACL NodeShapes as Lucene Documents

The current implementation uses `TextDocProducerTriples` where each RDF triple creates a separate Lucene document:

```
ex:doc1 rdfs:label "Machine Learning"  -> Lucene doc 1 (has text field)
ex:doc1 ex:category "technology"       -> Lucene doc 2 (has category facet)
ex:doc1 ex:author "Smith"              -> Lucene doc 3 (has author facet)
```

This causes overcounting (an entity with two `ex:category` values contributes 2 to the facet count instead of 1) and forces the expensive URI join workaround for filtered facets.

**Resolution:** The document-per-entity model should be driven by SHACL shapes:
- A **SHACL NodeShape** defines a Lucene document. It targets instances of a class (e.g., `ex:Book`). Each focus node matching the shape produces one Lucene document.
- **SHACL PropertyShapes** within the NodeShape define the Lucene fields of that document. Each PropertyShape path maps to a field, with extension properties specifying Lucene field type (string, text, numeric), whether it is facetable, multi-valued, etc.
- **Multiple NodeShapes per index** are supported. The matched shape is stored as a field on the document (e.g., `CarsShape`, `BusesShape` both contribute to a `vehicles` index, each document knows which shape it matched).
- SHACL integration should reuse Jena's existing SHACL library and listen for triple changes that impact the index (triples covered by the shapes).

This is the core architectural change that fixes the counting problem and eliminates the URI join entirely. The API split (Phase 1) should be designed with this model in mind, not locked to the triple-per-document workarounds.

### 2.3 Execution Reuse Needs Build-Time Detection

The design states: "First PF to run materializes Lucene execution state; the second PF reuses it."

SPARQL execution order is implementation-dependent. Rather than relying on runtime ordering, the reuse should be coordinated at query build/parse time:

- During the `build()` phase of each PF, detect whether a sibling PF (`text:query` or `text:facet`) exists in the same query with matching parameters.
- If found, both PFs register with a shared `SearchExecution` object stored in `ExecutionContext`.
- The `SearchExecution` lazily computes what each consumer needs:
  - The parsed Lucene query and index reader (always, on first access)
  - Hit results with scores (computed on first access by `text:query`)
  - Facet collector results (computed on first access by `text:facet`)
- Both PFs share the same underlying Lucene search. The Lucene `IndexSearcher`, `TopDocs`, and `FacetsCollector` are created once and reused.

This ensures we never run the same Lucene query twice when both `text:query` and `text:facet` appear in one SPARQL query.

### 2.4 Shared Execution Lookup

When both `text:query` and `text:facet` appear in the same SPARQL query, they need to identify that they should share the same Lucene execution. This is done via a lookup key stored in the `ExecutionContext`.

**How it works:** Both PFs parse their arguments during `build()`. From the parsed arguments, they compute a lookup key from the inputs that must match:
- The Lucene query string (after parsing into a Lucene `Query` object, use `Query.toString()` for a normalized form - Lucene normalizes structure during parsing)
- The property/field list (sorted)
- The graph URI and language scope (if any)

Parameters that are specific to one consumer (hit limit for `text:query`, maxValues for `text:facet`) are **excluded** from the key since they don't affect which documents match.

During `exec()`, the first PF to run creates the `SearchExecution` and stores it in `ExecutionContext` under a `Symbol` keyed by the lookup key. The second PF finds it and reuses the Lucene objects.

**Example:** For this query:
```sparql
(?s ?score) text:query (rdfs:label "climate" 20) .
(?f ?v ?c)  text:facet (rdfs:label "climate" ("category") 10) .
```
Both PFs compute the same lookup key from `[query="climate", props=[rdfs:label], graph=null, lang=null]`. The `20` (hit limit) and `10` (maxValues) are excluded.

### 2.5 Index Reader Sharing

The design says "Ensure both PFs run against the same reader snapshot" but does not specify who owns the reader.

Currently, `getFacetCounts()` opens and closes its own `IndexReader` (lines 1004, 1071), independently of `query$()`. The shared `SearchExecution` must hold an `IndexReader` open for the duration of the query.

**Resolution:** The `SearchExecution` opens the reader on first access and the reader remains open for the lifetime of the `ExecutionContext` (i.e., the SPARQL query execution). Since both PFs run within the same query execution, they naturally share the same reader snapshot. The reader is closed when the query iterator is exhausted or closed. This needs investigation into how `DatasetGraphText` manages the Lucene `Directory` to confirm we can tie reader lifecycle to the query execution.

### 2.6 Breaking Changes: Remove Old PFs

**Decision (David):** Assume breaking changes. No deprecation period or migration plan needed.

- `text:queryWithFacets` and `text:facetCounts` will be **removed**
- `text:query` will be **replaced** with the new implementation (breaking existing `TextQueryPF` behavior)
- `text:facet` is **new**

### 2.7 Structured Facet Filters

Both `text:query` and `text:facet` accept the same structured filter syntax from David's 2026-01-27 recommendation. This keeps the two PFs in sync (same base query + same filters = shared execution) and gives frontends a clean API for faceted navigation.

**`text:query` syntax:**
```sparql
(?s ?score) text:query (rdfs:label "climate" (("category" "Technology") ("author" "Smith")) 20)
```

**`text:facet` syntax:**
```sparql
(?f ?v ?c) text:facet (rdfs:label "climate" ("category" "author") (("category" "Technology") ("author" "Smith")) 10)
```

**`text:query` arguments:**
1. **Property** (optional URI): RDF property to search, e.g., `rdfs:label`
2. **Query string** (required literal): Lucene search query
3. **Facet filters** (optional list of pairs): Constrain which documents are returned, e.g., `(("author" "Smith") ("year" "2024"))`. Each pair is a field name and value. These become Lucene filter clauses (TermQuery with MUST).
4. **Limit** (optional integer): Maximum number of hits to return

**`text:facet` arguments:**
1. **Property** (optional URI): RDF property to search, e.g., `rdfs:label`
2. **Query string** (required literal): Lucene search query
3. **Facet fields** (required list): Fields to return counts for, e.g., `("category" "author")`
4. **Facet filters** (optional list of pairs): Constrain which documents are counted, e.g., `(("author" "Smith") ("year" "2024"))`. Each pair is a field name and value. These become Lucene filter clauses (TermQuery with MUST).
5. **Max values** (optional integer): Maximum facet values per field

Note: Lucene's query parser natively supports field-level filtering in the query string (e.g., `"climate AND author:Smith"`). This continues to work — building the structured filter support is a superset that delivers query string filtering for free. Users can use either or both.

#### Why structured filters?

Structured filters and query string filters serve different purposes in a search UI.

**Query string** (`"climate AND author:Smith"`) is what powers the **search box**. Lucene parses the raw string with full-text semantics: tokenized, analyzed, scored. The user must know Lucene field names and syntax.

**Structured facet filters** (`(("author" "Smith"))`) are what happen when a user **clicks a facet value** in a sidebar:

```
Category:  [x] Technology  [ ] Science  [ ] Cooking
Author:    [ ] Smith       [x] Jones
Year:      [x] 2024
```

Each checkbox click adds an exact-match filter pair. No Lucene syntax knowledge needed — the field names are internal to the index.

| Aspect | Query string | Structured filter |
|--------|-------------|-------------------|
| **UI action** | User types in search box | User clicks facet checkbox |
| **Matching** | Full-text (tokenized, fuzzy, scored) | Exact term match |
| **Syntax knowledge** | User needs Lucene syntax | None — field names are internal |
| **Scoring** | Affects relevance ranking | Does not affect ranking |
| **Typical use** | "Find documents about climate" | "Show only author=Smith" |

**Typical faceted search interaction:**
1. User types `"climate"` in search box → **query string**
2. `text:facet` returns counts: Technology(12), Science(8), Cooking(0)
3. User clicks "Technology" → **structured filter** `(("category" "Technology"))`
4. Both PFs re-run with the filter: hits narrowed, counts update: Smith(5), Jones(3)
5. User clicks "Smith" → filter grows to `(("category" "Technology") ("author" "Smith"))`

The query string stays `"climate"` throughout. The filters accumulate from user clicks. Both constrain the Lucene result set, but they come from different parts of the UI and have different semantics (full-text vs exact match).

Because both PFs accept the same filter list, the shared execution mechanism works naturally — same query string + same filters = same lookup key = one Lucene search.

The nested list parsing is more complex than the current flat positional parsing but there is precedent in Jena PFs for this pattern.

---

## 3. Implementation Plan

### Phase 0: Bug Fixes

Fix the two known bugs immediately. These are independent of any design work.

| Fix | Effort | Impact |
|-----|--------|--------|
| Add `facetsConfig.build(doc)` to `updateDocument()` | Trivial (1 line) | Fixes silent data corruption on updates |
| Replace BooleanQuery URI join with `TermInSetQuery` | Small | Eliminates TooManyClauses risk |
| Open reader once per `getFacetCounts` call, not per-facet-field | Small | Reduces reader churn |

### Phase 1: API Split + Execution Sharing

Replace the existing PFs with the new split API.

1. **New `text:facet` PF** with full syntax including facet fields list, structured facet filters (list of pairs), and maxValues.
2. **Replace `text:query`** (`TextQueryPF`) with new implementation supporting structured facet filters and limit. Breaking change to existing `text:query` behavior. Both PFs accept the same base arguments (property, query string, filters) so they naturally share execution.
3. **Remove `text:queryWithFacets` and `text:facetCounts`** entirely.
4. **Shared `SearchExecution`:**
   - Build-time detection of sibling PFs in the same query
   - Lookup key from normalized Lucene query + property list + filters + graph/lang scope
   - Lazy computation of hits and facet collector
   - Shared index reader for consistency
5. **Argument parsing** for nested lists (facet fields list, facet filter pairs). Same parsing logic shared between both PFs for the common arguments.

### Phase 2: SHACL-Driven Document Model

Replace the triple-per-document model with SHACL-driven entity-per-document indexing.

1. **SHACL-to-index mapping:**
   - NodeShape = Lucene document (one per focus node / entity)
   - PropertyShape path = Lucene field
   - Extension properties for field type (string, text, numeric), facetable, multi-valued, stored
   - Multiple NodeShapes per index, shape stored as a field on the document
2. **New document producer** that materializes one Lucene document per focus node, collecting all field values from the graph.
3. **Change listener** using Jena's SHACL library to detect triple changes that impact the index (triples matching the shapes). Incremental update of affected documents.
4. **Config extension** to the existing `config.ttl` assembler format to specify SHACL shapes and the Lucene field vocabulary.
5. **Remove triple-per-document model** and the URI join workaround in filtered faceting.

### Phase 3: Suggestions

Per David's recommendation:
1. `text:suggest` property function backed by Lucene suggesters
2. `CompletionField` for fast autocomplete
3. Optional analyzer config per field

---

## 4. Specific Code Changes Required

### Phase 0 (Bug Fixes)

**TextIndexLucene.java:280-294** - Fix `updateDocument`:
```java
protected void updateDocument(Entity entity) throws IOException {
    Document doc = doc(entity);
    Document indexDoc = facetFields.isEmpty() ? doc : facetsConfig.build(doc);
    Term term = new Term(docDef.getEntityField(), entity.getId());
    indexWriter.updateDocument(term, indexDoc);
}
```

**TextIndexLucene.java:1040-1046** - Replace BooleanQuery with TermInSetQuery:
```java
// Replace:
BooleanQuery.Builder uriQueryBuilder = new BooleanQuery.Builder();
for (String uri : matchedUris) {
    uriQueryBuilder.add(...);
}
// With:
List<BytesRef> terms = matchedUris.stream()
    .map(BytesRef::new).collect(Collectors.toList());
Query uriQuery = new TermInSetQuery(entityField, terms);
```

### Phase 1 (API Split)

New/modified files:
| File | Action |
|------|--------|
| `TextFacetPF.java` | New - implements `text:facet` with nested list parsing |
| `SearchExecution.java` | New - shared execution state with lazy computation |
| `TextQueryPF.java` | Replace - new implementation for `text:query` |
| `TextQuery.java` | Modify - register `text:facet`, remove old PF registrations |
| `TextVocab.java` | Modify - add `pFacet` vocabulary term, remove old terms |
| `TextIndexLucene.java` | Modify - expose shared reader/searcher/collector methods |
| `TextQueryFacetsPF.java` | Delete |
| `TextFacetCountsPF.java` | Delete |

### Phase 2 (SHACL Document Model)

New/modified files:
| File | Action |
|------|--------|
| `ShaclTextDocProducer.java` | New - SHACL-driven document producer |
| `ShaclIndexMapping.java` | New - NodeShape-to-document / PropertyShape-to-field mapping |
| `TextFieldVocab.java` | New - mini vocab for Lucene field options (type, facetable, etc.) |
| `TextIndexLuceneAssembler.java` | Modify - parse SHACL shape references from config |
| `TextIndexLucene.java` | Modify - remove URI join code, simplify filtered faceting |
| `TextDocProducerTriples.java` | Delete |
| `TextDocProducerEntities.java` | Delete (already dead code) |

---

## 5. Test Cases Required

### Phase 0 Tests

| Test | Validates |
|------|-----------|
| Update document, verify facet counts still correct | `updateDocument` + `facetsConfig.build` fix |
| Filtered facets with >1024 matching entities | `TermInSetQuery` replacement |
| Filtered facets with >10,000 matching entities | Removal of arbitrary 10k limit or explicit documentation of it |
| Entity with multiple values for same facet field | Overcounting behavior (document existing behavior) |

### Phase 1 Tests

| Test | Validates |
|------|-----------|
| `text:facet` alone returns correct counts | Independent operation |
| `text:query` alone returns correct hits | Independent operation |
| Both in same query, same results as separate queries | Shared execution correctness |
| Both in same query, different property filters | Key isolation (should NOT share) |
| `text:facet` with no query (open facets) | Open facet browsing |
| `text:facet` with query filter | Filtered facet counts |
| `text:facet` with facet filters (list of pairs) | Structured filter support |
| `text:facet` with both query and facet filters | Combined filtering |
| `text:query` with limit, `text:facet` with maxValues | Different per-consumer parameters |
| Concurrent queries sharing execution context | Thread safety |

### Phase 2 Tests

| Test | Validates |
|------|-----------|
| NodeShape creates one Lucene doc per focus node | Document model |
| PropertyShape paths map to correct Lucene fields | Field mapping |
| Multiple NodeShapes in same index | Shape coexistence |
| Shape field stored on document | Shape discrimination |
| Facet counts are per-entity, not per-triple | Overcounting fix |
| Triple insert triggers index update for matching shape | Change listener |
| Triple delete removes document when shape no longer matches | Change listener |
| Triple update to non-shape-covered predicate does not reindex | Change listener efficiency |
| Field type options (string, text, numeric, facetable) | Lucene field vocab |
| Multi-valued field indexing | Multiple values per field per entity |

---

## 6. Open Questions

1. **Should the 10,000 hit limit in filtered faceting be configurable or removed?** With `TermInSetQuery`, the clause limit is gone, but collecting 10k+ hits is still expensive. Under the SHACL document model (Phase 2), the URI join is eliminated entirely, making this moot. For Phase 0, consider raising or making configurable.

2. **Should open facets (no query) still be supported in `text:facet`?** Open facets are useful for "browse by category" UIs where no search has been performed. Confirm they remain a valid use case alongside filtered facets.

3. **SHACL change listener mechanism:** Does Jena's SHACL library provide hooks for monitoring triple changes, or does a custom `DatasetChanges` listener need to match triples against shapes? The existing `TextDocProducerTriples` uses `DatasetChanges` to intercept quad additions/deletions. The SHACL producer would need similar hooks but matching against shape paths rather than predicate lists.

4. **Incremental reindexing scope:** When a triple changes that matches a PropertyShape path, do we reindex just the affected field or the entire document for that focus node? Full document reindex is simpler but more expensive.

---

**Assessment Status:** Complete (updated with David's feedback)
**Recommended order:** Phase 0 (bug fixes) -> Phase 1 (API split + execution sharing) -> Phase 2 (SHACL document model) -> Phase 3 (suggestions)

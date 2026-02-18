# Design Decisions

## Phase History

| Phase | Date | Summary |
|-------|------|---------|
| 1 | Jan 2026 | Initial native faceting: `SortedSetDocValues`, `text:queryWithFacets`, `text:facetCounts` |
| 1.5 | Feb 2026 | Enhancements: minCount, configurable maxFacetHits, getAllChildren |
| 2 (API) | Feb 2026 | API redesign after review: split PFs, JSON filters, shared execution, bug fixes |
| 2 (SHACL) | Feb 2026 | Entity-per-document model: SHACL shapes, typed fields, change listener |

---

## Decision: Split PFs instead of combined query+facets

**Problem:** Phase 1 had `text:queryWithFacets` which returned both hits and facet counts in a single PF. This caused a cartesian product in SPARQL bindings — every hit was crossed with every facet value, creating N*M rows.

**Decision:** Two independent PFs that share execution when parameters match.

- `text:query` — returns hits (one row per match)
- `text:facet` — returns facet counts (one row per value)

**Rationale:** SPARQL property functions return flat bindings. Hits and facets have different cardinalities and different output shapes. Keeping them separate is semantically clean and avoids the binding explosion. Shared execution via `SearchExecution` ensures efficiency (one Lucene query, not two).

---

## Decision: JSON syntax for filters and facet field lists

**Problem:** How to pass structured filter criteria (`category=Technology AND author=Smith`) in a SPARQL property function argument list.

**Alternatives considered:**
1. RDF lists of pairs — verbose, hard to construct dynamically
2. Custom string DSL — requires new parser, unfamiliar
3. JSON — widely understood, easy to construct in application code

**Decision:** JSON object for filters, JSON array for facet field lists.

```sparql
(?s ?sc) text:query ("learning" '{"category": ["Technology"]}') .
(?f ?v ?c) text:facet ("learning" '["category", "author"]') .
```

**Rationale:** JSON is the lingua franca of web APIs. Application code generating SPARQL queries can construct filter JSON from UI state (e.g., selected facet checkboxes) trivially. The `{` and `[` prefixes make reliable detection in the argument list straightforward.

---

## Decision: Entity-per-document via SHACL shapes (not extending entityMap)

**Problem:** The triple-per-document model causes:
- Facet values on different docs from text values → requires URI-join
- Overcounting risk when same entity has multiple triples
- Cannot use `DrillSideways` (requires all fields on same doc)
- No numeric field types

**Alternatives considered:**
1. Extend `text:entityMap` with field type annotations — would add complexity to an already complex config format
2. New custom config vocabulary — no established standard
3. SHACL NodeShape/PropertyShape — maps naturally to "entity with typed fields"

**Decision:** Use SHACL vocabulary (`sh:targetClass`, `sh:path`, `sh:alternativePath`) with extension properties in the `idx:` namespace. Parse config using standard Jena RDF API — no jena-shacl dependency.

**Rationale:**
- SHACL shapes map directly to the concept of "an entity type with typed properties"
- `sh:targetClass` is exactly how you say "these are Book entities"
- `sh:path` / `sh:alternativePath` naturally express "index these predicates"
- Extension properties (`idx:fieldType`, `idx:facetable`, etc.) are clean additions
- No dependency on jena-shacl — we're reading config, not running validation

---

## Decision: Coexist with entityMap, don't replace it

**Decision:** `text:shapes` and `text:entityMap` are mutually exclusive per index, but the code for both is present. No existing code was removed.

**Rationale:** Existing deployments use `text:entityMap`. Forcing migration would be disruptive. Users can adopt SHACL mode for new deployments or when they need faceting/numeric fields, while existing setups continue to work unchanged.

---

## Decision: Rebuild entire entity document on any change

**Problem:** When a triple changes for an entity in SHACL mode, should we update just the affected field or rebuild the whole document?

**Decision:** Full rebuild — read all triples for the entity, construct complete Entity, replace the Lucene document.

**Rationale:** Lucene documents are immutable. Updating a single field requires deleting and re-adding the entire document anyway. Reading all triples for one entity from TDB is fast (typically <10 triples). The simplicity of "always rebuild" avoids complex partial-update logic and ensures consistency.

---

## Decision: Discriminator field for multi-shape indexes

**Problem:** When multiple shapes target different classes (Book, Article) in the same index, how to distinguish their documents.

**Decision:** A `docType` StringField containing the local name of the first `sh:targetClass` (e.g., "Book", "Article").

**Rationale:** Simple, human-readable, sufficient for MVP. The composite delete query uses `(uri + docType)` to target the right document when rebuilding.

---

## Decision: TermInSetQuery instead of BooleanQuery for URI joins

**Problem:** The original filtered faceting used `BooleanQuery` with one clause per matched entity URI. With 10k+ hits, this exceeds Lucene's 1024 clause limit (`TooManyClauses`).

**Decision:** Replace with `TermInSetQuery`, which handles arbitrary numbers of terms efficiently using sorted byte arrays.

**This was a bug fix** identified in David's review. The original code would silently fail on large result sets.

---

## Decision: FacetsConfig.build() on updateDocument

**Problem:** `addDocument()` called `facetsConfig.build(doc)` to create DocValues for facet fields, but `updateDocument()` did not. After an entity was updated, its facet DocValues were silently lost, causing facet counts to drift.

**Decision:** Both `addDocument()` and `updateDocument()` now call `facetsConfig.build()`.

**This was a bug fix** identified in David's review. Difficult to detect because updates appear to work (text search still finds the entity) but facet counts gradually become incorrect.

---

## Decision: maxFacetHits as assembler property (not per-query)

**Problem:** The two-pass facet approach runs a secondary search to collect facets. How many documents should that secondary search consider?

**Decision:** Configurable via `text:maxFacetHits` assembler property. 0 means unlimited (`Integer.MAX_VALUE`).

**Rationale:** This is an operational tuning knob, not a query semantics control. Setting it per-index via the assembler is appropriate. Applications that need per-query control can use the Java API directly.

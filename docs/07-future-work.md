# Known Limitations & Future Work

## Items Needing Attention

### Assembler integration test with TTL file

The `TestShaclAssembler` builds config programmatically using the Model API. There is no test that loads a real TTL file with `text:shapes`. Adding a TTL-file-based assembler test would catch config serialisation issues.

### Large-scale performance validation

All tests use small datasets (5-10 entities). The SHACL mode's "rebuild entire entity on any change" approach reads all triples for the entity from the base dataset. This should be validated with:
- Entities with 100+ triples
- Indexes with 100k+ entities
- High-frequency update scenarios

### Multiple sh:targetClass per shape

The data model supports multiple `sh:targetClass` values per shape. The change listener and document builder handle this correctly. However, this pattern has limited test coverage — most tests use one class per shape.

### Concurrent write transactions

The `ShaclTextDocProducer` uses a `ThreadLocal<Boolean>` for transaction tracking (matching the pattern in `TextDocProducerTriples`). Concurrent write scenarios should be stress-tested, particularly the rebuild-on-change flow where one transaction might read partially-committed state from the base dataset.

---

## Planned Extensions

### Spatial filtering (bbox)

Combine text search with a bounding-box spatial filter so that facet counts and search results are scoped to a geographic region. Proposed in the [January 2026 review](archive/2026-01-23-david-review.md) as an optional structured argument:

```sparql
(?f ?v ?c) luc:facet ("climate" '["category"]' '{"bbox": [40.0, -75.0, 41.0, -74.0]}') .
```

The bbox would become a Lucene spatial `MUST` filter combined with the text query. Would require:
- A spatial field type (e.g., `LatLonPoint`) in the SHACL field type enum
- Indexing coordinates into the entity document
- Parsing the bbox argument in `ShaclTextQueryPF` and `TextFacetPF`
- Integration with or independence from the existing `jena-geosparql` module (Lucene's native spatial may be simpler than bridging to JTS/SIS)

This does not change the query or response models — it extends the existing filter argument with a spatial dimension.

### Deferrable extensions

The following features do not change the existing query or response models. Each can be added later as either an opt-in parameter on an existing PF or as a new PF, with no breaking changes. They are listed here for completeness but are not prioritised.

#### DrillSideways

The entity-per-document model enables Lucene's `DrillSideways` API for efficient faceted navigation. Currently, facet counts are collected via `SortedSetDocValuesFacetCounts` with a `FacetsCollector`. DrillSideways would allow:

- Single-pass combined search + facet collection
- "Drill sideways" semantics: when filtering by category=Technology, the category facet still shows counts for *all* categories (not just Technology), while other facet fields are filtered

This is the standard faceted search UX pattern. Implementation requires:
1. Replace `FacetsCollector` + separate search with `DrillSideways.search()`
2. Build `DrillDownQuery` from filters
3. Collect both filtered hits and sideways facet counts in one pass

**Prerequisite:** Entity-per-document (completed). DrillSideways requires all facet fields on the same document as the searchable fields.

**API impact:** Opt-in parameter on `luc:facet`. No breaking changes.

#### Hierarchical facets

For taxonomy-based faceting (e.g., Science > Physics > Quantum Physics), Lucene supports hierarchical facets via path-based `FacetField`:

```java
doc.add(new FacetField("category", "Science", "Physics", "Quantum Physics"));
```

Would require:
- Config syntax for hierarchy delimiter or SKOS broader/narrower traversal
- Returning hierarchy paths in facet results
- UI support for drill-down

**API impact:** Facet values become path strings. Same response shape `(field, value, count)`. No breaking changes.

#### Range facets

Numeric and date range faceting (e.g., "2020-2024", "0-100"):

```sparql
PREFIX luc: <urn:jena:lucene:index#>
(?range ?count) luc:facetRange ("learning" "year" '[2020, 2022, 2024, 2026]') .
```

The entity-per-document model with `IntPoint`/`LongPoint`/`DoublePoint` fields provides the foundation. Implementation would use Lucene's `LongRangeFacetCounts` or similar.

**API impact:** New PF `luc:facetRange`. No changes to existing PFs.

#### Result grouping

Group search results by a field value (e.g., group books by author). Lucene's `GroupingSearch` can do this efficiently. The entity-per-document model makes this straightforward since the grouping field is on the same document.

**API impact:** New PF `luc:group`. No changes to existing PFs.

#### Suggest / Autocomplete

Add `luc:suggest` property function using Lucene's `SuggestField` for type-ahead autocomplete:

```sparql
PREFIX luc: <urn:jena:lucene:index#>
(?suggestion ?weight) luc:suggest ("mach" 10) .
```

Would require:
- New `SuggestField` in SHACL field types
- Lucene `Suggester` (e.g., `AnalyzingInfixSuggester`)
- New PF implementation
- Index rebuild to populate suggest data structures

**API impact:** New PF `luc:suggest`. No changes to existing PFs.

#### SHACL-aware bulk reindexing

The existing `jena.textindexer` CLI tool indexes all data using the triple-per-document model. A SHACL-aware variant would:
1. Load the SHACL mapping from the assembler config
2. For each entity matching a shape's `sh:targetClass`, build a complete entity document
3. Bulk-index efficiently (no per-triple rebuild overhead)

**API impact:** CLI tool only. No SPARQL API changes.

---

## Architectural Considerations

### Index rebuild required when switching modes

Switching from `text:entityMap` to `text:shapes` (or vice versa) requires a full reindex. The Lucene document structure is fundamentally different between the two modes. There is no migration path.

### FacetsConfig must match between index and read

The `FacetsConfig` (which fields are multi-valued, etc.) must be consistent between index time and read time. If a field is changed from single-valued to multi-valued, existing documents will have the old structure. A reindex resolves this.

### Memory considerations for faceting

`SortedSetDocValues` faceting stores all unique values in memory during facet collection. For fields with very high cardinality (e.g., URIs), this can consume significant memory. The `text:maxFacetHits` property limits the number of documents searched, which indirectly limits memory usage.

### Thread safety

`TextIndexLucene` assumes single-writer semantics (enforced by Jena's transaction system). The `IndexWriter` is volatile but not locked. The `ShaclTextDocProducer`'s `ThreadLocal<Boolean>` for transaction state follows the same pattern as `TextDocProducerTriples`.

Read operations (queries, facets) open a new `DirectoryReader` per call, which is thread-safe and sees a consistent snapshot.

# Known Limitations & Future Work

## Items Needing Attention

### Named graph support in SHACL mode

The `ShaclTextDocProducer` currently reads triples from the default graph only (`baseDataset.getDefaultGraph()`). Entities in named graphs will not be indexed. This should be extended to iterate over all graphs, or to accept a configurable graph URI.

**Impact:** Users who store data in named graphs will not see those entities indexed in SHACL mode. Classic mode handles named graphs via the `graphField` in `EntityDefinition`.

### Assembler integration test with TTL file

The `TestShaclAssembler` builds config programmatically using the Model API. There is no test that loads a real TTL file with `text:shapes`. Adding a TTL-file-based assembler test would catch config serialisation issues.

### Large-scale performance validation

All tests use small datasets (5-10 entities). The SHACL mode's "rebuild entire entity on any change" approach reads all triples for the entity from the base dataset. This should be validated with:
- Entities with 100+ triples
- Indexes with 100k+ entities
- High-frequency update scenarios

The two-pass URI-join in classic mode should also be profiled with large result sets to understand where `text:maxFacetHits` needs to be set.

### Multiple sh:targetClass per shape

The data model supports multiple `sh:targetClass` values per shape. The change listener and document builder handle this correctly. However, this pattern has limited test coverage — most tests use one class per shape.

### Concurrent write transactions

The `ShaclTextDocProducer` uses a `ThreadLocal<Boolean>` for transaction tracking (matching the pattern in `TextDocProducerTriples`). Concurrent write scenarios should be stress-tested, particularly the rebuild-on-change flow where one transaction might read partially-committed state from the base dataset.

---

## Planned Extensions

### DrillSideways (next priority)

The entity-per-document model enables Lucene's `DrillSideways` API for efficient faceted navigation. Currently, facet counts are collected via `SortedSetDocValuesFacetCounts` with a `FacetsCollector`. DrillSideways would allow:

- Single-pass combined search + facet collection
- "Drill sideways" semantics: when filtering by category=Technology, the category facet still shows counts for *all* categories (not just Technology), while other facet fields are filtered

This is the standard faceted search UX pattern. Implementation requires:
1. Replace `FacetsCollector` + separate search with `DrillSideways.search()`
2. Build `DrillDownQuery` from filters
3. Collect both filtered hits and sideways facet counts in one pass

**Prerequisite:** Entity-per-document (completed). DrillSideways requires all facet fields on the same document as the searchable fields.

### Result grouping

Group search results by a field value (e.g., group books by author). Lucene's `GroupingSearch` can do this efficiently. The entity-per-document model makes this straightforward since the grouping field is on the same document.

```sparql
# Hypothetical future syntax
(?author ?s ?score) text:queryGrouped ("learning" "author" 10) .
```

### Suggest / Autocomplete (Phase 3)

Add `text:suggest` property function using Lucene's `SuggestField` for type-ahead autocomplete:

```sparql
(?suggestion ?weight) text:suggest ("mach" 10) .
```

Would require:
- New `SuggestField` in SHACL field types
- Lucene `Suggester` (e.g., `AnalyzingInfixSuggester`)
- New PF implementation
- Index rebuild to populate suggest data structures

### Hierarchical facets

For taxonomy-based faceting (e.g., Science > Physics > Quantum Physics), Lucene supports hierarchical facets via path-based `FacetField`:

```java
doc.add(new FacetField("category", "Science", "Physics", "Quantum Physics"));
```

Would require:
- Config syntax for hierarchy delimiter or SKOS broader/narrower traversal
- Returning hierarchy paths in facet results
- UI support for drill-down

### Range facets

Numeric and date range faceting (e.g., "2020-2024", "0-100"):

```sparql
(?range ?count) text:facetRange ("learning" "year" '[2020, 2022, 2024, 2026]') .
```

The entity-per-document model with `IntPoint`/`LongPoint`/`DoublePoint` fields provides the foundation. Implementation would use Lucene's `LongRangeFacetCounts` or similar.

### SHACL-aware bulk reindexing

The existing `jena.textindexer` CLI tool indexes all data using the triple-per-document model. A SHACL-aware variant would:
1. Load the SHACL mapping from the assembler config
2. For each entity matching a shape's `sh:targetClass`, build a complete entity document
3. Bulk-index efficiently (no per-triple rebuild overhead)

### Inverse and sequence paths

Currently only `sh:path <uri>` and `sh:alternativePath` are supported. Future extensions:
- `sh:inversePath` — index objects that point *to* the entity
- Sequence paths — traverse multi-hop paths (e.g., `ex:author / ex:name`)

---

## Architectural Considerations

### Index rebuild required when switching modes

Switching from `text:entityMap` to `text:shapes` (or vice versa) requires a full reindex. The Lucene document structure is fundamentally different between the two modes. There is no migration path.

### FacetsConfig must match between index and read

The `FacetsConfig` (which fields are multi-valued, etc.) must be consistent between index time and read time. If a field is changed from single-valued to multi-valued, existing documents will have the old structure. A reindex resolves this.

### Memory considerations for faceting

`SortedSetDocValues` faceting stores all unique values in memory during facet collection. For fields with very high cardinality (e.g., URIs), this can consume significant memory. The `text:maxFacetHits` property limits the number of documents searched, which indirectly limits memory usage.

For the two-pass URI-join in classic mode, the entire set of matched entity URIs is materialised in a `Set<String>`. With 100k+ matches, this set itself can be large.

### Thread safety

`TextIndexLucene` assumes single-writer semantics (enforced by Jena's transaction system). The `IndexWriter` is volatile but not locked. The `ShaclTextDocProducer`'s `ThreadLocal<Boolean>` for transaction state follows the same pattern as `TextDocProducerTriples`.

Read operations (queries, facets) open a new `DirectoryReader` per call, which is thread-safe and sees a consistent snapshot.

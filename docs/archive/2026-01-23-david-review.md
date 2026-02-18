# 2026-01-23 David review

## Findings
- High: `updateDocument` skips `facetsConfig.build`, so updated docs lose facet DocValues and facet counts drift after updates. `jena-text/src/main/java/org/apache/jena/query/text/TextIndexLucene.java:280`
- High: filtered faceting collects only the top 10k hits then builds a BooleanQuery with one clause per URI; this truncates counts and can hit `BooleanQuery.TooManyClauses` on large result sets. `jena-text/src/main/java/org/apache/jena/query/text/TextIndexLucene.java:1025`
- Medium: `queryWithFacets$` reads a single stored value via `doc.get`, so multi-valued facets are undercounted and configs with `valueStored=false` return empty facet counts. `jena-text/src/main/java/org/apache/jena/query/text/TextIndexLucene.java:897` and `jena-text/src/main/java/org/apache/jena/query/text/TextIndexConfig.java:41`
- Medium: triple-per-document is still the active model; the entity-based path is dead code, so facet counts remain per-triple and can overcount per entity. `jena-text/src/main/java/org/apache/jena/query/text/TextDocProducerTriples.java:63` and `jena-text/src/main/java/org/apache/jena/query/text/TextDocProducerEntities.java:74`

## Design notes (object = document)
- The 1 object = 1 document model aligns with Lucene faceting and avoids expensive join-back patterns.
- SHACL NodeShapes/PropertyShapes are a good schema source, but incremental updates will need dependency tracking or restricted path patterns to avoid broad reindexing.
- If relying on inferred `rdf:type`, index-time entailment must match query-time entailment to avoid missing documents.
- `sh:datatype` can inform field typing, but explicit mappings for analyzer, docvalues, and facet behavior are still needed.

## API sketch (split PFs)
- Keep `text:query` for hits and paging; add `text:facet` for counts only.
- Require `text:facet` to use the same base args as `text:query` (props + query string) so counts match.
- Facet fields must be explicitly listed; `text:facet` ignores paging and returns full counts.

```sparql
SELECT ?s ?score {
  (?s ?score) text:query (rdfs:label "climate" 20) .
}

SELECT ?facetField ?facetValue ?facetCount {
  (?facetField ?facetValue ?facetCount) text:facet (rdfs:label "climate" "category" "author" 10) .
}
```

Facet filters (optional):
- Preferred: encode field filters in the Lucene query string, e.g., `category:"Books" AND author:Smith`.
- Also support a list of field/value pairs, e.g., `text:facet (rdfs:label "climate" ("category" "author") (("category" "Books") ("author" "Smith")) 10)`.

Spatial filters (optional):
- Provide a structured bbox argument that becomes a Lucene spatial filter, e.g.,
  `text:facet (rdfs:label "climate" ("category" "author") (("bbox" 40.0 -75.0 41.0 -74.0)) 10)`.
- This is combined as a MUST filter with the text query; facets are computed on the filtered set.

## Plan (incremental)
### Phase 1: Document model + SHACL-driven indexing
- Define a SHACL-to-index mapping (NodeShape = document, PropertyShape path = field, with extension flags for facet/index/store).
- Implement a new doc producer that materializes one Lucene doc per focus node.
- Ensure stored fields for projection (labels, identifiers) and facet docvalues are wired on the document.
- Keep triple-per-doc as legacy mode, but make document-per-entity the default for new configs.

### Phase 2: Query path simplification
- Move text search to return entity IDs directly.
- Use cheap subject-based lookups for returning triples (index nested-loop rather than full join).

### Phase 3: Suggestions (near-term)
- Add a `text:suggest` property function backed by Lucene suggesters.
- Start with `CompletionField` (fast and easy) and allow optional analyzer config per field.

### Phase 4: Optional future features
- Spell correction as a follow-on (requires separate term index or direct spellchecker).
- Vector/HNSW once an embedding pipeline exists; define vector field config but keep it off by default.

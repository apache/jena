# 2026-02-09 Text Query and Facet Split Design

## Context
- We want text search results and facet counts in SPARQL without forcing a single flattened binding shape that mixes hits and facets.
- We also want consistent counts and performance when both `text:query` and `text:facet` are used in the same SPARQL query.

## Motivations
- SPARQL property functions return flat bindings; nested structures are awkward.
- Returning hits and facets together tends to create cartesian/sparse binding problems.
- A split API gives cleaner semantics:
  - `text:query` returns hits.
  - `text:facet` returns facet counts only.
- This still aligns with Lucene usage: search constraints and facet dimensions are separate concerns.

## Proposed Surface API
- Keep and extend `text:query` for hits and pagination.
- Add/use separate `text:facet` for counts.
- Require both to accept the same base filter/query inputs so they can be guaranteed equivalent.
- `text:facet` adds:
  - requested facet fields
  - optional max facet values

### Example
```sparql
SELECT ?s ?score WHERE {
  (?s ?score) text:query (rdfs:label "climate AND color:red" 20) .
}

SELECT ?field ?value ?count WHERE {
  (?field ?value ?count) text:facet (rdfs:label "climate AND color:red" ("category" "author") 20) .
}
```

## Lucene Semantics
- This is "Lucene-like":
  - Query/filter clauses (e.g., `color:red`) belong to the query/filter expression.
  - Facet dimensions (`category`, `author`) are API parameters to facet counting.
- It is not required to model everything as one monolithic function call at the SPARQL layer.

## Execution Design
- Do not rely on brittle "detect sibling PF calls" logic.
- Instead, build a canonical request signature from normalized inputs:
  - properties
  - query/filter expression
  - graph/lang scope
  - other relevant options
- Use that key to share a per-query `SearchExecution` object in `ExecutionContext`.
- First PF to run materializes Lucene execution state; the second PF reuses it.

## Consistency Requirements
- Ensure both PFs run against the same reader snapshot/transaction view.
- If using cache entries, include snapshot identity in the cached execution state.
- This avoids drift between hit results and facet counts.

## Practical Implementation Notes
- Reuse existing context-based caching patterns used by `text:query`.
- Keep `text:query` and `text:facet` independently callable.
- When both are present and normalized inputs match, reuse shared execution automatically.


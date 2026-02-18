# Faceting Implementation Assessment

**Date:** 2026-01-17
**Reviewer:** Independent Review
**Scope:** Consistency with Jena patterns, alignment with GraphDB Lucene Connector

---

## 1. Executive Summary

The faceting implementation provides functional native Lucene faceting through two SPARQL property functions. The implementation generally follows Jena patterns but has several inconsistencies and gaps when compared to both Jena conventions and GraphDB's faceting approach. Key concerns include asymmetric functionality between SPARQL and Java APIs, naming that may confuse users, and missed alignment opportunities with GraphDB.

---

## 2. Jena Pattern Consistency

### 2.1 Conformant Patterns

| Aspect | Assessment |
|--------|------------|
| Property function structure | Correct - extends `PropertyFunctionBase` |
| Vocabulary definition | Correct - `pFacetFields` in `TextVocab.java` |
| Assembler integration | Correct - parsed in `TextIndexLuceneAssembler` |
| Registration in `TextQuery.init()` | Correct - follows existing pattern |
| License headers | Present and correct |

### 2.2 Non-Conformant Patterns

| Issue | Description | Severity |
|-------|-------------|----------|
| **Naming inconsistency** | `text:queryWithFacets` suggests it returns facets, but it only returns documents + scores. Facets require SPARQL GROUP BY. Misleading name. | Medium |
| **Asymmetric API** | Java API supports filtered facets via `getFacetCounts(query, fields, max)`. SPARQL `text:facetCounts` does not. Users must use different patterns for same functionality. | High |
| **RDF list parsing** | Assembler has redundant list-handling code (lines 203-225) with try-catch fallback. Other Jena assemblers use `GraphUtils.multiValueResource()` or similar utilities. | Low |
| **Type casting** | `TextFacetCountsPF.chooseTextIndex()` casts to `TextIndexLucene` directly. Other PFs use interface-based approach. Limits extensibility. | Low |

### 2.3 Comparison with `text:query`

| Feature | `text:query` | `text:queryWithFacets` | `text:facetCounts` |
|---------|--------------|------------------------|-------------------|
| Returns documents | Yes | Yes | No |
| Returns score | Yes | Yes | N/A |
| Returns facets | No | No (via GROUP BY) | Yes |
| Accepts limit | Yes | No | Yes (maxValues) |
| Accepts property filter | Yes | Yes | No |
| Caching | Yes | No | No |

**Observation:** `text:queryWithFacets` provides almost identical functionality to `text:query`. The "WithFacets" suffix is misleading since no facet data is directly returned.

---

## 3. GraphDB Lucene Connector Comparison

### 3.1 Configuration Syntax

| Aspect | Jena Implementation | GraphDB |
|--------|---------------------|---------|
| Field declaration | `text:facetFields ("f1" "f2")` (RDF list) | `"facet": true` per field |
| Default behavior | Faceting disabled unless configured | Faceting enabled by default |
| Field specification | Global list in index config | Per-field boolean |

**Gap:** Jena requires explicit field listing, GraphDB defaults to enabled. GraphDB's per-field approach is more granular.

### 3.2 Query Syntax

| Aspect | Jena | GraphDB |
|--------|------|---------|
| Facet field specification | Part of property function args | `luc:facetFields "f1,f2"` predicate |
| Return structure | Flat bindings `(?field ?value ?count)` | Blank nodes with `luc:facetName/Value/Count` |
| Combine search + facets | Requires two patterns or GROUP BY | Single pattern with `luc:query` + `luc:facets` |

**GraphDB Example:**
```sparql
SELECT ?facetName ?facetValue ?facetCount WHERE {
  ?r a luc-index:my_index ;
    luc:query "search terms" ;
    luc:facetFields "category,author" ;
    luc:facets [
      luc:facetName ?facetName;
      luc:facetValue ?facetValue;
      luc:facetCount ?facetCount
    ]
}
```

**Jena Equivalent (requires two patterns):**
```sparql
# Search
(?doc ?score) text:queryWithFacets ("search terms") .
# Facets (separate, no filtering by search)
(?field ?value ?count) text:facetCounts ("category" "author" 10) .
```

### 3.3 Feature Matrix

| Feature | Jena | GraphDB |
|---------|------|---------|
| Open facets | Yes | Yes |
| Filtered facets (SPARQL) | No | Yes |
| Filtered facets (API) | Yes | N/A |
| Multiple fields per query | Yes | Yes |
| Facet value limit | Yes | Not documented |
| Hierarchical facets | No | Not documented |
| Range facets | No | Not documented |

---

## 4. Identified Risks and Gaps

### 4.1 Critical Gaps

| Gap | Impact | Recommendation |
|-----|--------|----------------|
| **No SPARQL filtered facets** | Users cannot get facet counts for search results without SPARQL GROUP BY (inefficient for large result sets) | Add query parameter to `text:facetCounts` |
| **Misleading function name** | `text:queryWithFacets` implies facet return but doesn't deliver | Rename to `text:queryScored` or document clearly |

### 4.2 Alignment Opportunities

| Opportunity | Benefit | Effort |
|-------------|---------|--------|
| Support comma-delimited field syntax | Easier migration from GraphDB | Low |
| Add per-field `text:facet true` config | Alignment with GraphDB patterns | Medium |
| Unified search+facets in single PF | Matches GraphDB UX, reduces query complexity | High |
| Return facets as blank nodes | Structural alignment with GraphDB | Medium |

### 4.3 Technical Debt

| Item | Description |
|------|-------------|
| Duplicate list parsing logic | Assembler lines 203-225 should use Jena utilities |
| No query caching for facets | `text:query` supports caching; facet functions do not |
| Hard dependency on TextIndexLucene | Interface-based design would allow alternative backends |

---

## 5. Recommendations

### 5.1 Short-term (Before Release)

1. **Rename or document** `text:queryWithFacets` - Either rename to `text:queryScored` or add clear documentation that facets require GROUP BY
2. **Add filtered facets to SPARQL** - Extend `text:facetCounts` syntax:
   ```sparql
   (?field ?value ?count) text:facetCounts ("search query" "category" 10)
   ```
   (Note: The Javadoc suggests this was intended but not implemented)

### 5.2 Medium-term (Post-Release)

1. **Support comma-delimited field syntax** in configuration for GraphDB alignment
2. **Add unified property function** that returns both results and facets:
   ```sparql
   (?doc ?score ?field ?value ?count) text:search ("query" "category" 10)
   ```
3. **Implement facet caching** similar to query caching in `text:query`

### 5.3 Long-term

1. Consider per-field facet configuration (`text:facet true` on field definitions)
2. Hierarchical facets for taxonomy use cases
3. Range facets for numeric/date fields

---

## 6. Conclusion

The implementation is functional and follows most Jena conventions. However, the split between `text:facetCounts` (open facets only) and `text:queryWithFacets` (no facets, just scoring) creates a confusing API surface. The lack of SPARQL-accessible filtered facets is a significant gap compared to GraphDB. The naming of `text:queryWithFacets` is actively misleading.

**Recommendation:** Address the filtered facets gap and naming issue before considering the implementation complete. The current state requires users to understand non-obvious workarounds (Java API or GROUP BY) for common faceting use cases.

---

**Assessment Status:** Review Complete
**Recommended Actions:** 2 critical, 3 medium-term, 2 long-term

---
title: "Identifier prefix search and typeahead use case"
date: "2026-03-30"
---

## Purpose

This note records the intended use case for the `feature/per-field-query-analyzer` branch.

The branch exists to support identifier-heavy search workflows where users often know the beginning of an identifier, but not the full value yet. The immediate goal is prefix search and typeahead over identifiers such as:

- `BH123456`
- `RPT-BH-1985-001`
- `GSWA-12345-A`

Typical interaction:

1. A user starts typing `BH12`
2. The system returns matching identifiers such as `BH123456`, `BH123789`, `BH120001`
3. The UI shows those matches in a dropdown
4. The user selects one, or continues typing until the desired identifier is unique

## Decision

For now, autocomplete should be implemented using normal indexed search over an identifier field, not a dedicated suggestions API.

Specifically:

- use edge n-gram indexing for the identifier field
- use a keyword-style query analyzer for that same field at query time
- query the identifier field directly via `luc:query`
- return stored canonical identifier values to drive the UI dropdown

We are explicitly deferring a dedicated `luc:suggest` API.

That is sensible for the current scope. We are not trying to solve fuzzy completion, typo tolerance, weighted suggestions, or cross-field suggestion ranking yet. We only need prefix matching over stable identifiers.

## External API Contract

External field references are always IRIs.

This rule applies to:

- `luc:query` field specs, except the special `"default"` shorthand
- `luc:facet` field specs
- `facetFields` arrays
- CQL filter `property` references
- sort specifications
- returned `?field` bindings

Internal Lucene field names from `idx:fieldName` remain implementation details.

The only plain strings that should still appear in the public SPARQL syntax are:

- the Lucene query string itself
- literal field values
- the special `"default"` fieldSpec shorthand

## Intended Configuration Pattern

Identifier prefix search should use a field like this:

```turtle
PREFIX field: <urn:jena:lucene:field#>
PREFIX text:  <http://jena.apache.org/text#>
PREFIX idx:   <urn:jena:lucene:index#>

field:identifier
    idx:fieldName "identifier" ;
    idx:fieldType idx:TextField ;
    idx:stored true ;
    idx:analyzer [ a text:EdgeNGramAnalyzer ] ;
    idx:queryAnalyzer [ a text:LowerCaseKeywordAnalyzer ] ;
    sh:path ex:identifier .
```

Semantics:

- indexing analyzer tokenises the full identifier as a single term, lowercases it, and emits prefix n-grams
- query analyzer lowercases the user input but does not n-gram it
- `BH12` therefore matches indexed prefixes of `BH123456`

## Intended Query Pattern

Example prefix query:

```sparql
PREFIX luc: <urn:jena:lucene:index#>

SELECT ?s ?score WHERE {
    (?s ?score) luc:query ("urn:jena:lucene:field#identifier" "BH12") .
}
```

This should behave as prefix search over stored identifiers, not exact full-string match only.

## What This Branch Should Support

At minimum:

- per-field query analyzer override via `idx:queryAnalyzer`
- identifier field indexed with edge n-grams
- exact identifier values still stored and returned
- prefix lookups such as `BH12` returning documents whose identifier begins with that prefix
- demo UI support for identifier-specific search and dropdown suggestions

## Current State

Core implementation on this branch already adds:

- per-field query analyzer support
- `EdgeNGramAnalyzer`
- tests showing identifier prefix matching behavior

The demo/config layer must also be wired consistently, otherwise the branch feature exists in core but is not visible in the running demo.

## Remaining Gaps To Check During Implementation

- confirm the demo identifier field uses `idx:analyzer [ a text:EdgeNGramAnalyzer ]`
- confirm the demo identifier field uses `idx:queryAnalyzer [ a text:LowerCaseKeywordAnalyzer ]`
- confirm the UI identifier dropdown queries the identifier field, not the default field
- confirm displayed suggestions come from canonical stored identifier values
- confirm docs consistently show field IRIs in all external examples

## Non-Goals For This Branch

This branch is not yet trying to implement:

- fuzzy suggestions
- typo correction
- weighted popularity ranking
- separate Lucene suggester infrastructure
- a dedicated `luc:suggest` SPARQL API

Those can be added later if prefix search over identifiers proves insufficient.

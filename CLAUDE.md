# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Apache Jena — a Java framework for semantic web and linked data applications. This fork adds **SHACL-based entity-per-document indexing with faceted search** to the `jena-text` module using Lucene, alongside the existing triple-per-document model.

**Version**: 6.1.0-SNAPSHOT | **Java**: 21+ | **Build**: Maven 3.9+

## Repository

This is a **fork** of `apache/jena`. The upstream repo is a read-only reference.

- **Fork (ours):** `aiworkerjohns/jena` — all issues, PRs, and pushes go here
- **Upstream:** `apache/jena` — do NOT create issues, PRs, or push to this repo

The `gh` CLI default is set to `aiworkerjohns/jena`. Always use `-R aiworkerjohns/jena` if there is any ambiguity. Never use `-R apache/jena` for write operations.

**Docker image pushes to GHCR**: The `gh` CLI must have `aiworkerjohns` as the active account (not `hjohns`) with the `write:packages` scope. Before pushing, verify with `gh auth status` and switch if needed: `gh auth switch --user aiworkerjohns`.

## Build Commands

```bash
# Development build (fast, skips license checks and javadoc)
mvn clean install -Pdev

# Full build with all modules
mvn clean install

# Fastest possible build (skip tests and javadoc)
mvn -DskipTests -Dmaven.javadoc.skip=true clean install

# Build specific module and its dependencies
mvn -pl :jena-text -am install

# Skip license header checks during development
mvn clean install -Drat.skip
```

## Running Tests

```bash
# Run all jena-text tests (366 tests)
mvn test -pl jena-text

# Run a single test class
mvn test -pl jena-text -Dtest=TestNativeFacetCounts

# Run a single test method
mvn test -pl jena-text -Dtest=TestNativeFacetCounts#testBasicFacetCounts

# Run only SHACL/faceting tests
mvn test -pl jena-text -Dtest="TestShaclIndexMapping,TestShaclDocumentBuilding,TestShaclTextDocProducer,TestShaclAssembler,TestShaclEntityPerDocument,TestNativeFacetCounts,TestTextFacetPF,TestTextQueryPFFilters,TestSearchExecution"
```

**Important**: Surefire only discovers `**/TS_*.java` suite files. New test classes must be added to `TS_Text.java` or they won't run in CI.

### Fuseki UI (JavaScript)

```bash
cd jena-fuseki2/jena-fuseki-ui
yarn install
yarn dev          # Vite dev server
yarn test:unit    # Vitest
yarn test:e2e     # Cypress
yarn lint         # ESLint with --fix
```

## Architecture

### Module Hierarchy

25+ Maven modules in two build profiles:

- **`-Pdev`** — Core modules only (jena-base, jena-core, jena-arq, jena-tdb2, jena-text, jena-fuseki2, etc.). Fast for local dev.
- **`-Pcomplete`** (default) — Everything including distribution, examples, benchmarks, geosparql.

Key dependency chain: `jena-base` → `jena-core` → `jena-arq` → `jena-tdb2` → `jena-text` → `jena-fuseki2`

### Dual Indexing in jena-text

**Classic mode** (upstream): Triple-per-document. Config via `text:entityMap`. SPARQL via `text:query`. No faceting.

**SHACL mode** (new): Entity-per-document. Config via `text:shapes` (SHACL). SPARQL via `luc:query` + `luc:facet`. Supports typed fields (TEXT, KEYWORD, INT, LONG, DOUBLE), range queries, and faceted navigation.

All new code is additive — upstream code paths are unmodified.

**Backward compatibility policy**: The classic `text:query` mode (upstream) must remain untouched. Within SHACL mode (`luc:query` / `luc:facet`), **no backward compatibility is required**. Breaking changes are expected as the query syntax and implementation are refined. Do not maintain multiple syntaxes or support previous commit-era formats — only the target model matters. Once stable, backward compatibility will be considered for release.

### Key SHACL Mode Classes (jena-text)

| Class | Role |
|-------|------|
| `ShaclIndexMapping` | Parsed data model: `IndexProfile` (shape), `FieldDef` (field), `FieldType` enum |
| `ShaclTextDocProducer` | Change listener — rebuilds entity Lucene docs on triple add/delete |
| `ShaclTextQueryPF` | `luc:query` property function with JSON filter support and `?totalHits` binding |
| `TextFacetPF` | `luc:facet` property function — returns (field, value, count) bindings |
| `SearchExecution` | Shared state between `luc:query` and `luc:facet` in same SPARQL query via `ExecutionContext` |
| `ShaclIndexAssembler` | Parses `text:shapes` RDF config into `ShaclIndexMapping` |
| `TextIndexLucene` | Extended with SHACL faceting methods (core methods unchanged) |

### Shared Execution Pattern

When `luc:query` and `luc:facet` appear in the same SPARQL query, both build a normalised key from query params. `SearchExecution.getOrCreate()` stores/retrieves shared state in `ExecutionContext`, avoiding redundant Lucene searches. Key normalisation sorts property URIs and filter values for deterministic matching.

### Change Listener Flow

`DatasetGraphTextMonitor.add()` → `super.add()` (base dataset updated first) → `ShaclTextDocProducer.change()` → if relevant predicate or `rdf:type`, calls `rebuildEntityDocuments()` which reads all entity triples from base dataset and replaces the Lucene document.

## Git Commits

- Do NOT add `Co-Authored-By` lines to commit messages
- Do NOT add "Generated with Claude Code" or similar attribution lines to PR descriptions

## Code Style

- K&R "Egyptian brackets" braces
- **4 spaces** for Java, **2 spaces** for XML (no tabs)
- One statement per line
- Use `@Override`, proper generic types, no `@author` tags
- No compiler warnings (use `@SuppressWarnings` as needed)
- Don't mix reformatting with functional changes
- All source files require Apache License 2.0 header (enforced by RAT plugin, skip with `-Drat.skip`)

## Documentation

Fork-specific documentation lives in `/docs/`:
- `01-user-guide.md` — Configuration and usage
- `02-sparql-api.md` — SPARQL query API
- `03-configuration.md` — Assembler configuration
- `04-architecture.md` — Internal design
- `05-testing.md` — Test coverage overview

## Running Fuseki Server

```bash
mvn clean install -pl jena-fuseki2/jena-fuseki-server -am -DskipTests
java -jar jena-fuseki2/jena-fuseki-server/target/jena-fuseki-server-*.jar --config config.ttl
```

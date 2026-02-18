# Jena Text Index: Faceting & Entity-Per-Document

This documentation covers the faceted search and entity-per-document indexing features added to Apache Jena's `jena-text` module.

## Documents

| Document | Audience | Description |
|----------|----------|-------------|
| [User Guide](01-user-guide.md) | Users / Integrators | How to configure and query with facets |
| [SPARQL API Reference](02-sparql-api.md) | Users / Developers | Complete `text:query` and `text:facet` syntax |
| [Configuration Reference](03-configuration.md) | Admins / Integrators | Assembler TTL config for both indexing modes |
| [Architecture](04-architecture.md) | Developers | Internal design, document models, shared execution |
| [Testing](05-testing.md) | Developers / QA | Test coverage, how to run tests |
| [Design Decisions](06-design-decisions.md) | Developers / Reviewers | Why things are the way they are |
| [Known Limitations & Future Work](07-future-work.md) | All | What's deferred, what needs attention |

## Quick Start

```turtle
# Assembler config — classic triple-per-document with faceting
text:facetFields ("category" "author") ;

# OR — SHACL entity-per-document (all fields on one Lucene doc)
text:shapes ( ex:BookShape ) ;
```

```sparql
# Search with facets
(?s ?sc) text:query ("machine learning") .
(?f ?v ?c) text:facet ("machine learning" '["category"]' 10) .
```

## Build & Test

```bash
mvn test -pl jena-text                    # 362 tests
mvn clean install -pl jena-fuseki2/jena-fuseki-server -am -DskipTests  # build Fuseki
```

## Archive

Previous working documents, design reviews, and phase summaries are in [archive/](archive/).

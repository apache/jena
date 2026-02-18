# Jena Text Index: Faceting & Entity-Per-Document

This documentation covers the faceted search and entity-per-document indexing features added to Apache Jena's `jena-text` module.

## Documents

| Document | Audience | Description |
|----------|----------|-------------|
| [User Guide](01-user-guide.md) | Users / Integrators | Configure, query, deploy with Fuseki, troubleshoot |
| [SPARQL API Reference](02-sparql-api.md) | Users / Developers | `text:query`, `luc:query`, `luc:facet` syntax, Lucene query syntax, Java API |
| [Configuration Reference](03-configuration.md) | Admins / Integrators | Assembler TTL config for both indexing modes |
| [Architecture](04-architecture.md) | Developers | Internal design, document models, shared execution |
| [Testing](05-testing.md) | Developers / QA | Test coverage, how to run tests |
| [Design Decisions](06-design-decisions.md) | Developers / Reviewers | Why things are the way they are |
| [Known Limitations & Future Work](07-future-work.md) | All | What's deferred, what needs attention |

## Quick Start

```turtle
# Classic mode — upstream Jena text search (text:query)
text:entityMap <#entMap> ;

# SHACL mode — entity-per-document with faceting (luc:query, luc:facet)
text:shapes ( <#BookShape> ) ;
```

```sparql
# Classic: text search
PREFIX text: <http://jena.apache.org/text#>
(?s ?sc) text:query ("machine learning") .

# SHACL: search with filters + facets
PREFIX luc: <urn:jena:lucene:index#>
(?s ?sc) luc:query ("machine learning") .
(?f ?v ?c) luc:facet ("machine learning" '["category"]' 10) .
```

## Build & Test

```bash
mvn test -pl jena-text                    # 366 tests
mvn clean install -pl jena-fuseki2/jena-fuseki-server -am -DskipTests  # build Fuseki
```

## Archive

Previous working documents, design reviews, and phase summaries are in [archive/](archive/).

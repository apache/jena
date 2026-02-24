# Demo: SHACL Entity-per-Document Indexing

This demo shows the SHACL-based full-text indexing mode for Apache Jena Fuseki,
using an Australian mining domain with reports, boreholes, sites, and authors.

## Prerequisites

- Java 21+
- [go-task](https://taskfile.dev/) (optional, for `task` commands)
- Maven 3.9+ (for building)

## Quick start

```bash
# 1. Build the Fuseki server JAR (from repo root)
task build
# or: cd .. && mvn clean install -Pdev -DskipTests && cd demo

# 2. Start the server
task serve
# or: java -jar ../jena-fuseki2/jena-fuseki-server/target/jena-fuseki-server-*.jar --config config.ttl

# 3. Load the demo data (in a separate terminal)
task load
# or: curl -X PUT "http://localhost:3030/mining/data?default" -H "Content-Type: text/turtle" --data-binary @data/mining.ttl

# 4. Run all queries
task query
# or run individually: task query-one -- queries/01-basic-search.rq

# 5. Stop the server
task stop
```

## Data model

The demo data (`data/mining.ttl`) contains:

- **6 Sites** — mines and operations across Australian states
- **7 Boreholes** — drill holes linked to sites
- **8 Mining Reports** — geological and production reports
- **4 Authors** — people who authored the reports

### Relationships

```
MiningReport --ex:authoredBy--> Author --ex:name--> "name string"
Author --ex:authored--> MiningReport
```

Reports link to authors via `ex:authoredBy`. Authors link back via `ex:authored`.
Author names (`ex:name`) are stored on the Author entity, not on the report.

## Index configuration

`config.ttl` defines three SHACL shapes that control what gets indexed:

| Shape | Entity type | Fields |
|-------|------------|--------|
| MiningReportShape | `ex:MiningReport` | title, description, commodity, state, operator, status, year, **authorName**, **authoredByUri** |
| BoreholeShape | `ex:Borehole` | title, commodity, state, depth |
| SiteShape | `ex:Site` | title, commodity, state, status |

### Path types demonstrated

**Direct paths** (most fields): `sh:path rdfs:label` indexes the value directly from the entity.

**Sequence path** (`authorName`):
```turtle
sh:path ( ex:authoredBy ex:name )
```
Traverses from the report to its author, then reads the author's name. The author name
is indexed on the report document even though it's stored on a different entity.

**Inverse path** (`authoredByUri`):
```turtle
sh:path [ sh:inversePath ex:authored ]
```
Finds authors who link to this report via `ex:authored` and indexes their URIs.
This is the reverse direction — instead of following a link from the report,
it finds entities that link *to* the report.

## Queries

| File | What it tests |
|------|--------------|
| `01-basic-search.rq` | Full-text search for "copper" |
| `02-filtered-search.rq` | Search with JSON filter (state=QLD) |
| `03-facet-counts.rq` | Facet counts across commodity, state, operator |
| `04-facet-filtered.rq` | Drill-down: facets for "gold" in WA only |
| `05-combined.rq` | Search + facets in one query (UNION pattern) |
| `06-sequence-path-facet.rq` | Facet counts on `authorName` (sequence path field) |
| `07-filter-by-author.rq` | Filter results by `authorName` = "Dr Sarah Jones" |

### Expected results for path queries

**Query 06** should return author facets showing each author wrote 2 reports:
```
authorName  "Dr Priya Patel"   2
authorName  "Dr Sarah Jones"   2
authorName  "James Williams"   2
authorName  "Prof Wei Chen"    2
```

**Query 07** should return exactly 2 reports by Dr Sarah Jones:
```
report-mia-2023  "Mount Isa Copper Resource Estimation 2023"
report-od-2024   "Olympic Dam Expansion Feasibility Study"
```

## Synthetic data generation

Generate larger datasets for performance testing:

```bash
task generate -- --count 1000
# or: python3 generate.py --count 1000
```

## Cleanup

```bash
task clean
# or: rm -rf DB Lucene
```

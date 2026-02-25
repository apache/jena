# Demo: SHACL Entity-per-Document Indexing

This demo shows the SHACL-based full-text indexing mode for Apache Jena Fuseki,
using an Australian mining domain with reports, boreholes, sites, and authors.

## Prerequisites

- Java 21+
- Maven 3.9+ (for building)
- [go-task](https://taskfile.dev/) (for `task` commands)

For Docker workflows:
- Docker Desktop
- **GitHub CR**: `gh` CLI authenticated with `write:packages` scope
- **Azure CR**: `az` CLI authenticated

## Quick start

```bash
# 1. Build the Fuseki server JAR (from repo root)
task build

# 2. Start the server
task serve

# 3. Load the demo data (in a separate terminal)
task load

# 4. Run all queries
task query

# 5. Stop the server
task stop
```

## Quick start (Docker)

```bash
# Start the server using the pre-built image from GitHub CR
docker compose up

# Load data and run queries (in a separate terminal)
task load
task query

# Stop
docker compose down      # keep data
docker compose down -v   # wipe data volumes
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

## Docker image

### Building

Build the Docker image locally:

```bash
task image-build
```

This produces `fuseki-ai:6.1.0-SNAPSHOT` by default. The image is based on `eclipse-temurin:21-jre-alpine` and includes the server config and demo mining dataset.

Override the image name or tag:

```bash
task image-build IMAGE_NAME=myapp IMAGE_TAG=latest
```

### Pushing to GitHub Container Registry

```bash
task ghcr-push
```

Pushes to `ghcr.io/aiworkerjohns/fuseki-ai:6.1.0-SNAPSHOT`. Override the owner:

```bash
task ghcr-push GHCR_OWNER=other-org
```

New packages default to private. Set visibility to public via GitHub > Package Settings > Change visibility.

The `gh` CLI must have the `write:packages` scope. Add it with:

```bash
gh auth refresh -s write:packages
```

### Pushing to Azure Container Registry

```bash
task image-push ACR_NAME=myregistry
```

Pushes to `myregistry.azurecr.io/fuseki-ai:6.1.0-SNAPSHOT`. The task checks for an active Azure session and runs `az login` if needed, then authenticates with the ACR before pushing.

## Demo app (FastAPI + Bulma)

A lightweight web UI for interactive faceted search. Built with FastAPI, Jinja2 templates, and Bulma CSS. Provides a search box, sidebar facet checkboxes with counts, and result cards with clickable facet badges.

The app dynamically reads `config.ttl` to discover shapes, fields, and facetability — no hardcoded field names.

```bash
# Install dependencies (once)
task app-setup

# Start the app (Fuseki must be running)
task app
```

Opens at `http://localhost:8000`. The app queries the Fuseki endpoint at `localhost:3030/mining` — start the server first with `task serve` or `docker compose up`, then load data with `task load`.

Configure via environment variables:
- `FUSEKI_ENDPOINT` — SPARQL endpoint URL (default: `http://localhost:3030/mining/query`)
- `FUSEKI_CONFIG` — path to assembler config (default: `../config.ttl`)

## Synthetic data generation

Generate larger datasets for performance testing:

```bash
task generate -- --count 1000
```

## Cleanup

```bash
task clean
```

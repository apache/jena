# Demo: SHACL Entity-per-Document Indexing

This demo shows the SHACL-based full-text indexing mode for Apache Jena Fuseki,
using an Australian mining domain with reports, boreholes, sites, and authors.

## Prerequisites

- Java 21+
- Maven 3.9+ (for building)
- [go-task](https://taskfile.dev/) (for `task` commands)

For Docker workflows:
- Docker Desktop
- **GitHub CR**: `gh` CLI authenticated with `write:packages` scope (only for `task ghcr-push`)
- **Azure CR**: `az` CLI authenticated (only for `task image-push`)

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
# Build the image with a multi-stage Docker build and start Fuseki locally
task docker-start

# Load data and run queries (in a separate terminal)
task load
task query

# Stop
task docker-stop         # keep data
task docker-clean        # wipe data volumes
```

To run the same `docker-compose` service using an image published to GitHub Container Registry instead of building locally:

```bash
# Start Fuseki from GHCR
task docker-start-ghcr

# Stop
task docker-stop
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

## Server image

### Building

Build the server Docker image locally:

```bash
task image-build
```

This produces `fuseki-ai:6.1.0-SNAPSHOT` by default. The image is built with a multi-stage Docker build, compiling the Fuseki server jar inside Docker rather than requiring a host-built jar. The runtime image is based on `eclipse-temurin:21-jre-alpine` and includes the server config and demo mining dataset.

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

To run the published GHCR image with the demo `docker-compose.yml`:

```bash
task docker-start-ghcr
```

### Pushing to Azure Container Registry

```bash
task image-push ACR_NAME=myregistry
```

Pushes to `myregistry.azurecr.io/fuseki-ai:6.1.0-SNAPSHOT`. The task checks for an active Azure session and runs `az login` if needed, then authenticates with the ACR before pushing.

## Loader / reindexer image

A separate Docker image for offline bulk data operations: loading N-Quads/Turtle/N-Triples into TDB2 and building the SHACL Lucene index. Useful for large datasets where GSP loading is too slow (e.g. the drillhole dataset — 17.6M entities, 184M triples).

The image runs two steps sequentially:
1. `tdb2.tdbloader` — bulk loads data files into TDB2
2. `shacltextindexer` — builds the SHACL Lucene index from the TDB2 store

### Building

```bash
task loader-build
```

Produces `fuseki-loader:6.1.0-SNAPSHOT`. Requires the Fuseki JAR to be built first (`task build`).

### Running

```bash
docker run --rm \
  -v /path/to/config.ttl:/config/config.ttl:ro \
  -v /path/to/data:/input:ro \
  -v fuseki-db:/data/DB \
  -v fuseki-lucene:/data/Lucene \
  -e JAVA_OPTS="-Xmx8g" \
  fuseki-loader:6.1.0-SNAPSHOT
```

| Volume mount | Purpose |
|---|---|
| `/config/config.ttl` | Assembler config (must reference `/data/DB` for TDB2 and `/data/Lucene` for the index) |
| `/input` | Directory containing `.nq`, `.ttl`, or `.nt` data files |
| `/data/DB` | TDB2 database output |
| `/data/Lucene` | Lucene index output |

| Environment variable | Default | Purpose |
|---|---|---|
| `MODE` | `all` | `all` = load + reindex, `load` = TDB2 load only, `index` = SHACL reindex only |
| `CONFIG` | `/config/config.ttl` | Path to assembler config inside the container |
| `DB_DIR` | `/data/DB` | TDB2 output directory |
| `INPUT_DIR` | `/input` | Data files directory |
| `JAVA_OPTS` | (none) | JVM flags, e.g. `-Xmx8g` for large datasets |

### Using with the server image

The loader and server images share volumes, so you can bulk-load data offline then start the server:

```bash
# 1. Load data
docker run --rm \
  -v ./config.ttl:/config/config.ttl:ro \
  -v ./data:/input:ro \
  -v fuseki-db:/data/DB \
  -v fuseki-lucene:/data/Lucene \
  -e JAVA_OPTS="-Xmx8g" \
  ghcr.io/aiworkerjohns/fuseki-loader:6.1.0-SNAPSHOT

# 2. Start the server with the same volumes
docker run -d -p 3030:3030 \
  -v ./config.ttl:/fuseki/config.ttl:ro \
  -v fuseki-db:/fuseki/DB \
  -v fuseki-lucene:/fuseki/Lucene \
  ghcr.io/aiworkerjohns/fuseki-ai:6.1.0-SNAPSHOT
```

A `docker-compose.yml` example is provided in `loader/`.

### Pushing

```bash
# GitHub Container Registry
task loader-ghcr-push

# Azure Container Registry
task loader-acr-push ACR_NAME=gswadevacr
```

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

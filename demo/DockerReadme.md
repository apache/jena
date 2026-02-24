# Docker Build and Deployment

This document covers building, pushing, and running the Fuseki AI demo as a Docker container.

## Prerequisites

- Docker Desktop running locally
- [Task](https://taskfile.dev) installed
- The Fuseki server JAR built (`task build` from the demo directory)

For pushing images you also need one of:
- **GitHub CR**: `gh` CLI authenticated with `write:packages` scope
- **Azure CR**: `az` CLI authenticated

## Building the Image

Build the Docker image locally:

```bash
task image-build
```

This produces `fuseki-ai:6.1.0-SNAPSHOT` by default. Override the image name or tag:

```bash
task image-build IMAGE_NAME=myapp IMAGE_TAG=latest
```

The build copies the Fuseki server JAR into the build context, runs `docker build`, then cleans up the JAR copy. The image is based on `eclipse-temurin:21-jre-alpine` and includes the server config and demo mining dataset.

## Pushing to a Registry

### GitHub Container Registry (public)

```bash
task ghcr-push
```

Pushes to `ghcr.io/aiworkerjohns/fuseki-ai:6.1.0-SNAPSHOT`. Override the owner:

```bash
task ghcr-push GHCR_OWNER=other-org
```

Note: new packages default to private. Set visibility to public via GitHub > Package Settings > Change visibility.

The `gh` CLI must have the `write:packages` scope. Add it with:

```bash
gh auth refresh -s write:packages
```

### Azure Container Registry (private)

```bash
task image-push ACR_NAME=myregistry
```

Pushes to `myregistry.azurecr.io/fuseki-ai:6.1.0-SNAPSHOT`. The task checks for an active Azure session and runs `az login` if needed, then authenticates with the ACR before pushing.

## Running with Docker Compose

Start the server:

```bash
docker compose up
```

This pulls the image from GitHub CR and starts Fuseki on port 3030. The DB and Lucene index are persisted as named Docker volumes so data survives restarts.

Stop the server:

```bash
docker compose down      # stop, keep data
docker compose down -v   # stop and wipe data volumes
```

## Loading Data and Running Queries

With the container running, use the Taskfile to load the demo dataset and run queries. These tasks target `localhost:3030` by default.

Load the mining dataset:

```bash
task load
```

Run all demo queries:

```bash
task query
```

Run a single query:

```bash
task query-one -- queries/01-basic-search.rq
```

The demo queries cover:

| Query | Description |
|-------|-------------|
| `01-basic-search.rq` | Full-text search for "copper" |
| `02-filtered-search.rq` | Search "copper" filtered to Queensland |
| `03-facet-counts.rq` | Facet distribution across commodity, state, operator |
| `04-facet-filtered.rq` | Drill-down facets for "gold" in Western Australia |
| `05-combined.rq` | Combined search results and facets in one query |

# Apache Jena RDF Delta

A system for synchronizing RDF Datasets across multiple Jena instances to enable horizontal scaling and high availability.

## Overview

RDF Delta provides:

* High-availability for Apache Jena Fuseki
* Dataset replication and synchronization
* Change tracking for RDF datasets
* Incremental backup
* Transactional change feed

## Design

RDF Delta consists of these key components:

1. **RDF Patch**: A format for recording changes to RDF datasets
2. **Patch Log**: A sequence of patches for tracking dataset changes
3. **Patch Log Server**: Coordination service for distributing dataset changes
4. **TDB2 Change Tracker**: Integration with TDB2 transaction mechanism
5. **Fuseki HA Support**: High-availability extensions for Fuseki

## Architecture

![Architecture Diagram](./docs/architecture.svg)

The system allows multiple Fuseki+TDB2 instances to stay synchronized by:

1. Capturing changes made to any dataset instance
2. Broadcasting those changes through a central patch log
3. Applying changes to all other dataset instances
4. Providing failover and load balancing capabilities

## Usage Patterns

### High-Availability Cluster

Deploy multiple Fuseki servers with TDB2 behind a load balancer, all connected to a central patch log server for automatic synchronization.

### Incremental Backup

Use the patch log to capture all changes to a dataset, enabling point-in-time recovery and efficient incremental backups.

### Change Feed

Subscribe to dataset changes to trigger external systems when data is modified (e.g., for search index updates, notifications, or data pipeline triggers).

## TDB2 Integration

RDF Delta includes a direct integration with Apache Jena TDB2:

```java
// Create a TDB2 dataset with change tracking
DatasetGraph dsg = TDB2DeltaConnection.connect(tdb2Dataset, deltaLink, "datasetId");
```

The integration:
- Automatically tracks all changes made to TDB2 datasets
- Sends changes to a central patch log server
- Enables multiple TDB2 instances to stay synchronized
- Works with both programmatic API and Assembler configuration

See [TDB2 Integration Documentation](./docs/tdb2-delta-integration.md) for detailed usage instructions.

## Patch Server

RDF Delta includes a robust, scalable patch log server:

```bash
# Start a standalone server
delta-server server --store /path/to/store --port 1066

# Start a distributed server with ZooKeeper
delta-server server --store /path/to/store --zk localhost:2181 --port 1066

# Start with advanced conflict detection and resolution
delta-server server --store /path/to/store --conflict-detection --conflict-strategy merge
```

The patch server:
- Provides an HTTP API for accessing and managing patch logs
- Supports standalone and distributed modes
- Uses ZooKeeper for coordination in distributed mode
- Features advanced conflict detection and resolution
- Includes monitoring via JMX and Prometheus
- Comes with CLI tools for administration

See [Patch Server Documentation](./docs/patch-server.md) and [Conflict Resolution](./docs/conflict-resolution.md) for detailed usage instructions.

## Fuseki High-Availability Integration

RDF Delta provides direct integration with Apache Jena Fuseki for creating high-availability deployments:

```ttl
# In Fuseki configuration file
<#delta_dataset> rdf:type delta:ReplicatedDataset ;
    delta:dataset <#tdb_dataset> ;
    delta:server "http://delta-server:1066/" ;
    delta:datasetName "example" ;
    delta:zone "fuseki-1" ;
    .
```

The Fuseki integration:
- Enables synchronization between multiple Fuseki servers
- Provides transparent failover for high availability
- Allows horizontal scaling of SPARQL query capacity
- Works with standard Fuseki configuration mechanisms
- Includes monitoring for replication status

See [Fuseki HA Integration](./docs/fuseki-ha-integration.md) for detailed usage instructions.

## Docker Deployment

RDF Delta components can be deployed using Docker and Docker Compose:

```bash
# Start a complete environment with ZooKeeper, Delta server, and multiple Fuseki servers
docker-compose -f docker-compose-fuseki-ha.yml up -d
```

The Docker setup:
- Runs multiple redundant patch servers for high availability
- Uses ZooKeeper for cluster coordination
- Includes HAProxy for load balancing
- Deploys multiple synchronized Fuseki servers

## Getting Started

See [Documentation](./docs/delta.md) for general usage instructions.
# RDF Delta User Guide

RDF Delta is a system for keeping copies of an RDF Dataset synchronized. It provides high availability for Jena Fuseki and supports distributed deployment of RDF systems.

## Main Components

### RDF Patch

An RDF Patch is a format for recording changes to an RDF Dataset. It consists of a header section and a data section:

```
H id <uuid:c25475bf-b202-4b8d-b667-0fa29cd12fcd>
H prev <uuid:2b46e217-b884-4328-8269-75b880c22ade>
TX
A <http://example/s> <http://example/p> <http://example/o> .
D <http://example/s1> <http://example/p1> "foo" .
TC .
```

Each patch is uniquely identified and linked to its predecessor through the `id` and `prev` headers, creating a chain of patches.

### Patch Log

A patch log is a sequence of patches in the order that changes occurred. A patch log server maintains multiple logs for different datasets and provides HTTP endpoints for publishing and consuming changes.

## High Availability Architecture

The high-availability architecture consists of:

1. **Patch Log Server**: 
   - Manages patch logs 
   - Ensures consistent ordering of changes
   - May use ZooKeeper for coordination in clustered deployments

2. **RDF Delta Client**:
   - Integrated with TDB2 storage
   - Captures changes from local operations
   - Retrieves and applies changes from remote sources

3. **Fuseki Server(s)**:
   - Each maintains a local TDB2 database
   - Synchronizes using the RDF Delta Client
   - Can be deployed across multiple hosts

## Usage

### Configuration

#### Patch Log Server Configuration

```
# Configuration file for the patch log server
port = 1066
zone = log
uribase = http://localhost:1066/

# File storage
provider = file
location = LOGS
backups = BACKUPS
```

#### Fuseki Server Configuration

```ttl
@prefix :        <#> .
@prefix fuseki:  <http://jena.apache.org/fuseki#> .
@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
@prefix tdb2:    <http://jena.apache.org/2016/tdb#> .
@prefix delta:   <http://jena.apache.org/delta#> .

<#service> rdf:type fuseki:Service ;
    fuseki:name "ds" ;
    fuseki:serviceQuery "query" ;
    fuseki:serviceUpdate "update" ;
    fuseki:serviceReadGraphStore "get" ;
    fuseki:serviceReadWriteGraphStore "data" ;
    fuseki:dataset <#dataset> .

<#dataset> rdf:type delta:ReplicatedDataset ;
    delta:patchLogServer "http://localhost:1066/" ;
    delta:datasetName "ds" ;
    delta:storage <#tdb> .

<#tdb> rdf:type tdb2:DatasetTDB2 ;
    tdb2:location "DB" .
```

### Commands

#### Starting a Patch Log Server

```bash
$ java -jar delta-server.jar --config=server.cfg
```

#### Starting Fuseki with RDF Delta

```bash
$ fuseki-server --conf=config-ha.ttl
```

### API Usage

#### Creating a Replicated Dataset

```java
// Create a replicated dataset
DatasetGraph dataset = DeltaFuseki.connectDataset(
    "http://localhost:1066/",  // Patch log server URL
    "dataset1",                // Dataset name
    Location.create("DB"));    // Storage location
```

#### Listening for Changes

```java
// Create a change listener
PatchLogListener listener = new PatchLogListener() {
    @Override
    public void patchLog(RDFPatch patch) {
        System.out.println("Dataset changed: " + patch.getId());
    }
};

// Attach the listener to a patch log
PatchLog patchLog = client.getPatchLog("dataset1");
patchLog.register(listener);
```

## Deployment Patterns

### Simple High-Availability

Two or more servers with a shared patch log server:

```
[Load Balancer] --> [Fuseki Server 1]
                    [Fuseki Server 2]
                          ↓ ↑
                 [Patch Log Server]
```

### Distributed Deployment

Geo-distributed setup with regional patch log servers:

```
[Region A]               [Region B]
[Fuseki A1]              [Fuseki B1]
[Fuseki A2] ←→ [ZK] ←→   [Fuseki B2]
[PLS A]                  [PLS B]
```

## Advanced Topics

### Security

Secure your deployment by:
- Using HTTPS for all communications
- Configuring authentication for the patch log server
- Setting up access controls for Fuseki

### Monitoring

Monitor your RDF Delta deployment using:
- JMX metrics exposed by the patch log server
- Micrometer integration for metrics collection
- Health check endpoints

### Backup and Recovery

For disaster recovery:
- Configure regular backups of patch logs
- Implement periodic full dataset snapshots
- Test recovery procedures regularly
# TDB2 Integration with RDF Delta

This document describes how to use TDB2 with RDF Delta to enable automatic change tracking and 
synchronization across multiple Jena instances.

## Overview

The TDB2 Delta integration allows you to:

1. Track changes made to a TDB2 dataset and automatically log them to a patch log server
2. Synchronize multiple TDB2 datasets using a common patch log
3. Deploy high-availability Fuseki servers with automatic replication

This integration works by:

- Adding a transaction listener to TDB2 that tracks all dataset modifications
- Converting those modifications to RDF patches
- Sending the patches to a centralized patch log server
- Providing a mechanism for other datasets to synchronize with the patch log

## Usage Options

There are two ways to use TDB2 with RDF Delta:

### 1. Programmatic API

You can connect a TDB2 dataset to a Delta patch log server using the `TDB2DeltaConnection` class:

```java
// Create or connect to a TDB2 dataset
DatasetGraph dsg = TDB2Factory.connectDataset("/path/to/tdb2-data");

// Connect to the Delta server
DeltaLink deltaLink = DeltaLinkHTTP.connect("http://localhost:1066/");

// Connect the dataset to the Delta server
String datasetId = "myDataset";
DatasetGraph connectedDsg = TDB2DeltaConnection.connect(dsg, deltaLink, datasetId);

// Use the connected dataset - all changes will be tracked
// Always wrap the original dataset with the connected one
Dataset dataset = DatasetFactory.wrap(connectedDsg);
```

### 2. Assembler Configuration

You can also configure a TDB2 dataset with Delta change tracking using Jena's assembler system:

```ttl
PREFIX :        <#>
PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX tdb2:    <http://jena.apache.org/2016/tdb#>
PREFIX delta:   <http://jena.apache.org/delta#>

# The base TDB2 dataset
:tdb_dataset rdf:type tdb2:DatasetTDB2 ;
    tdb2:location "/path/to/tdb2-data" ;
    .

# The Delta tracked TDB2 dataset
:delta_dataset rdf:type delta:TDB2Dataset ;
    delta:dataset :tdb_dataset ;
    delta:server "http://localhost:1066/" ;
    delta:name "myDataset" ;
    .
```

To load this configuration:

```java
// Read the configuration file
Model configModel = RDFDataMgr.loadModel("config.ttl");

// Get the resource for the dataset
Resource datasetRes = configModel.getResource("urn:example:delta_dataset");

// Create the dataset using the assembler
Dataset dataset = (Dataset)Assembler.general.open(datasetRes);
```

## High-Availability Architecture

You can use this integration to build a high-availability system with multiple Fuseki servers
sharing the same data:

```
                      +-------------------+
                      |                   |
                      | Delta Patch Log   |
                      | Server            |
                      |                   |
                      +-------------------+
                               ^
                               |
                 +-------------+-------------+
                 |                           |
    +------------v------------+  +-----------v------------+
    |                         |  |                        |
    | Fuseki Server 1         |  | Fuseki Server 2        |
    | with TDB2 + Delta       |  | with TDB2 + Delta      |
    |                         |  |                        |
    +-------------------------+  +------------------------+
```

The steps to set up such a system are:

1. Set up a Delta patch log server
2. Create a patch log for your dataset
3. Configure each Fuseki server to use TDB2 with Delta change tracking
4. Point all servers to the same patch log

All changes made through any server will automatically propagate to all other servers.

## Replica Synchronization

When used with the `ReplicatedDataset` class, the system can automatically pull changes from the patch log server:

```java
// Create the TDB2 dataset with Delta tracking
DatasetGraph dsg = TDB2DeltaConnection.createConnected("/path/to/tdb2-data", deltaLink, datasetId);

// Create a replicated dataset that polls for changes
ReplicatedDataset replicatedDsg = new ReplicatedDataset(dsg, deltaLink, datasetId);

// Set polling interval (in milliseconds)
replicatedDsg.setPollingInterval(1000);

// Start polling for changes
replicatedDsg.start();

// Don't forget to stop polling when done
replicatedDsg.stop();
```

## Integration with Fuseki

To use TDB2 with Delta change tracking in Fuseki, add a configuration file like the example in
`examples/tdb2-delta-config.ttl` to your Fuseki configuration directory and reference it 
from a service configuration.

See the [Fuseki configuration documentation](https://jena.apache.org/documentation/fuseki2/fuseki-configuration.html)
for more details on how to set up custom dataset configurations.

## Performance Considerations

- The change tracking adds a small overhead to TDB2 transactions
- Regular polling for changes can generate network traffic
- The patch log server should be deployed in a reliable, high-performance environment
- For high-throughput systems, consider adjusting the polling interval to balance 
  freshness vs. performance

## Monitoring

The TDB2 Delta integration includes monitoring capabilities:

- Transaction counts tracked through Micrometer metrics
- Patch sizes and counts
- Synchronization latency

These metrics can be exposed through JMX or other monitoring systems supported by Micrometer.
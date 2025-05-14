# Fuseki High-Availability Integration

RDF Delta includes direct integration with Apache Jena Fuseki to create high-availability deployments. This integration allows multiple Fuseki servers to share the same data through an RDF Delta patch log server, enabling:

- Horizontal scaling of SPARQL query capacity
- High availability with transparent failover
- Load balancing across multiple servers
- Geographic distribution of servers

## Architecture

![HA Architecture](./images/fuseki-ha-architecture.png)

In this architecture:

1. Multiple Fuseki servers operate independently, each with its own local TDB2 dataset
2. All changes to any server are captured as RDF patches and sent to the Delta patch log server
3. All other servers receive and apply the patches automatically
4. A load balancer distributes requests across the Fuseki servers
5. If one server fails, the load balancer routes traffic to the remaining servers

### Components

- **Fuseki Servers**: Standard Fuseki servers with the Delta module
- **Delta Patch Log Server**: Centralized service for sharing patches
- **Load Balancer**: Routes client requests to available Fuseki servers
- **TDB2 Datasets**: Local storage for each Fuseki server
- **ZooKeeper** (optional): Provides coordination for clustered setups

## Setup Options

You can set up a high-availability Fuseki deployment in several ways:

### 1. Configuration File

The simplest approach is to use a Fuseki configuration file that includes Delta settings:

```ttl
# Base TDB2 dataset
<#tdb_dataset> rdf:type tdb2:DatasetTDB2 ;
    tdb2:location "/fuseki/databases/DB" ;
    .

# Delta-replicated dataset
<#delta_dataset> rdf:type delta:ReplicatedDataset ;
    delta:dataset <#tdb_dataset> ;
    delta:server "http://delta-server:1066/" ;
    delta:datasetName "example" ;
    delta:zone "fuseki-1" ;
    .

# Fuseki service
<#service> rdf:type fuseki:Service ;
    fuseki:name "example" ;
    fuseki:dataset <#delta_dataset> ;
    .
```

### 2. System Properties

You can also configure the Delta integration using system properties:

```bash
FUSEKI_ARGS="--conf=config.ttl -Ddelta.fuseki.servers=http://delta-server:1066/ -Ddelta.fuseki.zone=fuseki-1"
fuseki-server $FUSEKI_ARGS
```

### 3. Programmatic API

For embedded Fuseki applications, you can use the programmatic API:

```java
// Create the base dataset
DatasetGraph baseDataset = TDB2Factory.createDatasetGraph("/path/to/db");

// Connect to Delta server
DeltaLink link = DeltaLinkHTTP.connect("http://delta-server:1066/");
DeltaClient client = DeltaClient.create(link, "fuseki-1");

// Create a replicated dataset
DeltaReplicatedDataset replicatedDataset = 
    new DeltaReplicatedDataset(client, "example", baseDataset);

// Create and start the Fuseki server
FusekiServer server = FusekiServer.create()
    .port(3030)
    .add("/example", replicatedDataset)
    .build();
server.start();
```

## Deployment Scenarios

### Basic High-Availability

Two or more Fuseki servers with a single Delta patch log server:

1. Set up a Delta patch log server
2. Deploy multiple Fuseki servers with identical configurations
3. Configure each Fuseki server to use the same Delta server and dataset name
4. Set up a load balancer (like HAProxy, NGINX, or AWS ALB) in front of the Fuseki servers

### Scaled Delta Server

For higher throughput and reliability:

1. Set up a cluster of Delta servers with ZooKeeper coordination
2. Deploy multiple Fuseki servers
3. Configure each Fuseki to connect to the Delta cluster

### Geographic Distribution

For multi-region deployments:

1. Set up a Delta patch log server in each region
2. Configure cross-region replication between Delta servers
3. Deploy Fuseki servers in each region connected to their local Delta server
4. Use geo-routing to direct clients to the nearest region

## Docker Deployment

The repository includes Docker Compose files for easy deployment:

```bash
# Start a complete HA setup with 2 Fuseki servers and a Delta server
docker-compose -f docker-compose-fuseki-ha.yml up -d
```

This will start:
- 2 Fuseki servers with Delta replication
- 1 Delta patch log server
- 1 HAProxy load balancer
- 1 ZooKeeper instance for coordination

## Operational Considerations

### Monitoring

The Fuseki Delta integration includes metrics for monitoring:

- Transaction counts and latency
- Patch synchronization status
- Replication lag between servers

These metrics are available through JMX or Prometheus.

### Backup and Recovery

For backup, you can:

1. Use the Delta backup command to create a consistent backup of the patch log
2. Use TDB2 backup commands to create backups of individual Fuseki servers

To restore:

1. Restore the patch log on the Delta server
2. Either restore TDB2 backups on each Fuseki server, or
3. Let Fuseki servers rebuild their datasets from the patch log

### Performance Tuning

For optimal performance:

- Place the Delta server in the same network as Fuseki servers to minimize latency
- Consider SSD storage for TDB2 datasets and Delta patch logs
- Adjust Delta polling intervals based on your update frequency
- Scale the number of Fuseki servers based on query load
- Consider sharding datasets for very large deployments

## Limitations

- All Fuseki servers must have sufficient storage to hold the complete dataset
- There is a small delay between a change on one server and its appearance on other servers
- For very high write throughput, you may need to scale the Delta server cluster

## Troubleshooting

### Common Issues

1. **Synchronization Failures**: Check network connectivity between Fuseki and Delta servers
2. **Version Conflicts**: May occur if two servers update the same data simultaneously
3. **Performance Degradation**: Check for replication lag or network issues

### Logs

Relevant log messages have the prefix `RDF Delta` or `DeltaReplicated` and include:

- Synchronization status
- Patch application success or failure
- Connection issues with the Delta server

## Example Configuration

See the `examples/fuseki-ha-config.ttl` file for a complete example configuration.
# RDF Delta Patch Server

The RDF Delta Patch Server is a central component of the RDF Delta architecture. It provides the following functionality:

- Stores and manages patch logs for RDF datasets
- Enables synchronization between dataset replicas
- Provides an HTTP API for accessing patches
- Supports distributed operation with automatic leader election
- Includes monitoring and metrics for operational visibility

## Installation

### Prerequisites

- Java 11 or later
- ZooKeeper 3.5+ (for distributed mode)

### Package Installation

You can run the patch server directly from the Apache Jena binary distribution:

```bash
# Extract the Jena distribution
tar -xzf apache-jena-4.x.x.tar.gz
cd apache-jena-4.x.x

# Start the server
bin/delta-server server --store /path/to/store
```

## Running the Server

The patch server can be run in two modes:

1. **Standalone Mode**: Single server with local storage
2. **Distributed Mode**: Multiple servers with coordination via ZooKeeper

### Standalone Mode

To run the server in standalone mode:

```bash
delta-server server --store /path/to/store [--port 1066]
```

Options:
- `--store PATH`: Path to the directory for storing patch logs (required)
- `--port PORT`: HTTP port to listen on (default: 1066)
- `--jmx`: Enable JMX monitoring

### Distributed Mode

For high-availability and horizontal scaling, you can run multiple patch servers in a cluster with ZooKeeper coordination:

```bash
delta-server server --store /path/to/store --zk localhost:2181 [--port 1066]
```

Additional options for distributed mode:
- `--zk CONN`: ZooKeeper connection string (required for distributed mode)

In distributed mode, each server can handle any request, but only one server is designated as the leader for each dataset. Write operations are forwarded to the leader automatically.

## Configuration

Configuration can be provided through a properties file or environment variables.

### Properties File

Create a file `delta-server-config.properties` in the server's working directory or specify it with `--config` option:

```properties
# Basic configuration
delta.server.port=1066
delta.storage.path=/var/lib/delta-server/store

# Cluster configuration
delta.cluster.enabled=true
delta.cluster.zookeeper.connect=localhost:2181
```

### Environment Variables

Configuration can also be set through environment variables:

```bash
export DELTA_SERVER_PORT=1066
export DELTA_STORAGE_PATH=/var/lib/delta-server/store
export DELTA_CLUSTER_ENABLED=true
export DELTA_CLUSTER_ZOOKEEPER_CONNECT=localhost:2181

delta-server server
```

## Server Administration

The patch server provides a command-line interface for administration tasks:

### Listing Datasets

```bash
delta-server list --server http://localhost:1066/
```

### Creating a Dataset

```bash
delta-server create my-dataset --server http://localhost:1066/
```

### Getting Dataset Information

```bash
delta-server info my-dataset --server http://localhost:1066/
```

### Listing Patches

```bash
delta-server patches my-dataset --server http://localhost:1066/ [--from VERSION]
```

### Backup and Restore

```bash
# Backup a dataset
delta-server backup my-dataset --server http://localhost:1066/ --output my-backup.nq

# Restore a dataset
delta-server restore my-backup.nq [my-dataset] --server http://localhost:1066/
```

## HTTP API

The patch server provides a RESTful HTTP API:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/$/list` | GET | List all patch logs |
| `/$/info/{name}` | GET | Get information about a patch log |
| `/$/create/{name}` | POST | Create a new patch log |
| `/$/append/{name}` | POST | Append a patch to a patch log |
| `/$/fetch/{name}` | GET | Get patches from a patch log |
| `/$/patch/{name}/{id}` | GET | Get a specific patch |

### Example API Usage

```bash
# List all patch logs
curl http://localhost:1066/$/list

# Create a new patch log
curl -X POST http://localhost:1066/$/create/my-dataset

# Get information about a patch log
curl http://localhost:1066/$/info/my-dataset

# Get patches from a patch log
curl http://localhost:1066/$/fetch/my-dataset?version=12345

# Get a specific patch
curl http://localhost:1066/$/patch/my-dataset/12345
```

## Monitoring

The patch server provides monitoring through JMX and Prometheus:

### JMX Metrics

When running with `--jmx`, you can access metrics via JMX:

- `rdf_delta_server_list_count`: Count of list operations
- `rdf_delta_server_create_count`: Count of create operations
- `rdf_delta_server_append_count`: Count of append operations
- `rdf_delta_server_get_patches_count`: Count of get patches operations
- `rdf_delta_server_operation_time`: Timing of various operations

### Prometheus Metrics

When configured with `delta.metrics.reporters=prometheus`, metrics are available at `/$/metrics`.

## Deployment

### Docker

You can run the patch server using Docker:

```bash
docker run -p 1066:1066 -v /path/to/store:/var/lib/delta-server/store apache/jena-delta-server
```

### Kubernetes

For Kubernetes deployments, see the [Kubernetes deployment guide](kubernetes-deployment.md).

## Integration with Fuseki

See the [Fuseki HA integration guide](fuseki-ha.md) for details on how to use the patch server with Fuseki to create a high-availability setup.

## Troubleshooting

### Common Issues

- **Connection refused**: Check the server is running and the port is correct
- **Dataset not found**: Ensure the dataset name is correct and the dataset exists
- **ZooKeeper connection failed**: Check ZooKeeper is running and the connection string is correct
- **Permission denied**: Check file permissions on the store directory

### Logs

The server logs to standard output. To redirect logs to a file:

```bash
delta-server server --store /path/to/store > server.log 2>&1
```

### Support

For additional help, please contact the Apache Jena mailing list or open an issue on GitHub.
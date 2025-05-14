# Conflict Detection and Resolution

RDF Delta includes a sophisticated conflict detection and resolution system to handle concurrent modifications to datasets. This document explains how this system works and how to configure it for your needs.

## Overview

In distributed systems, conflicts can occur when multiple clients attempt to modify the same data concurrently. RDF Delta's conflict management system provides:

1. **Conflict Detection**: Identifies when two patches would conflict with each other
2. **Conflict Classification**: Categorizes conflicts by type for appropriate handling
3. **Conflict Resolution**: Applies strategies to automatically resolve conflicts
4. **Conflict Logging**: Records conflicts for monitoring and analysis

## Conflict Types

RDF Delta detects several types of conflicts:

| Type | Description | Example |
|------|-------------|---------|
| **Direct** | Same triple modified by both patches | One patch adds a triple while another deletes it |
| **Object** | Same subject-predicate with different objects | Two patches setting different values for the same property |
| **Subject** | Same predicate-object with different subjects | Two patches linking different subjects to the same value |
| **Graph** | Modifications to related parts of a graph | Changes to different properties of the same resource |
| **Semantic** | Changes that violate constraints | Multiple values for a property with cardinality constraints |

## Resolution Strategies

The following strategies are available for resolving conflicts:

| Strategy | Description | Best For |
|----------|-------------|----------|
| **Last Write Wins** | Apply the most recent patch | Most cases where newer data is preferred |
| **First Write Wins** | Apply the earliest patch | Preserving initial values |
| **Server Wins** | Apply the patch from the primary server | Centralizing control |
| **Client Wins** | Apply the patch from the client | Local priority environments |
| **Merge** | Combine non-conflicting changes from both patches | Maximizing data preservation |
| **Reject Both** | Reject both conflicting patches | Critical data requiring manual resolution |
| **Keep Both** | Create versions for both patches | Tracking data lineage |
| **Semantic** | Apply domain-specific rules | Complex data models with constraints |

## Configuration

### Server Configuration

You can configure conflict detection and resolution at server startup:

```bash
delta-server server --store /path/to/store --conflict-detection \
  --conflict-strategy merge \
  --object-conflict-strategy last-write-wins \
  --conflict-cache-expiry 120000
```

Available options:
- `--conflict-detection`: Enable conflict detection and resolution
- `--conflict-strategy`: Set the default resolution strategy
- `--object-conflict-strategy`: Set a specific strategy for object conflicts
- `--conflict-cache-expiry`: Set the expiry time for the conflict cache in milliseconds

### Programmatic Configuration

When using the `ServerBuilder` API, you can configure conflict resolution programmatically:

```java
ServerBuilder builder = new ServerBuilder()
    .port(1066)
    .storePath("/path/to/store")
    .conflictDetectionEnabled(true)
    .defaultResolutionStrategy(ResolutionStrategy.MERGE)
    .resolutionStrategy(ConflictType.OBJECT, ResolutionStrategy.LAST_WRITE_WINS)
    .conflictCacheExpiryMs(120000);

DeltaServer server = builder.build();
```

### Strategy Selection

You can set different resolution strategies for different conflict types:

```java
ConflictResolver resolver = new ConflictResolver();
resolver.setStrategy(ConflictType.DIRECT, ResolutionStrategy.LAST_WRITE_WINS);
resolver.setStrategy(ConflictType.OBJECT, ResolutionStrategy.MERGE);
resolver.setStrategy(ConflictType.SEMANTIC, ResolutionStrategy.SEMANTIC);
```

## Monitoring

The conflict detection and resolution system exports metrics that you can monitor:

- `delta_conflicts_detected`: Number of conflicts detected
- `delta_conflicts_resolved`: Number of conflicts successfully resolved
- `delta_conflict_analysis_time`: Time spent analyzing conflicts
- `delta_conflict_resolution_time`: Time spent resolving conflicts
- `delta_server_resolutions_rejected`: Number of conflict resolutions rejected

These metrics are available via JMX or Prometheus when enabled.

## Advanced Usage

### Custom Resolution Strategies

For advanced use cases, you can implement custom resolution strategies by extending the `ConflictResolver` class:

```java
public class CustomConflictResolver extends ConflictResolver {
    @Override
    protected ResolutionResult resolveSemantic(RDFPatch patch1, RDFPatch patch2) {
        // Custom semantic resolution logic
    }
}
```

### Integration with External Systems

You can integrate the conflict resolution system with external systems:

- Record conflicts in an audit log
- Send notifications for conflicts that require manual resolution
- Implement domain-specific resolution rules based on your data model

## Performance Considerations

Conflict detection and resolution adds some overhead to the patch processing pipeline. Consider these factors:

- The cache expiry time affects memory usage and detection accuracy
- The complexity of resolution strategies affects processing time
- For high-throughput systems, use simpler strategies like Last Write Wins
- For data-critical systems, use more conservative strategies like First Write Wins or Reject Both

## Example: Managing Conflicting RDF Data

Consider an example where two users update a person's contact information:

**User 1's Update:**
```
<person:123> <contact:email> "john@example.com" .
<person:123> <contact:phone> "555-1234" .
```

**User 2's Update:**
```
<person:123> <contact:email> "john.doe@example.org" .
<person:123> <contact:address> "123 Main St" .
```

With the `MERGE` strategy, the resolved data would be:
```
<person:123> <contact:email> "john.doe@example.org" . # Last write wins for conflicting email
<person:123> <contact:phone> "555-1234" .            # Preserved from User 1
<person:123> <contact:address> "123 Main St" .       # Preserved from User 2
```

This demonstrates how appropriate conflict resolution can preserve the maximum amount of valid information while still resolving conflicts consistently.
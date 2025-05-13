/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.delta.sharding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Alarm;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphBaseFind;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalNotSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * A dataset that is sharded across multiple underlying datasets.
 * 
 * This dataset distributes data across multiple underlying datasets (shards)
 * according to a sharding strategy. It provides a unified view of all shards,
 * and transparently routes operations to the appropriate shard(s).
 */
public class ShardedDatasetGraph extends DatasetGraphBaseFind implements Transactional {
    private static final Logger LOG = LoggerFactory.getLogger(ShardedDatasetGraph.class);
    
    private final ShardingStrategy shardingStrategy;
    private final DatasetGraph[] shards;
    private final ExecutorService executor;
    private final PrefixMap prefixes;
    private final MeterRegistry registry;
    
    // Thread-local transaction state
    private final ThreadLocal<TxnState> txnState = new ThreadLocal<>();
    
    // Metrics
    private final Counter shardedQueries;
    private final Counter allShardQueries;
    private final Counter singleShardQueries;
    private final Timer queryTime;
    private final Timer findTime;
    private final Timer addTime;
    private final Timer deleteTime;
    
    /**
     * Create a new ShardedDatasetGraph.
     * 
     * @param shardingStrategy The sharding strategy to use
     * @param shards The underlying datasets (one per shard)
     */
    public ShardedDatasetGraph(ShardingStrategy shardingStrategy, DatasetGraph[] shards) {
        this(shardingStrategy, shards, null);
    }
    
    /**
     * Create a new ShardedDatasetGraph with metrics.
     * 
     * @param shardingStrategy The sharding strategy to use
     * @param shards The underlying datasets (one per shard)
     * @param registry Registry for metrics (can be null)
     */
    public ShardedDatasetGraph(ShardingStrategy shardingStrategy, DatasetGraph[] shards, MeterRegistry registry) {
        if (shardingStrategy.getShardCount() != shards.length) {
            throw new IllegalArgumentException(
                "Shard count mismatch: strategy has " + shardingStrategy.getShardCount() + 
                " shards, but " + shards.length + " datasets provided");
        }
        
        this.shardingStrategy = shardingStrategy;
        this.shards = shards;
        this.executor = Executors.newFixedThreadPool(
            Math.min(Runtime.getRuntime().availableProcessors(), shards.length));
        this.prefixes = PrefixMapFactory.create();
        this.registry = registry;
        
        // Initialize metrics
        if (registry != null) {
            this.shardedQueries = Counter.builder("delta_sharded_queries")
                .description("Number of sharded queries executed")
                .register(registry);
            
            this.allShardQueries = Counter.builder("delta_all_shard_queries")
                .description("Number of queries executed on all shards")
                .register(registry);
            
            this.singleShardQueries = Counter.builder("delta_single_shard_queries")
                .description("Number of queries executed on a single shard")
                .register(registry);
            
            this.queryTime = Timer.builder("delta_sharded_query_time")
                .description("Time spent executing sharded queries")
                .register(registry);
            
            this.findTime = Timer.builder("delta_sharded_find_time")
                .description("Time spent finding data in sharded dataset")
                .register(registry);
            
            this.addTime = Timer.builder("delta_sharded_add_time")
                .description("Time spent adding data to sharded dataset")
                .register(registry);
            
            this.deleteTime = Timer.builder("delta_sharded_delete_time")
                .description("Time spent deleting data from sharded dataset")
                .register(registry);
        } else {
            this.shardedQueries = null;
            this.allShardQueries = null;
            this.singleShardQueries = null;
            this.queryTime = null;
            this.findTime = null;
            this.addTime = null;
            this.deleteTime = null;
        }
    }
    
    /**
     * Get the sharding strategy.
     */
    public ShardingStrategy getShardingStrategy() {
        return shardingStrategy;
    }
    
    /**
     * Get the number of shards.
     */
    public int getShardCount() {
        return shards.length;
    }
    
    /**
     * Get a specific shard.
     */
    public DatasetGraph getShard(int index) {
        if (index < 0 || index >= shards.length) {
            throw new IndexOutOfBoundsException("Shard index out of range: " + index);
        }
        return shards[index];
    }
    
    /**
     * Execute an operation on all shards in parallel.
     */
    private void executeOnAllShards(Consumer<DatasetGraph> operation) {
        try {
            List<Future<?>> futures = new ArrayList<>();
            
            for (DatasetGraph shard : shards) {
                futures.add(executor.submit(() -> {
                    operation.accept(shard);
                    return null;
                }));
            }
            
            // Wait for all operations to complete
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            throw new DeltaException("Error executing operation on shards", e);
        }
    }
    
    /**
     * Execute an operation on a specific shard.
     */
    private void executeOnShard(int shardIndex, Consumer<DatasetGraph> operation) {
        if (shardIndex < 0 || shardIndex >= shards.length) {
            throw new IndexOutOfBoundsException("Shard index out of range: " + shardIndex);
        }
        
        operation.accept(shards[shardIndex]);
    }
    
    // === DatasetGraph Implementation ===
    
    @Override
    public void add(Quad quad) {
        if (addTime != null) {
            addTime.record(() -> addInternal(quad));
        } else {
            addInternal(quad);
        }
    }
    
    private void addInternal(Quad quad) {
        int shardIndex = shardingStrategy.getShardForQuad(quad);
        executeOnShard(shardIndex, shard -> shard.add(quad));
    }
    
    @Override
    public void delete(Quad quad) {
        if (deleteTime != null) {
            deleteTime.record(() -> deleteInternal(quad));
        } else {
            deleteInternal(quad);
        }
    }
    
    private void deleteInternal(Quad quad) {
        // If this is a specific quad (no variables), we can route to a specific shard
        if (!quad.getSubject().isVariable() && !quad.getPredicate().isVariable() && 
            !quad.getObject().isVariable() && (quad.getGraph() == null || !quad.getGraph().isVariable())) {
            
            int shardIndex = shardingStrategy.getShardForQuad(quad);
            executeOnShard(shardIndex, shard -> shard.delete(quad));
        } else {
            // Otherwise, we need to delete from all shards that might have matching quads
            if (shardingStrategy.requiresAllShards(quad)) {
                executeOnAllShards(shard -> shard.delete(quad));
            } else {
                int shardIndex = shardingStrategy.getShardForPattern(
                    quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
                
                if (shardIndex >= 0) {
                    executeOnShard(shardIndex, shard -> shard.delete(quad));
                } else {
                    executeOnAllShards(shard -> shard.delete(quad));
                }
            }
        }
    }
    
    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        if (findTime != null) {
            return findTime.record(() -> findInternal(g, s, p, o));
        } else {
            return findInternal(g, s, p, o);
        }
    }
    
    private Iterator<Quad> findInternal(Node g, Node s, Node p, Node o) {
        if (shardedQueries != null) {
            shardedQueries.increment();
        }
        
        // Check if we need to query all shards
        if (shardingStrategy.requiresAllShards(new Quad(g, s, p, o))) {
            if (allShardQueries != null) {
                allShardQueries.increment();
            }
            
            // Query all shards in parallel and combine results
            List<Iterator<Quad>> iterators = new ArrayList<>();
            AtomicInteger activeShards = new AtomicInteger(shards.length);
            
            try {
                List<Future<Iterator<Quad>>> futures = new ArrayList<>();
                
                for (DatasetGraph shard : shards) {
                    futures.add(executor.submit(() -> shard.find(g, s, p, o)));
                }
                
                // Collect results
                for (Future<Iterator<Quad>> future : futures) {
                    iterators.add(future.get());
                }
                
                // Combine iterators
                return Iter.flatMap(Iter.fromList(iterators), it -> it);
            } catch (Exception e) {
                throw new DeltaException("Error executing find on shards", e);
            }
        } else {
            // Query the specific shard(s) that might have matching data
            if (singleShardQueries != null) {
                singleShardQueries.increment();
            }
            
            int shardIndex = shardingStrategy.getShardForPattern(g, s, p, o);
            
            if (shardIndex >= 0) {
                // Query a specific shard
                return shards[shardIndex].find(g, s, p, o);
            } else {
                // Query all shards (fallback)
                return findInternal(g, s, p, o);
            }
        }
    }
    
    @Override
    public Graph getDefaultGraph() {
        throw new UnsupportedOperationException("Operation not supported on sharded dataset");
    }
    
    @Override
    public Graph getGraph(Node graphNode) {
        throw new UnsupportedOperationException("Operation not supported on sharded dataset");
    }
    
    @Override
    public boolean containsGraph(Node graphNode) {
        for (DatasetGraph shard : shards) {
            if (shard.containsGraph(graphNode)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Iterator<Node> listGraphNodes() {
        // Collect graph nodes from all shards
        List<Iterator<Node>> iterators = new ArrayList<>();
        
        for (DatasetGraph shard : shards) {
            iterators.add(shard.listGraphNodes());
        }
        
        // Combine and deduplicate iterators
        return Iter.distinct(Iter.flatMap(Iter.fromList(iterators), it -> it));
    }
    
    // === Transaction Support ===
    
    @Override
    public void begin(ReadWrite readWrite) {
        begin(TxnType.convert(readWrite));
    }
    
    @Override
    public void begin(TxnType type) {
        // Check if already in a transaction
        TxnState state = txnState.get();
        if (state != null) {
            throw new DeltaException("Already in a transaction");
        }
        
        // Create a new transaction state
        state = new TxnState(type);
        txnState.set(state);
        
        // Begin transactions on all shards
        for (DatasetGraph shard : shards) {
            shard.begin(type);
        }
    }
    
    @Override
    public boolean promote(Promote mode) {
        // Check if in a transaction
        TxnState state = txnState.get();
        if (state == null) {
            throw new DeltaException("Not in a transaction");
        }
        
        // Try to promote all shards
        boolean allPromoted = true;
        for (DatasetGraph shard : shards) {
            if (!shard.promote(mode)) {
                allPromoted = false;
            }
        }
        
        if (allPromoted) {
            state.type = TxnType.WRITE;
        }
        
        return allPromoted;
    }
    
    @Override
    public void commit() {
        // Check if in a transaction
        TxnState state = txnState.get();
        if (state == null) {
            throw new DeltaException("Not in a transaction");
        }
        
        try {
            // Commit all shards
            boolean allCommitted = true;
            Exception firstException = null;
            
            for (DatasetGraph shard : shards) {
                try {
                    shard.commit();
                } catch (Exception e) {
                    if (firstException == null) {
                        firstException = e;
                    }
                    allCommitted = false;
                }
            }
            
            if (!allCommitted) {
                // Try to abort all shards that haven't committed
                for (DatasetGraph shard : shards) {
                    try {
                        shard.abort();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                
                if (firstException != null) {
                    throw new DeltaException("Failed to commit all shards", firstException);
                }
            }
        } finally {
            // Clean up transaction state
            txnState.remove();
        }
    }
    
    @Override
    public void abort() {
        // Check if in a transaction
        TxnState state = txnState.get();
        if (state == null) {
            throw new DeltaException("Not in a transaction");
        }
        
        try {
            // Abort all shards
            for (DatasetGraph shard : shards) {
                try {
                    shard.abort();
                } catch (Exception e) {
                    // Ignore
                }
            }
        } finally {
            // Clean up transaction state
            txnState.remove();
        }
    }
    
    @Override
    public void end() {
        // Check if in a transaction
        TxnState state = txnState.get();
        if (state == null) {
            return; // Not in a transaction
        }
        
        try {
            // End all shards
            for (DatasetGraph shard : shards) {
                try {
                    shard.end();
                } catch (Exception e) {
                    // Ignore
                }
            }
        } finally {
            // Clean up transaction state
            txnState.remove();
        }
    }
    
    @Override
    public boolean isInTransaction() {
        return txnState.get() != null;
    }
    
    @Override
    public ReadWrite transactionMode() {
        TxnState state = txnState.get();
        if (state == null) {
            return null;
        }
        return TxnType.READ.equals(state.type) ? ReadWrite.READ : ReadWrite.WRITE;
    }
    
    @Override
    public TxnType transactionType() {
        TxnState state = txnState.get();
        if (state == null) {
            return null;
        }
        return state.type;
    }
    
    /**
     * Close the sharded dataset and release resources.
     */
    public void close() {
        if (executor != null) {
            executor.shutdown();
        }
    }
    
    /**
     * Class to hold transaction state.
     */
    private static class TxnState {
        TxnType type;
        
        TxnState(TxnType type) {
            this.type = type;
        }
    }
}
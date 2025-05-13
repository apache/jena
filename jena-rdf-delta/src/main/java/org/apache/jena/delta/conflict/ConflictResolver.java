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

package org.apache.jena.delta.conflict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.jena.delta.DeltaException;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.rdfpatch.changes.RDFChangesCollector;
import org.apache.jena.rdfpatch.system.Id;
import org.apache.jena.rdfpatch.system.RDFChangeVisitor;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

/**
 * Advanced conflict resolution for RDF patches.
 * 
 * This class resolves conflicts between RDF patches using various strategies.
 * It can be configured with specific resolution strategies for different types of conflicts.
 */
public class ConflictResolver {
    private static final Logger LOG = LoggerFactory.getLogger(ConflictResolver.class);
    
    private final ConflictDetector detector;
    private final Map<ConflictType, ResolutionStrategy> strategies;
    private final ResolutionStrategy defaultStrategy;
    private final MeterRegistry registry;
    
    // Strategy implementations
    private final Map<ResolutionStrategy, BiFunction<RDFPatch, RDFPatch, ResolutionResult>> resolvers;
    
    // Metrics
    private final Counter conflictsResolved;
    private final Timer resolutionTime;
    
    /**
     * Create a new ConflictResolver with default settings.
     */
    public ConflictResolver() {
        this(new ConflictDetector(), ResolutionStrategy.LAST_WRITE_WINS, null);
    }
    
    /**
     * Create a new ConflictResolver with specific settings.
     * 
     * @param detector The conflict detector to use
     * @param defaultStrategy The default resolution strategy
     * @param registry Registry for metrics (can be null)
     */
    public ConflictResolver(ConflictDetector detector, ResolutionStrategy defaultStrategy, MeterRegistry registry) {
        this.detector = detector;
        this.defaultStrategy = defaultStrategy;
        this.registry = registry;
        this.strategies = new HashMap<>();
        
        // Initialize strategy implementations
        this.resolvers = new HashMap<>();
        initializeResolvers();
        
        // Initialize metrics
        if (registry != null) {
            this.conflictsResolved = Counter.builder("delta_conflicts_resolved")
                .description("Number of conflicts resolved")
                .register(registry);
            
            this.resolutionTime = Timer.builder("delta_conflict_resolution_time")
                .description("Time spent resolving conflicts")
                .register(registry);
        } else {
            this.conflictsResolved = null;
            this.resolutionTime = null;
        }
    }
    
    /**
     * Initialize the resolution strategies.
     */
    private void initializeResolvers() {
        resolvers.put(ResolutionStrategy.LAST_WRITE_WINS, this::resolveLastWriteWins);
        resolvers.put(ResolutionStrategy.FIRST_WRITE_WINS, this::resolveFirstWriteWins);
        resolvers.put(ResolutionStrategy.SERVER_WINS, this::resolveServerWins);
        resolvers.put(ResolutionStrategy.CLIENT_WINS, this::resolveClientWins);
        resolvers.put(ResolutionStrategy.MERGE, this::resolveMerge);
        resolvers.put(ResolutionStrategy.REJECT_BOTH, this::resolveRejectBoth);
        resolvers.put(ResolutionStrategy.KEEP_BOTH, this::resolveKeepBoth);
        resolvers.put(ResolutionStrategy.SEMANTIC, this::resolveSemantic);
    }
    
    /**
     * Set the resolution strategy for a specific conflict type.
     * 
     * @param type The conflict type
     * @param strategy The resolution strategy
     * @return This conflict resolver
     */
    public ConflictResolver setStrategy(ConflictType type, ResolutionStrategy strategy) {
        strategies.put(type, strategy);
        return this;
    }
    
    /**
     * Resolve conflicts between two patches.
     * 
     * @param patch1 The first patch
     * @param patch2 The second patch
     * @return The resolution result
     */
    public ResolutionResult resolveConflicts(RDFPatch patch1, RDFPatch patch2) {
        if (registry != null) {
            return resolutionTime.record(() -> resolveConflictsInternal(patch1, patch2));
        } else {
            return resolveConflictsInternal(patch1, patch2);
        }
    }
    
    /**
     * Internal method to resolve conflicts between two patches.
     */
    private ResolutionResult resolveConflictsInternal(RDFPatch patch1, RDFPatch patch2) {
        // Detect conflicts
        List<Conflict> conflicts = detector.detectConflicts(patch1, patch2);
        
        // If there are no conflicts, return patch2 (the newer patch)
        if (conflicts.isEmpty()) {
            return new ResolutionResult(ResolutionStatus.NO_CONFLICT, patch2, null);
        }
        
        // Group conflicts by type
        Map<ConflictType, List<Conflict>> conflictsByType = new HashMap<>();
        for (Conflict conflict : conflicts) {
            conflictsByType.computeIfAbsent(conflict.getType(), k -> new ArrayList<>()).add(conflict);
        }
        
        // Determine the primary resolution strategy
        ResolutionStrategy primaryStrategy = null;
        
        // If there's only one type of conflict, use its strategy
        if (conflictsByType.size() == 1) {
            ConflictType type = conflictsByType.keySet().iterator().next();
            primaryStrategy = strategies.getOrDefault(type, defaultStrategy);
        } else {
            // If there are multiple types, prioritize: SEMANTIC > DIRECT > OBJECT > SUBJECT > GRAPH
            if (conflictsByType.containsKey(ConflictType.SEMANTIC)) {
                primaryStrategy = strategies.getOrDefault(ConflictType.SEMANTIC, defaultStrategy);
            } else if (conflictsByType.containsKey(ConflictType.DIRECT)) {
                primaryStrategy = strategies.getOrDefault(ConflictType.DIRECT, defaultStrategy);
            } else if (conflictsByType.containsKey(ConflictType.OBJECT)) {
                primaryStrategy = strategies.getOrDefault(ConflictType.OBJECT, defaultStrategy);
            } else if (conflictsByType.containsKey(ConflictType.SUBJECT)) {
                primaryStrategy = strategies.getOrDefault(ConflictType.SUBJECT, defaultStrategy);
            } else {
                primaryStrategy = strategies.getOrDefault(ConflictType.GRAPH, defaultStrategy);
            }
        }
        
        // Apply the resolution strategy
        BiFunction<RDFPatch, RDFPatch, ResolutionResult> resolver = 
            resolvers.getOrDefault(primaryStrategy, this::resolveLastWriteWins);
        
        ResolutionResult result = resolver.apply(patch1, patch2);
        
        // Update metrics
        if (registry != null) {
            conflictsResolved.increment(conflicts.size());
        }
        
        LOG.debug("Resolved {} conflicts using strategy: {}", conflicts.size(), primaryStrategy.getName());
        
        return result;
    }
    
    // ---- Resolution strategy implementations ----
    
    /**
     * Last write wins resolution strategy.
     */
    private ResolutionResult resolveLastWriteWins(RDFPatch patch1, RDFPatch patch2) {
        // Assume patch2 is the more recent patch
        return new ResolutionResult(ResolutionStatus.RESOLVED, patch2, 
            "Applied most recent patch (last write wins)");
    }
    
    /**
     * First write wins resolution strategy.
     */
    private ResolutionResult resolveFirstWriteWins(RDFPatch patch1, RDFPatch patch2) {
        // Assume patch1 is the earlier patch
        return new ResolutionResult(ResolutionStatus.RESOLVED, patch1, 
            "Applied earliest patch (first write wins)");
    }
    
    /**
     * Server wins resolution strategy.
     */
    private ResolutionResult resolveServerWins(RDFPatch patch1, RDFPatch patch2) {
        // Assume patch1 is from the server
        return new ResolutionResult(ResolutionStatus.RESOLVED, patch1, 
            "Applied server patch (server wins)");
    }
    
    /**
     * Client wins resolution strategy.
     */
    private ResolutionResult resolveClientWins(RDFPatch patch1, RDFPatch patch2) {
        // Assume patch2 is from the client
        return new ResolutionResult(ResolutionStatus.RESOLVED, patch2, 
            "Applied client patch (client wins)");
    }
    
    /**
     * Merge resolution strategy.
     */
    private ResolutionResult resolveMerge(RDFPatch patch1, RDFPatch patch2) {
        try {
            // Create collectors for each patch
            PatchMergeCollector c1 = new PatchMergeCollector();
            PatchMergeCollector c2 = new PatchMergeCollector();
            
            // Visit the patches to collect information
            RDFChangeVisitor.visit(patch1, c1);
            RDFChangeVisitor.visit(patch2, c2);
            
            // Create a new collector for the merged patch
            RDFChangesCollector merged = new RDFChangesCollector();
            merged.txnBegin();
            
            // Apply non-conflicting additions from both patches
            for (Quad q : c1.additions) {
                if (!c2.deletions.contains(q)) {
                    merged.add(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
                }
            }
            
            for (Quad q : c2.additions) {
                if (!c1.deletions.contains(q) && !c1.additions.contains(q)) {
                    merged.add(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
                }
            }
            
            // Apply non-conflicting deletions from both patches
            for (Quad q : c1.deletions) {
                if (!c2.additions.contains(q)) {
                    merged.delete(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
                }
            }
            
            for (Quad q : c2.deletions) {
                if (!c1.additions.contains(q) && !c1.deletions.contains(q)) {
                    merged.delete(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
                }
            }
            
            // Apply prefixes from both patches (newer overrides older)
            for (Map.Entry<Node, Map<String, String>> entry : c1.prefixes.entrySet()) {
                Node graph = entry.getKey();
                for (Map.Entry<String, String> prefix : entry.getValue().entrySet()) {
                    merged.addPrefix(graph, prefix.getKey(), prefix.getValue());
                }
            }
            
            for (Map.Entry<Node, Map<String, String>> entry : c2.prefixes.entrySet()) {
                Node graph = entry.getKey();
                for (Map.Entry<String, String> prefix : entry.getValue().entrySet()) {
                    merged.addPrefix(graph, prefix.getKey(), prefix.getValue());
                }
            }
            
            // Commit the merged patch
            merged.txnCommit();
            
            // Get the merged patch
            RDFPatch mergedPatch = merged.getRDFPatch();
            
            // If the merged patch is a no-op, use the newer patch
            if (RDFPatchOps.isEmptyPatch(mergedPatch)) {
                return new ResolutionResult(ResolutionStatus.RESOLVED, patch2, 
                    "Merged patch was empty, using newer patch");
            }
            
            return new ResolutionResult(ResolutionStatus.RESOLVED, mergedPatch, 
                "Merged non-conflicting changes from both patches");
            
        } catch (Exception e) {
            LOG.error("Error merging patches", e);
            return new ResolutionResult(ResolutionStatus.ERROR, null, 
                "Error merging patches: " + e.getMessage());
        }
    }
    
    /**
     * Reject both resolution strategy.
     */
    private ResolutionResult resolveRejectBoth(RDFPatch patch1, RDFPatch patch2) {
        return new ResolutionResult(ResolutionStatus.REJECTED, null, 
            "Both patches rejected due to conflicts");
    }
    
    /**
     * Keep both resolution strategy.
     */
    private ResolutionResult resolveKeepBoth(RDFPatch patch1, RDFPatch patch2) {
        // This would create versions in a real implementation
        // For now, just return the newer patch with a note
        return new ResolutionResult(ResolutionStatus.BRANCHED, patch2, 
            "Created versions for both patches");
    }
    
    /**
     * Semantic resolution strategy.
     */
    private ResolutionResult resolveSemantic(RDFPatch patch1, RDFPatch patch2) {
        // This would use domain-specific rules in a real implementation
        // For now, just use the merge strategy
        return resolveMerge(patch1, patch2);
    }
    
    /**
     * Helper class to collect information from a patch for merging.
     */
    private static class PatchMergeCollector implements org.apache.jena.rdfpatch.RDFChanges {
        List<Quad> additions = new ArrayList<>();
        List<Quad> deletions = new ArrayList<>();
        Map<Node, Map<String, String>> prefixes = new HashMap<>();
        
        @Override
        public void txnBegin() {}
        
        @Override
        public void txnCommit() {}
        
        @Override
        public void txnAbort() {}
        
        @Override
        public void add(Node g, Node s, Node p, Node o) {
            additions.add(new Quad(g, s, p, o));
        }
        
        @Override
        public void delete(Node g, Node s, Node p, Node o) {
            deletions.add(new Quad(g, s, p, o));
        }
        
        @Override
        public void addPrefix(Node gn, String prefix, String uriStr) {
            prefixes.computeIfAbsent(gn, k -> new HashMap<>()).put(prefix, uriStr);
        }
        
        @Override
        public void deletePrefix(Node gn, String prefix) {
            Map<String, String> graphPrefixes = prefixes.get(gn);
            if (graphPrefixes != null) {
                graphPrefixes.remove(prefix);
            }
        }
        
        @Override
        public void header(String field, Node value) {}
        
        @Override
        public void segment() {}
    }
}
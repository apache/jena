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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.changes.PatchSummary;
import org.apache.jena.rdfpatch.changes.RDFChangesCollector;
import org.apache.jena.rdfpatch.system.RDFChangeVisitor;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

/**
 * Advanced conflict detection for RDF patches.
 * 
 * This class analyzes pairs of patches to detect different types of conflicts:
 * 1. Direct conflicts - same triple modified by both patches
 * 2. Object conflicts - same subject-predicate modified with different objects
 * 3. Subject conflicts - same graph-predicate-object used with different subjects
 * 4. Graph conflicts - modifications to the same part of a graph
 * 5. Semantic conflicts - changes that violate constraints or inferred relationships
 */
public class ConflictDetector {
    private static final Logger LOG = LoggerFactory.getLogger(ConflictDetector.class);
    
    // Conflict analysis options
    private final boolean detectDirectConflicts;
    private final boolean detectObjectConflicts;
    private final boolean detectSubjectConflicts;
    private final boolean detectGraphConflicts;
    private final boolean detectSemanticConflicts;
    
    // Time window for conflict detection (in milliseconds)
    private final long conflictTimeWindow;
    
    // Registry for metrics
    private final MeterRegistry registry;
    
    // Metrics
    private final Counter conflictsDetected;
    private final Counter patchesAnalyzed;
    private final Timer conflictAnalysisTime;
    
    /**
     * Create a new ConflictDetector with default settings.
     */
    public ConflictDetector() {
        this(true, true, false, true, false, 5000, null);
    }
    
    /**
     * Create a new ConflictDetector with specific settings.
     * 
     * @param detectDirectConflicts Detect direct triple conflicts (same triple modified)
     * @param detectObjectConflicts Detect object conflicts (same S-P with different O)
     * @param detectSubjectConflicts Detect subject conflicts (same P-O with different S)
     * @param detectGraphConflicts Detect graph-level conflicts
     * @param detectSemanticConflicts Detect semantic conflicts based on rules
     * @param conflictTimeWindow Time window for conflict detection (in milliseconds)
     * @param registry Registry for metrics (can be null)
     */
    public ConflictDetector(
            boolean detectDirectConflicts,
            boolean detectObjectConflicts,
            boolean detectSubjectConflicts,
            boolean detectGraphConflicts,
            boolean detectSemanticConflicts,
            long conflictTimeWindow,
            MeterRegistry registry) {
        
        this.detectDirectConflicts = detectDirectConflicts;
        this.detectObjectConflicts = detectObjectConflicts;
        this.detectSubjectConflicts = detectSubjectConflicts;
        this.detectGraphConflicts = detectGraphConflicts;
        this.detectSemanticConflicts = detectSemanticConflicts;
        this.conflictTimeWindow = conflictTimeWindow;
        this.registry = registry;
        
        // Initialize metrics
        if (registry != null) {
            this.conflictsDetected = Counter.builder("delta_conflicts_detected")
                .description("Number of conflicts detected")
                .register(registry);
            
            this.patchesAnalyzed = Counter.builder("delta_patches_analyzed")
                .description("Number of patches analyzed for conflicts")
                .register(registry);
            
            this.conflictAnalysisTime = Timer.builder("delta_conflict_analysis_time")
                .description("Time spent analyzing conflicts")
                .register(registry);
        } else {
            this.conflictsDetected = null;
            this.patchesAnalyzed = null;
            this.conflictAnalysisTime = null;
        }
    }
    
    /**
     * Detect conflicts between two patches.
     * 
     * @param patch1 The first patch
     * @param patch2 The second patch
     * @return A list of detected conflicts
     */
    public List<Conflict> detectConflicts(RDFPatch patch1, RDFPatch patch2) {
        if (registry != null) {
            patchesAnalyzed.increment(2);
            return conflictAnalysisTime.record(() -> detectConflictsInternal(patch1, patch2));
        } else {
            return detectConflictsInternal(patch1, patch2);
        }
    }
    
    /**
     * Internal method to detect conflicts between two patches.
     */
    private List<Conflict> detectConflictsInternal(RDFPatch patch1, RDFPatch patch2) {
        List<Conflict> conflicts = new ArrayList<>();
        
        // Extract patch information
        PatchCollector collector1 = new PatchCollector();
        PatchCollector collector2 = new PatchCollector();
        
        // Visit the patches to collect information
        RDFChangeVisitor.visit(patch1, collector1);
        RDFChangeVisitor.visit(patch2, collector2);
        
        // Check for direct conflicts (same triple modified)
        if (detectDirectConflicts) {
            detectDirectConflicts(collector1, collector2, conflicts);
        }
        
        // Check for object conflicts (same S-P with different O)
        if (detectObjectConflicts) {
            detectObjectConflicts(collector1, collector2, conflicts);
        }
        
        // Check for subject conflicts (same P-O with different S)
        if (detectSubjectConflicts) {
            detectSubjectConflicts(collector1, collector2, conflicts);
        }
        
        // Check for graph conflicts
        if (detectGraphConflicts) {
            detectGraphConflicts(collector1, collector2, conflicts);
        }
        
        // Check for semantic conflicts
        if (detectSemanticConflicts) {
            detectSemanticConflicts(collector1, collector2, conflicts);
        }
        
        // Update metrics
        if (registry != null && !conflicts.isEmpty()) {
            conflictsDetected.increment(conflicts.size());
        }
        
        return conflicts;
    }
    
    /**
     * Detect direct conflicts (same triple modified by both patches).
     */
    private void detectDirectConflicts(PatchCollector c1, PatchCollector c2, List<Conflict> conflicts) {
        // Check for additions in p1 that are deleted in p2
        for (Quad q : c1.additions) {
            if (c2.deletions.contains(q)) {
                conflicts.add(new Conflict(ConflictType.DIRECT, q, "Same quad added in one patch but deleted in another"));
            }
        }
        
        // Check for deletions in p1 that are added in p2
        for (Quad q : c1.deletions) {
            if (c2.additions.contains(q)) {
                conflicts.add(new Conflict(ConflictType.DIRECT, q, "Same quad deleted in one patch but added in another"));
            }
        }
    }
    
    /**
     * Detect object conflicts (same S-P with different O).
     */
    private void detectObjectConflicts(PatchCollector c1, PatchCollector c2, List<Conflict> conflicts) {
        // Build S-P maps for additions
        Map<SP, Set<Node>> spMap1 = buildSPMap(c1.additions);
        Map<SP, Set<Node>> spMap2 = buildSPMap(c2.additions);
        
        // Check for S-P combinations with different objects
        for (Map.Entry<SP, Set<Node>> entry : spMap1.entrySet()) {
            SP sp = entry.getKey();
            Set<Node> objects1 = entry.getValue();
            
            if (spMap2.containsKey(sp)) {
                Set<Node> objects2 = spMap2.get(sp);
                
                // If the object sets are different, there's a potential conflict
                if (!objects1.equals(objects2)) {
                    // Create a conflict for each object in p2 not in p1
                    for (Node o2 : objects2) {
                        if (!objects1.contains(o2)) {
                            Quad q = new Quad(sp.graph, sp.subject, sp.predicate, o2);
                            conflicts.add(new Conflict(ConflictType.OBJECT, q, 
                                "Different objects used for same subject-predicate"));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Detect subject conflicts (same P-O with different S).
     */
    private void detectSubjectConflicts(PatchCollector c1, PatchCollector c2, List<Conflict> conflicts) {
        // Build P-O maps for additions
        Map<PO, Set<Node>> poMap1 = buildPOMap(c1.additions);
        Map<PO, Set<Node>> poMap2 = buildPOMap(c2.additions);
        
        // Check for P-O combinations with different subjects
        for (Map.Entry<PO, Set<Node>> entry : poMap1.entrySet()) {
            PO po = entry.getKey();
            Set<Node> subjects1 = entry.getValue();
            
            if (poMap2.containsKey(po)) {
                Set<Node> subjects2 = poMap2.get(po);
                
                // If the subject sets are different, there's a potential conflict
                if (!subjects1.equals(subjects2)) {
                    // Create a conflict for each subject in p2 not in p1
                    for (Node s2 : subjects2) {
                        if (!subjects1.contains(s2)) {
                            Quad q = new Quad(po.graph, s2, po.predicate, po.object);
                            conflicts.add(new Conflict(ConflictType.SUBJECT, q, 
                                "Different subjects used for same predicate-object"));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Detect graph conflicts (modifications to the same graph region).
     */
    private void detectGraphConflicts(PatchCollector c1, PatchCollector c2, List<Conflict> conflicts) {
        // Build maps of subjects modified in each graph
        Map<Node, Set<Node>> graphSubjects1 = buildGraphSubjectsMap(c1);
        Map<Node, Set<Node>> graphSubjects2 = buildGraphSubjectsMap(c2);
        
        // Check for overlapping subjects in the same graph
        for (Map.Entry<Node, Set<Node>> entry : graphSubjects1.entrySet()) {
            Node graph = entry.getKey();
            Set<Node> subjects1 = entry.getValue();
            
            if (graphSubjects2.containsKey(graph)) {
                Set<Node> subjects2 = graphSubjects2.get(graph);
                
                // Find common subjects (overlap)
                Set<Node> commonSubjects = new HashSet<>(subjects1);
                commonSubjects.retainAll(subjects2);
                
                // If there are common subjects, there's a potential graph conflict
                if (!commonSubjects.isEmpty()) {
                    for (Node subject : commonSubjects) {
                        // Create a representative quad for the conflict
                        Quad q = new Quad(graph, subject, null, null);
                        conflicts.add(new Conflict(ConflictType.GRAPH, q, 
                            "Modifications to same subject in a graph"));
                    }
                }
            }
        }
    }
    
    /**
     * Detect semantic conflicts based on rules and constraints.
     */
    private void detectSemanticConflicts(PatchCollector c1, PatchCollector c2, List<Conflict> conflicts) {
        // This is a placeholder for more advanced semantic conflict detection
        // In a real implementation, this would use an OWL reasoner or rule engine
        // to detect conflicts based on the semantics of the data
        
        // Example: detect changes that violate cardinality constraints
        // For this example, we'll assume that any property with "unique" in the name
        // should have only one value per subject
        
        detectCardinalityConflicts(c1, c2, conflicts);
    }
    
    /**
     * Detect cardinality conflicts (e.g., unique properties with multiple values).
     */
    private void detectCardinalityConflicts(PatchCollector c1, PatchCollector c2, List<Conflict> conflicts) {
        // Build S-P maps for additions
        Map<SP, Set<Node>> spMap1 = buildSPMap(c1.additions);
        Map<SP, Set<Node>> spMap2 = buildSPMap(c2.additions);
        
        // Check unique properties
        for (Map.Entry<SP, Set<Node>> entry : spMap1.entrySet()) {
            SP sp = entry.getKey();
            
            // Check if this looks like a unique property
            String predicateStr = sp.predicate.toString();
            boolean isUniqueProperty = predicateStr.contains("unique") || 
                                       predicateStr.contains("identifier") || 
                                       predicateStr.contains("id") ||
                                       predicateStr.contains("key");
            
            if (isUniqueProperty && spMap2.containsKey(sp)) {
                Set<Node> objects1 = entry.getValue();
                Set<Node> objects2 = spMap2.get(sp);
                
                // Combine the object sets
                Set<Node> allObjects = new HashSet<>(objects1);
                allObjects.addAll(objects2);
                
                // If a unique property has multiple values, it's a conflict
                if (allObjects.size() > 1) {
                    Quad q = new Quad(sp.graph, sp.subject, sp.predicate, null);
                    conflicts.add(new Conflict(ConflictType.SEMANTIC, q, 
                        "Multiple values for unique property: " + sp.predicate));
                }
            }
        }
    }
    
    /**
     * Build a map of subject-predicate combinations to their objects.
     */
    private Map<SP, Set<Node>> buildSPMap(Set<Quad> quads) {
        Map<SP, Set<Node>> spMap = new HashMap<>();
        
        for (Quad q : quads) {
            SP sp = new SP(q.getGraph(), q.getSubject(), q.getPredicate());
            spMap.computeIfAbsent(sp, k -> new HashSet<>()).add(q.getObject());
        }
        
        return spMap;
    }
    
    /**
     * Build a map of predicate-object combinations to their subjects.
     */
    private Map<PO, Set<Node>> buildPOMap(Set<Quad> quads) {
        Map<PO, Set<Node>> poMap = new HashMap<>();
        
        for (Quad q : quads) {
            PO po = new PO(q.getGraph(), q.getPredicate(), q.getObject());
            poMap.computeIfAbsent(po, k -> new HashSet<>()).add(q.getSubject());
        }
        
        return poMap;
    }
    
    /**
     * Build a map of graphs to the subjects modified in each graph.
     */
    private Map<Node, Set<Node>> buildGraphSubjectsMap(PatchCollector collector) {
        Map<Node, Set<Node>> graphSubjects = new HashMap<>();
        
        // Add subjects from additions
        for (Quad q : collector.additions) {
            graphSubjects.computeIfAbsent(q.getGraph(), k -> new HashSet<>())
                .add(q.getSubject());
        }
        
        // Add subjects from deletions
        for (Quad q : collector.deletions) {
            graphSubjects.computeIfAbsent(q.getGraph(), k -> new HashSet<>())
                .add(q.getSubject());
        }
        
        return graphSubjects;
    }
    
    /**
     * Helper class to collect information from a patch.
     */
    private static class PatchCollector implements RDFChanges {
        Set<Quad> additions = new HashSet<>();
        Set<Quad> deletions = new HashSet<>();
        
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
        public void addPrefix(Node gn, String prefix, String uriStr) {}
        
        @Override
        public void deletePrefix(Node gn, String prefix) {}
        
        @Override
        public void header(String field, Node value) {}
        
        @Override
        public void segment() {}
    }
    
    /**
     * Subject-Predicate key for conflict detection.
     */
    private static class SP {
        final Node graph;
        final Node subject;
        final Node predicate;
        
        SP(Node graph, Node subject, Node predicate) {
            this.graph = graph;
            this.subject = subject;
            this.predicate = predicate;
        }
        
        @Override
        public int hashCode() {
            return (graph == null ? 0 : graph.hashCode()) ^
                   (subject == null ? 0 : subject.hashCode()) ^
                   (predicate == null ? 0 : predicate.hashCode());
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SP)) return false;
            SP other = (SP) obj;
            return (graph == null ? other.graph == null : graph.equals(other.graph)) &&
                   (subject == null ? other.subject == null : subject.equals(other.subject)) &&
                   (predicate == null ? other.predicate == null : predicate.equals(other.predicate));
        }
    }
    
    /**
     * Predicate-Object key for conflict detection.
     */
    private static class PO {
        final Node graph;
        final Node predicate;
        final Node object;
        
        PO(Node graph, Node predicate, Node object) {
            this.graph = graph;
            this.predicate = predicate;
            this.object = object;
        }
        
        @Override
        public int hashCode() {
            return (graph == null ? 0 : graph.hashCode()) ^
                   (predicate == null ? 0 : predicate.hashCode()) ^
                   (object == null ? 0 : object.hashCode());
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof PO)) return false;
            PO other = (PO) obj;
            return (graph == null ? other.graph == null : graph.equals(other.graph)) &&
                   (predicate == null ? other.predicate == null : predicate.equals(other.predicate)) &&
                   (object == null ? other.object == null : object.equals(other.object));
        }
    }
}
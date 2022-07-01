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

package org.apache.jena.riot.lang;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.out.NodeToLabel;
import org.apache.jena.riot.system.MapWithScope;
import org.apache.jena.riot.system.SyntaxLabels;

/**
 * Allocation of Nodes (blank nodes usually) based on the graph and scope.
 * There are various different policies.
 * See {@link SyntaxLabels#createLabelToNode} for getting a default setup;
 * some of the others are for testing and debugging and may not generate
 * legal RDF overall (e.g. reparsing the same file gets the same bNodes)
 */

public class LabelToNode extends MapWithScope<String, Node, Node>
{
    /**
     * Allocation from a single scope; just the label matters.
     * This is the RDF syntax correct policy.
     * See {@link #createScopeGlobal} for some history.
     */
    public static LabelToNode createScopeByDocumentHash()
    { return new LabelToNode(new FixedScopePolicy(), nodeAllocatorHash()); }

    /**
     * Allocation from a single scope; just the label matters.  Use this policy if repeated runs must give identical allocations
     * @param seed Seed
     */
    public static LabelToNode createScopeByDocumentHash(UUID seed)
    { return new LabelToNode(new FixedScopePolicy(), nodeAllocatorHash(seed)); }

    /**
     * Allocation, with a map from seen label to node. It uses the jena-core blank
     * node allocator and a map from label to blank node. This style works for any
     * blank node allocation style but the map can grow to arbitrary size.
     * <p>
     * This was the policy up to Jena 2.10.0 but it occasionally ran into problems at
     * very large scale because it generates and remembers a new UUIDs for new each
     * blank node. The policy changed to {@link #createScopeByDocumentHash} which
     * calculates the blank node label needed without needing to retain previous
     * allocations.
     */
    public static LabelToNode createScopeGlobal()
    { return new LabelToNode(new SingleScopePolicy(), nodeAllocatorGlobal()); }

    /** Allocation scoped by graph and label. */
    public static LabelToNode createScopeByGraph() {
        return new LabelToNode(new GraphScopePolicy(), nodeAllocatorByGraph());
    }

    /**
     * Allocation using syntax label; output is unsafe for reading (use
     * {@link #createUseLabelEncoded()} for output-input).
     *
     * The reverse operation is provided by {@link NodeToLabel#createBNodeByLabelAsGiven()}
     * but the pair is <em>unsafe</em> for output-input.  Use encoded labels for that.
     *
     * The main purpose of this LabelToNode is to preserve the used label for debugging.
     */
    public static LabelToNode createUseLabelAsGiven()
    { return new LabelToNode(new FixedScopePolicy(), nodeAllocatorRawLabel()); }

    /**
     * Allocation using an encoded syntax label
     * (i.e. _:B&lt;encoded&gt; format from {@link NodeFmtLib#encodeBNodeLabel}).
     *
     * The reverse operation is provided by {@link NodeToLabel#createBNodeByLabelEncoded()}.
     * This pair should be used to write out and recover blank node by internal id.
     */
    public static LabelToNode createUseLabelEncoded()
    { return new LabelToNode(new FixedScopePolicy(), nodeAllocatorEncoded()); }

    /** Allocation, globally scoped, that uses a incrementing field to create new nodes */
    public static LabelToNode createIncremental()
    { return new LabelToNode(new SingleScopePolicy(), nodeAllocatorDeterministic()); }

    // ---- Create fresh allocators per call

    // The preferred node allocator - completely scalable.
    // Nodes are unique-per-run.

    private static Allocator<String, Node, Node> nodeAllocatorHash() {
        return new Alloc(new BlankNodeAllocatorHash());
    }

    private static Allocator<String, Node, Node> nodeAllocatorHash(UUID seed) {
        return new Alloc(new BlankNodeAllocatorFixedSeedHash(seed));
    }

    private static Allocator<String, Node, Node> nodeAllocatorDeterministic() {
        return new Alloc(new BlankNodeAllocatorCounter());
    }

    private static Allocator<String, Node, Node> nodeAllocatorGlobal() {
        return new Alloc(new BlankNodeAllocatorGlobal());
    }

    private static Allocator<String, Node, Node> nodeAllocatorEncoded() {
        return new Alloc(new BlankNodeAllocatorLabelEncoded());
    }

    private static Allocator<String, Node, Node> nodeAllocatorRawLabel() {
        return new Alloc(new BlankNodeAllocatorCounter());
    }

    private static Allocator<String, Node, Node> nodeAllocatorByGraph() {
        return new AllocByScope();
    }

    // ---- The class

    public LabelToNode(ScopePolicy<String, Node, Node> scopePolicy, Allocator<String, Node, Node> allocator) {
        super(scopePolicy, allocator);
    }

    // ======== Scope Policies

    /** Single scope per instance. */
    private static class SingleScopePolicy implements ScopePolicy<String, Node, Node> {
        private Map<String, Node> map = new HashMap<>();
        @Override
        public Map<String, Node> getScope(Node scope) {
            return map;
        }

        @Override
        public void clear() {
            map.clear();
        }
    }

    /** One scope for labels per graph. */
    private static class GraphScopePolicy implements ScopePolicy<String, Node, Node> {
        private Map<String, Node> dftMap = new HashMap<>();
        private Map<Node, Map<String, Node>> map = new HashMap<>();
        @Override
        public Map<String, Node> getScope(Node scope) {
            if ( scope == null )
                return dftMap;

            Map<String, Node> x = map.get(scope);
            if ( x == null ) {
                x = new HashMap<>();
                map.put(scope, x);
            }
            return x;
        }

        @Override
        public void clear() {
            dftMap.clear();
            map.clear();
        }
    }

    /** No scope - use the allocator */
    private static class FixedScopePolicy implements ScopePolicy<String, Node, Node> {
        @Override
        public Map<String, Node> getScope(Node scope) {
            return null;
        }

        @Override
        public void clear() {}
    }

    // ======== Node Allocators
    // Adapter class from  MapWithScope.Allocator to BlankNodeAllocator
    // which does not take scope into account when creating nodes.
    private static class Alloc implements Allocator<String, Node, Node> {
        final BlankNodeAllocator alloc;

        Alloc(BlankNodeAllocator alloc) {
            this.alloc = alloc;
        }

        @Override
        public Node alloc(Node scope, String label) {
            return alloc.alloc(label);
        }

        @Override
        public Node create() {
            return alloc.create();
        }

        @Override
        public void reset() {
            alloc.reset();
        }
    }

    /** Allocate a fresh blank node each time with a scope. */
    private static class AllocByScope implements Allocator<String, Node, Node> {
        BlankNodeAllocator dft = make();
        Map<Node, BlankNodeAllocator> graphs = new HashMap<>();

        @Override
        public Node alloc(Node scope, String label) {
            if ( scope == null )
                return dft.alloc(label);

            BlankNodeAllocator alloc = graphs.get(scope);
            if ( alloc == null ) {
                alloc = make();
                graphs.put(scope, alloc);
            }
            return alloc.alloc(label);
        }

        @Override
        public Node create() {
            return dft.create();
        }

        @Override
        public void reset() {
            graphs.clear();
            dft.reset();
        }

        private BlankNodeAllocator make() {
            return new BlankNodeAllocatorHash();
        }
    }
}

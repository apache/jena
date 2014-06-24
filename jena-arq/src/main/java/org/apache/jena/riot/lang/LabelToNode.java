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
import java.util.HashMap ;
import java.util.Map ;
import java.util.UUID;

import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.riot.out.NodeToLabel ;
import org.apache.jena.riot.system.MapWithScope ;
import org.apache.jena.riot.system.SyntaxLabels ;

import com.hp.hpl.jena.graph.Node ;

/** Allocation Nodes (Bnodes usually) based on the graph and label 
 * Various different policies.
 * See {@link SyntaxLabels#createLabelToNode} for getting a default setup; 
 * some of the others are for testing and debugging and may not generate
 * legal RDF overall (e.g. reparsign the same file gets the same bNodes) 
 */  

public class LabelToNode extends MapWithScope<String, Node, Node>
{
    /** Allocation from a single scope; just the label matters. 
     *  This is the RDF syntax correct policy.
     */
    public static LabelToNode createScopeByDocumentHash()
    { return new LabelToNode(new AllocScopePolicy(), nodeAllocatorHash()) ; }
    
    /**
     * Allocation from a single scope; just the label matters.  Use this policy if repeated runs must give identical allocations
     * @param seed Seed
     */
    public static LabelToNode createScopeByDocumentHash(UUID seed)
    { return new LabelToNode(new AllocScopePolicy(), nodeAllocatorHash(seed)) ; }

    /** The policy up to jena 2.10.0 - problems at very large scale */
    public static LabelToNode createScopeByDocumentOld()
    { return new LabelToNode(new SingleScopePolicy(), nodeAllocatorTraditional()) ; }

    /** Allocation scoped by graph and label. */
    public static LabelToNode createScopeByGraph() {
        // Not AllocNodeHash here - that is unique per run, not per graph-in-run.
        // Must use an unconditionally unique allocator.
        return new LabelToNode(new GraphScopePolicy(), nodeAllocatorByGraph()) ;
    }

    /** Allocation using syntax label; output is unsafe for reading (use 
     * {@link #createUseLabelEncoded()} for output-input).
     * 
     * The reverse operation is provided by {@link NodeToLabel#createBNodeByLabelAsGiven()}
     * but the pair is <em>unsafe</em> for output-input.  Use encoded labels for that.
     * 
     * The main pupose of this LabelToNode is to preserve the used label for debugging. 
     */
    public static LabelToNode createUseLabelAsGiven()
    { return new LabelToNode(new AllocScopePolicy(), nodeAllocatorRawLabel()) ; }
    
    /** Allocation using an encoded syntax label 
     * (i.e. _:B&lt;encoded&gt; format from {@link NodeFmtLib#encodeBNodeLabel}).
     * 
     * The reverse operation is provided by {@link NodeToLabel#createBNodeByLabelEncoded()}.
     * This pair should be used to write out and recover blank node by internal id. 
     */
    public static LabelToNode createUseLabelEncoded()
    { return new LabelToNode(new AllocScopePolicy(), nodeAllocatorEncoded()) ; }

    /** Allocation, globally scoped, that uses a incrementing field to create new nodes */  
    public static LabelToNode createIncremental()
    { return new LabelToNode(new SingleScopePolicy(), nodeAllocatorDeterministic()) ; } 

    // ---- Create fresh allocators per call
    
    // The preferred node allocator - completely scalable.
    // Nodes are unique-per-run.
    
    private static Allocator<String, Node, Node> nodeAllocatorHash() { 
        return new Alloc(new BlankNodeAllocatorHash()) ; 
    } 
    
    private static Allocator<String, Node, Node> nodeAllocatorHash(UUID seed) {
        return new Alloc(new BlankNodeAllocatorFixedSeedHash(seed)) ;
    }
    
    private static Allocator<String, Node, Node> nodeAllocatorDeterministic() { 
        return new Alloc(new BlankNodeAllocatorLabel()) ; 
    } 
    
    private static Allocator<String, Node, Node> nodeAllocatorTraditional() { 
        return new Alloc(new BlankNodeAllocatorTraditional()) ; 
    } 
    
    private static Allocator<String, Node, Node> nodeAllocatorEncoded() { 
        return new Alloc(new BlankNodeAllocatorLabelEncoded()) ; 
    } 
    
    private static Allocator<String, Node, Node> nodeAllocatorRawLabel() { 
        return new Alloc(new BlankNodeAllocatorLabel()) ; 
    } 

    private static Allocator<String, Node, Node> nodeAllocatorByGraph() { 
        return new AllocByGraph() ;
    } 

    // ---- The class
    
    public LabelToNode(ScopePolicy<String, Node, Node> scopePolicy, Allocator<String, Node, Node> allocator)
    {
        super(scopePolicy, allocator) ;
    }

    // ======== Scope Policies
    
    /** Single scope */
    private static class SingleScopePolicy implements ScopePolicy<String, Node, Node>
    { 
        private Map<String, Node> map = new HashMap<>() ;
        @Override
        public Map<String, Node> getScope(Node scope) { return map ; }
        @Override
        public void clear() { map.clear(); }
    }
    
    /** One scope for labels per graph */
    private static class GraphScopePolicy implements ScopePolicy<String, Node, Node>
    { 
        private Map<String, Node> dftMap = new HashMap<>() ;
        private Map<Node, Map<String, Node>> map = new HashMap<>() ;
        @Override
        public Map<String, Node> getScope(Node scope)
        {
            if ( scope == null )
                return dftMap ;
            
            Map<String, Node> x = map.get(scope) ;
            if ( x == null )
            {
                x = new HashMap<>() ;
                map.put(scope, x) ;
            }
            return x ;
        }
        @Override
        public void clear() {
            dftMap.clear() ;
            map.clear(); }
    }

    /** No scope - use raw allocator */
    private static class AllocScopePolicy  implements ScopePolicy<String, Node, Node>
    { 
        @Override
        public Map<String, Node> getScope(Node scope)   { return null ; }
        @Override
        public void clear() { }
    }

    
    // ======== Node Allocators
    // Adapter class from  MapWithScope.Allocator to BlankNodeAllocator
    // This does not take scope into account when creating nodes.
    
    private static class Alloc implements Allocator<String, Node, Node> {
        final BlankNodeAllocator alloc ;
        
        Alloc(BlankNodeAllocator alloc)     { this.alloc = alloc ; }
        
        @Override
        public Node alloc(Node scope, String label)     { return alloc.alloc(label) ; }

        @Override
        public Node create()                { return alloc.create() ; }

        @Override
        public void reset()                 { alloc.reset() ; }
    }
    
    /** Allocate a fresh blank node each time. */
    private static class AllocByGraph implements Allocator<String, Node, Node> {
        BlankNodeAllocator dft = make() ;
        Map<Node, BlankNodeAllocator> graphs = new HashMap<>() ;
        
        @Override public Node alloc(Node scope, String label)
        { 
            if ( scope == null)
                return dft.alloc(label) ;
            
            BlankNodeAllocator alloc = graphs.get(scope) ;
            if ( alloc == null ) {
                alloc = make() ;
                graphs.put(scope, alloc) ;
            }
            return alloc.alloc(label) ;
        }
        
        @Override public Node create()                  { return dft.create() ; }
        @Override public void reset()                   
        { 
            graphs.clear() ;
            dft.reset() ;
        }
        
        private BlankNodeAllocator make() { return new BlankNodeAllocatorHash() ; }
    }

    
    // TODO Switch to BlankNodeAllocator and a single wrapper.
    // variables if the allocator is reusable across runs
    // classes if a new one is needed each time.
    // Shared ones must be thread-safe.
    

//    /** Allocate bnode using a per-run seed and the label presented.
//     *  This is the most scalable, always legal allocator.
//     *  Not thread safe - not reusable.
//     *  Create a new allocator for each parser run. 
//     */  
//    private static class AllocNodeHash implements Allocator<String, Node> {
//        private BlankNodeAllocator alloc = new BlankNodeAllocatorHash() ;
//        
//        @Override public Node alloc(String label)   { return alloc.alloc(label) ; }
//        @Override public Node create()              { return alloc.create() ; }
//        @Override public void reset()               { alloc.reset() ; }
//    } ;
//    
//    
//
//    /** Allocate bnodes using an incremental counter - deterministic for parsing a given file */ 
//    private static Allocator<String, Node> nodeMakerDeterministic  = new Allocator<String, Node>()
//    {
//        private AtomicLong counter = new AtomicLong(0) ;
//
//        @Override
//        public Node alloc(Node scope, String label) {
//            return create() ;
//        }
//
//        @Override
//        public Node create() {
//            String $ = format("B0x%04X", counter.incrementAndGet()) ;
//            return NodeFactory.createAnon(new AnonId($)) ;
//        }
//
//        @Override
//        public void reset()     {}
//    } ;
//    
//    private static Allocator<String, Node> nodeMakerByLabel = new Allocator<String, Node>()
//    {
//        private AtomicLong counter = new AtomicLong(0) ;
//        
//        @Override
//        public Node alloc(String label)
//        {
//            return NodeFactory.createAnon(new AnonId(label)) ;
//        }
//
//        @Override public Node create()      { return alloc(null, SysRIOT.BNodeGenIdPrefix+(counter.getAndIncrement())) ; } 
//        
//        @Override
//        public void reset()     {}
//    } ;
//    
//    private static Allocator<String, Node> nodeMakerByLabelEncoded = new Allocator<String, Node>()
//    {
//        private AtomicLong counter = new AtomicLong(0) ;
//        
//        @Override
//        public Node alloc(Node scope, String label)
//        {
//            return NodeFactory.createAnon(new AnonId(NodeFmtLib.decodeBNodeLabel(label))) ;
//        }
//
//        @Override public Node create()      { return alloc(SysRIOT.BNodeGenIdPrefix+(counter.getAndIncrement())) ; }
//        @Override
//        
//        public void reset()     {}
//    } ;
}

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

package com.hp.hpl.jena.tdb.solver;


import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.* ;
import org.apache.jena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.tdb.store.NodeId;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.nodetupletable.NodeTupleTable ;

public class StageMatchTuple extends RepeatApplyIterator<BindingNodeId>
{
    private final NodeTupleTable nodeTupleTable ;
    private final Tuple<Node> patternTuple ;

    private final ExecutionContext execCxt ;
    private boolean anyGraphs ;
    private Filter<Tuple<NodeId>> filter ;

    public StageMatchTuple(NodeTupleTable nodeTupleTable, Iterator<BindingNodeId> input, 
                            Tuple<Node> tuple, boolean anyGraphs, 
                            Filter<Tuple<NodeId>> filter, 
                            ExecutionContext execCxt)
    {
        super(input) ;
        this.filter = filter ;
        this.nodeTupleTable = nodeTupleTable ; 
        this.patternTuple = tuple ;
        this.execCxt = execCxt ;
        this.anyGraphs = anyGraphs ; 
    }

    /** Prepare a pattern (tuple of nodes), and an existing binding of NodeId, into NodeIds and Variables. 
     *  A variable in the pattern is replaced by its binding or null in the Nodeids.
     *  A variable that is not bound by the binding is placed in the var array.
     */
    public static void prepare(NodeTable nodeTable, Tuple<Node> patternTuple, BindingNodeId input, NodeId ids[], Var[] var)
    {
        // Process the Node to NodeId conversion ourselves because
        // we wish to abort if an unknown node is seen.
        for ( int i = 0 ; i < patternTuple.size() ; i++ )
        {
            Node n = patternTuple.get(i) ;
            // Substitution and turning into NodeIds
            // Variables unsubstituted are null NodeIds
            NodeId nId = idFor(nodeTable, input, n) ;
            if ( NodeId.isDoesNotExist(nId) )
                new NullIterator<BindingNodeId>() ;
            ids[i] = nId ;
            if ( nId == null )
                var[i] = asVar(n) ;
        }
    }
    
    @Override
    protected Iterator<BindingNodeId> makeNextStage(final BindingNodeId input)
    {
        // ---- Convert to NodeIds 
        NodeId ids[] = new NodeId[patternTuple.size()] ;
        // Variables for this tuple after subsitution
        final Var[] var = new Var[patternTuple.size()] ;

        prepare(nodeTupleTable.getNodeTable(), patternTuple, input, ids, var) ;
        
        Iterator<Tuple<NodeId>> iterMatches = nodeTupleTable.find(Tuple.create(ids)) ;  
        
        // ** Allow a triple or quad filter here.
        if ( filter != null )
            iterMatches = Iter.filter(iterMatches, filter) ;
        
        // If we want to reduce to RDF semantics over quads,
        // we need to reduce the quads to unique triples. 
        // We do that by having the graph slot as "any", then running
        // through a distinct-ifier. 
        // Assumes quads are GSPO - zaps the first slot.
        // Assumes that tuples are not shared.
        if ( anyGraphs )
        {
            iterMatches = Iter.operate(iterMatches, quadsToAnyTriples) ;
            // If any slots were set, then the index would be ???G and we can use distinctAdjacent.
            // If all slots are unset, the index is probably GSPO (SPOG would be better in this one case). 
            // This is a safe, if potentially costly, choice. 
            
            //Guaranteed 
            //iterMatches = Iter.distinct(iterMatches) ;
            
            // This depends on the way indexes are choose and
            // the indexing pattern. It assumes that the index 
            // chosen ends in G so same triples are adjacent 
            // in a union query.
            // If any slot is defined, then the index will be X??G.
            // if no slot is defined, then the index will be ???G.
            //  See TupleTable.scanAllIndex that ensures the latter.
            //  The former assumes indexes are either G... or ...G.
            //  No G part way through.
            iterMatches = Iter.distinctAdjacent(iterMatches) ;
        }
        
        // Map Tuple<NodeId> to BindingNodeId
        Transform<Tuple<NodeId>, BindingNodeId> binder = new Transform<Tuple<NodeId>, BindingNodeId>()
        {
            @Override
            public BindingNodeId convert(Tuple<NodeId> tuple)
            {
                BindingNodeId output = new BindingNodeId(input) ;
                for ( int i = 0 ; i < var.length ; i++ )
                {
                    Var v = var[i] ;
                    if ( v == null )
                        continue ;
                    NodeId id = tuple.get(i) ;
                    if ( reject(output, v, id) )
                        return null ;
                    output.put(v, id) ;
                }
                return output ;
            }
        } ;
        
        return Iter.iter(iterMatches).map(binder).removeNulls() ;
    }
    
   
    // -- Copying
    private static Transform<Tuple<NodeId>,Tuple<NodeId>> projectToTriples = new Transform<Tuple<NodeId>,Tuple<NodeId>>(){
        @Override
        public Tuple<NodeId> convert(Tuple<NodeId> item)
        {
            // Zap graph node id.
            Tuple<NodeId> t2 = Tuple.createTuple(NodeId.NodeIdAny,    // Can't be null - gets bound to a daft variable.
                                                 item.get(1),
                                                 item.get(2),
                                                 item.get(3)) ;
            return t2 ;
        } } ;
    
    
    private static Iterator<Tuple<NodeId>> print(Iterator<Tuple<NodeId>> iter)
    {
        if ( ! iter.hasNext() )
            System.err.println("<empty>") ;
        else
        {
            List<Tuple<NodeId>> r = Iter.toList(iter) ;
            String str = Iter.asString(r, "\n") ;
            System.err.println(str) ;
            // Reset iter
            iter = Iter.iter(r) ;
        }
        return iter ;
    }
    
    private static boolean reject(BindingNodeId output , Var var, NodeId value)
    {
        if ( ! output.containsKey(var) )
            return false ;
        
        if ( output.get(var).equals(value) )
            return false ;

        return true ;
    }
    
    private static Var asVar(Node node)
    {
        if ( Var.isVar(node) )
            return Var.alloc(node) ;
        return null ;
    }

    /** Return null for variables, and for nodes, the node id or NodeDoesNotExist */
    private static NodeId idFor(NodeTable nodeTable, BindingNodeId input, Node node)
    {
        if ( Var.isVar(node) )
        {
            NodeId n = input.get((Var.alloc(node))) ;
            // Bound to NodeId or null. 
            return n ;
        } 
        // May return NodeId.NodeDoesNotExist which must not be null. 
        return nodeTable.getNodeIdForNode(node) ;
    }
    
    // -- Mutating "transform in place"
    private static Action<Tuple<NodeId>> quadsToAnyTriples = new Action<Tuple<NodeId>>(){
        @Override
        public void apply(Tuple<NodeId> item)
        { item.tuple()[0] = NodeId.NodeIdAny ; }
    } ;
}

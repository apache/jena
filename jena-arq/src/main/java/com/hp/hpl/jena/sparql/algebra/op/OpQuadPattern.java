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

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.QuadPattern ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** The main Op used in converting SPARQL algebra to quad form.
 * OpQuadPattern reflects the fact that quads come in per-GRAPH units. 
 * {@linkplain OpQuadBlock} is for a general containers of quads
 * without any contract on the quads sharing the same garph node.   
 */
public class OpQuadPattern extends Op0
{
    public static boolean isQuadPattern(Op op)
    {
        return (op instanceof OpQuadPattern ) ;
    }
    
    private Node graphNode ;
    private BasicPattern triples ;
    
    private QuadPattern quads = null ;
    
    // A QuadPattern is a block of quads with the same graph arg.
    // i.e. a BasicGraphPattern.

    // Match switch so OpQuadPattern is a 
    // a sequence of OpQuadBlocks.
    
    public OpQuadPattern(Node quadNode, BasicPattern triples)
    { 
        this.graphNode = quadNode ;
        this.triples = triples ;
    }
    
    private void initQuads()
    {
        if ( quads == null )
        {
            quads = new QuadPattern() ;
            for (Triple t : triples )
                quads.add(new Quad(graphNode, t)) ;
        }
    }
    
    public QuadPattern getPattern()
    {
        initQuads() ;
        return quads ;
    } 
    
    public Node getGraphNode()              { return graphNode ; } 
    public BasicPattern getBasicPattern()   { return triples ; }
    public boolean isEmpty()                { return triples.size() == 0 ; }
    
    /** Is this quad pattern referring to the default graph by quad transformation or explict naming? */ 
    public boolean isDefaultGraph()         { return Quad.isDefaultGraph(graphNode) ; }
    
    /** Is this quad pattern explicitly naming the default graph? */ 
    public boolean isExplicitDefaultGraph() { return Quad.isDefaultGraphExplicit(graphNode) ; }
    /** Is this quad pattern explicitly naming the union graph? */
    
    public boolean isUnionGraph()           { return Quad.isUnionGraph(graphNode) ; }
    
    @Override
    public String getName()                 { return Tags.tagQuadPattern ; }
    @Override
    public Op apply(Transform transform)    { return transform.transform(this) ; } 
    @Override
    public void visit(OpVisitor opVisitor)  { opVisitor.visit(this) ; }
    @Override
    public Op0 copy()                        { return new OpQuadPattern(graphNode, triples) ; }

    @Override
    public int hashCode()
    { return graphNode.hashCode() ^ triples.hashCode() ; }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! ( other instanceof OpQuadPattern ) ) return false ;
        OpQuadPattern opQuad = (OpQuadPattern)other ;
        if ( ! graphNode.equals(opQuad.graphNode) )
            return false ;
        return triples.equiv(opQuad.triples, labelMap) ;
    }

}

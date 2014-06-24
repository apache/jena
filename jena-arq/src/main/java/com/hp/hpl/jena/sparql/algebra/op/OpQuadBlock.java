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

import java.util.ArrayList ;
import java.util.List ;

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

/** A list of quads. 
 * 
 * <code>OpQuadBlock</code> is anny colelction of quads, whereas
 * {@linkplain OpQuadPattern} is quads with the same graph node.  
 * The flip in naming is historical. 
 */
public class OpQuadBlock extends Op0
{
    public static boolean isQuadBlock(Op op)
    {
        return (op instanceof OpQuadBlock ) ;
    }
    
    private final QuadPattern quads  ;
    
    // A QuadPattern is a block of quads with the same graph arg.
    // i.e. a BasicGraphPattern.  This gets the blank node scoping right.
    
    // Quads are for a specific quad store.
    
    // Later, we may introduce OpQuadBlock for this and OpQuadPattern becomes
    // a sequence of such blocks.
    
    
    
    public static OpQuadBlock create(Node quadNode, BasicPattern triples) {
        QuadPattern qp = new QuadPattern() ;
        for ( Triple t : triples ) {
            qp.add(new Quad(quadNode, t)) ;
        }
        return new OpQuadBlock(qp) ;
    }
    
    public OpQuadBlock() { quads = new QuadPattern() ; }
    public OpQuadBlock(QuadPattern quads) { this.quads = quads ; }
    public OpQuadBlock(OpQuadPattern quadPattern) { this.quads = quadPattern.getPattern() ; }
    
    public QuadPattern getPattern() {
        return quads ;
    } 
    
    public boolean isEmpty()                { return quads.size() == 0 ; }
    
    @Override
    public String getName()                 { return Tags.tagQuadBlock ; }
    @Override
    public Op apply(Transform transform)    { return transform.transform(this) ; } 
    @Override
    public void visit(OpVisitor opVisitor)  { opVisitor.visit(this) ; }
    @Override
    public Op0 copy()                       { return new OpQuadBlock(quads) ; }

    public List<OpQuadPattern> convert()    {
        List<OpQuadPattern> x = new ArrayList<>() ;
        Node gn = null ;
        BasicPattern bgp = null ;
        
        for ( Quad q : quads ) {
            if ( gn == null || ! gn.equals(q.getGraph()) ) {
                if ( gn != null )
                    x.add(new OpQuadPattern(gn, bgp)) ;
                gn = q.getGraph() ;
                bgp = new BasicPattern() ;
            }
            bgp.add(q.asTriple());
        }
        x.add(new OpQuadPattern(gn, bgp)) ;
        return x ;
    }
    
    /** Convenience - convert to OpQuadPatterns which are more widely used (currently?) */ 
    public Op convertOp()    {
        if ( quads.size() == 0 )
            return  OpTable.empty() ;
        
        if ( quads.size() == 1 )
        {
            Quad q = quads.get(0) ; 
            BasicPattern bgp = new BasicPattern() ;
            bgp.add(q.asTriple()) ;
            return new OpQuadPattern(q.getGraph(), bgp) ;
        }

        List<OpQuadPattern> x = convert() ;
        OpSequence ops = OpSequence.create() ;
        for ( OpQuadPattern oqp : x )
            ops.add(oqp);
        return ops ;
    }
    
    @Override
    public int hashCode()
    { 
        int calcHashCode = OpBase.HashBasicGraphPattern ;
        calcHashCode ^=  quads.hashCode() ; 
        return calcHashCode ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! ( other instanceof OpQuadBlock ) ) return false ;
        OpQuadBlock opQuad = (OpQuadBlock)other ;
        return quads.equiv(opQuad.quads, labelMap) ;
    }

}

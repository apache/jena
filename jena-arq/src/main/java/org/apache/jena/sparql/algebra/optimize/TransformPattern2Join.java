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

package org.apache.jena.sparql.algebra.optimize;

import java.util.List ;

import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.TransformCopy ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.core.QuadPattern ;

/** Expand to joins of triples and quads. */ 
public class TransformPattern2Join extends TransformCopy
{
    /*
     * Get standard shaped trees?
     * Alternative is hard flattening to (sequence of all the quads, triples) 
     */
    
    @Override
    public Op transform(OpBGP opBGP)                        { return expand(opBGP.getPattern()) ; }
    
    @Override
    public Op transform(OpQuadPattern quadPattern)          { return expand(quadPattern.getPattern()) ; }

    @Override
    public Op transform(OpSequence opSeq, List<Op> elts)    { return expand(opSeq, elts) ; }

//    @Override
//    public Op transform(OpJoin opJoin, Op left, Op right)
//    { return super.transform(opJoin, left, right) ; }

    private static Op expand(BasicPattern bgp)
    {
        if ( bgp.getList().isEmpty() )
            return OpTable.unit() ;
        Op op = null ;
        for ( Triple t : bgp.getList() )
        {
            OpTriple x = new OpTriple(t) ;
            op = join(op, x) ;
        }
        return op ;
    }
    
    private static Op expand(QuadPattern quads)
    {
        if ( quads.getList().isEmpty() )
            return OpTable.unit() ;
        Op op = null ;
        for ( Quad q : quads.getList() )
        {
            OpQuad x = new OpQuad(q) ;
            op = join(op, x) ;
        }
        return op ;
    }
    
    private static Op expand(OpSequence opSeq, List<Op> elts)
    {
        Op x = null ;
        // shape choices.
        for ( Op op : elts )
            x = join(x, op) ;
        return x ;
    }

    private static Op join(Op left, Op right)
    {
      return OpJoin.createReduce(left, right) ;
  }
}

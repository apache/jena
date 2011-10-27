/**
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

package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.QuadPattern ;

public class TransformPattern2Join extends TransformCopy
{
    @Override
    public Op transform(OpBGP opBGP)                        { return expand(opBGP.getPattern()) ; }
    
    @Override
    public Op transform(OpQuadPattern quadPattern)          { return expand(quadPattern.getPattern()) ; }

    public static Op expand(BasicPattern bgp)
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
    
    public static Op expand(QuadPattern quads)
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
    
    
    private static Op join(Op left, Op right)
    {
        if ( left == null || OpJoin.isJoinIdentify(left) )
          return right ;
      if ( right == null || OpJoin.isJoinIdentify(right) )
          return left ;
      return OpJoin.create(left, right) ;
  }
}

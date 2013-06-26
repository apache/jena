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

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.QuadPattern ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.Iso ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** Algebra operation for a single quad.
 * @see OpTriple
 */ 
public class OpQuad extends Op0
{
    private final Quad quad ;
    private OpQuadPattern opQuadPattern = null ;
    
    public OpQuad(Quad quad)
    {
        this.quad = quad ;
    }
    
    public final Quad getQuad() { return quad ; }
    

    public OpQuadPattern asQuadPattern()
    {
        if ( opQuadPattern == null )
        {
            BasicPattern bp = new BasicPattern() ;
            bp.add(getQuad().asTriple()) ;
            opQuadPattern = new OpQuadPattern(quad.getGraph(),bp) ;
        }
        return opQuadPattern ;
    }
    
    @Override
    public Op apply(Transform transform)
    { return transform.transform(this) ; }

    @Override
    public Op0 copy()
    {
        return new OpQuad(quad) ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! (other instanceof OpQuad) )
            return false ;
        OpQuad opQuad = (OpQuad)other ;
        return Iso.quadIso(getQuad(), opQuad.getQuad(), labelMap) ;
    }

    @Override
    public int hashCode()
    {
        return OpBase.HashTriple ^ quad.hashCode() ;
    }

    @Override
    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }

    @Override
    public String getName()
    {
        return Tags.tagTriple ;
    }

    public boolean equivalent(OpQuadPattern opQuads)
    {
        QuadPattern quads = opQuads.getPattern() ;
        if ( quads.size() != 1 ) return false ;
        Quad q = quads.get(0) ;
        return quad.equals(q) ;  
    }
}

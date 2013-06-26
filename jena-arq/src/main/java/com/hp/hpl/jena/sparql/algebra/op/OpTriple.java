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

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.Iso ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** Algebra operation for a single triple.  Not normally used - triples are
 * contained in basic graph patterns (which is the unit of extension in SPARQL, 
 * and also the unit for adapting to other data store in ARQ).  But for
 * experimentation, it can be useful to have a convenience direct triple access.

 * @see OpBGP
 */ 
public class OpTriple extends Op0
{
    private final Triple triple ;
    private OpBGP opBGP = null ;
    
    public OpTriple(Triple triple)
    {
        this.triple = triple ;
    }
    
    public final Triple getTriple() { return triple ; }
    
    public final OpBGP asBGP()
    { 
        if ( opBGP == null )
        {
            BasicPattern bp = new BasicPattern() ;
            bp.add(getTriple()) ;
            opBGP = new OpBGP(bp) ;
        }
        return opBGP ;
    }        
    
    @Override
    public Op apply(Transform transform)
    { return transform.transform(this) ; }

    @Override
    public Op0 copy()
    {
        return new OpTriple(triple) ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! (other instanceof OpTriple) )
            return false ;
        OpTriple opTriple = (OpTriple)other ;
        return Iso.tripleIso(getTriple(), opTriple.getTriple(), labelMap) ;
    }

    @Override
    public int hashCode()
    {
        return OpBase.HashTriple ^ triple.hashCode() ;
    }

    @Override
    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }

    @Override
    public String getName()
    {
        return Tags.tagTriple ;
    }

    public boolean equivalent(OpBGP opBGP)
    {
        BasicPattern bgp = opBGP.getPattern() ;
        if ( bgp.size() != 1 ) return false ;
        Triple t = bgp.get(0) ;
        return triple.equals(t) ;  
    }

}

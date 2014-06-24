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
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public abstract class OpN extends OpBase
{ 
    private List<Op> elements = new ArrayList<>() ;
    
    protected OpN()         { elements = new ArrayList<>() ; }
    protected OpN(List<Op> x)   { elements = x ; }
    
    /** Accumulate an op in the OpN.
     *  This exists to help building OpN in teh first place.
     *  Once built, an OpN, like any Op should be treated as immutable
     *  with no calls change the sub ops contents.
     *  No calls to .add.
     */
    public void add(Op op) { elements.add(op) ; }
    public Op get(int idx) { return elements.get(idx) ; }
    
    public abstract Op apply(Transform transform, List<Op> elts) ;
    public abstract OpN copy(List<Op> elts) ;

    // Tests the sub-elements for equalTo.
    protected boolean equalsSubOps(OpN op, NodeIsomorphismMap labelMap)
    {
        if (elements.size() != op.elements.size() )
            return false ;
        
        Iterator<Op> iter1 = elements.listIterator() ;
        Iterator<Op> iter2 = op.elements.listIterator() ;
        
        for ( ; iter1.hasNext() ; )
        {
            Op op1 = iter1.next();
            Op op2 = iter2.next();
            if ( ! op1.equalTo(op2, labelMap) )
                return false ;
        }
        return true ;
    }

    public int size()                   { return elements.size() ; } 

    
    @Override
    public int hashCode()               { return elements.hashCode() ; } 

    public List<Op> getElements()           { return elements ; }

    public Iterator<Op> iterator()          { return elements.iterator() ; }
}

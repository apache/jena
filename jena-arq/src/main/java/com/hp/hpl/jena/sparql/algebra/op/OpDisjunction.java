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

import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** N-way disjunction.
 *  OpUnion remains as the strict SPARQL algebra operator.
 */
public class OpDisjunction extends OpN
{
    public static OpDisjunction create() { return new OpDisjunction() ; }
    
    public static Op create(Op left, Op right)
    {
        // Avoid stages of nothing
        if ( left == null && right == null )
            return null ;
        // Avoid stages of one.
        if ( left == null )
            return right ;
        if ( right == null )
            return left ;

        if ( left instanceof OpDisjunction )
        {
            OpDisjunction opDisjunction = (OpDisjunction)left ;
            opDisjunction.add(right) ;
            return opDisjunction ; 
        }
        
//        if ( right instanceof OpDisjunction )
//        {
//            OpDisjunction opSequence = (OpDisjunction)right ;
//            // Add front.
//            opDisjunction.getElements().add(0, left) ;
//            return opDisjunction ; 
//        }
        
        OpDisjunction stage = new OpDisjunction() ;
        stage.add(left) ;
        stage.add(right) ;
        return stage ;
    }

    private OpDisjunction(List<Op> elts) { super(elts) ; }
    private OpDisjunction() { super() ; }
    
    @Override
    public String getName() { return Tags.tagDisjunction ; }
    
    @Override
    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }
    
    @Override
    public boolean equalTo(Op op, NodeIsomorphismMap labelMap)
    {
        if ( ! ( op instanceof OpDisjunction) ) return false ;
        OpDisjunction other = (OpDisjunction) op ;
        return super.equalsSubOps(other, labelMap) ;
    }

    
    
    @Override
    public Op apply(Transform transform, List<Op> elts)
    { return transform.transform(this, elts) ; }

    @Override
    public OpN copy(List<Op> elts)
    {
        return new OpDisjunction(elts) ; 
    }
}

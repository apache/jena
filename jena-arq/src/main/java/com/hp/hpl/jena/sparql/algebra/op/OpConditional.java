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
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** Conditional execution - works with streamed execution and is known to safe to
 *  evaluate that way (no issues from nested optionals). 
 *  For each element in the input stream, 
 *  execute the expression (i.e. index-join it to the element
 *  in the input stream).  If it matches, return those results.
 *  If it does not, return the input stream element. */
public class OpConditional extends Op2
{
    public OpConditional(Op left, Op right)
    {
        super(left, right) ;
    }

    @Override
    public Op apply(Transform transform, Op left, Op right)
    { return transform.transform(this, left, right) ; }
        
    @Override
    public void visit(OpVisitor opVisitor) 
    { opVisitor.visit(this) ; }
    
    @Override
    public Op2 copy(Op newLeft, Op newRight)
    { return new OpConditional(newLeft, newRight) ; }
    
    @Override
    public boolean equalTo(Op op2, NodeIsomorphismMap labelMap)
    {
        if ( ! ( op2 instanceof OpConditional) ) return false ;
        return super.sameArgumentsAs((OpConditional)op2, labelMap) ;
    }
    
    @Override
    public String getName()
    {
        return Tags.tagConditional ;
    }

}

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

package org.apache.jena.sparql.algebra.op;

import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.Transform ;
import org.apache.jena.sparql.sse.Tags ;
import org.apache.jena.sparql.util.NodeIsomorphismMap ;

public class OpDiff extends Op2
{
    public static Op create(Op left, Op right)
    { 
        return new OpDiff(left, right) ;
    }
    
    private OpDiff(Op left, Op right) { super(left, right) ; }
    
    @Override
    public String getName() { return Tags.tagDiff ; }

    @Override
    public Op apply(Transform transform, Op left, Op right)
    { return transform.transform(this, left, right) ; }
        
    @Override
    public void visit(OpVisitor opVisitor) { opVisitor.visit(this) ; }
    @Override
    public Op2 copy(Op newLeft, Op newRight)
    { return new OpDiff(newLeft, newRight) ; }
    
    @Override
    public boolean equalTo(Op op2, NodeIsomorphismMap labelMap)
    {
        if ( ! ( op2 instanceof OpDiff) ) return false ;
        return super.sameArgumentsAs((Op2)op2, labelMap) ;
    }
}

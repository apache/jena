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
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.table.TableUnit ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class OpJoin extends Op2
{
    /** Create join - an argument of null is 
     * simply dropped so Join.create(null, op) is op and Join.create(op,null) is op.
     */
    public static Op create(Op left, Op right)
    {
        if ( left == null )
            return right ;
        if ( right == null )
            return left ;
        return new OpJoin(left, right) ;
    }
    
    /** Create join, removing any joins with the identity table and any nulls.
     *  <br>Join.create(null, op) is op.
     *  <br>Join.create(op, null) is op.
     *  <br>Join.create(TableUnit, op) is op.
     *  <br>Join.create(op, TableUnit) is op.
     */
    public static Op createReduce(Op left, Op right)
    {
        if ( left == null || isJoinIdentify(left) )
            return right ;
        if ( right == null || isJoinIdentify(right) )
            return left ;
        return new OpJoin(left, right) ;
    }
    

    public static boolean isJoinIdentify(Op op)
    {
        if ( ! ( op instanceof OpTable ) )
            return false ;
        Table t = ((OpTable)op).getTable() ;
        // Safe answer.
        return TableUnit.isTableUnit(t) ;
    }
    
    private OpJoin(Op left, Op right) { super(left, right) ; }
    
    @Override
    public String getName() { return Tags.tagJoin ; }

    @Override
    public Op apply(Transform transform, Op left, Op right)
    { return transform.transform(this, left, right) ; }
        
    @Override
    public void visit(OpVisitor opVisitor) { opVisitor.visit(this) ; }
    
    @Override
    public Op2 copy(Op newLeft, Op newRight)
    { return new OpJoin(newLeft, newRight) ; }
    
    @Override
    public boolean equalTo(Op op2, NodeIsomorphismMap labelMap)
    {
        if ( ! ( op2 instanceof OpJoin) ) return false ;
        return super.sameArgumentsAs((Op2)op2, labelMap) ;
    }

}

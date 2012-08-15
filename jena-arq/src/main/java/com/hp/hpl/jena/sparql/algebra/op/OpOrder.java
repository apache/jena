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

import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class OpOrder extends OpModifier
{
    private List<SortCondition> conditions ;
    public OpOrder(Op subOp, List<SortCondition> conditions)
    { 
        super(subOp) ;
        this.conditions = conditions ;
    }
    
    public List<SortCondition> getConditions() { return conditions ; }
    
    @Override
    public String getName()                 { return Tags.tagOrderBy ; }
    @Override
    public void visit(OpVisitor opVisitor)  { opVisitor.visit(this) ; }
    @Override
    public Op1 copy(Op subOp)                { return new OpOrder(subOp, conditions) ; }

    @Override
    public Op apply(Transform transform, Op subOp)
    { return transform.transform(this, subOp) ; }
    
    @Override
    public int hashCode()
    {
        return conditions.hashCode() ^ getSubOp().hashCode() ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! (other instanceof OpOrder) ) return false ;
        OpOrder opOrder = (OpOrder)other ;
        
        if ( ! opOrder.getConditions().equals(this.getConditions()) )
            return false ;
        
        //
        return getSubOp().equalTo(opOrder.getSubOp(), labelMap) ;
    }


}

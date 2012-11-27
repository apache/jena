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

import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.ExprAggregator ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class OpGroup extends Op1
{
    private VarExprList groupVars ;
    private List<ExprAggregator> aggregators ;

    public OpGroup(Op subOp, VarExprList groupVars, List<ExprAggregator> aggregators)
    { 
        super(subOp) ;
        this.groupVars  = groupVars ;
        this.aggregators = aggregators ;
    }
    
    @Override
    public String getName()                     { return Tags.tagGroupBy ; }
    public VarExprList getGroupVars()           { return groupVars ; }
    public List<ExprAggregator> getAggregators()  { return aggregators ; }

    @Override
    public void visit(OpVisitor opVisitor)      { opVisitor.visit(this) ; }
    @Override
    public Op1 copy(Op subOp)                    { return new OpGroup(subOp, groupVars, aggregators) ; }

    @Override
    public Op apply(Transform transform, Op subOp)
    { return transform.transform(this, subOp) ; }

    @Override
    public int hashCode()
    { 
        int x = getSubOp().hashCode() ;
        if ( groupVars != null ) 
            x ^= groupVars.hashCode() ; 
        if ( aggregators != null ) 
            x ^= aggregators.hashCode() ; 
        return x ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! (other instanceof OpGroup) ) return false ;
        OpGroup opGroup = (OpGroup)other ;
        if ( ! Lib.equal(groupVars, opGroup.groupVars) ) 
            return false ;
        if ( ! Lib.equal(aggregators, opGroup.aggregators) )
            return false ;
            
        return getSubOp().equalTo(opGroup.getSubOp(), labelMap) ;
    }

}

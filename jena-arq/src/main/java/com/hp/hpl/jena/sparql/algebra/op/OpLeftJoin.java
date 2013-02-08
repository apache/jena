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
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class OpLeftJoin extends Op2
{
    ExprList expressions = null ;

    /** Guaranteed to return a new left join*/
    public static OpLeftJoin createLeftJoin(Op left, Op right, ExprList exprs)
    { 
        return new OpLeftJoin(left, right, exprs) ;
    }

    public static Op create(Op left, Op right, ExprList exprs)
    { 
        return new OpLeftJoin(left, right, exprs) ;
    }
    
    public static Op create(Op left, Op right, Expr expr)
    { 
        return new OpLeftJoin(left, right, expr == null ? null : new ExprList(expr)) ;
    }

    protected OpLeftJoin(Op left, Op right, ExprList exprs) 
    { 
        super(left, right) ;
        expressions = exprs ;
    }
    
    public ExprList getExprs()      { return expressions ; } 
    @Override
    public String getName()         { return Tags.tagLeftJoin ; }
    
    @Override
    public Op apply(Transform transform, Op left, Op right)
    { return transform.transform(this, left, right) ; }
        
    @Override
    public void visit(OpVisitor opVisitor) { opVisitor.visit(this) ; }
    @Override
    public Op2 copy(Op newLeft, Op newRight)
    { return new OpLeftJoin(newLeft, newRight, expressions) ; }
    
    @Override
    public boolean equalTo(Op op2, NodeIsomorphismMap labelMap)
    {
        if ( ! ( op2 instanceof OpLeftJoin) ) return false ;
        return super.sameArgumentsAs((Op2)op2, labelMap) ;
    }
}

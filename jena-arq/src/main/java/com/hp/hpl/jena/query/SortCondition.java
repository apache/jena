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

package com.hp.hpl.jena.query;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.expr.E_Function ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprVar ;
import com.hp.hpl.jena.sparql.serializer.FmtExprSPARQL ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.PrintSerializableBase ;


public class SortCondition extends PrintSerializableBase
{
    public Expr expression = null ;
    public int direction = 0 ;

    public SortCondition(Var var, int dir)
    { this(new ExprVar(var),dir) ; } 
  
    public SortCondition(Node var, int dir)
    { this(ExprUtils.nodeToExpr(var), dir) ; }

    public SortCondition(Expr expr, int dir)
    {
        expression = expr ;
        direction = dir ;
        
        if ( dir != Query.ORDER_ASCENDING && dir != Query.ORDER_DESCENDING && dir != Query.ORDER_DEFAULT )
            Log.fatal(this, "Unknown sort direction") ;
    }
    
    public void format(FmtExprSPARQL fmt,
                       IndentedWriter writer)
    {
        boolean explicitDirection = false ;
        // Not always necessary but safe.
        // At this point there must be brackets but some forms (e.g. ?x+?y)
        // are going to put their own brackets in regardless.
        boolean needParens = false ;
        
        if ( direction != Query.ORDER_DEFAULT )
        {
            // Need parens if the expression isn't going to add them anyway.
            if ( expression.isVariable() || expression instanceof E_Function )
                // Bracketless by expression formatting
                needParens = true ;
        }
        
        if ( direction == Query.ORDER_ASCENDING )
        {
            writer.print("ASC") ;
            needParens = true ;
        }
        
        if ( direction == Query.ORDER_DESCENDING )
        {
            writer.print("DESC") ;
            needParens = true ;
        }
        
        if ( needParens )
            writer.print("(") ;
        
        fmt.format(expression) ;
        
        if ( needParens )
            writer.print(")") ;
    }

    /** @return Returns the direction. */
    public int getDirection()
    {
        return direction ;
    }

    /** @return Returns the expression. */
    public Expr getExpression()
    {
        return expression ;
    }

    @Override
    public int hashCode()
    { 
        int x = this.getDirection() ;
        if ( getExpression() != null )
            x ^= getExpression().hashCode() ;
        return x ;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;

        if ( ! ( other instanceof SortCondition ) )
            return false ;
        
        SortCondition sc = (SortCondition)other ;
        
        if ( sc.getDirection() != this.getDirection() )
            return false ;
        
        if ( ! Lib.equal(this.getExpression(), sc.getExpression()) )
            return false ;
        
//        if ( ! Utils.eq(this.getVariable(), sc.getVariable()) )
//            return false ;
        
        return true ;
    }

    @Override
    public void output(IndentedWriter out)
    { 
        out.print(Plan.startMarker) ;
        out.print("SortCondition ") ;
        FmtExprSPARQL fmt = new FmtExprSPARQL(out, null) ;
        format(fmt, out) ;
        out.print(Plan.finishMarker) ;
    }
    
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        if ( sCxt == null )
            sCxt = new SerializationContext() ;
        FmtExprSPARQL fmt = new FmtExprSPARQL(out, sCxt) ;
        format(fmt, out) ;
    }
}

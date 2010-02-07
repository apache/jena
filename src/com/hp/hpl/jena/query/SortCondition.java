/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.serializer.FmtExpr;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.*;


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
            ALog.fatal(this, "Unknown sort direction") ;
    }
    
    public void format(FmtExpr fmt,
                       IndentedWriter writer)
    {
        boolean explicitDirection = false ;
        boolean needParens = false ;
        
        if ( direction != Query.ORDER_DEFAULT )
        {
            // Need parens if the expression isn't going to add them anyway.
            if ( expression.isVariable() || expression instanceof E_Function )
                // Bracketless by expression formatting
                needParens = true ;
        }
        
        if ( direction == Query.ORDER_ASCENDING )
            writer.print("ASC") ;
        
        if ( direction == Query.ORDER_DESCENDING )
            writer.print("DESC") ;
        
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
        
        if ( ! Utils.equal(this.getExpression(), sc.getExpression()) )
            return false ;
        
//        if ( ! Utils.eq(this.getVariable(), sc.getVariable()) )
//            return false ;
        
        return true ;
    }

    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        FmtExpr fmt = new FmtExpr(out, sCxt) ;
        format(fmt, out) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
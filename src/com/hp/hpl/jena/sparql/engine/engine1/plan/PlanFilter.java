/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.engine1.plan;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.engine1.*;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterFilterExpr;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprBuild;
import com.hp.hpl.jena.sparql.expr.ExprWalker;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Utils;


public class PlanFilter extends PlanElement0
{
    ElementFilter element ;
    
    public static PlanElement make(Context context, ElementFilter el)
    { return new PlanFilter(context, el) ; }
    
    private PlanFilter(Context context, ElementFilter el)
    { 
        super(context) ;
        element = el ;
    }
    
    public QueryIterator build(QueryIterator input, ExecutionContext execCxt)
    {
        if ( input == null )
            LogFactory.getLog(this.getClass()).fatal("Null input to "+Utils.classShortName(this.getClass())) ;
        
        // Take a copy.  The exprs can now cache query execution information.
        Expr ex = element.getExpr().deepCopy() ;
        if ( ex == null )
            // Choose not to deepCopy (RDQL)
            ex = element.getExpr() ;
        ExprWalker.walk(new ExprBuild(execCxt.getContext()), ex) ;
        
        return new QueryIterFilterExpr(input, element.getExpr(), execCxt) ;
    }

    public Expr getExpr() { return element.getExpr() ; }

    public void visit(PlanVisitor visitor) { visitor.visit(this) ; }

    public PlanElement apply(Transform transform)  { return transform.transform(this) ; }

    public PlanElement copy()
    {
        return make(getContext(), element) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
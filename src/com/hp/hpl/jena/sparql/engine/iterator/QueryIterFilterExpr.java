/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprException;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Utils;

/** 
 *  Filter a stream of bindings by a constraint.  
 * 
 * @author Andy Seaborne
 * @version $Id: QueryIterFilterExpr.java,v 1.3 2007/02/06 17:06:01 andy_seaborne Exp $
 */

public class QueryIterFilterExpr extends QueryIterFilter
{
    Expr expr ;
    
    public QueryIterFilterExpr(QueryIterator input, Expr expr, ExecutionContext context)
    {
        super(input, context) ;
        this.expr = expr ;
    }
    
    public boolean accept(Binding binding)
    {
        boolean passFilter = false ;
        try {
            //Binding b = new BindingFixed(binding) ;
            Binding b = binding ;
            return expr.isSatisfied(b, super.getExecContext()) ;
        } catch (ExprException ex)
        { // Some evaluation exception
          return false ;
        }
        
        catch (Exception ex)
        {
            ALog.warn(this, "General exception in "+expr, ex) ;
            return false ;
        }
    }

    protected void details(IndentedWriter out, SerializationContext cxt)
    { 
        out.print(Utils.className(this)) ;
        out.print(" ") ;
        ExprUtils.fmtSPARQL(out, expr, cxt.getPrefixMapping()) ;
    }
       
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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

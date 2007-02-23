/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.extension;

import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply;
import com.hp.hpl.jena.sparql.expr.Expr;

/** Extension base class for extensions that deal with the incoming
 *  input stream of bindings one by one and access the unevaluated arguments.  
 * 
 * @author Andy Seaborne
 * @version $Id: ExtensionBase.java,v 1.19 2007/02/06 17:05:43 andy_seaborne Exp $
 */ 

public abstract class ExtensionBase implements Extension
{
    String uri = null ;
    List args ;
    private ExecutionContext execCxt ;
    
    public QueryIterator exec(QueryIterator input, List args, String uri, ExecutionContext execCxt)
    {
        this.uri = uri ;
        this.args = args ;
        this.execCxt = execCxt ;
        
        // Make a repeat apply.
        // Could generalize the repeat-apply pattern if we had generics.
        // We don't want the logging or the mark or execution context.
        // ?? Make QueryIteratorBase plain with .setTracking(Mark, Log, Context)  
        // ?? Make repeat apply not use QueryIteratorBase
        
        return new RepeatApplyIterator(input) ;
    }

    public abstract QueryIterator execUnevaluated(List args, Binding binding, ExecutionContext execCxt) ;
    
    public ExecutionContext getExecutionContext() { return execCxt ; }
    
    class RepeatApplyIterator extends QueryIterRepeatApply
    {
       public RepeatApplyIterator(QueryIterator input)
       { super(input, getExecutionContext()) ; }

        //@Override
        protected QueryIterator nextStage(Binding binding)
        {
            QueryIterator iter = execUnevaluated(args, binding, getExecutionContext()) ;
            return iter ;
        }
    }
    
    protected Node evalIfPossible(Expr expr, Binding binding, ExecutionContext execCxt)
    {
        if ( expr.isConstant() )
            return expr.getConstant().getNode() ;
        
        if ( expr.isVariable() )
        {
            Var v = Var.alloc(expr.getNodeVar()) ;
            if ( binding.contains(v) )
                return binding.get(v) ;
            // Drop through
        }
        return null ;
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
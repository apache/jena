/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.core.ARQInternalErrorException;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.expr.ExprEvalException;
import com.hp.hpl.jena.query.expr.NodeValue;
import com.hp.hpl.jena.query.util.Context;

/** Interface to value-testing extensions to the expression evaluator.
 * 
 * @author Andy Seaborne
 * @version $Id: FunctionBase.java,v 1.21 2007/02/06 17:06:08 andy_seaborne Exp $
 */

public abstract class FunctionBase implements Function
{
    String uri = null ;
    protected List arguments = null ;
    protected Binding currentBinding = null ; 
    private Context context ;
    
    public final void build(String uri, List args)
    {
        this.uri = uri ;
        arguments = args ;
        checkBuild(uri, args) ;
    }

    public NodeValue exec(Binding binding, List args, String uri, Context cxt)
    {
        this.context = cxt ;
        
        if ( args == null )
            // The contract on the function interface is that this should not happen.
            throw new ARQInternalErrorException("FunctionBase: Null args list") ;
        
        List evalArgs = new ArrayList() ;
        for ( Iterator iter = args.listIterator() ; iter.hasNext() ; )
        {
            Expr e = (Expr)iter.next() ;
            
            try {
                NodeValue x = e.eval(binding, cxt) ;
                evalArgs.add(x) ;
            } catch (ExprEvalException ex)
            {
                throw ex ;
            }
        }
        
        currentBinding = binding ;
        NodeValue nv =  exec(evalArgs) ;
        currentBinding = null ;
        arguments = null ;
        return nv ;
    }
    
    /** Return the Context object for this execution */
    public Context getContext() { return context ; }
    
    /** Function call to a list of evaluated argument values */ 
    public abstract NodeValue exec(List args) ;

    public abstract void checkBuild(String uri, List args) ;
    
    /** Get argument, indexing from 1 **/
    public NodeValue getArg(int i)
    {
        i = i-1 ;
        if ( i < 0 || i >= arguments.size()  )
            return null ;
        return (NodeValue)arguments.get(i) ;
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
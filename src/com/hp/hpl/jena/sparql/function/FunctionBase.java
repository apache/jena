/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.function;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Interface to value-testing extensions to the expression evaluator. */

public abstract class FunctionBase implements Function
{
    String uri = null ;
    protected ExprList arguments = null ;
    private FunctionEnv env ;
    
    public final void build(String uri, ExprList args)
    {
        this.uri = uri ;
        arguments = args ;
        checkBuild(uri, args) ;
    }

    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env)
    {
        // This is merely to allow functions to be 
        // It duplicates code in E_Function/ExprFunctionN.
        
        this.env = env ;
        
        if ( args == null )
            // The contract on the function interface is that this should not happen.
            throw new ARQInternalErrorException("FunctionBase: Null args list") ;
        
        List<NodeValue> evalArgs = new ArrayList<NodeValue>() ;
        for ( Iterator<Expr> iter = args.iterator() ; iter.hasNext() ; )
        {
            Expr e = iter.next() ;
            NodeValue x = e.eval(binding, env) ;
            evalArgs.add(x) ;
        }
        
        NodeValue nv =  exec(evalArgs) ;
        arguments = null ;
        return nv ;
    }
    
    /** Return the Context object for this execution */
    public Context getContext() { return env.getContext() ; }
    
    /** Function call to a list of evaluated argument values */ 
    public abstract NodeValue exec(List<NodeValue> args) ;

    public abstract void checkBuild(String uri, ExprList args) ;
    
//    /** Get argument, indexing from 1 **/
//    public NodeValue getArg(int i)
//    {
//        i = i-1 ;
//        if ( i < 0 || i >= arguments.size()  )
//            return null ;
//        return (NodeValue)arguments.get(i) ;
//    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
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
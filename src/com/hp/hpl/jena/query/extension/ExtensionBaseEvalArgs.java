/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.extension;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import com.hp.hpl.jena.query.core.ARQInternalErrorException;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.expr.ExprEvalException;
import com.hp.hpl.jena.query.expr.NodeValue;

/** Extension base class for extensions that deal with the incoming
 *  input stream of bindings one by one and have their arguments evaluated.
 *  This is the usual extension base class. 
 * 
 * @author Andy Seaborne
 * @version $Id: ExtensionBaseEvalArgs.java,v 1.6 2007/02/06 17:05:44 andy_seaborne Exp $
 */ 

public abstract class ExtensionBaseEvalArgs extends ExtensionBase
{
    //@Override
    public QueryIterator execUnevaluated(List args, Binding binding, ExecutionContext execCxt)
    {
        if ( args == null )
            // The contract on the function interface is that this should not happen.
            throw new ARQInternalErrorException("ExtensionBase1: Null args list") ;
        
        List argsEval = new ArrayList() ;
        for ( Iterator iter = args.listIterator() ; iter.hasNext() ; )
        {
            Expr e = (Expr)iter.next() ;
            
            try {
                NodeValue x = e.eval(binding, execCxt.getContext()) ;
                argsEval.add(x) ;
            } catch (ExprEvalException ex)
            {
                throw ex ;
            }
        }
        
        return execEvaluated(argsEval, binding) ;
    }
    
    public abstract QueryIterator execEvaluated(List args, Binding binding) ;
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
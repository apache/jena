/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.function.library;

import java.util.List;

//import org.apache.commons.logging.*;
import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.query.core.ARQInternalErrorException;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.expr.ExprEvalException;
import com.hp.hpl.jena.query.expr.NodeValue;
import com.hp.hpl.jena.query.function.Function;
import com.hp.hpl.jena.query.function.FunctionEnv;

/** Function that evaluates an expression - catches evaluation failures
 *  and returns false.
 *  Mainly used in extensions.
 *  Would be better if that were eval and this were "safe" or somesuch
 * @author Andy Seaborne
 * @version $Id: eval.java,v 1.12 2007/02/06 17:06:15 andy_seaborne Exp $
 */

public class eval implements Function
{
    //private static Log log = LogFactory.getLog(eval) ;
    
    public void build(String uri, List args)
    {
        if ( args.size() != 1 )
            throw new QueryBuildException("'eval' takes one argument") ;
    }

    
    /** Processes unevaluated arguments */
    
    public NodeValue exec(Binding binding, List args, String uri, FunctionEnv env)
    {
        if ( args == null )
            // The contract on the function interface is that this should not happen.
            throw new ARQInternalErrorException("function eval: Null args list") ;
        
        if ( args.size() != 1 )
            throw new ARQInternalErrorException("function eval: Arg list not of size 1") ;
        
        Expr ex = (Expr)args.get(0) ;
        try {
            NodeValue v = ex.eval(binding, env) ;
            return v ;
        } catch (ExprEvalException evalEx)
        {
            return NodeValue.FALSE ;
        }
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
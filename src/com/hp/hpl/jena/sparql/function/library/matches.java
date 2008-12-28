/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.function.library;

import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.*;
import com.hp.hpl.jena.sparql.function.Function;
import com.hp.hpl.jena.sparql.function.FunctionEnv;

/** Function for XPath fn:matches
 * 
 * @author Andy Seaborne
 */

public class matches implements Function
{
    // Wrapper around an E_Regex. Maybe move actual regex to Function.regex.
    E_Regex regex = null;
    ExprList myArgs = null ;
    
    public void build(String uri, ExprList args)
    {
        if ( args.size() != 3 && args.size() != 2 )
            throw new ExprEvalException("matches: Wrong number of arguments: Wanted 2 or 3, got "+args.size()) ;
        myArgs = args ;
        
    }
    
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env)
    {
        if ( myArgs != args )
            throw new ARQInternalErrorException("matches: Arguments have changed since checking") ;

        Expr expr = args.get(0) ;
        E_Regex regexEval = regex ;
        
        if ( regexEval == null )
        {
            Expr e1 = args.get(1) ;
            Expr e2 = null ;
            if ( args.size() == 3 )
                e2 = args.get(2) ;

            String pattern = e1.eval(binding, env).getString() ;
            String flags = (e2==null)?null : e2.eval(binding, env).getString() ;
            
            regexEval = new E_Regex(expr, pattern, flags) ;

            // Cache if the pattern is fixed and the flags are fixed or non-existant
            if ( e1 instanceof NodeValue && ( e2 == null || e2 instanceof NodeValue ) )
                regex = regexEval ;
        }

        NodeValue nv = regexEval.eval(binding, env) ;
        return nv ;
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
/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.extension.library;

import java.util.List;

import org.apache.commons.logging.*;

import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.Binding1;
import com.hp.hpl.jena.sparql.expr.*;
import com.hp.hpl.jena.sparql.extension.ExtensionBase;
import com.hp.hpl.jena.sparql.extension.ExtensionSingleton;
import com.hp.hpl.jena.sparql.util.Utils;

/** Assignment - first argument must be a variable; second is the expression, 
 * third optional value is a default used when the evaluation of the second
 * at runtime fails. 
 * 
 * EXPERIMENTAL
 * 
 * @author Andy Seaborne
 * @version $Id: assign.java,v 1.16 2007/02/06 17:06:17 andy_seaborne Exp $
 */ 

public class assign extends ExtensionBase
{
    //private static Log log = LogFactory.getLog(assign.class) ;
    List myArgs = null ;
    
    //@Override
    public void build(String uri, List args)
    {
        if ( args.size() != 2 && args.size() != 3)
            throw new QueryBuildException(Utils.className(this)+": Must be two or three arguments") ;
        
        Expr ex = (Expr)args.get(0) ;
        
        if ( ! ex.isVariable() )
            throw new QueryBuildException(Utils.className(this)+": First argument must be a variable") ;
        myArgs = args ;
    }
    
    //@Override
    public QueryIterator execUnevaluated(List args, Binding binding, ExecutionContext execCxt)
    {
        if ( args != myArgs )
            throw new ARQInternalErrorException(Utils.className(this)+": Arguments have changed since checking") ;
        
        NodeVar v = (NodeVar)args.get(0) ;
        Expr value = (Expr)args.get(1) ;
        
        NodeValue nv = null ;
        try {
            nv = value.eval(binding, execCxt) ;
        } catch (ExprEvalException ex)
        {   
            if ( args.size() == 3 )
            {
                Expr dft = (Expr)args.get(2) ;
                try {
                    nv = dft.eval(binding, execCxt) ;
                } catch (ExprEvalException ex2)
                {
                    LogFactory.getLog(assign.class).warn("Default value could not be evaluated: "+ex2.getMessage()) ;
                }
            }
        }
        
        Binding b = new Binding1(binding, v.asVar(), NodeValue.toNode(nv)) ;
        return new ExtensionSingleton(b) ;
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
/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.extension;

import java.util.List;

import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.Utils;

/** Extension base class for two argument extensions - this includes
 *  "magic properties", which are triple-pattern-like query constructs
 *  that require evaluation, not graph matching.  For example:
 *  <pre>
 *    ?list :listMember ?member 
 *  </pre>
 * 
 * @author Andy Seaborne
 * @version $Id: Extension2.java,v 1.7 2007/02/06 17:05:44 andy_seaborne Exp $
 */ 

public abstract class Extension2 extends ExtensionBase
{
    List myArgs = null ;

    //@Override
    public void build(String uri, List args)
    {
        if ( args.size() != 2 )
            throw new QueryBuildException(Utils.className(this)+": Must be two arguments") ;
        
        myArgs = args ;
    }
    
    //@Override
    public QueryIterator execUnevaluated(List args, Binding binding, ExecutionContext execCxt)
    {
        if ( args != myArgs )
            throw new ARQInternalErrorException(Utils.className(this)+": Arguments have changed since checking") ;
        
        Expr arg1 = (Expr)args.get(0) ;
        Expr arg2 = (Expr)args.get(1);
        return exec(arg1, arg2, binding, execCxt) ;
    }

    protected abstract  QueryIterator exec(Expr arg1, Expr arg2, Binding binding, ExecutionContext execCxt) ;
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
/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.util.iterator.SingletonIterator;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.Binding1;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.pfunction.PFuncSimple;
import com.hp.hpl.jena.sparql.procedure.ProcLib;

public class PathPropertyFunction extends PFuncSimple
{
    private Path path ;
    public PathPropertyFunction(Path path)
    {
        super() ;
        this.path = path ;
    }

    //@Override
    public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object,
                                       ExecutionContext execCxt)
    {
        Iterator iter = PathEval.eval(execCxt.getActiveGraph(), 
                                      new SingletonIterator(subject), 
                                      path) ;
        if ( Var.isVar(object) )
        {
            Var var = Var.alloc(object)  ;
            // Assign.
            List results = new ArrayList() ;
            for ( ; iter.hasNext(); )
            {
                Node n = (Node)iter.next() ;
                results.add(new Binding1(binding, var, n)) ;
            }
            return new QueryIterPlainWrapper(results.iterator()) ;
        }
        else
        {
            for ( ; iter.hasNext(); )
            {
                Node n = (Node)iter.next() ;
                if ( n.equals(object) )
                    return ProcLib.result(binding, execCxt) ;
            }
        }
        return ProcLib.noResults(execCxt) ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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
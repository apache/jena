/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.pfunction.library;

import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.engine.binding.Binding1;
import com.hp.hpl.jena.query.engine.iterator.QueryIterConcat;
import com.hp.hpl.jena.query.pfunction.PropFuncArg;
import com.hp.hpl.jena.query.pfunction.PropFuncArgType;
import com.hp.hpl.jena.query.pfunction.PropertyFunctionEval;
import com.hp.hpl.jena.query.util.GraphList;


public abstract class ListBase extends PropertyFunctionEval
{
    private PropFuncArgType objFuncArgType ;


    public ListBase(PropFuncArgType objFuncArgType)
    { 
        super(PropFuncArgType.PF_ARG_SINGLE, objFuncArgType) ;
    }
    
    final
    public QueryIterator execEvaluated(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        Node listNode = argSubject.getArg() ;
        Graph graph = execCxt.getActiveGraph() ;
        if ( !Var.isVar(listNode) )
            return execOneList(binding, listNode, predicate, argObject, execCxt) ;
        // Subject unbound. BFI.
        Var listVar = Var.alloc(listNode) ;
        // Gulp.  Find all lists; work hard.
        Set x = GraphList.findAllLists(graph) ;
        QueryIterConcat qIter = new QueryIterConcat(execCxt) ;
        for ( Iterator iter = x.iterator() ; iter.hasNext() ; )
        {
            Node n = (Node)iter.next();
            Binding b = new Binding1(binding, listVar, n) ;
            QueryIterator q = execOneList(b, n, predicate, argObject, execCxt) ;
            qIter.add(q) ;
        }
        return qIter ;
    }
    
    protected abstract
    QueryIterator execOneList(Binding binding, Node listNode, Node predicate, PropFuncArg object, ExecutionContext execCxt) ;
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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
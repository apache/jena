/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.pfunction.library;

import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArgType;

public abstract class ListBaseList extends ListBase
{
    public ListBaseList()
    { super(PropFuncArgType.PF_ARG_LIST) ; }

    @Override
    final protected 
    QueryIterator execOneList(Binding binding, Node listNode, Node predicate, PropFuncArg object,
                              ExecutionContext execCxt)
    {
        return execOneList(binding, listNode, predicate, object.getArgList(), execCxt) ;
    }

    @Override
    protected QueryIterator execObjectBound(Binding binding, Var listVar, Node predicate, Node object,
                                            ExecutionContext execCxt)
    { throw new ARQException("ListBaseList: wrong dispatch") ; }

    @Override
    protected abstract QueryIterator execObjectList(Binding binding, 
                                                     Var listVar, Node predicate, List<Node> objectArgs,
                                                     ExecutionContext execCxt) ;

    protected abstract QueryIterator execOneList(Binding binding, 
                                                 Node listNode, Node predicate, List<Node> argList,
                                                 ExecutionContext execCxt) ;
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
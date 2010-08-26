/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify.request;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.modify.GraphStoreAction ;
import com.hp.hpl.jena.sparql.modify.GraphStoreUtils ;
import com.hp.hpl.jena.sparql.syntax.Element ;
import com.hp.hpl.jena.update.GraphStore ;

public class UpdateEngine implements UpdateVisitor
{
    private GraphStore graphStore ;
    private Binding initialBinding ;

    public UpdateEngine(GraphStore graphStore, Binding initialBinding)
    {
        this.graphStore = graphStore ;
        this.initialBinding = initialBinding ;
    }

    public void visit(UpdateDrop update)
    {}

    public void visit(UpdateClear update)
    {}

    public void visit(UpdateCreate update)
    {}

    public void visit(UpdateLoad update)
    {}

    public void visit(UpdateDataInsert update)
    {}

    public void visit(UpdateDataDelete update)
    {}

    public void visit(UpdateDeleteWhere update)
    {}

    public void visit(UpdateModify update)
    {
        final List<Binding> bindings = evalBindings(update.getWherePattern()) ;
//        GraphStoreUtils.action(graphStore, update.getGraphNames(), 
//                               new GraphStoreAction() { public void exec(Graph graph) { execDeletes(modify, graph, bindings) ; }}) ;
//        GraphStoreUtils.action(graphStore, update.getGraphNames(), 
//                               new GraphStoreAction() { public void exec(Graph graph) { execInserts(modify, graph, bindings) ; }}) ;
    }
    
    protected List<Binding> evalBindings(Element pattern)
    {
        List<Binding> bindings = new ArrayList<Binding>() ;
        
        if ( pattern != null )
        {
            Plan plan = QueryExecutionFactory.createPlan(pattern, graphStore, initialBinding) ;
            QueryIterator qIter = plan.iterator() ;

            for( ; qIter.hasNext() ; )
            {
                Binding b = qIter.nextBinding() ;
                bindings.add(b) ;
            }
            qIter.close() ;
        }
        else
        {
            if ( initialBinding != null )
                bindings.add(initialBinding) ;
            else
                bindings.add(BindingRoot.create()) ;
        }
        return bindings ;
    }

}

/*
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
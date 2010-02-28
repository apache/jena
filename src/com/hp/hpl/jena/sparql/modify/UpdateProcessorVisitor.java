/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify;

import java.util.* ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.AlreadyExists ;
import com.hp.hpl.jena.sparql.DoesNotExist ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.modify.op.* ;
import com.hp.hpl.jena.sparql.syntax.Element ;
import com.hp.hpl.jena.sparql.syntax.Template ;
import com.hp.hpl.jena.sparql.util.ALog ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.util.FileManager ;

/** A general processor for executing SPARQL/Updates on GraphStoreBasic objects*/ 
public class UpdateProcessorVisitor implements UpdateVisitor
{

    private GraphStore graphStore ;
    private Binding binding ;

    public UpdateProcessorVisitor(GraphStore graphStore, Binding initialBinding)
    {
        this.graphStore = graphStore ;
        this.binding = initialBinding ;
    }
    
    public void visit(final UpdateInsertData insertData)
    {
        GraphStoreUtils.action(graphStore, insertData.getGraphNames(), new GraphStoreAction() {
            public void exec(Graph graph)
            {
                graph.getBulkUpdateHandler().add(insertData.getData()) ;  
            } } ) ;
    }
    
    public void visit(final UpdateDeleteData deleteData)
    {
        GraphStoreUtils.action(graphStore, deleteData.getGraphNames(), new GraphStoreAction() {
            public void exec(Graph graph)
            {
                graph.getBulkUpdateHandler().delete(deleteData.getData()) ;  
            } } ) ;
    }

    public void visit(UpdateModify modify)      { visitModify(modify) ; }

    public void visit(UpdateDelete delete)      { visitModify(delete) ; }
    
    public void visit(UpdateInsert insert)      { visitModify(insert) ; }

    private void visitModify(final UpdateModifyBase modify)
    {
        final List<Binding> bindings = evalBindings(modify.getElement() ) ;
        GraphStoreUtils.action(graphStore, modify.getGraphNames(), new GraphStoreAction() { public void exec(Graph graph) { execDeletes(modify, graph, bindings) ; }}) ;
        GraphStoreUtils.action(graphStore, modify.getGraphNames(), new GraphStoreAction() { public void exec(Graph graph) { execInserts(modify, graph, bindings) ; }}) ;
    }
  
    private List<Binding> evalBindings(Element pattern)
    {
        List<Binding> bindings = new ArrayList<Binding>() ;
        if ( pattern != null )
        {
            Plan plan = QueryExecutionFactory.createPlan(pattern, graphStore, binding) ;
            QueryIterator qIter = plan.iterator() ;

            for( ; qIter.hasNext() ; )
            {
                Binding b = qIter.nextBinding() ;
                bindings.add(b) ;
            }
            qIter.close() ;
        }
        else
            bindings.add(BindingRoot.create()) ;
        return bindings ;
    }

    private void execDeletes(UpdateModifyBase modify, Graph graph, List<Binding> bindings)
    {
        if ( modify.getDeletes() != null )
        {
            QueryIterator qIter = new QueryIterPlainWrapper(bindings.iterator()) ;
            Collection<Triple> acc = subst(modify.getDeletes(), qIter) ;
            graph.getBulkUpdateHandler().delete(acc.iterator()) ;
        }
    }

    private void execInserts(UpdateModifyBase modify, Graph graph, List<Binding> bindings)
    {
        if ( modify.getInserts() != null )
        {
            QueryIterator qIter = new QueryIterPlainWrapper(bindings.iterator()) ;
            Collection<Triple> acc = subst(modify.getInserts(), qIter) ;
            graph.getBulkUpdateHandler().add(acc.iterator()) ;
        }
    }

    public void visit(UpdateClear clear)
    {
        GraphStoreUtils.action(graphStore, clear.getGraphName(),  new GraphStoreAction() {
            public void exec(Graph graph)
            {
                graph.getBulkUpdateHandler().removeAll() ;
            }}) ;
    }
    
    public void visit(final UpdateLoad load)
    {
        GraphStoreUtils.action(graphStore, load.getGraphName(),  new GraphStoreAction() {
            public void exec(Graph graph)
            {
                Model model = ModelFactory.createModelForGraph(graph) ;
                for ( Iterator<String> iter = load.getLoadIRIs().iterator() ; iter.hasNext() ; )
                {
                    String s = iter.next() ;
                    FileManager.get().readModel(model, s) ;
                }
            }}) ;
    }
    
    public void visit(UpdateDrop drop)
    {
        Node n = drop.getIRI() ;
        if ( ! graphStore.containsGraph(n) )
        {
            if ( drop.isSilent() )
                return ; 
            throw new DoesNotExist("Named graph: "+n) ;
        }
        graphStore.removeGraph(n) ;
    }

    public void visit(UpdateCreate create)
    {
        Node n = create.getIRI() ;
        if ( graphStore.containsGraph(n) )
        {
            if ( create.isSilent() )
                return ; 
            throw new AlreadyExists("Named graph: "+n) ;
        }
        // Create an in-memory graph.  Persistent layers need to modify this operation.  
        Graph graph = GraphFactory.createDefaultGraph() ;
        graphStore.addGraph(n, graph) ;
    }

    public void visit(UpdateExt updateExt)
    {
        updateExt.update() ;
    }

    // -----------------------------------------------------
    
    protected static Collection<Triple> subst(Template template, QueryIterator qIter)
    {
        Set<Triple> acc = new HashSet<Triple>() ;
        for ( ; qIter.hasNext() ; )
        {
            Map<Node, Node> bNodeMap = new HashMap<Node, Node>() ;
            Binding b = qIter.nextBinding() ;
            template.subst(acc, bNodeMap, b) ;
        }

        for ( Iterator<Triple> iter = acc.iterator() ; iter.hasNext() ; )
        {
            Triple triple = iter.next() ;
            if ( ! isGroundTriple(triple))
            {
                ALog.warn(UpdateProcessorVisitor.class, "Unbound triple: "+FmtUtils.stringForTriple(triple)) ;
                iter.remove() ;
            }
        }
        
        return acc ;
    }
    
    private static boolean isGroundTriple(Triple triple)
    {
        return 
            isGroundNode(triple.getSubject()) &&
            isGroundNode(triple.getPredicate()) &&
            isGroundNode(triple.getObject()) ;
    }

    private static boolean isGroundNode(Node node)
    {
        return node.isConcrete() ; 
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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
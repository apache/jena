/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.modify;

import static com.hp.hpl.jena.sparql.modify.TemplateLib.template ;

import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.data.BagFactory ;
import org.openjena.atlas.data.DataBag ;
import org.openjena.atlas.data.ThresholdPolicy ;
import org.openjena.atlas.data.ThresholdPolicyFactory ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.riot.SerializationFactoryFinder ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphMap ;
import com.hp.hpl.jena.sparql.core.DatasetGraphWrapper ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;
import com.hp.hpl.jena.sparql.graph.NodeTransformLib ;
import com.hp.hpl.jena.sparql.modify.request.* ;
import com.hp.hpl.jena.sparql.syntax.Element ;
import com.hp.hpl.jena.sparql.syntax.ElementGroup ;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph ;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateException ;
import com.hp.hpl.jena.util.FileManager ;

/** Implementation of general purpose update request execution */ 
public class UpdateEngineWorker implements UpdateVisitor
{
    protected final GraphStore graphStore ;
    protected final Binding initialBinding ;
    protected final boolean alwaysSilent = true ;
    protected final Context context ;

    public UpdateEngineWorker(GraphStore graphStore, Binding initialBinding, Context context)
    {
        this.graphStore = graphStore ;
        this.initialBinding = initialBinding ;
        this.context = context ;
    }

    @Override
    public void visit(UpdateDrop update)
    { execDropClear(update, false) ; }

    @Override
    public void visit(UpdateClear update)
    { execDropClear(update, true) ; }

    // ReDo with gs* primitives
    protected void execDropClear(UpdateDropClear update, boolean isClear)
    {
        if ( update.isAll() )
        {
            execDropClear(update, null, true) ;    // Always clear.
            execDropClearAllNamed(update, isClear) ;
        }
        else if ( update.isAllNamed() )
            execDropClearAllNamed(update, isClear) ;
        else if ( update.isDefault() )
            execDropClear(update, null, true) ;
        else if ( update.isOneGraph() )
            execDropClear(update, update.getGraph(), isClear) ;
        else
            throw new ARQInternalErrorException("Target is undefined: "+update.getTarget()) ;
    }

    protected void execDropClear(UpdateDropClear update, Node g, boolean isClear)
    {
        if ( ! alwaysSilent )
        {
            if ( g != null && ! graphStore.containsGraph(g) && ! update.isSilent())
                error("No such graph: "+g) ;
        }
        
        if ( isClear )
        {
            if ( g == null || graphStore.containsGraph(g) )
                graph(graphStore, g).getBulkUpdateHandler().removeAll() ;
        }
        else
            graphStore.removeGraph(g) ;
    }

    protected void execDropClearAllNamed(UpdateDropClear update, boolean isClear)
    {
        // Avoid ConcurrentModificationException
        List<Node> list = Iter.toList(graphStore.listGraphNodes()) ;
        
        for ( Node gn : list )
            execDropClear(update, gn, isClear) ;
    }

    @Override
    public void visit(UpdateCreate update)
    {
        Node g = update.getGraph() ;
        if ( g == null )
            return ;
        if ( graphStore.containsGraph(g) )
        {
            if ( ! alwaysSilent && ! update.isSilent() )
                error("Graph store already contains graph : "+g) ;
            return ;
        }
        // In-memory specific 
        graphStore.addGraph(g, GraphFactory.createDefaultGraph()) ;
    }

    @Override
    public void visit(UpdateLoad update)
    {
        String source = update.getSource() ;
        Node dest = update.getDest() ;
        try {
//            // Experimental ; quads reading.  Needs redoing.  No conneg. 
//            if ( dest == null )
//            {
//                // Quads?
//                Lang guess = Lang.guess(source, Lang.NTRIPLES) ;
//                if ( guess.isQuads() )
//                {
//                   RiotLoader.read(source, graphStore, guess) ;
//                    return ;
//                }
//            }
            
            // Read into temporary model to protect against parse errors.
            Model model = FileManager.get().loadModel(source) ;
            Graph g = graph(graphStore, dest) ;
            g.getBulkUpdateHandler().add(model.getGraph()) ;
        } catch (RuntimeException ex)
        {
            if ( ! update.getSilent() )
                throw ex ;
        }
    }

    @Override
    public void visit(UpdateAdd update)
    { 
        if ( ! validBinaryGraphOp(update) ) return ;
        if ( update.getSrc().equals(update.getDest()) )
            return ;
        //  ADD (DEFAULT or GRAPH) TO (DEFAULT or GRAPH)
        // Different source and destination.
        gsCopyTriples(graphStore, update.getSrc(), update.getDest()) ;
    }

    @Override
    public void visit(UpdateCopy update)
    { 
        if ( ! validBinaryGraphOp(update) ) return ;
        if ( update.getSrc().equals(update.getDest()) )
            return ;
        // COPY (DEFAULT or GRAPH) TO (DEFAULT or GRAPH) 
        gsCopy(graphStore, update.getSrc(), update.getDest(), update.getSilent()) ;
    }

    @Override
    public void visit(UpdateMove update)
    { 
        if ( ! validBinaryGraphOp(update) ) return ;
        if ( update.getSrc().equals(update.getDest()) )
            return ;
        // MOVE (DEFAULT or GRAPH) TO (DEFAULT or GRAPH)
        // Difefrent source and destination.
        gsCopy(graphStore, update.getSrc(), update.getDest(), update.getSilent()) ;
        gsDrop(graphStore, update.getSrc(), true) ;
    }

    private boolean validBinaryGraphOp(UpdateBinaryOp update)
    {
        if ( update.getSrc().isDefault() )
            return true ;
        
        if ( update.getSrc().isOneNamedGraph() )
        {
            Node gn =  update.getSrc().getGraph() ;
            if ( ! graphStore.containsGraph(gn) )
            {
                if ( ! update.getSilent() )
                    error("No such graph: "+gn) ;
                return false ;
            }
            return true ;
        }
        error("Invalid source target for oepration; "+update.getSrc()) ;
        return false ;
    }

    // ----
    // Core operations
    
    protected static void gsCopy(GraphStore gStore, Target src, Target dest, boolean isSilent)
    {
        if ( dest.equals(src) ) 
            return ;
        gsClear(gStore, dest, true) ;
        gsCopyTriples(gStore, src, dest) ;
    }

    protected static void gsCopyTriples(GraphStore gStore, Target src, Target dest)
    {
        Graph gSrc = graph(gStore, src) ;
        Graph gDest = graph(gStore, dest) ;
        
        // Avoids concurrency problems by reading fully before writing
        ThresholdPolicy<Triple> policy = ThresholdPolicyFactory.policyFromContext(gStore.getContext());
        DataBag<Triple> db = BagFactory.newDefaultBag(policy, SerializationFactoryFinder.tripleSerializationFactory()) ;
        try
        {
            Iterator<Triple> triples = gSrc.find(null, null, null) ;
            db.addAll(triples) ;
            Iter.close(triples) ;
            
            Iterator<Triple> it = db.iterator() ;
            gDest.getBulkUpdateHandler().add(it) ;
            Iter.close(it);
        }
        finally
        {
            db.close() ;
        }
    }

    protected static void gsClear(GraphStore gStore, Target target, boolean isSilent)
    {
        // No create - we tested earlier.
        Graph g = graph(gStore, target) ;
        g.getBulkUpdateHandler().removeAll() ;
    }

    protected static void gsDrop(GraphStore gStore, Target target, boolean isSilent)
    {
        if ( target.isDefault() )
            gStore.getDefaultGraph().getBulkUpdateHandler().removeAll() ;
        else
            gStore.removeGraph(target.getGraph()) ;
    }
    
    // ----
    
    @Override
    public void visit(UpdateDataInsert update)
    {
        for ( Quad quad : update.getQuads() )
            addToGraphStore(graphStore, quad) ;
    }

    @Override
    public void visit(UpdateDataDelete update)
    {
        for ( Quad quad : update.getQuads() )
            deleteFromGraphStore(graphStore, quad) ;
    }

    @Override
    public void visit(UpdateDeleteWhere update)
    {
        List<Quad> quads = update.getQuads() ;
        // Convert bNodes to named variables first.
//        if ( false )
//            // Removed from SPARQL
//            quads = convertBNodesToVariables(quads) ;
        // Convert quads to a pattern.
        Element el = elementFromQuads(quads) ;
        
        // Decided to serialize the bindings, but could also have decided to
        // serialize the quads after applying the template instead.
        
        ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(graphStore.getContext());
        DataBag<Binding> db = BagFactory.newDefaultBag(policy, SerializationFactoryFinder.bindingSerializationFactory()) ;
        try
        {
            Iterator<Binding> bindings = evalBindings(el, null) ;
            db.addAll(bindings) ;
            Iter.close(bindings) ;
            
            Iterator<Binding> it = db.iterator() ;
            execDelete(quads, null, it) ;
            Iter.close(it) ;
        }
        finally
        {
            db.close() ;
        }
    }
    
    @Override
    public void visit(UpdateModify update)
    {
        Node withGraph = update.getWithIRI() ;
        Query query = elementToQuery(update.getWherePattern()) ;
        
        // USING/USING NAMED
        DatasetGraph dsg = processUsing(update, query) ;
        
        // USING overrides WITH
        if ( dsg == null && withGraph != null )
        {
            //Graph g = graphStore.getGraph(withGraph) ;
            Graph g = graph(graphStore, withGraph) ;
            dsg = new DatasetGraphAltDefaultGraph(graphStore, g) ;
        }
        if ( dsg == null )
            dsg = graphStore ;
        
        ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(graphStore.getContext());
        DataBag<Binding> db = BagFactory.newDefaultBag(policy, SerializationFactoryFinder.bindingSerializationFactory()) ;
        try
        {
            Iterator<Binding> bindings = evalBindings(query, dsg, initialBinding, context) ;
            
            db.addAll(bindings) ;
            Iter.close(bindings) ;
            
            Iterator<Binding> it = db.iterator() ;
            execDelete(update.getDeleteQuads(), withGraph, it) ;
            Iter.close(it) ;
            
            Iterator<Binding> it2 = db.iterator() ;
            execInsert(update.getInsertQuads(), withGraph, it2) ;
            Iter.close(it2) ;
        }
        finally
        {
            db.close() ;
        }
    }

    // Indirection for subsystems to support USING/USING NAMED.
    protected DatasetGraph processUsing(UpdateModify update, Query query)
    {
        if ( update.getUsing().size() == 0 && update.getUsingNamed().size() == 0 )
            return null ;
        
        DatasetGraphMap dsg = new DatasetGraphMap(graphStore) ;
        if ( update.getUsing().size() > 0  )
        {
            if ( update.getUsing().size() > 1 )
            {
                // Lack of scaling here
                // Need to take a copy to merge.
                Graph g = GraphFactory.createGraphMem() ;
                
                for ( Node gn : update.getUsing() )
                {
                    Graph g2 = graphStore.getGraph(gn) ;
                    g.getBulkUpdateHandler().add(g2) ;
                }
                dsg.setDefaultGraph(g) ;
            }
            else
            {
                Node gn = update.getUsing().get(0) ;
                dsg.setDefaultGraph(graphStore.getGraph(gn)) ;
            }
        }
        
        if ( update.getUsingNamed().size() > 0  )
        {
            // Replace with a no named graphs version.
            dsg = new DatasetGraphMap(dsg.getDefaultGraph()) ;
            
            for ( Node gn : update.getUsingNamed() )
                dsg.addGraph(gn, graphStore.getGraph(gn)) ; 
        }
        return dsg ;
    }
    
    protected static List<Quad> unused_convertBNodesToVariables(List<Quad> quads)
    {
        NodeTransform bnodesToVariables = new NodeTransformBNodesToVariables() ;
        return NodeTransformLib.transformQuads(bnodesToVariables, quads) ;
    }
    
    protected Element elementFromQuads(List<Quad> quads)
    {
        ElementGroup el = new ElementGroup() ;
        ElementTriplesBlock x = new ElementTriplesBlock() ;
        // Maybe empty??
        el.addElement(x) ;
        Node g = Quad.defaultGraphNodeGenerated ;
        
        for ( Quad q : quads )
        {
            if ( q.getGraph() != g )
            {
                g = q.getGraph() ;
                x = new ElementTriplesBlock() ;
                if ( g == null || g == Quad.defaultGraphNodeGenerated )
                    el.addElement(x) ;
                else
                {
                    ElementNamedGraph eng = new ElementNamedGraph(g, x) ;
                    el.addElement(eng) ;
                }
            }
            x.addTriple(q.asTriple()) ;
        }
        return el ;
    }

    protected void execDelete(List<Quad> quads, Node dftGraph, Iterator<Binding> bindings)
    {
        Iterator<Quad> it = template(quads, dftGraph, bindings) ;
        if ( it == null ) return ;
        
        while (it.hasNext())
        {
            Quad q = it.next();
            graphStore.delete(q);
        }
        
        
        // Alternate implementation that can use the graph BulkUpdateHandler, but forces all quads into
        // memory (we don't want that!).  The issue is that all of the quads can be mixed up based on the
        // user supplied template.  If graph stores can benefit from bulk insert/delete operations, then we
        // need to expose a bulk update interface on GraphStore, not just Graph.
//        MultiMap<Node, Triple> acc = MultiMap.createMapList() ;
//        while (it.hasNext())
//        {
//            Quad q = it.next();
//            acc.put(q.getGraph(), q.asTriple()) ;
//        }
//        for ( Node gn : acc.keys() )
//        {
//            Collection<Triple> triples = acc.get(gn) ;
//            graph(graphStore, gn).getBulkUpdateHandler().delete(triples.iterator()) ;
//        }
    }

    protected void execInsert(List<Quad> quads, Node dftGraph, Iterator<Binding> bindings)
    {
        Iterator<Quad> it = template(quads, dftGraph, bindings) ;
        if ( it == null ) return ;
        
        while (it.hasNext())
        {
            Quad q = it.next();
            addToGraphStore(graphStore, q);
        }
    }
    
    // Catch all individual adds of quads (and deletes - mainly for symmetry). 
    private static void addToGraphStore(GraphStore graphStore, Quad quad) 
    {
        // Check legal triple.
        if ( quad.isLegalAsData() )
            graphStore.add(quad);
        // Else drop.
        //Log.warn(UpdateEngineWorker.class, "Bad quad as data: "+quad) ;
    }

    private static void deleteFromGraphStore(GraphStore graphStore, Quad quad)
    {
        graphStore.delete(quad) ;
    }

    protected Query elementToQuery(Element pattern)
    {
        if ( pattern == null )
            return null ;
        Query query = new Query() ;
        query.setQueryPattern(pattern) ;
        query.setQuerySelectType() ;
        query.setQueryResultStar(true) ;
        query.setResultVars() ;
        return query ;
    }
    
    static class DatasetGraphAltDefaultGraph extends DatasetGraphWrapper
    {
        private Graph dftGraph ;
        
        public DatasetGraphAltDefaultGraph(DatasetGraph dsg, Graph dftGraph)
        { super(dsg) ; setDefaultGraph(dftGraph) ; }
        
        @Override
        public Graph getDefaultGraph()
        { return dftGraph; }
    
        @Override
        public void setDefaultGraph(Graph g)
        { dftGraph = g ; }
    }

    protected Iterator<Binding> evalBindings(Element pattern, Node dftGraph)
    {
        return evalBindings(elementToQuery(pattern), dftGraph) ;
    }
    
    protected Iterator<Binding> evalBindings(Query query, Node dftGraph)
    {
        DatasetGraph dsg = graphStore ;
        if ( query != null )
        {
            if ( dftGraph != null )
            {
                Graph g = dsg.getGraph(dftGraph) ;
                dsg = new DatasetGraphAltDefaultGraph(dsg, g) ;
            }
        }
        
        return evalBindings(query, dsg, initialBinding, context) ;
        
    }
    
    protected static Iterator<Binding> evalBindings(Query query, DatasetGraph dsg, Binding initialBinding, Context context)
    {
        // SET UP CONTEXT
        // The UpdateProcessorBase already copied the context and made it safe ... but that's going to happen again :-(
        
        Iterator<Binding> toReturn ;
        
        if ( query != null )
        {
            Plan plan = QueryExecutionFactory.createPlan(query, dsg, initialBinding, context) ;
            toReturn = plan.iterator();
        }
        else
        {
            toReturn = Iter.singleton((initialBinding != null) ? initialBinding : BindingRoot.create()) ;
        }
        return toReturn ;
    }
    
    protected static Graph graph(GraphStore graphStore, Node gn)
    {
        if ( gn == null || gn == Quad.defaultGraphNodeGenerated )
            return graphStore.getDefaultGraph() ;
        else
            return graphStore.getGraph(gn) ;
    }

    protected static Graph graph(GraphStore graphStore, Target target)
    {
        if ( target.isDefault() )
            return graphStore.getDefaultGraph() ;
        if ( target.isOneNamedGraph() )
            return graph(graphStore, target.getGraph()) ;
        error("Target does not name one graph: "+target) ;
        return null ;
    }

    protected static void error(String msg)
    {
        throw new UpdateException(msg) ;
    }
}

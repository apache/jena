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

package org.apache.jena.sparql.modify;

import static org.apache.jena.sparql.modify.TemplateLib.remapDefaultGraph ;
import static org.apache.jena.sparql.modify.TemplateLib.template ;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.data.BagFactory ;
import org.apache.jena.atlas.data.DataBag ;
import org.apache.jena.atlas.data.ThresholdPolicy ;
import org.apache.jena.atlas.data.ThresholdPolicyFactory ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.GraphUtil ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecutionFactory ;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.SerializationFactoryFinder ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.core.* ;
import org.apache.jena.sparql.engine.Plan ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingRoot ;
import org.apache.jena.sparql.graph.GraphFactory ;
import org.apache.jena.sparql.graph.GraphOps ;
import org.apache.jena.sparql.modify.request.* ;
import org.apache.jena.sparql.syntax.Element ;
import org.apache.jena.sparql.syntax.ElementGroup ;
import org.apache.jena.sparql.syntax.ElementNamedGraph ;
import org.apache.jena.sparql.syntax.ElementTriplesBlock ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.update.UpdateException ;

/** Implementation of general purpose update request execution */ 
public class UpdateEngineWorker implements UpdateVisitor
{
    protected final DatasetGraph datasetGraph ;
    protected final boolean alwaysSilent = true ;
    protected final Binding inputBinding;       // Used for UpdateModify only
    protected final Context context ;

    public UpdateEngineWorker(DatasetGraph datasetGraph, Binding inputBinding, Context context) {
        this.datasetGraph = datasetGraph ;
        this.inputBinding = inputBinding ;
        this.context = context ;
    }

    @Override
    public void visit(UpdateDrop update)
    { execDropClear(update, false) ; }

    @Override
    public void visit(UpdateClear update)
    { execDropClear(update, true) ; }

    protected void execDropClear(UpdateDropClear update, boolean isClear) {
        if ( update.isAll() ) {
            execDropClear(update, null, true); // Always clear.
            execDropClearAllNamed(update, isClear);
        } else if ( update.isAllNamed() )
            execDropClearAllNamed(update, isClear);
        else if ( update.isDefault() )
            execDropClear(update, null, true);
        else if ( update.isOneGraph() )
            execDropClear(update, update.getGraph(), isClear);
        else
            throw new ARQInternalErrorException("Target is undefined: " + update.getTarget());
    }

    protected void execDropClear(UpdateDropClear update, Node g, boolean isClear) {
        if ( !alwaysSilent ) {
            if ( g != null && !datasetGraph.containsGraph(g) && !update.isSilent() )
                error("No such graph: " + g);
        }

        if ( isClear ) {
            if ( g == null || datasetGraph.containsGraph(g) )
                graph(datasetGraph, g).clear();
        } else
            datasetGraph.removeGraph(g);
    }

    protected void execDropClearAllNamed(UpdateDropClear update, boolean isClear) {
        // Avoid ConcurrentModificationException
        List<Node> list = Iter.toList(datasetGraph.listGraphNodes());

        for ( Node gn : list )
            execDropClear(update, gn, isClear);
    }

    @Override
    public void visit(UpdateCreate update) {
        Node g = update.getGraph();
        if ( g == null )
            return;
        if ( datasetGraph.containsGraph(g) ) {
            if ( !alwaysSilent && !update.isSilent() )
                error("Graph store already contains graph : " + g);
            return;
        }
        // In-memory specific
        datasetGraph.addGraph(g, GraphFactory.createDefaultGraph());
    }

    @Override
    public void visit(UpdateLoad update) {
        // LOAD SILENT? iri ( INTO GraphRef )? 
        String source = update.getSource();
        Node dest = update.getDest();
        Graph graph = graph(datasetGraph, dest);
        // We must load buffered if silent so that the dataset graph sees
        // all or no triples/quads when there is a parse error
        // (no nested transaction abort). 
        boolean loadBuffered = update.getSilent() || ! datasetGraph.supportsTransactionAbort() ;
        try {
            if ( dest == null ) {
                // LOAD SILENT? iri
                // Quads accepted (extension).
                if ( loadBuffered ) {
                    DatasetGraph dsg2 = DatasetGraphFactory.create();
                    RDFDataMgr.read(dsg2, source);
                    dsg2.find().forEachRemaining(datasetGraph::add);
                } else {
                    RDFDataMgr.read(datasetGraph, source);
                }
                return ;
            }
            // LOAD SILENT? iri INTO GraphRef
            // Load triples. To give a decent error message and also not have the usual
            // parser behaviour of just selecting default graph triples when the
            // destination is a graph, we need to do the same steps as RDFParser.parseURI,
            // with different checking.
            TypedInputStream input = RDFDataMgr.open(source);
            String contentType = input.getContentType();
            Lang lang = RDFDataMgr.determineLang(source, contentType, Lang.TTL); 
            if ( lang == null )
                throw new UpdateException("Failed to determine the syntax for '"+source+"'");
            if ( ! RDFLanguages.isTriples(lang) )
                throw new UpdateException("Attempt to load quads into a graph");
            RDFParser parser = RDFParser
                .source(input.getInputStream())
                .forceLang(lang)
                .build();
            if ( loadBuffered ) {
                Graph g = GraphFactory.createGraphMem();
                parser.parse(g);
                GraphUtil.addInto(graph, g);
            } else {
                parser.parse(graph);
            }
        } catch (RuntimeException ex) {
            if ( !update.getSilent() ) {
                if ( ex instanceof UpdateException )
                    throw ex;
                throw new UpdateException("Failed to LOAD '" + source + "' :: " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void visit(UpdateAdd update) {
        // ADD SILENT? (DEFAULT or GRAPH) TO (DEFAULT or GRAPH)
        if ( !validBinaryGraphOp(update) )
            return;
        if ( update.getSrc().equals(update.getDest()) )
            return;
        // Different source and destination.
        gsAddTriples(datasetGraph, update.getSrc(), update.getDest());
    }

    @Override
    public void visit(UpdateCopy update) {
        // COPY SILENT? (DEFAULT or GRAPH) TO (DEFAULT or GRAPH)
        if ( !validBinaryGraphOp(update) )
            return;
        if ( update.getSrc().equals(update.getDest()) )
            return;
        gsCopy(datasetGraph, update.getSrc(), update.getDest(), update.getSilent());
    }

    @Override
    public void visit(UpdateMove update) {
        // MOVE SILENT? (DEFAULT or GRAPH) TO (DEFAULT or GRAPH)
        if ( !validBinaryGraphOp(update) )
            return;
        if ( update.getSrc().equals(update.getDest()) )
            return;
        // MOVE (DEFAULT or GRAPH) TO (DEFAULT or GRAPH)
        // Difefrent source and destination.
        gsCopy(datasetGraph, update.getSrc(), update.getDest(), update.getSilent());
        gsDrop(datasetGraph, update.getSrc(), true);
    }

    private boolean validBinaryGraphOp(UpdateBinaryOp update) {
        if ( update.getSrc().isDefault() )
            return true;

        if ( update.getSrc().isOneNamedGraph() ) {
            Node gn = update.getSrc().getGraph();
            if ( !datasetGraph.containsGraph(gn) ) {
                if ( !update.getSilent() )
                    error("No such graph: " + gn);
                return false;
            }
            return true;
        }
        error("Invalid source target for oepration; " + update.getSrc());
        return false;
    }

    // ----
    // Core operations
    /** Copy from src to dst : copy overwrites (= deletes) the old contents */
    protected static void gsCopy(DatasetGraph dsg, Target src, Target dest, boolean isSilent)
    {
        if ( dest.equals(src) ) 
            return ;
        gsClear(dsg, dest, true) ;
        gsAddTriples(dsg, src, dest) ;
    }

    /** Add triples from src to dest */
    protected static void gsAddTriples(DatasetGraph dsg, Target src, Target dest) {
        Graph gSrc = graph(dsg, src);
        Graph gDest = graph(dsg, dest);

        // Avoids concurrency problems by reading fully before writing
        ThresholdPolicy<Triple> policy = ThresholdPolicyFactory.policyFromContext(dsg.getContext());
        DataBag<Triple> db = BagFactory.newDefaultBag(policy, SerializationFactoryFinder.tripleSerializationFactory());
        try {
            Iterator<Triple> triples = gSrc.find(null, null, null);
            db.addAll(triples);
            Iter.close(triples);
            GraphOps.addAll(gDest, db.iterator());
        }
        finally {
            db.close();
        }
    }

    /** Clear target */
    protected static void gsClear(DatasetGraph dsg, Target target, boolean isSilent) {
        // No create - we tested earlier.
        Graph g = graph(dsg, target);
        g.clear();
    }

    /** Remove the target graph */
    protected static void gsDrop(DatasetGraph dsg, Target target, boolean isSilent) {
        if ( target.isDefault() )
            dsg.getDefaultGraph().clear();
        else
            dsg.removeGraph(target.getGraph());
    }
    
    // ----
    
    @Override
    public Sink<Quad> createInsertDataSink() {
        return new Sink<Quad>() {
            @Override
            public void send(Quad quad) {
                addToDatasetGraph(datasetGraph, quad);
            }

            @Override
            public void flush() {
                SystemARQ.sync(datasetGraph);
            }

            @Override
            public void close() {}
        };
    }
    
    @Override
    public void visit(UpdateDataInsert update) {
        for ( Quad quad : update.getQuads() )
            addToDatasetGraph(datasetGraph, quad);
    }

    @Override
    public Sink<Quad> createDeleteDataSink() {
        return new Sink<Quad>() {
            @Override
            public void send(Quad quad) {
                deleteFromDatasetGraph(datasetGraph, quad);
            }

            @Override
            public void flush() {
                SystemARQ.sync(datasetGraph);
            }

            @Override
            public void close() {}
        };
    }

    @Override
    public void visit(UpdateDataDelete update) {
        for ( Quad quad : update.getQuads() )
            deleteFromDatasetGraph(datasetGraph, quad);
    }

    @Override
    public void visit(UpdateDeleteWhere update) {
        List<Quad> quads = update.getQuads() ;
        // Removed from SPARQL : Convert bNodes to named variables first.
        //quads = convertBNodesToVariables(quads) ;
        
        // Convert quads to a pattern.
        Element el = elementFromQuads(quads) ;
        
        // Decided to serialize the bindings, but could also have decided to
        // serialize the quads after applying the template instead.
        
        ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(datasetGraph.getContext());
        DataBag<Binding> db = BagFactory.newDefaultBag(policy, SerializationFactoryFinder.bindingSerializationFactory());
        try {
            Iterator<Binding> bindings = evalBindings(el);
            db.addAll(bindings);
            Iter.close(bindings);

            Iterator<Binding> it = db.iterator();
            execDelete(datasetGraph, quads, null, it);
            Iter.close(it);
        }
        finally {
            db.close();
        }
    }
    
    @Override
    public void visit(UpdateModify update) {
        Node withGraph = update.getWithIRI();
        Element elt = update.getWherePattern();

        // null or a dataset for USING clause.
        // USING/USING NAMED
        DatasetGraph dsg = processUsing(update);

        // -------------------
        // WITH
        // USING overrides WITH
        if ( dsg == null && withGraph != null ) {
            // Subtle difference : WITH <uri>... WHERE {}
            // and an empty/unknown graph <uri>
            //   rewrite with GRAPH -> no match.
            //   redo as dataset with different default graph -> match
            // SPARQL is unclear about what happens when the graph does not exist.
            //   but the rewrite with ElementNamedGraph is closer to SPARQL.
            // Better, treat as
            // WHERE { GRAPH <with> { ... } }
            // This is the SPARQL wording (which is a bit loose).  
            elt = new ElementNamedGraph(withGraph, elt) ;
        }

        // WITH :
        // The quads from deletion/insertion are altered when streamed
        // into the templates later on. 
        
        // -------------------
        
        if ( dsg == null )
            dsg = datasetGraph ;
        
        Query query = elementToQuery(elt) ;
        ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(datasetGraph.getContext());
        DataBag<Binding> db = BagFactory.newDefaultBag(policy, SerializationFactoryFinder.bindingSerializationFactory()) ;
        try {
            Iterator<Binding> bindings = evalBindings(query, dsg, inputBinding, context);

            if ( false ) {
                List<Binding> x = Iter.toList(bindings);
                System.out.printf("====>> Bindings (%d)\n", x.size());
                Iter.print(System.out, x.iterator());
                System.out.println("====<<");
                bindings = Iter.iter(x);
            }
            db.addAll(bindings);
            Iter.close(bindings);

            Iterator<Binding> it = db.iterator();
            execDelete(datasetGraph, update.getDeleteQuads(), withGraph, it);
            Iter.close(it);

            Iterator<Binding> it2 = db.iterator();
            execInsert(datasetGraph, update.getInsertQuads(), withGraph, it2);
            Iter.close(it2);
        }
        finally {
            db.close();
        }
    }

    // Indirection for subsystems to support USING/USING NAMED.
    protected DatasetGraph processUsing(UpdateModify update) {
        if ( update.getUsing().size() == 0 && update.getUsingNamed().size() == 0 )
            return null;
        return DynamicDatasets.dynamicDataset(update.getUsing(), update.getUsingNamed(), datasetGraph, false);
    }

    private Graph graphOrDummy(DatasetGraph dsg, Node gn) {
        Graph g = graph(datasetGraph, gn);
        if ( g == null )
            g = GraphFactory.createGraphMem();
        return g;
    }

    protected Element elementFromQuads(List<Quad> quads) {
        ElementGroup el = new ElementGroup();
        ElementTriplesBlock x = new ElementTriplesBlock();
        // Maybe empty??
        el.addElement(x);
        Node g = Quad.defaultGraphNodeGenerated;

        for ( Quad q : quads ) {
            if ( q.getGraph() != g ) {
                g = q.getGraph();
                x = new ElementTriplesBlock();
                if ( g == null || g == Quad.defaultGraphNodeGenerated )
                    el.addElement(x);
                else {
                    ElementNamedGraph eng = new ElementNamedGraph(g, x);
                    el.addElement(eng);
                }
            }
            x.addTriple(q.asTriple());
        }
        return el;
    }

    // JENA-1059 : optimization : process templates for ground triples and do these once.
    // execDelete ; execInsert
    // Quads involving only IRIs and literals do not change from binding to
    // binding so any inserts, rather than repeatedly if they are going to be
    // done at all. Note bNodes (if legal at this point) change from template
    // instantiation to instantiation.
    /**
     * Split quads into ground terms (no variables) and templated quads.
     * @param quads
     * @return Pair of (ground quads, templated quads) 
     */
    private static Pair<List<Quad>, List<Quad>> split(Collection<Quad> quads) {
        // Guess size.
        //    Pre-size in case large (i.e. 10K+). 
        List<Quad> constQuads = new ArrayList<>(quads.size()) ;
        //    ... in which case we assume the templated triples are small / non-existent.
        List<Quad> templateQuads = new ArrayList<>() ;
        quads.forEach((q)-> {
            if ( constQuad(q))
                constQuads.add(q) ;
            else
                templateQuads.add(q) ;
        }) ;
        return Pair.create(constQuads, templateQuads);
    }

    private static boolean constQuad(Quad quad) {
        return constTerm(quad.getGraph())     && constTerm(quad.getSubject()) &&
               constTerm(quad.getPredicate()) && constTerm(quad.getObject());
    }
    
    private static boolean constTerm(Node n) {
        return n.isURI() || n.isLiteral() ;
    }

    protected static void execDelete(DatasetGraph dsg, List<Quad> quads, Node dftGraph, Iterator<Binding> bindings) {
        Pair<List<Quad>, List<Quad>> p = split(quads) ;
        execDelete(dsg, p.getLeft(), p.getRight(), dftGraph, bindings) ;
    }
    
    protected static void execDelete(DatasetGraph dsg, List<Quad> onceQuads, List<Quad> templateQuads, Node dftGraph, Iterator<Binding> bindings) {
        if ( onceQuads != null && bindings.hasNext() ) {
            onceQuads = remapDefaultGraph(onceQuads, dftGraph) ;
            onceQuads.forEach(q->deleteFromDatasetGraph(dsg, q)) ;
        }
        Iterator<Quad> it = template(templateQuads, dftGraph, bindings) ;
        if ( it == null )
            return ;
        it.forEachRemaining(q->deleteFromDatasetGraph(dsg, q)) ;
    }

    protected static void execInsert(DatasetGraph dsg, List<Quad> quads, Node dftGraph, Iterator<Binding> bindings) {
        Pair<List<Quad>, List<Quad>> p = split(quads) ;
        execInsert(dsg, p.getLeft(), p.getRight(), dftGraph, bindings) ;
    }
    
    protected static void execInsert(DatasetGraph dsg, List<Quad> onceQuads, List<Quad> templateQuads, Node dftGraph, Iterator<Binding> bindings) {
        if ( onceQuads != null && bindings.hasNext() ) {
            onceQuads = remapDefaultGraph(onceQuads, dftGraph) ;
            onceQuads.forEach((q)->addToDatasetGraph(dsg, q)) ;
        }
        Iterator<Quad> it = template(templateQuads, dftGraph, bindings) ;
        if ( it == null )
            return ;
        it.forEachRemaining((q)->addToDatasetGraph(dsg, q)) ;
    }

    // Catch all individual adds of quads
    private static void addToDatasetGraph(DatasetGraph datasetGraph, Quad quad) {
        // Check legal triple.
        if ( quad.isLegalAsData() )
            datasetGraph.add(quad);
        // Else drop.
        // Log.warn(UpdateEngineWorker.class, "Bad quad as data: "+quad) ;
    }

    // Catch all individual deletes of quads
    private static void deleteFromDatasetGraph(DatasetGraph datasetGraph, Quad quad) {
        if ( datasetGraph instanceof DatasetGraphReadOnly )
            Log.warn(UpdateEngineWorker.class, "Read only dataset");
        datasetGraph.delete(quad);
    }

    protected Query elementToQuery(Element pattern) {
        if ( pattern == null )
            return null;
        Query query = new Query();
        query.setQueryPattern(pattern);
        query.setQuerySelectType();
        query.setQueryResultStar(true);
        query.resetResultVars();
        return query;
    }

    protected Iterator<Binding> evalBindings(Element pattern) {
        Query query = elementToQuery(pattern);
        return evalBindings(query, datasetGraph, inputBinding, context);
    }

    protected static Iterator<Binding> evalBindings(Query query, DatasetGraph dsg, Binding inputBinding, Context context) {
        // The UpdateProcessorBase already copied the context and made it safe
        // ... but that's going to happen again :-(

        Iterator<Binding> toReturn;

        if ( query != null ) {
            Plan plan = QueryExecutionFactory.createPlan(query, dsg, inputBinding, context);
            toReturn = plan.iterator();
        } else {
            toReturn = Iter.singleton((null != inputBinding) ? inputBinding : BindingRoot.create());
        }
        return toReturn;
    }

    protected static Graph graph(DatasetGraph datasetGraph, Node gn) {
        if ( gn == null || gn == Quad.defaultGraphNodeGenerated )
            return datasetGraph.getDefaultGraph();
        else
            return datasetGraph.getGraph(gn);
    }

    protected static Graph graph(DatasetGraph datasetGraph, Target target) {
        if ( target.isDefault() )
            return datasetGraph.getDefaultGraph();
        if ( target.isOneNamedGraph() )
            return graph(datasetGraph, target.getGraph());
        error("Target does not name one graph: " + target);
        return null;
    }

    protected static void error(String msg) {
        throw new UpdateException(msg);
    }
}

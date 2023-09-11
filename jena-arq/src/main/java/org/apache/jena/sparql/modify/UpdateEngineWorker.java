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

import static org.apache.jena.sparql.modify.TemplateLib.remapDefaultGraph;
import static org.apache.jena.sparql.modify.TemplateLib.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.data.BagFactory;
import org.apache.jena.atlas.data.DataBag;
import org.apache.jena.atlas.data.ThresholdPolicy;
import org.apache.jena.atlas.data.ThresholdPolicyFactory;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.riot.*;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.core.*;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecDatasetBuilder;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.GraphOps;
import org.apache.jena.sparql.modify.request.*;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.system.SerializationFactoryFinder;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateException;

/** Implementation of general purpose update request execution */
public class UpdateEngineWorker implements UpdateVisitor
{
    protected final DatasetGraph datasetGraph;
    protected final boolean autoSilent = true;  // DROP and CREATE
    protected final Binding inputBinding;       // Used for UpdateModify only: substitution is better.
    protected final Context context;

    public UpdateEngineWorker(DatasetGraph datasetGraph, Binding inputBinding, Context context) {
        this.datasetGraph = datasetGraph;
        this.inputBinding = inputBinding;
        this.context = context;
    }

    @Override
    public void visit(UpdateDrop update)
    { execDropClear(update, false); }

    @Override
    public void visit(UpdateClear update)
    { execDropClear(update, true); }

    protected void execDropClear(UpdateDropClear update, boolean isClear) {
        if ( update.isAll() ) {
            // ALL
            execDropClear(update, null, true);  // DROP is CLEAR on DEFAULT.
            execDropClearAllNamed(update, isClear);
        } else if ( update.isAllNamed() )
            // NAMED
            execDropClearAllNamed(update, isClear);
        else if ( update.isDefault() )
            // DEFAULT
            execDropClear(update, null, true);  // DROP is CLEAR on DEFAULT.
        else if ( update.isOneGraph() )
            // GRAPH iri
            execDropClear(update, update.getGraph(), isClear);
        else
            // Error: should not happen.
            throw new ARQInternalErrorException("Target is undefined: " + update.getTarget());
    }

    protected void execDropClear(UpdateDropClear update, Node g, boolean isClear) {
        // DROP always works.
        // """
        //   After successful completion of this operation, the specified graphs are no
        //   longer available for further graph update operations.
        // """
        boolean auto = autoSilent && !isClear;
        executeOperation( auto || update.isSilent(), () -> {
            if ( g != null && !datasetGraph.containsGraph(g) )
                    throw errorEx("No such graph: " + g);
            if ( isClear ) {
                if ( g == null || datasetGraph.containsGraph(g) )
                    graphOrThrow(datasetGraph, g).clear();
            } else {
                try {
                    datasetGraph.removeGraph(g);
                } catch (UnsupportedOperationException ex) {
                    throw new UpdateException("DROP of named graph not supported");
                }
            }
        });
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
            if ( !autoSilent && !update.isSilent() )
                throw errorEx("Graph store already contains graph : " + g);
            return;
        }
        // To be general, add an empty graph.
        // Most datasets implementations have "auto-create" so CREATE is a no-op.
        // But dataset of separate graphs needs this (check!) to trigger the graph.
        // This is "copy-in" of zero triples.
        executeOperation(update.isSilent(), () ->
                { try { datasetGraph.addGraph(g, GraphFactory.createDefaultGraph()); }
                  catch(UnsupportedOperationException ex) {
                      throw new UpdateException("CREATE of named graph not supported");
                }
        });
    }

    @Override
    public void visit(UpdateLoad update) {
        // LOAD SILENT? iri ( INTO GraphRef )?
        String source = update.getSource();
        Node dest = update.getDest();
        executeOperation(update.isSilent(), ()->{
            Graph graph = graphOrThrow(datasetGraph, dest);
            // We must load buffered if silent so that the dataset graph sees
            // all or no triples/quads when there is a parse error
            // (no nested transaction abort).
            try {
                boolean loadBuffered = update.isSilent() || ! datasetGraph.supportsTransactionAbort();
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
                    return;
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
            } catch (RiotException ex) {
                if ( !update.isSilent() ) {
                    throw new UpdateException("Failed to LOAD '" + source + "' :: " + ex.getMessage(), ex);
                }
            }
        });
    }

    @Override
    public void visit(UpdateAdd update) {
        executeOperation(update.isSilent(), ()->{
            // ADD SILENT? (DEFAULT or GRAPH) TO (DEFAULT or GRAPH)
            validateBinaryGraphOp(update);
            if ( update.getSrc().equals(update.getDest()) )
                return;
            // Different source and destination.
            gsAddTriples(datasetGraph, update.getSrc(), update.getDest());
        });
    }

    @Override
    public void visit(UpdateCopy update) {
        executeOperation(update.isSilent(), ()->{
            // COPY SILENT? (DEFAULT or GRAPH) TO (DEFAULT or GRAPH)
            validateBinaryGraphOp(update);
            if ( update.getSrc().equals(update.getDest()) )
                // Same source and destination.
                return;
            // Different source and destination.
            gsCopy(datasetGraph, update.getSrc(), update.getDest());
        });
    }

    @Override
    public void visit(UpdateMove update) {
        executeOperation(update.isSilent(), ()->{
            // MOVE SILENT? (DEFAULT or GRAPH) TO (DEFAULT or GRAPH)
            validateBinaryGraphOp(update);
            if ( update.getSrc().equals(update.getDest()) )
                // Same source and destination.
                return;
            // Different source and destination.
            gsCopy(datasetGraph, update.getSrc(), update.getDest());
            gsDrop(datasetGraph, update.getSrc());
        });
    }

    /** Test whether the operation of G1 to G2 is valid */
    private void validateBinaryGraphOp(UpdateBinaryOp update) {
        if ( update.getSrc().isDefault() )
            return;
        if ( update.getSrc().isOneNamedGraph() ) {
            Node gn = update.getSrc().getGraph();
            if ( !datasetGraph.containsGraph(gn) )
                throw errorEx("No such graph: " + gn);
            return;
        }
        throw errorEx("Invalid source target for operation; " + update.getSrc());
    }

    // ----
    // Core operations
    /** Copy from src to dst : copy overwrites (= deletes) the old contents */
    protected static void gsCopy(DatasetGraph dsg, Target src, Target dest) {
        if ( dest.equals(src) )
            return;
        gsClear(dsg, dest);
        gsAddTriples(dsg, src, dest);
    }

    /** Add triples from src to dest */
    protected static void gsAddTriples(DatasetGraph dsg, Target src, Target dest) {
        Graph gSrc = graphOrThrow(dsg, src);
        Graph gDest = graphOrThrow(dsg, dest);
        GraphOps.addAll(gDest, gSrc.find());
    }

    /** Clear target */
    protected static void gsClear(DatasetGraph dsg, Target target) {
        // No create - we tested earlier.
        Graph g = graphOrThrow(dsg, target);
        g.clear();
    }

    /** Remove the target graph */
    protected static void gsDrop(DatasetGraph dsg, Target target) {
        if ( target.isDefault() )
            dsg.getDefaultGraph().clear();
        else
            dsg.removeGraph(target.getGraph());
    }

    // ----

    @Override
    public void visit(UpdateDataInsert update) {
        for ( Quad quad : update.getQuads() )
            addToDatasetGraph(datasetGraph, quad);
    }

    @Override
    public void visit(UpdateDataDelete update) {
        for ( Quad quad : update.getQuads() )
            deleteFromDatasetGraph(datasetGraph, quad);
    }

    @Override
    public void visit(UpdateDeleteWhere update) {
        List<Quad> quads = update.getQuads();
        // Removed from SPARQL : Convert bNodes to named variables first.
        //quads = convertBNodesToVariables(quads);

        // Convert quads to a pattern.
        Element el = elementFromQuads(quads);

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
            elt = new ElementNamedGraph(withGraph, elt);
        }

        // WITH :
        // The quads from deletion/insertion are altered when streamed
        // into the templates later on.

        // -------------------

        if ( dsg == null )
            dsg = datasetGraph;

        Query query = elementToQuery(elt);
        ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(datasetGraph.getContext());
        DataBag<Binding> db = BagFactory.newDefaultBag(policy, SerializationFactoryFinder.bindingSerializationFactory());
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
    // execDelete; execInsert
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
        List<Quad> constQuads = new ArrayList<>(quads.size());
        //    ... in which case we assume the templated triples are small / non-existent.
        List<Quad> templateQuads = new ArrayList<>();
        quads.forEach((q)-> {
            if ( constQuad(q))
                constQuads.add(q);
            else
                templateQuads.add(q);
        });
        return Pair.create(constQuads, templateQuads);
    }

    private static boolean constQuad(Quad quad) {
        return constTerm(quad.getGraph())     && constTerm(quad.getSubject()) &&
               constTerm(quad.getPredicate()) && constTerm(quad.getObject());
    }

    private static boolean constTerm(Node n) {
        return n.isURI() || n.isLiteral();
    }

    protected static void execDelete(DatasetGraph dsg, List<Quad> quads, Node dftGraph, Iterator<Binding> bindings) {
        Pair<List<Quad>, List<Quad>> p = split(quads);
        execDelete(dsg, p.getLeft(), p.getRight(), dftGraph, bindings);
    }

    protected static void execDelete(DatasetGraph dsg, List<Quad> onceQuads, List<Quad> templateQuads, Node dftGraph, Iterator<Binding> bindings) {
        if ( onceQuads != null && bindings.hasNext() ) {
            onceQuads = remapDefaultGraph(onceQuads, dftGraph);
            onceQuads.forEach(q->deleteFromDatasetGraph(dsg, q));
        }
        Iterator<Quad> it = template(templateQuads, dftGraph, bindings);
        if ( it == null )
            return;
        it.forEachRemaining(q->deleteFromDatasetGraph(dsg, q));
    }

    protected static void execInsert(DatasetGraph dsg, List<Quad> quads, Node dftGraph, Iterator<Binding> bindings) {
        Pair<List<Quad>, List<Quad>> p = split(quads);
        execInsert(dsg, p.getLeft(), p.getRight(), dftGraph, bindings);
    }

    protected static void execInsert(DatasetGraph dsg, List<Quad> onceQuads, List<Quad> templateQuads, Node dftGraph, Iterator<Binding> bindings) {
        if ( onceQuads != null && bindings.hasNext() ) {
            onceQuads = remapDefaultGraph(onceQuads, dftGraph);
            onceQuads.forEach((q)->addToDatasetGraph(dsg, q));
        }
        Iterator<Quad> it = template(templateQuads, dftGraph, bindings);
        if ( it == null )
            return;
        it.forEachRemaining((q)->addToDatasetGraph(dsg, q));
    }

    // Catch all individual adds of quads
    private static void addToDatasetGraph(DatasetGraph datasetGraph, Quad quad) {
        // Check legal triple.
        if ( quad.isLegalAsData() )
            datasetGraph.add(quad);
        // Else drop.
        // Log.warn(UpdateEngineWorker.class, "Bad quad as data: "+quad);
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
        if ( query == null ) {
            Binding binding = (null != inputBinding) ? inputBinding : BindingRoot.create();
            return Iter.singleton(binding);
        }

        // Not QueryExecDataset.dataset(...) because of initialBinding.
        QueryExecDatasetBuilder builder = QueryExecDatasetBuilder.create().dataset(dsg).query(query);
        if ( inputBinding != null ) {
            // Must use initialBinding - it puts the input in the results, unlike substitution.
            builder.initialBinding(inputBinding);
            // substitution does not put results in the output.
            // builder.substitution(inputBinding);
        }
        QueryExec qExec = builder.build();
        return qExec.select();
    }

    /**
     * Execute.
     * <br/>
     * Return true if successful.
     * <br/>
     *
     * Otherwise if not silent: throw UpdateException, if silent, return false
     */
    private boolean executeOperation(boolean isSilent, Runnable action) {
        try {
            action.run();
            return true;
        } catch (UpdateException ex) {
            if ( isSilent )
                return false;
            throw ex;
        }
    }

    protected static Graph graphOrThrow(DatasetGraph datasetGraph, Node gn) {
        if ( gn == null || Quad.isDefaultGraph(gn) )
            return datasetGraph.getDefaultGraph();
        Graph g = datasetGraph.getGraph(gn);
        if ( g == null )
            throw errorEx("No such graph in this dataset: "+gn);
        return g;
    }

    protected static Graph graphOrThrow(DatasetGraph datasetGraph, Target target) {
        if ( target.isDefault() )
            return datasetGraph.getDefaultGraph();
        if ( target.isOneNamedGraph() )
            return graphOrThrow(datasetGraph, target.getGraph());
        throw errorEx("Target does not name one graph: " + target);
    }

    protected static UpdateException errorEx(String msg) {
        return new UpdateException(msg);
    }
}

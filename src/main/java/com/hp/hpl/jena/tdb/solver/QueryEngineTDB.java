/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import java.util.Set ;

import org.openjena.atlas.logging.Log ;


import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.GraphNamedTDB ;
import com.hp.hpl.jena.tdb.store.TransformDynamicDataset ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

// This exists to intercept the query execution setup.
//  e.g choose the transformation optimizations
// then to make the quad form.
// TDB also uses a custom OpExecutor to intercept certain part 
// of the Op evaluations

public class QueryEngineTDB extends QueryEngineMain
{
    // ---- Wiring
    static public QueryEngineFactory getFactory() { return factory ; } 
    static public void register()       { QueryEngineRegistry.addFactory(factory) ; }
    static public void unregister()     { QueryEngineRegistry.removeFactory(factory) ; }
    
    private Binding initialInput ;

    // ---- Object
    protected QueryEngineTDB(Op op, DatasetGraphTDB dataset, Binding input, Context context)
    {
        super(op, dataset, input, context) ;
        this.initialInput = input ;

        // Push up to QueryEngineMain after TDB 0.8.5 release
        if ( dataset.getContext() != null )
            context.setAll(dataset.getContext()) ;
    }
    
    protected QueryEngineTDB(Query query, DatasetGraphTDB dataset, Binding input, Context context)
    { 
        super(query, dataset, input, context) ; 
        this.initialInput = input ; 
        if ( dataset.getContext() != null )
            context.setAll(dataset.getContext()) ;
    }
    
    // Choose the algebra-level optimizations to invoke. 
    @Override
    protected Op modifyOp(Op op)
    {
        op = Substitute.substitute(op, initialInput) ;
        // Optimize (high-level)
        op = super.modifyOp(op) ;
        // Quadification
        op = Algebra.toQuadForm(op) ;

        // Could apply dynamic dataset transform before everything else
        // but default merged graphs works on quads. 
        op = dynamicDatasetOp(op, context) ;
        
        // Record it.
        setOp(op) ;
        
        Explain.explain("ALGEBRA", op, context) ;
        return op ;
    }

    @Override
    public QueryIterator eval(Op op, DatasetGraph dsg, Binding input, Context context)
    {
        // Top of execution of a query.
        // Op is quad'ed by now but there still may be some (graph ....) forms e.g. paths
        
        // Fix DatasetGraph for global union.
        if ( context.isTrue(TDB.symUnionDefaultGraph) ) 
        {
            // Rewrite so that any explicitly named "default graph" is union graph.
            Transform t = new TransformGraphRename(Quad.defaultGraphNodeGenerated, Quad.unionGraph)  ;
            op = Transformer.transform(t, op) ;

            // And set the default graph to be the union graph as well.
            DatasetGraphTDB ds = ((DatasetGraphTDB)dsg).duplicate() ;
            ds.setEffectiveDefaultGraph(new GraphNamedTDB(ds, Quad.unionGraph)) ;
            dsg = ds ;
        }
        return super.eval(op, dsg, input, context) ;
    }
    
    // Execution time (needs wiring to ARQ).
    public long getMillis() { return -1 ; }
    
    // ---- Rewrite that looks for a fixed node as the graph name 
    // (in (graph) and (quad)) and changes it to another one.
    private static class TransformGraphRename extends TransformCopy
    { 
        private Node oldGraphName ;
        private Node newGraphName ;

        public TransformGraphRename(Node oldGraphName, Node newGraphName)
        {
            this.oldGraphName = oldGraphName ;
            this.newGraphName = newGraphName ;
        }

        // Does not affect variables.
        @Override
        public Op transform(OpGraph opGraph, Op x)
        { 
            if ( opGraph.getNode().equals(oldGraphName) )
                opGraph = new OpGraph(newGraphName, x) ;
            return super.transform(opGraph, x) ;
        }

        @Override
        public Op transform(OpQuadPattern opQuadPattern)
        {
            if ( opQuadPattern.getGraphNode().equals(oldGraphName) )
                opQuadPattern = new OpQuadPattern(newGraphName, opQuadPattern.getBasicPattern()) ;
            return super.transform(opQuadPattern) ;
        }
    } ;
    
    
    
    // ---- Factory
    private static QueryEngineFactory factory = new QueryEngineFactoryTDB() ;
        
    private static class QueryEngineFactoryTDB implements QueryEngineFactory
    {
        public boolean accept(Query query, DatasetGraph dataset, Context context) 
        { return (dataset instanceof DatasetGraphTDB) ; }

        public Plan create(Query query, DatasetGraph ds, Binding input, Context context)
        {
            DatasetGraphTDB dataset = (DatasetGraphTDB)ds ;
            
            if ( false )
            {
                // Old behaviour - use the general purpose query engine.
                if ( query.hasDatasetDescription() )
                {
                    QueryEngineMain engine = new QueryEngineMain(query, dataset, input, context) ;
                    return engine.getPlan() ;
                }
            }
            
            dynamicDatasetQE(query, context) ;
            
            Explain.explain("QUERY", query, context) ;
            QueryEngineTDB engine = new QueryEngineTDB(query, dataset, input, context) ;
            return engine.getPlan() ;
        }
        
        public boolean accept(Op op, DatasetGraph dataset, Context context) 
        { return (dataset instanceof DatasetGraphTDB) ; }

        public Plan create(Op op, DatasetGraph dataset, Binding binding, Context context)
        {
            //Explain.explain("QUERY", query, context) ;
            QueryEngineTDB engine = new QueryEngineTDB(op, (DatasetGraphTDB)dataset, binding, context) ;
            return engine.getPlan() ;
        }
    } ;
    
    private static void dynamicDatasetQE(Query query,  Context context)
    {
        if ( query.hasDatasetDescription() )
        {
            Set<Node> defaultGraphs = SolverLib.convertToNodes(query.getGraphURIs()) ; 
            Set<Node> namedGraphs = SolverLib.convertToNodes(query.getNamedGraphURIs()) ;
            
            context.set(SystemTDB.symDatasetDefaultGraphs, defaultGraphs) ;
            context.set(SystemTDB.symDatasetNamedGraphs, namedGraphs) ;
        }
    }
    
    private static Op dynamicDatasetOp(Op op,  Context context)
    {
        Transform transform = null ;
    
        try {
            @SuppressWarnings("unchecked")
            Set<Node> defaultGraphs = (Set<Node>)(context.get(SystemTDB.symDatasetDefaultGraphs)) ;
            @SuppressWarnings("unchecked")
            Set<Node> namedGraphs = (Set<Node>)(context.get(SystemTDB.symDatasetNamedGraphs)) ;
            if ( defaultGraphs != null || namedGraphs != null )
                transform = new TransformDynamicDataset(defaultGraphs, 
                                                        namedGraphs, 
                                                        context.isTrue(TDB.symUnionDefaultGraph)) ;
        } catch (ClassCastException ex)
        {
            Log.warn(QueryEngineTDB.class, "Bad dynamic dataset description (ClassCastException)", ex) ;
            transform = null ;
            return op ;
        }

        // Apply dynamic dataset modifications.
        if ( transform != null )
            op = Transformer.transform(transform, op) ;
        return op ;
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
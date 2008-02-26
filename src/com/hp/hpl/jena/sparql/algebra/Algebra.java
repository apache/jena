/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.PrefixMapping;

import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.algebra.opt.TransformEqualityFilter;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.sse.builders.BuilderOp;
import com.hp.hpl.jena.sparql.syntax.Element;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;

/** Utilities to produce SPARQL algebra */
public class Algebra
{
    public static boolean AllowOptimization = false ;
    private static Transform optimization = new TransformEqualityFilter() ;
    
    // -------- Optimize
    
    public static Op optimize(Op op) { return optimize(op, true) ; }
    
    private static Op optimize(Op op, boolean optimize)
    { 
        if ( op == null )
            return null ;
        if ( ! optimize )
            return op ;
        if ( !AllowOptimization || optimization == null )
            return op ;
        return Transformer.transform(optimization, op) ;
    }    
    
    // -------- Compile
    
    /** Compile a query - pattern and modifiers.  Optionally optimize the algebra expression. */
    public static Op compile(Query query, boolean optimize)
    {
        if ( query == null )
            return null ;
        Op op = new AlgebraGenerator().compile(query) ;
        return optimize(op, optimize) ;
    }

    /** Compile a pattern.  Optionally optimize the algebra expression. */
    public static Op compile(Element elt, boolean optimize)
    {
        if ( elt == null )
            return null ;
        Op op = new AlgebraGenerator().compile(elt) ;
        return optimize(op, optimize) ;

    }

    /** Compile a query - pattern and modifiers - to quad form.  Optionally optimize the algebra expression. */
    public static Op compileQuad(Query query, boolean optimize)
    {
        if ( query == null )
            return null ;
        Op op = new AlgebraGeneratorQuad().compile(query) ;
        return optimize(op, optimize) ;
    }

    /** Compile a pattern - to quad form.  Optionally optimize the algebra expression. */
    public static Op compileQuad(Element elt, boolean optimize)
    {
        if ( elt == null )
            return null ;
        Op op = new AlgebraGeneratorQuad().compile(elt) ;
        return optimize(op, optimize) ;
    }

    // ---- SSE
    
    static public Op read(String filename)
    {
        Item item = SSE.readFile(filename) ;
        return parse(item) ;
    }

    static public Op parse(String string)
    {
        Item item = SSE.parse(string) ;
        return parse(item) ;
    }
    

    static public Op parse(String string, PrefixMapping pmap)
    {
        Item item = SSE.parse(string, pmap) ;
        return parse(item) ;
    }
    
    static public Op parse(Item item)
    {
        Op op = BuilderOp.build(item) ;
        return op ;
    }
    
    // -------- Execute

    static public QueryIterator exec(Op op, Dataset ds)
    {
        return exec(op, ds.asDatasetGraph()) ;
    }

    static public QueryIterator exec(Op op, Model model)
    {
        return exec(op, model.getGraph()) ;
    }

    static public QueryIterator exec(Op op, Graph graph)
    {
        return exec(op, new DataSourceGraphImpl(graph)) ;
    }

    static public QueryIterator exec(Op op, DatasetGraph ds)
    {
        QueryEngineFactory f = QueryEngineRegistry.findFactory(op, ds, null) ;
        Plan plan = f.create(op, ds, BindingRoot.create(), null) ;
        return plan.iterator() ;
    }

    //  Reference engine

    static public QueryIterator execRef(Op op, Dataset ds)
    {
        return execRef(op, ds.asDatasetGraph()) ;
    }

    static public QueryIterator execRef(Op op, Model model)
    {
        return execRef(op, model.getGraph()) ;
    }

    static public QueryIterator execRef(Op op, Graph graph)
    {
        return execRef(op, new DataSourceGraphImpl(graph)) ;
    }

    static public QueryIterator execRef(Op op, DatasetGraph ds)
    {
        QueryEngineRef qe = new QueryEngineRef(op, ds, null) ;
        return qe.getPlan().iterator() ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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
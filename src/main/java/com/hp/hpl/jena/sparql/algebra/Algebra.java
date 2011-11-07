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

package com.hp.hpl.jena.sparql.algebra;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.algebra.optimize.Optimize ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderOp ;
import com.hp.hpl.jena.sparql.syntax.Element ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Utilities to produce SPARQL algebra */
public class Algebra
{
    // -------- Optimize
    
    /** Apply static transformations to a query to optimize it */
    public static Op optimize(Op op) { return optimize(op, null) ; }
    
    /** Apply static transformations to a query to optimize it */
    public static Op optimize(Op op, Context context)
    {
        if ( context == null )
            context = ARQ.getContext() ;
        // Call-through to somewhere to manage all the optimizations
        if ( op == null )
            return null ;
        return Optimize.optimize(op, context) ;
    }   
    
    // -------- Compile
    
    /** Compile a query - pattern and modifiers.  */
    public static Op compile(Query query)
    {
        if ( query == null )
            return null ;
        return new AlgebraGenerator().compile(query) ;
    }

    /** Compile a pattern.*/
    public static Op compile(Element elt)
    {
        if ( elt == null )
            return null ;
        return new AlgebraGenerator().compile(elt) ;
    }

    /** Turn an algebra expression into quad form */
    public static Op toQuadForm(Op op)
    {
        return AlgebraQuad.quadize(op) ;
    }
    
    /** Transform an algebra expression so that default graph is union of the named graphs. */
    public static Op unionDefaultGraph(Op op)
    {
        return TransformUnionQuery.transform(op) ;
    }
    
    // -------- SSE uses these operations ...
    
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
        return exec(op, DatasetGraphFactory.createOneGraph(graph)) ;
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
        return execRef(op, DatasetGraphFactory.createOneGraph(graph)) ;
    }

    static public QueryIterator execRef(Op op, DatasetGraph dsg)
    {
        QueryEngineRef qe = new QueryEngineRef(op, dsg, ARQ.getContext().copy()) ;
        return qe.getPlan().iterator() ;
    }
    
    // This is the SPARQL merge rule. 
    public static Binding merge(Binding bindingLeft, Binding bindingRight)
    {
        // Test to see if compatible: Iterate over variables in left
        boolean matches = compatible(bindingLeft, bindingRight) ;
        
        if ( ! matches ) 
            return null ;
        
        // If compatible, merge. Iterate over variables in right but not in left.
        BindingMap b = BindingFactory.create(bindingLeft) ;
        for ( Iterator<Var> vIter = bindingRight.vars() ; vIter.hasNext() ; )
        {
            Var v = vIter.next();
            Node n = bindingRight.get(v) ;
            if ( ! bindingLeft.contains(v) )
                b.add(v, n) ;
        }
        return b ;
    }
    
    public static boolean compatible(Binding bindingLeft, Binding bindingRight)
    {
        // Test to see if compatible: Iterate over variables in left
        for ( Iterator<Var> vIter = bindingLeft.vars() ; vIter.hasNext() ; )
        {
            Var v = vIter.next();
            Node nLeft  = bindingLeft.get(v) ; 
            Node nRight = bindingRight.get(v) ;
            
            if ( nRight != null && ! nRight.equals(nLeft) )
                return false ;
        }
        return true ;
    }
    
    public static boolean disjoint(Binding binding1, Binding binding2)
    {
        Iterator<Var> iterVar1 = binding1.vars() ;
        for ( ; iterVar1.hasNext() ; )
        {
            Var v = iterVar1.next() ; 
            if ( binding2.contains(v) )
                return false ;
        }
        return true ;
    }
}

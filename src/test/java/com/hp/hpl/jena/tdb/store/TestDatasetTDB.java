/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import org.junit.Test;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

/** Tests of datasets, prefixes, special URIs etc (see also {@link com.hp.hpl.jena.sparql.graph.GraphsTests} */
public class TestDatasetTDB extends BaseTest
{
    
    private static Dataset create()
    {
        return TDBFactory.createDataset() ;
    }
    
    private static void load(Model model, String file)
    {
        FileManager.get().readModel(model, file) ;
    }
    
    private static String base1 = "http://example/" ;
    private static String baseNS = "http://example/ns#" ;
    
    private static void load1(Model model)
    {
        model.setNsPrefix("", base1) ;
        Resource r1 = model.createResource(base1+"r1") ;
        Property p1 = model.createProperty(baseNS+"p1") ;
        model.add(r1, p1, "x1") ;
        model.add(r1, p1, "x2") ;
    }
    
    private static void load2(Model model)
    {
        Resource r1 = model.createResource(base1+"r2") ;
        Property p1 = model.createProperty(baseNS+"p1") ;
        model.add(r1, p1, "x1") ;
        model.add(r1, p1, "x2") ;
    }

    private static void load3(Model model)
    {
        Resource r1 = model.createResource(base1+"r3") ;
        Property p1 = model.createProperty(baseNS+"p2") ;
        model.add(r1, p1, "x1") ;
        model.add(r1, p1, "x2") ;
    }

    @Test public void prefix1()
    {
        Dataset ds = create() ;
        Model m = ds.getDefaultModel() ;
        load1(m) ;
        String x = m.expandPrefix(":x") ;
        assertEquals(x, base1+"x") ;
    }
    
    @Test public void prefix2()
    {
        Dataset ds = create() ;
        Model m = ds.getDefaultModel() ;
        load1(m) ;
        Model m2 = ds.getNamedModel("http://example/graph/") ;
        String x = m2.expandPrefix(":x") ;
        assertEquals(x, ":x") ;
    }
    
    @Test public void query1()
    {
        Dataset ds = create() ;
        Model m = ds.getDefaultModel() ;
        load1(m) ;
        
        String qs = "CONSTRUCT {?s ?p ?o } WHERE {?s ?p ?o}" ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qExec = QueryExecutionFactory.create(q, ds) ;
        Model m2 = qExec.execConstruct() ;
        assertTrue(m.isIsomorphicWith(m2)) ;
    }
    
    @Test public void query2()
    {
        Dataset ds = create() ;
        Model m = ds.getDefaultModel() ;
        load1(m) ;
        
        String qs = "CONSTRUCT {?s ?p ?o } WHERE { GRAPH <http://example/graph/> {?s ?p ?o}}" ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qExec = QueryExecutionFactory.create(q, ds) ;
        Model m2 = qExec.execConstruct() ;
        assertTrue(m2.isEmpty()) ;
    }
    
    static String defaultGraph = Quad.defaultGraphIRI.getURI() ; 
    static String unionGraph = Quad.unionGraph.getURI() ;
    
    @Test public void special1()
    {
        Dataset ds = create() ;
        Model m = ds.getDefaultModel() ;
        load1(m) ;
        
        String qs = "CONSTRUCT {?s ?p ?o } WHERE { GRAPH <"+defaultGraph+"> {?s ?p ?o}}" ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qExec = QueryExecutionFactory.create(q, ds) ;
        Model m2 = qExec.execConstruct() ;
        assertTrue(m.isIsomorphicWith(m2)) ;
    }
    
    @Test public void special2()
    {
        Dataset ds = create() ;

        load1(ds.getDefaultModel()) ;
        load2(ds.getNamedModel("http://example/graph1")) ;
        load3(ds.getNamedModel("http://example/graph2")) ;
        
        Model m = ModelFactory.createDefaultModel() ;
        load2(m) ;
        load3(m) ;
        
        String qs = "CONSTRUCT {?s ?p ?o } WHERE { GRAPH <"+unionGraph+"> {?s ?p ?o}}" ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qExec = QueryExecutionFactory.create(q, ds) ;
        Model m2 = qExec.execConstruct() ;
        assertTrue(m.isIsomorphicWith(m2)) ;
    }
    
    @Test public void special3()
    {
        Dataset ds = create() ;

        load1(ds.getDefaultModel()) ;
        load2(ds.getNamedModel("http://example/graph1")) ;
        load3(ds.getNamedModel("http://example/graph2")) ;
        
        Model m = ModelFactory.createDefaultModel() ;
        load2(m) ;
        load3(m) ;
        
        String qs = "CONSTRUCT {?s ?p ?o } WHERE { ?s ?p ?o }" ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qExec = QueryExecutionFactory.create(q, ds) ;
        qExec.getContext().set(TDB.symUnionDefaultGraph, true) ;
        Model m2 = qExec.execConstruct() ;
        assertTrue(m.isIsomorphicWith(m2)) ;
    }
    
    @Test public void special4()
    {
        Dataset ds = create() ;

        load1(ds.getDefaultModel()) ;
        load2(ds.getNamedModel("http://example/graph1")) ;
        load3(ds.getNamedModel("http://example/graph2")) ;
        
        Model m = ModelFactory.createDefaultModel() ;
        load2(m) ;
        load3(m) ;
        TDB.sync(ds) ;
        
        String qs = "PREFIX : <"+baseNS+"> SELECT (COUNT(?x) as ?c) WHERE { ?x (:p1|:p2) 'x1' }" ;
        Query q = QueryFactory.create(qs, Syntax.syntaxARQ) ; 
        QueryExecution qExec = QueryExecutionFactory.create(q, ds) ;
        qExec.getContext().set(TDB.symUnionDefaultGraph, true) ;
        long c1 = qExec.execSelect().next().getLiteral("c").getLong() ;
        qExec.close() ;
        
        qExec = QueryExecutionFactory.create(q, m) ;
        long c2 = qExec.execSelect().next().getLiteral("c").getLong() ;
        assertEquals(c1, c2) ; 
        qExec.close() ;
    }
    
    
    
    // Put a model into a general dataset and use it.
    @Test public void generalDataset1()
    {
        Dataset ds = create() ;
        load1(ds.getDefaultModel()) ;
        load2(ds.getNamedModel("http://example/graph1")) ;
        load3(ds.getNamedModel("http://example/graph2")) ;
        Model m = ds.getNamedModel("http://example/graph2") ;
        
        // Use graph2 as default model.
        DataSource ds2 = DatasetFactory.create() ;
        ds2.setDefaultModel(ds.getNamedModel("http://example/graph2")) ;
        
        String qs = "CONSTRUCT {?s ?p ?o } WHERE { ?s ?p ?o}" ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qExec = QueryExecutionFactory.create(q, ds2) ;
        Model m2 = qExec.execConstruct() ;
        assertTrue(m.isIsomorphicWith(m2)) ;
    }
    
    @Test public void generalDataset2()
    {
        Dataset ds = create() ;
        load1(ds.getDefaultModel()) ;
        load2(ds.getNamedModel("http://example/graph1")) ;
        load3(ds.getNamedModel("http://example/graph2")) ;
        Model m = ds.getNamedModel("http://example/graph2") ;
        
        // Use graph1 as a differently named model.
        DataSource ds2 = DatasetFactory.create() ;
        ds2.addNamedModel("http://example/graphOther", m) ;
        
        String qs = "CONSTRUCT {?s ?p ?o } WHERE { {?s ?p ?o} UNION { GRAPH <http://example/graphOther> {?s ?p ?o} } }" ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qExec = QueryExecutionFactory.create(q, ds2) ;
        Model m2 = qExec.execConstruct() ;
        assertTrue(m.isIsomorphicWith(m2)) ;
    }
    
    @Test public void generalDataset3()
    {
        Dataset ds = create() ;
        load1(ds.getDefaultModel()) ;
        load2(ds.getNamedModel("http://example/graph1")) ;
        load3(ds.getNamedModel("http://example/graph2")) ;
        Model m = ds.getDefaultModel() ;
        
        // Use the default model in one dataset as a named model in another.
        DataSource ds2 = DatasetFactory.create() ;
        ds2.addNamedModel("http://example/graphOther", m) ;
        
        String qs = "CONSTRUCT {?s ?p ?o } WHERE { {?s ?p ?o} UNION { GRAPH <http://example/graphOther> {?s ?p ?o} } }" ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qExec = QueryExecutionFactory.create(q, ds2) ;
        Model m2 = qExec.execConstruct() ;
        assertTrue(m.isIsomorphicWith(m2)) ;
    }

    // removeAll
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
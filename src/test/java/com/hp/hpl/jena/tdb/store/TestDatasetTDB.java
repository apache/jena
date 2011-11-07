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

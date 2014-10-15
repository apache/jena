/**
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

package org.apache.jena.query.text;

import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.lucene.store.Directory ;
import org.apache.lucene.store.RAMDirectory ;
import org.junit.Test ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.vocabulary.RDFS ;

public class TestTextTDB extends BaseTest
{
    private static Dataset create() {
        Dataset ds1 = TDBFactory.createDataset() ;
        Directory dir = new RAMDirectory() ;
        EntityDefinition eDef = new EntityDefinition("iri", "text", RDFS.label) ;
        TextIndex tidx = new TextIndexLucene(dir, eDef) ;
        Dataset ds = TextDatasetFactory.create(ds1, tidx) ;
        return ds ;
    }

    @Test public void textDB_1() {
        // Check the union graph stil works  
        Dataset ds = create() ;
        ds.getContext().set(TDB.symUnionDefaultGraph, true) ;
        Quad quad = SSE.parseQuad("(<g> <p> rdfs:label 'foo')") ;
        ds.asDatasetGraph().add(quad) ;
        Query q = QueryFactory.create("SELECT * { ?s ?p ?o }") ;
        QueryExecution qexec = QueryExecutionFactory.create(q, ds) ;
        ResultSet rs = qexec.execSelect() ;
        List<QuerySolution> x = Iter.toList(rs) ;
        assertEquals(1,x.size());
    }
    
    @Test public void textDB_2() {
        // Check text query and union graph
        Dataset ds = create() ;
        ds.getContext().set(TDB.symUnionDefaultGraph, true) ;
        Quad quad = SSE.parseQuad("(<g> <s> rdfs:label 'foo')") ;
        ds.asDatasetGraph().add(quad) ;
        
        String qs = StrUtils.strjoinNL("PREFIX text: <http://jena.apache.org/text#>",
                                       "PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>",
                                       "SELECT *",
                                       "{ ?s text:query 'foo' ;",
                                       "     rdfs:label 'foo'",
                                       "}"
                                       ) ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, ds) ;
        ResultSet rs = qexec.execSelect() ;
        List<QuerySolution> x = Iter.toList(rs) ;
        assertEquals(1,x.size());
    }
    
    @Test public void textDB_3() {
        Dataset ds = create() ;
        ds.getContext().set(TDB.symUnionDefaultGraph, true) ;
        data(ds, 
             "(<ex:g1> <s1> rdfs:label 'foo')",
             "(<ex:g2> <s2> rdfs:label 'bar')") ;

        ds.begin(ReadWrite.READ) ;
        String qs = StrUtils.strjoinNL(
            "PREFIX text: <http://jena.apache.org/text#>",
            "PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>",
            "SELECT *",
            "{ ?s text:query 'foo' ;",
            "     rdfs:label 'foo'",
            "}"
            ) ;
        
        Query q = QueryFactory.create(qs) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, ds) ;
        ResultSet rs = qexec.execSelect() ;
        List<QuerySolution> x = Iter.toList(rs) ;
        ds.end() ;
        assertEquals(1,x.size());
    }
    
    @Test public void textDB_4() {
        Dataset ds = create() ;
        data(ds, 
             "(<ex:g1> <s1> rdfs:label 'foo')",
             "(<ex:g1> <s2> rdfs:label 'apple')",
             "(<ex:g2> <s3> rdfs:label 'bar')") ;
        
        ds.begin(ReadWrite.READ) ;
        String qs = StrUtils.strjoinNL(
            "PREFIX text:   <http://jena.apache.org/text#>",
            "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
            "SELECT *",
            "FROM <ex:g1>",
            "{ ?s text:query 'foo' . ?s rdfs:label ?o }"
            ) ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, ds) ;
        ResultSet rs = qexec.execSelect() ;
        List<QuerySolution> x = Iter.toList(rs) ;
        ds.end() ;
        assertEquals(1,x.size());
    }

    @Test public void textDB_5() {
        Dataset ds = create() ;
        data(ds, 
             "(<ex:g1> <s1> rdfs:label 'foo')",
             "(<ex:g1> <s2> rdfs:label 'apple')",
             "(<ex:g2> <s3> rdfs:label 'food')") ;
        
        ds.begin(ReadWrite.READ) ;
        String qs = StrUtils.strjoinNL(
            "PREFIX text:   <http://jena.apache.org/text#>",
            "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
            "SELECT *",
            "FROM <"+Quad.unionGraph+">",
            "{ ?s text:query 'foo*' . ?s rdfs:label ?o }"
            ) ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, ds) ;
        ResultSet rs = qexec.execSelect() ;
        List<QuerySolution> x = Iter.toList(rs) ;
        ds.end() ;
        assertEquals(2,x.size());
    }

    @Test public void textDB_6() {
        Dataset ds = create() ;
        data(ds, 
             "(<ex:g1> <s1> rdfs:label 'foo')",
             "(<ex:g1> <s2> rdfs:label 'apple')",
             "(<ex:g2> <s3> rdfs:label 'food')") ;
        
        ds.begin(ReadWrite.READ) ;
        String qs = StrUtils.strjoinNL(
            "PREFIX text:   <http://jena.apache.org/text#>",
            "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
            "SELECT *",
            "{ GRAPH <"+Quad.unionGraph+">",
            "    { ?s text:query 'foo*' . ?s rdfs:label ?o }",
            "}"
            ) ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, ds) ;
        ResultSet rs = qexec.execSelect() ;
        List<QuerySolution> x = Iter.toList(rs) ;
        ds.end() ;
        assertEquals(2,x.size());
    }

    private static void data(Dataset ds, String... quadStrs) {
        for ( String qs : quadStrs ) {
            Quad quad = SSE.parseQuad(qs) ;
            ds.asDatasetGraph().add(quad) ;
        }
    }
    
    // With transactions
    // With FROM and FROM NAMED + TDB
}


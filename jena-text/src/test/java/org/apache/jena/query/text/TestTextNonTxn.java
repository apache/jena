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

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.vocabulary.RDFS ;
import org.apache.lucene.store.Directory ;
import org.apache.lucene.store.ByteBuffersDirectory ;
import org.junit.Test ;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** Test using various dataset implmentations without transactions
 *  No context-set union graph usage either.
 */  
@RunWith(Parameterized.class)
public class TestTextNonTxn
{
    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]>  data() {
        Creator<Dataset> plainFactory = ()->DatasetFactory.create();
        Creator<Dataset> timFactory = ()->DatasetFactory.createTxnMem();
        Creator<Dataset> tdb1Factory = ()->TDBFactory.createDataset();
        // TDB2 does not work with these, non transactional, tests.
        return Arrays.asList( new Object[][]{
            { "Plain", plainFactory , false } ,
            { "TIM",  timFactory , false } ,
            { "TDB1", tdb1Factory , true }
            // TDB2 requires transactions.
        });
    }
    private Dataset create() {
        Dataset ds1 = factory.create();
        Directory dir = new ByteBuffersDirectory() ;
        EntityDefinition eDef = new EntityDefinition("iri", "text");
        eDef.setPrimaryPredicate(RDFS.label);
        TextIndex tidx = new TextIndexLucene(dir, new TextIndexConfig(eDef)) ;
        Dataset ds = TextDatasetFactory.create(ds1, tidx) ;
        return ds ;
    }
    
    private final Creator<Dataset> factory;
    private final boolean dsFrom;
    
    public TestTextNonTxn(String name, Creator<Dataset> factory, Boolean dsFrom) {
        this.factory = factory;
        // Does FROM work by pulling graphs from the dataset?
        this.dsFrom = dsFrom;
    }

    @Test public void textNonTxn_from_named_graph_1() {
        assumeTrue(dsFrom);
        Dataset ds = create() ;
        data(ds, 
             "(<ex:g1> <s1> rdfs:label 'foo')",
             "(<ex:g1> <s2> rdfs:label 'apple')",
             "(<ex:g2> <s3> rdfs:label 'bar')") ;
        String qs = StrUtils.strjoinNL(
            "PREFIX text:   <http://jena.apache.org/text#>",
            "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
            "SELECT *",
            "FROM <ex:g1>",
            "{  ?s text:query 'foo*' . ?s rdfs:label ?o }"
            ) ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, ds) ;
        ResultSet rs = qexec.execSelect() ;
        List<QuerySolution> x = Iter.toList(rs) ;
        assertEquals(1,x.size());
    }

    @Test public void textNonTxn_from_union_graph() {
        assumeTrue(dsFrom);
        Dataset ds = create() ;
        data(ds, 
             "(<ex:g1> <s1> rdfs:label 'foo')",
             "(<ex:g1> <s2> rdfs:label 'apple')",
             "(<ex:g2> <s3> rdfs:label 'food')") ;
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
        assertEquals(2,x.size());
    }

    @Test public void textNonTxn_graph_union_graph() {
        Dataset ds = create() ;
        data(ds, 
             "(<ex:g1> <s1> rdfs:label 'foo')",
             "(<ex:g1> <s2> rdfs:label 'apple')",
             "(<ex:g2> <s3> rdfs:label 'food')") ;
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
        assertEquals(2,x.size());
    }
    
    @Test public void textDB_7_subject_bound_first() {
        Dataset ds = create() ;
        data(ds, 
            "(<ex:g1> <s1> rdfs:label 'foo')",
            "(<ex:g1> <s1> rdf:type <http://example.org/Entity>)",
            "(<ex:g1> <s2> rdfs:label 'apple')",
            "(<ex:g1> <s2> rdf:type <http://example.org/Entity>)",
            "(<ex:g2> <s3> rdfs:label 'food')",
            "(<ex:g2> <s3> rdf:type <http://example.org/Entity>)");
        String qs = StrUtils.strjoinNL(
            "PREFIX text:   <http://jena.apache.org/text#>",
            "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
            "SELECT *",
            "{ GRAPH ?g { ?s a <http://example.org/Entity> . ?s text:query 'foo' } }"
            ) ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, ds) ;
        ResultSet rs = qexec.execSelect() ;
        List<QuerySolution> x = Iter.toList(rs) ;
        assertEquals(1,x.size());
    }

    @Test public void textDB_8_bnode_subject() {
        Dataset ds = create() ;
        dataTurtle(ds,
            StrUtils.strjoinNL(
                "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
                "[] a <http://example.org/Entity>; rdfs:label 'foo' ."
            )
        );
        String qs = StrUtils.strjoinNL(
            "PREFIX text:   <http://jena.apache.org/text#>",
            "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
            "SELECT *",
            "{ ?s text:query 'foo' . ?s a <http://example.org/Entity> }"
            ) ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, ds) ;
        ResultSet rs = qexec.execSelect() ;
        List<QuerySolution> x = Iter.toList(rs) ;
        assertEquals(1,x.size());
    }

    @Test public void textDB_9_bnode_subject_bound_first() {
        Dataset ds = create() ;
        dataTurtle(ds,
            StrUtils.strjoinNL(
                "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
                "[] a <http://example.org/Entity>; rdfs:label 'foo' ."
            )
        );
        String qs = StrUtils.strjoinNL(
            "PREFIX text:   <http://jena.apache.org/text#>",
            "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
            "SELECT *",
            "{ ?s a <http://example.org/Entity> . ?s text:query 'foo' }"
            ) ;
        Query q = QueryFactory.create(qs) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, ds) ;
        ResultSet rs = qexec.execSelect() ;
        List<QuerySolution> x = Iter.toList(rs) ;
        assertEquals(1,x.size());
    }

    private static void data(Dataset ds, String... quadStrs) {
        for ( String qs : quadStrs ) {
            Quad quad = SSE.parseQuad(qs) ;
            ds.asDatasetGraph().add(quad) ;
        }
    }

    private static void dataTurtle(Dataset ds, String turtle) {
        Model model = ds.getDefaultModel();
        Reader reader = new StringReader(turtle);
        model.read(reader, "", "TURTLE");
    }
}


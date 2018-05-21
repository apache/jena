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

import static org.junit.Assert.assertEquals;
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
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.vocabulary.RDFS ;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory ;
import org.apache.lucene.store.RAMDirectory ;
import org.junit.Test ;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** Text dataset tests using datasets transactionally, named unionDefaultGraph.
 * <p>
 * Note that in Lucene, writes are not visible to a reader of the {@code Directory}
 * until committed. A special {@link IndexReader} is needed. See
 * {@link DirectoryReader#open(IndexWriter)}. jena-text does not currentyl do this.
 * <p>
 * When used outside a transaction, writes are "autocommit" (see
 * {@link TextDocProducerTriples}) and so are visible immediately.
 * <p>
 * TDB2 is transactional only. 
 * <p>Union graph support by context is required for these tests.
 */

@RunWith(Parameterized.class)
public class TestTextTxn
{
    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]>  data() {
        Creator<Dataset> plainFactory = ()->DatasetFactory.create();
        Creator<Dataset> timFactory = ()->DatasetFactory.createTxnMem();
        Creator<Dataset> tdb1Factory = ()->TDBFactory.createDataset();
        Creator<Dataset> tdb2Factory = ()->TDB2Factory.createDataset();
        return Arrays.asList( new Object[][]{
            { "Plain", plainFactory, false } ,
            { "TIM",   timFactory, false } , 
            { "TDB1", tdb1Factory, true } ,
            { "TDB2", tdb2Factory, true }
        });
    }
    
    private final Creator<Dataset> factory;
    private final Boolean dsFrom;

    public TestTextTxn(String name, Creator<Dataset> factory, Boolean dsFrom) {
        this.factory = factory;
        // Does FROM work by pulling graphs from the dataset?
        this.dsFrom = dsFrom;
    }
    
    private Dataset create() {
        Dataset ds1 = factory.create();
        Directory dir = new RAMDirectory() ;
        EntityDefinition eDef = new EntityDefinition("iri", "text");
        eDef.setPrimaryPredicate(RDFS.label);
        TextIndex tidx = new TextIndexLucene(dir, new TextIndexConfig(eDef)) ;
        Dataset ds = TextDatasetFactory.create(ds1, tidx) ;
        return ds ;
    }

    @Test public void textTxn_dftGraph_1() {
        Dataset ds = create() ;
        Quad quad = SSE.parseQuad("(_ <p> rdfs:label 'foo')") ;
        Txn.executeWrite(ds, ()->ds.asDatasetGraph().add(quad));
        
        Txn.executeRead(ds, ()->{
            Query q = QueryFactory.create("SELECT * { ?s ?p ?o }") ;
            QueryExecution qexec = QueryExecutionFactory.create(q, ds) ;
            ResultSet rs = qexec.execSelect() ;
            List<QuerySolution> x = Iter.toList(rs) ;
            assertEquals(1,x.size());
        });
    }
    
    @Test public void textTxn_namedGraphplain_1() {
        Dataset ds = create() ;
        Quad quad = SSE.parseQuad("(<g> <p> rdfs:label 'foo')") ;
        Txn.executeWrite(ds, ()->ds.asDatasetGraph().add(quad));
        
        Txn.executeRead(ds, ()->{
            Query q = QueryFactory.create("SELECT * { GRAPH ?g { ?s ?p ?o } }") ;
            QueryExecution qexec = QueryExecutionFactory.create(q, ds) ;
            ResultSet rs = qexec.execSelect() ;
            List<QuerySolution> x = Iter.toList(rs) ;
            assertEquals(1,x.size());
        });
    }
    
    @Test public void textTxn_query_1() {
        Dataset ds = create() ;
        Txn.executeWrite(ds, ()->{
            Quad quad = SSE.parseQuad("(_ <s> rdfs:label 'foo')") ;
            ds.asDatasetGraph().add(quad) ;
        });

        Txn.executeRead(ds, ()->{
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
        });
    }
    
    @Test public void textTxn_query_2() {
        Dataset ds = create() ;
        Txn.executeWrite(ds, ()->{
            data(ds, 
                "(_ <s1> rdfs:label 'foo')",
                "(_ <s2> rdfs:label 'bar')") ;
        });
        Txn.executeRead(ds, ()->{
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
            assertEquals(1,x.size());
        });
    }
    
    @Test public void textTxn_from_namedGraph_query_1() {
        assumeTrue(dsFrom);
        Dataset ds = create() ;
        Txn.executeWrite(ds, ()->{
            data(ds, 
                "(<ex:g1> <s1> rdfs:label 'foo')",
                "(<ex:g1> <s2> rdfs:label 'apple')",
                "(<ex:g2> <s3> rdfs:label 'bar')") ;
        });
        Txn.executeRead(ds,  ()->{
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
            assertEquals(1,x.size());
        });
    }

    @Test public void textTxn_from_unionGraph() {
        assumeTrue(dsFrom);
        Dataset ds = create() ;
        Txn.executeWrite(ds, ()->{
        data(ds, 
             "(<ex:g1> <s1> rdfs:label 'foo')",
             "(<ex:g1> <s2> rdfs:label 'apple')",
             "(<ex:g2> <s3> rdfs:label 'food')") ;
        });
        Txn.executeRead(ds,  ()->{
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
        });
    }

    @Test public void textTxn_graphUnionGraph() {
        Dataset ds = create() ;
        Txn.executeWrite(ds, ()->{
            data(ds, 
                "(<ex:g1> <s1> rdfs:label 'foo')",
                "(<ex:g1> <s2> rdfs:label 'apple')",
                "(<ex:g2> <s3> rdfs:label 'food')") ;
        });
        Txn.executeRead(ds,  ()->{
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
        });
    }
    
    @Test public void textTxn_subject_bound_first() {
        Dataset ds = create() ;
        Txn.executeWrite(ds, ()->{
            data(ds, 
                "(<ex:g1> <s1> rdfs:label 'foo')",
                "(<ex:g1> <s1> rdf:type <http://example.org/Entity>)",
                "(<ex:g1> <s2> rdfs:label 'apple')",
                "(<ex:g1> <s2> rdf:type <http://example.org/Entity>)",
                "(<ex:g2> <s3> rdfs:label 'food')",
                "(<ex:g2> <s3> rdf:type <http://example.org/Entity>)");
        });
        Txn.executeRead(ds,  ()->{
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
        });
    }

    @Test public void textTxn_bnode_subject() {
        Dataset ds = create() ;
        Txn.executeWrite(ds, ()->{
            dataTurtle(ds,
                StrUtils.strjoinNL(
                    "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
                    "[] a <http://example.org/Entity>; rdfs:label 'foo' ."
                    )
                );
        });
        Txn.executeRead(ds,  ()->{
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
        });
    }

    @Test public void textTxn_bnode_subject_bound_first() {
        Dataset ds = create() ;
        Txn.executeWrite(ds, ()->{
            dataTurtle(ds,
                StrUtils.strjoinNL(
                    "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
                    "[] a <http://example.org/Entity>; rdfs:label 'foo' ."
                    )
                );
        });
        Txn.executeRead(ds,  ()->{
            String qs = StrUtils.strjoinNL(
                "PREFIX text:   <http://jena.apache.org/text#>",
                "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
                "SELECT *",
                "{ ?s a <http://example.org/Entity> . ?s text:query 'foo' }"
                );
            Query q = QueryFactory.create(qs);
            QueryExecution qexec = QueryExecutionFactory.create(q, ds);
            ResultSet rs = qexec.execSelect();
            List<QuerySolution> x = Iter.toList(rs);
            assertEquals(1,x.size());
        });
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


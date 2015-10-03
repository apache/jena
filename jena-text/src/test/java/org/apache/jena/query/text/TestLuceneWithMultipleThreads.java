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

package org.apache.jena.query.text;

import java.util.ArrayList ;
import java.util.List ;
import java.util.concurrent.ExecutionException ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;
import java.util.concurrent.Future ;
import java.util.concurrent.TimeUnit ;

import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.* ;
import org.apache.jena.query.text.assembler.TextVocab ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ResourceFactory ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.modify.GraphStoreNullTransactional ;
import org.apache.jena.vocabulary.RDFS ;
import org.apache.lucene.analysis.standard.StandardAnalyzer ;
import org.apache.lucene.store.RAMDirectory ;
import org.apache.lucene.util.Version ;
import org.junit.Test ;
import static org.junit.Assert.* ;

/**
 * Spin up multiple threads against a multiple-reader/single-writer Dataset to test that the Lucene index handles concurrency properly.
 */
public class TestLuceneWithMultipleThreads
{
    private static final EntityDefinition entDef;
    
    static {
        entDef = new EntityDefinition("uri", "label");
        entDef.setGraphField("graph");
        entDef.setPrimaryPredicate(RDFS.label);
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_4_9);
        entDef.setAnalyzer("label", analyzer);
    }
    
    @Test
    public void testReadInMiddleOfWrite() throws InterruptedException, ExecutionException
    {
        final DatasetGraphText dsg = (DatasetGraphText)TextDatasetFactory.createLucene(new GraphStoreNullTransactional(), new RAMDirectory(), new TextIndexConfig(entDef));
        final Dataset ds = DatasetFactory.create(dsg);
        final ExecutorService execService = Executors.newSingleThreadExecutor();
        final Future<?> f = execService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                // Hammer the dataset with a series of read queries
                while (!Thread.interrupted())
                {
                    dsg.begin(ReadWrite.READ);
                    try
                    {
                        QueryExecution qExec = QueryExecutionFactory.create("select * where { ?s ?p ?o }", ds);
                        ResultSet rs = qExec.execSelect();
                        while (rs.hasNext())
                        {
                            rs.next();
                        }
                        dsg.commit();
                    }
                    finally
                    {
                        dsg.end();
                    }
                }
            }
        });
        
        dsg.begin(ReadWrite.WRITE);
        try
        {
            Model m = ds.getDefaultModel();
            m.add(ResourceFactory.createResource("http://example.org/"), RDFS.label, "entity");
            // Sleep for a bit so that the reader thread can get in between these two writes
            Thread.sleep(100);
            m.add(ResourceFactory.createResource("http://example.org/"), RDFS.comment, "comment");
            
            dsg.commit();
        }
        finally
        {
            dsg.end();
        }
        
        execService.shutdownNow();
        execService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        
        // If there was an exception in the read thread then Future.get() will throw an ExecutionException
        assertTrue(f.get() == null);
    }
    
    @Test
    public void testWriteInMiddleOfRead() throws InterruptedException, ExecutionException
    {
        final DatasetGraphText dsg = (DatasetGraphText)TextDatasetFactory.createLucene(new GraphStoreNullTransactional(), new RAMDirectory(), new TextIndexConfig(entDef));
        final int numReads = 10;
        final Dataset ds = DatasetFactory.create(dsg);
        final ExecutorService execService = Executors.newFixedThreadPool(10);
        final Future<?> f = execService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                while (!Thread.interrupted())
                {
                    dsg.begin(ReadWrite.WRITE);
                    try
                    {
                        Model m = ds.getDefaultModel();
                        m.add(ResourceFactory.createResource("http://example.org/"), RDFS.label, "entity");
                        // Sleep for a bit so that the reader thread can get in between these two writes
                        try
                        {
                            Thread.sleep(100);
                        }
                        catch (InterruptedException e)
                        {
                            break;
                        }
                        m.add(ResourceFactory.createResource("http://example.org/"), RDFS.comment, "comment");
                        
                        dsg.commit();
                    }
                    finally
                    {
                        dsg.end();
                    }
                }
            }
        });
        
        for (int i=0; i<numReads; i++)
        {
            dsg.begin(ReadWrite.READ);
            try
            {
                QueryExecution qExec = QueryExecutionFactory.create("select * where { ?s ?p ?o }", ds);
                ResultSet rs = qExec.execSelect();
                while (rs.hasNext())
                {
                    rs.next();
                }
                // Sleep for a bit so that the writer thread can get in between the reads
                Thread.sleep(100);
                dsg.commit();
            }
            finally
            {
                dsg.end();
            }
        }
        
        execService.shutdownNow();
        execService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        
        // If there was an exception in the write thread then Future.get() will throw an ExecutionException
        assertTrue(f.get() == null);
    }
    
    @Test
    public void testIsolation() throws InterruptedException, ExecutionException {
        
        final DatasetGraphText dsg = (DatasetGraphText)TextDatasetFactory.createLucene(DatasetGraphFactory.createMem(), new RAMDirectory(), new TextIndexConfig(entDef));
        
        final int numReaders = 2;
        final List<Future<?>> futures = new ArrayList<Future<?>>(numReaders);
        final ExecutorService execService = Executors.newFixedThreadPool(numReaders);
        final Dataset ds = DatasetFactory.create(dsg);
        
        
        for (int i=0; i<numReaders; i++) {
            futures.add(execService.submit(new Runnable() {
                @Override
                public void run()
                {
                    while (!Thread.interrupted()) {
                        dsg.begin(ReadWrite.READ);
                        try {
                            QueryExecution qExec = QueryExecutionFactory.create(
                                    "select * where { graph <http://example.org/graph> { ?s <" + TextVocab.pfQuery + "> (<" + RDFS.label.getURI() + "> \"test\") } }", ds);
//                                    "select * where { graph <http://example.org/graph> { ?s <" + RDFS.label.getURI() + "> \"test\" } }", ds);
                            ResultSet rs = qExec.execSelect();
                            assertFalse(rs.hasNext());
                            dsg.commit();
                        }
                        finally {
                            dsg.end();
                        }
                        
                        try {
                            Thread.sleep(10);
                        }
                        catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }));
        }
        
        // Give the read threads a chance to start up
        Thread.sleep(500);
        dsg.begin(ReadWrite.WRITE);
        try {
            dsg.add(NodeFactory.createURI("http://example.org/graph"), NodeFactory.createURI("http://example.org/test"), RDFS.label.asNode(), NodeFactory.createLiteral("test"));
            
            // Now give the read threads a chance to note the change
            Thread.sleep(500);
            
            // Don't commit this change
        }
        finally {
            dsg.end();
        }
        // Just in case dsg.end() inappropriately commits the change
        Thread.sleep(500);
        
        execService.shutdownNow();
        execService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        for(Future<?> f : futures) {
            assertTrue(f.get() == null);
        }
    }
    
}

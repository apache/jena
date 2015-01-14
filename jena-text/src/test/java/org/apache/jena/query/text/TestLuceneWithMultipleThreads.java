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

import java.util.concurrent.ExecutionException ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;
import java.util.concurrent.Future ;
import java.util.concurrent.TimeUnit ;

import org.apache.lucene.analysis.standard.StandardAnalyzer ;
import org.apache.lucene.store.RAMDirectory ;
import org.apache.lucene.util.Version ;
import org.junit.Before ;
import org.junit.Test ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ResourceFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Transactional ;
import com.hp.hpl.jena.sparql.modify.GraphStoreNullTransactional ;
import com.hp.hpl.jena.vocabulary.RDFS ;

import static org.junit.Assert.* ;

/**
 * Spin up multiple threads against a multiple-reader/single-writer Dataset to test that the Lucene index handles concurrency properly.
 */
public class TestLuceneWithMultipleThreads
{
    private static final EntityDefinition entDef;
    
    static {
        entDef = new EntityDefinition("uri", "label", "graph", RDFS.label.asNode());
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
        entDef.setAnalyzer("label", analyzer);
    }
    
    private DatasetGraph dsg;
    private Transactional tx;
    
    @Before
    public void setup()
    {
        dsg = TextDatasetFactory.createLucene(new GraphStoreNullTransactional(), new RAMDirectory(), entDef);
        tx = (Transactional)dsg;
    }
    
    
    @Test
    public void testReadInMiddleOfWrite() throws InterruptedException, ExecutionException
    {
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
                    tx.begin(ReadWrite.READ);
                    try
                    {
                        QueryExecution qExec = QueryExecutionFactory.create("select * where { ?s ?p ?o }", ds);
                        ResultSet rs = qExec.execSelect();
                        while (rs.hasNext())
                        {
                            rs.next();
                        }
                        tx.commit();
                    }
                    finally
                    {
                        tx.end();
                    }
                }
            }
        });
        
        tx.begin(ReadWrite.WRITE);
        try
        {
            Model m = ds.getDefaultModel();
            m.add(ResourceFactory.createResource("http://example.org/"), RDFS.label, "entity");
            // Sleep for a bit so that the reader thread can get in between these two writes
            Thread.sleep(100);
            m.add(ResourceFactory.createResource("http://example.org/"), RDFS.comment, "comment");
            
            tx.commit();
        }
        finally
        {
            tx.end();
        }
        
        execService.shutdownNow();
        execService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        
        // If there was an exception in the read thread then Future.get() will throw an ExecutionException
        assertTrue(f.get() == null);
    }
    
    @Test
    public void testWriteInMiddleOfRead() throws InterruptedException, ExecutionException
    {
        final int numReads = 10;
        final Dataset ds = DatasetFactory.create(dsg);
        final ExecutorService execService = Executors.newFixedThreadPool(10); //.newSingleThreadExecutor();
        final Future<?> f = execService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                while (!Thread.interrupted())
                {
                    tx.begin(ReadWrite.WRITE);
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
                        
                        tx.commit();
                    }
                    finally
                    {
                        tx.end();
                    }
                }
            }
        });
        
        for (int i=0; i<numReads; i++)
        {
            tx.begin(ReadWrite.READ);
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
                tx.commit();
            }
            finally
            {
                tx.end();
            }
        }
        
        execService.shutdownNow();
        execService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        
        // If there was an exception in the write thread then Future.get() will throw an ExecutionException
        assertTrue(f.get() == null);
    }
}

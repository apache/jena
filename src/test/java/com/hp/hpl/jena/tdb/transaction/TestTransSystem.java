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

package com.hp.hpl.jena.tdb.transaction;

import static com.hp.hpl.jena.tdb.transaction.TransTestLib.count ;

import java.util.Date ;
import java.util.concurrent.Callable ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;
import java.util.concurrent.TimeUnit ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.lib.Lib ;
import org.openjena.atlas.lib.RandomLib ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.ReadWrite ;
import com.hp.hpl.jena.tdb.StoreConnection ;

/** System testing of the transactions. */
public class TestTransSystem
{
    static ExecutorService execService = Executors.newCachedThreadPool() ;
    static Quad q  = SSE.parseQuad("(_ <s> <p> <o>) ") ;
    static Quad q1 = SSE.parseQuad("(_ <s> <p> <o1>)") ;
    static Quad q2 = SSE.parseQuad("(_ <s> <p> <o2>)") ;
    static Quad q3 = SSE.parseQuad("(_ <s> <p> <o3>)") ;
    static Quad q4 = SSE.parseQuad("(_ <s> <p> <o4>)") ;
    
    static final String DIR = ConfigTest.getTestingDirDB() ;
    private StoreConnection sConn ;
    private int initCount = -1 ;
    static final AtomicInteger gen = new AtomicInteger() ;
    protected StoreConnection getStoreConnection()
    {
        FileOps.clearDirectory(DIR) ;
        StoreConnection sConn = StoreConnection.make(DIR) ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
        dsg.add(q1) ;
        dsg.add(q2) ;
        initCount = 2 ;
        dsg.commit() ;
        dsg.close() ;
        return sConn ;
    }
    
    public static void main(String...args)
    {
        System.out.println("Starting: "+new Date()) ;
        new TestTransSystem().manyReaderAndOneWriter() ;
        System.out.println("Stopping: "+new Date()) ;
        
    }
    
    //@Test
    public void manyRead()
    {
        final StoreConnection sConn = getStoreConnection() ;
        Callable<?> proc = new Reader(sConn, 50, 200)  ;

            
        for ( int i = 0 ; i < 50 ; i++ )
            execService.submit(proc) ;
        try
        {
            execService.shutdown() ;
            execService.awaitTermination(100, TimeUnit.SECONDS) ;
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    
    
    //@Test
    public void manyReaderAndOneWriter()
    {
        final StoreConnection sConn = getStoreConnection() ;
        
        Callable<?> procR = new Reader(sConn, 5, 200) ;
        Callable<?> procW = new Writer(sConn, 5, 100, true) {
            @Override
            protected int change(DatasetGraphTxn dsg, int id, int i)
            {
                int count = 0 ;
                int N = 5 ;
                for ( int j = 0 ; j < N; j++ )
                {
                    Quad q = genQuad(N*id+j) ;
                    if ( ! dsg.contains(q) )
                    {
                        dsg.add(q) ;
                        count++ ;
                    }
                }
                return count ;
            } } ;
            
        for ( int i = 0 ; i < 50 ; i++ )
        {
            execService.submit(procR) ;   
            execService.submit(procW) ;
        }
        try
        {
            execService.shutdown() ;
            execService.awaitTermination(100, TimeUnit.SECONDS) ;
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } 
    }

    static class Reader implements Callable<Object>
    {
        private final int repeats ;
        private final int maxpause ;
        private final StoreConnection sConn ; 
    
        Reader(StoreConnection sConn, int number, int pause)
        {
            this.repeats = number ;
            this.maxpause = pause ;
            this.sConn = sConn ;
        }
        
        @Override
        public Object call()
        {
            int id = gen.incrementAndGet() ;
            for ( int i = 0 ; i < repeats; i++ )
            {
                DatasetGraphTxn dsg = sConn.begin(ReadWrite.READ) ;
                int x1 = count("SELECT * { ?s ?p ?o }", dsg) ;
                pause(maxpause) ;
                int x2 = count("SELECT * { ?s ?p ?o }", dsg) ;
                if ( x1 != x2 )
                    System.err.printf("Change seen: id=%d: i=%d\n", id, i) ;
                dsg.close() ;
            }
            return null ;
        }
    }

    static abstract class Writer implements Callable<Object>
    {
        private final int repeats ;
        private final int maxpause ;
        private final StoreConnection sConn ;
        private final boolean commit ; 
    
        protected Writer(StoreConnection sConn, int number, int pause, boolean commit)
        {
            this.repeats = number ;
            this.maxpause = pause ;
            this.sConn = sConn ;
            this.commit = commit ;
        }
        
        @Override
        public Object call()
        {
            int id = gen.incrementAndGet() ;
            for ( int i = 0 ; i < 10; i++ )
            {
                DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
                int x1 = count("SELECT * { ?s ?p ?o }", dsg) ;
                int z = change(dsg, id, i) ;
                pause(maxpause) ;
                int x2 = count("SELECT * { ?s ?p ?o }", dsg) ;
                if ( x1+z != x2 )
                    System.err.printf("Change seen: id=%d: i=%d\n", id, i) ;
                if (commit) 
                    dsg.commit() ;
                else
                    dsg.abort() ;
                dsg.close() ;
            }
            return null ; 
        }

        // return the delta.
        protected abstract int change(DatasetGraphTxn dsg, int id, int i) ;
    }

    static void pause(int maxInternal)
    {
        int x = (int)Math.round(Math.random()*maxInternal) ;
        Lib.sleep(x) ;
    }
    
    static Quad genQuad(int value)
    {
        Quad q1 = SSE.parseQuad("(_ <s> <p> <o>)") ;
        Node g1 = q.getGraph() ;
        
        Node g = Quad.defaultGraphNodeGenerated ; // urn:x-arq:DefaultGraphNode
        Node s = Node.createURI("S") ;
        Node p = Node.createURI("P") ;
        Node o = Node.createLiteral(Integer.toString(value), null, XSDDatatype.XSDinteger) ;
        return new Quad(g,s,p,o) ;
    }
    
}


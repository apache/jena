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
import static java.lang.String.format ;

import java.util.concurrent.Callable ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;
import java.util.concurrent.TimeUnit ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.lib.Lib ;
import org.openjena.atlas.lib.RandomLib ;
import org.openjena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.ReadWrite ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.base.file.Location ;

/** System testing of the transactions. */
public class TestTransSystem
{
    static { Log.setLog4j() ; }
    private static Logger log = LoggerFactory.getLogger(TestTransSystem.class) ;

    static final int Iterations             = 10 ;
    static final boolean progress           = (! log.isDebugEnabled()) && Iterations > 5 ;
    
    
    static final int numReaderTasks         = 5 ;   // Add some
    static final int numWriterTasksA        = 0 ;
    static final int numWriterTasksC        = 5 ;
    
    static final int readerSeqRepeats       = 5 ;    
    static final int readerMaxPause         = 20 ;
    
    static final int writerAbortSeqRepeats  = 0 ;
    static final int writerCommitSeqRepeats = 5 ;
    static final int writerMaxPause         = 20 ;
    
//  static final int numReaderTasks         = 5 ;
//  static final int numWriterTasksA        = 0 ;
//  static final int numWriterTasksC        = 2 ;
//  
//  static final int readerSeqRepeats       = 2 ;    
//  static final int readerMaxPause         = 100 ;
//  
//  static final int writerAbortSeqRepeats  = 0 ;
//  static final int writerCommitSeqRepeats = 2 ;
//  static final int writerMaxPause         = 20 ;
  
    
    public static void main(String...args)
    {
        if ( ! progress )
            System.out.println("START") ;
        
        final int N = (Iterations < 10) ? 1 : Iterations / 10 ;
        int i ;
        
        for ( i = 0 ; i < Iterations ; i++ )
        {
            if ( i%N == 0 )
                printf("%03d: ",i) ;
            printf(".") ;
            if ( i%N == (N-1) )
                println() ;
            new TestTransSystem().manyReaderAndOneWriter() ;
        }
        if ( i%N != 0 )
            System.out.println() ;
        println() ;
        printf("DONE (%03d)\n",i) ;
        if ( ! progress )
            System.out.println("FINISH") ;
    }
    
    private static void println()
    {
        printf("\n") ;
    }
    
    private static void printf(String string, Object...args)
    {
        if ( progress )
            System.out.printf(string, args) ;
    }
    
    private ExecutorService execService = Executors.newCachedThreadPool() ;
    static Quad q  = SSE.parseQuad("(_ <s> <p> <o>) ") ;
    static Quad q1 = SSE.parseQuad("(_ <s> <p> <o1>)") ;
    static Quad q2 = SSE.parseQuad("(_ <s> <p> <o2>)") ;
    static Quad q3 = SSE.parseQuad("(_ <s> <p> <o3>)") ;
    static Quad q4 = SSE.parseQuad("(_ <s> <p> <o4>)") ;
    
    static final Location LOC = Location.mem() ;
    //static final Location LOC = new Location(ConfigTest.getTestingDirDB()) ;
    static final AtomicInteger gen = new AtomicInteger() ;
    
    @BeforeClass 
    public static void beforeClass()
    {
        if ( ! LOC.isMem() )
            FileOps.clearDirectory(LOC.getDirectoryPath()) ;
        StoreConnection.reset() ;
        StoreConnection sConn = StoreConnection.make(LOC) ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
        dsg.add(q1) ;
        dsg.add(q2) ;
        initCount = 2 ;
        dsg.commit() ;
        dsg.close() ;
    }
    
    @AfterClass 
    public static void afterClass() {}

    private StoreConnection sConn ;
    private static int initCount = -1 ;

    protected synchronized StoreConnection getStoreConnection()
    {
        StoreConnection sConn = StoreConnection.make(LOC) ;
        sConn.getTransMgr().recording(true) ;
        return sConn ;
    }
    
    public TestTransSystem() {}
    
    //@Test
    public void manyRead()
    {
        final StoreConnection sConn = getStoreConnection() ;
        Callable<?> proc = new Reader(sConn, 50, 200)  ;        // Number of repeats, max pause
            
        for ( int i = 0 ; i < 5 ; i++ )
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
        
        Callable<?> procR = new Reader(sConn, readerSeqRepeats, readerMaxPause) ;      // Number of repeats, max pause
        Callable<?> procW_a = new Writer(sConn, writerAbortSeqRepeats, writerMaxPause, false)  // Number of repeats, max pause, commit. 
        {
            @Override
            protected int change(DatasetGraphTxn dsg, int id, int i)
            { return changeProc(dsg, id, i) ; }
        } ;
            
        Callable<?> procW_c = new Writer(sConn, writerCommitSeqRepeats, writerMaxPause, true)  // Number of repeats, max pause, commit. 
        {
            @Override
            protected int change(DatasetGraphTxn dsg, int id, int i)
            { return changeProc(dsg, id, i) ; }
        } ;

        submit(execService, procR,   numReaderTasks) ;
        submit(execService, procW_c, numWriterTasksC) ;
        submit(execService, procW_a, numWriterTasksA) ;
        
        try
        {
            execService.shutdown() ;
            execService.awaitTermination(100, TimeUnit.SECONDS) ;
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } 
    }

    private void submit(ExecutorService execService2, Callable<?> proc, int numTasks)
    {
        for ( int i = 0 ; i < numTasks ; i++ )
            execService.submit(proc) ;
    }

    static int changeProc(DatasetGraphTxn dsg, int id, int i)
    {
        int count = 0 ;
        int maxN = 500 ;
        int N = RandomLib.qrandom.nextInt(maxN) ;
        for ( int j = 0 ; j < N; j++ )
        {
            Quad q = genQuad(id*maxN+j) ;
            if ( ! dsg.contains(q) )
            {
                dsg.add(q) ;
                count++ ;
            }
        }
        log.debug("Change = "+dsg.getDefaultGraph().size()) ;
        return count ;
    }
    
    static class Reader implements Callable<Object>
    {
        private final int repeats ;
        private final int maxpause ;
        private final StoreConnection sConn ; 
    
        Reader(StoreConnection sConn, int numSeqRepeats, int pause)
        {
            this.repeats = numSeqRepeats ;
            this.maxpause = pause ;
            this.sConn = sConn ;
        }

        @Override
        public Object call()
        {
            try { return call$() ; }
            catch (RuntimeException ex) { System.err.println(ex.getMessage()) ; return null ; }
        }
        
        private Object call$()
        {
            
            int id = gen.incrementAndGet() ;
            for ( int i = 0 ; i < repeats; i++ )
            {
                DatasetGraphTxn dsg = sConn.begin(ReadWrite.READ) ;
                log.debug("reader start "+id+"/"+i) ;                
                
                int x1 = count("SELECT * { ?s ?p ?o }", dsg) ;
                pause(maxpause) ;
                int x2 = count("SELECT * { ?s ?p ?o }", dsg) ;
                if ( x1 != x2 )
                    log.warn(format("%s Change seen: %d/%d : id=%d: i=%d", 
                                    dsg.getTransaction().getLabel(), 
                                    x1, x2, id, i)) ;
                log.debug("reader finish "+id+"/"+i) ;                
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
    
        protected Writer(StoreConnection sConn, int numSeqRepeats, int pause, boolean commit)
        {
            this.repeats = numSeqRepeats ;
            this.maxpause = pause ;
            this.sConn = sConn ;
            this.commit = commit ;
        }
        
        @Override
        public Object call()
        {
            try { return call$() ; }
            catch (RuntimeException ex) { System.err.println(ex.getMessage()) ; return null ; }
        }
        
        public Object call$()
        {
            int id = gen.incrementAndGet() ;
            for ( int i = 0 ; i < repeats ; i++ )
            {
                log.debug("writer start "+id+"/"+i) ;                
                DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
                
                int x1 = count("SELECT * { ?s ?p ?o }", dsg) ;
                int z = change(dsg, id, i) ;
                pause(maxpause) ;
                int x2 = count("SELECT * { ?s ?p ?o }", dsg) ;
                if ( x1+z != x2 )
                {
                    TransactionManager txnMgr = dsg.getTransaction().getTxnMgr() ;
                    SysTxnState state = txnMgr.state() ;
                    String label = dsg.getTransaction().getLabel() ; 
                    log.warn(format("%s Change seen: %d + %d != %d : id=%d: i=%d", label, x1, z, x2, id, i)) ;
                    log.warn(state.toString()) ;
                    log.warn("BP") ;
                }
                if (commit) 
                    dsg.commit() ;
                else
                    dsg.abort() ;
                SysTxnState state = sConn.getTransMgrState() ;
                log.debug(state.toString()) ;
                log.debug("writer finish "+id+"/"+i) ;                
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


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

package com.hp.hpl.jena.tdb.extra;

import static com.hp.hpl.jena.tdb.transaction.TransTestLib.count ;
import static java.lang.String.format ;

import java.io.File ;
import java.util.ArrayList ;
import java.util.Random ;
import java.util.concurrent.Callable ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;
import java.util.concurrent.TimeUnit ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.RandomLib ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.block.FileMode ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.transaction.SysTxnState ;
import com.hp.hpl.jena.tdb.transaction.TransactionManager ;

/** System testing using multiple datasets of the transactions. */
public class T_TransSystemMultiDatasets
{
    // Use this to flip between FileMode.direct and FileMode.mapped
    static { SystemTDB.setFileMode(FileMode.mapped) ; }
    static { org.apache.jena.atlas.logging.LogCtl.setLog4j() ; }
    private static Logger log = LoggerFactory.getLogger(T_TransSystemMultiDatasets.class) ;

    static boolean MEM = false ;
    static boolean USE_TRANSACTIONS = false ;
    
    static final int NUM_DATASETS = 3 ;
    static final ArrayList<Location> LOCATIONS = new ArrayList<>() ;
    
    static {
    	for ( int i = 0; i < NUM_DATASETS; i++ ) 
    		LOCATIONS.add(createLocation()) ;
    }

    private static int count_datasets = 0 ;
    static Location createLocation() {
    	return MEM ? Location.mem() : new Location(ConfigTest.getTestingDirDB() + File.separator + "DB-" + ++count_datasets) ;
    }

    static final int Iterations             = MEM ? 1000 : 100 ;
    // Output style.
    static boolean inlineProgress           = true ; // (! log.isDebugEnabled()) && Iterations > 20 ;
    static boolean logging                  = ! inlineProgress ; // (! log.isDebugEnabled()) && Iterations > 20 ;
    
    static final int numReaderTasks         = 10 ;
    static final int numWriterTasksA        = 10 ;
    static final int numWriterTasksC        = 10 ;

    static final int readerSeqRepeats       = 8 ; 
    static final int readerMaxPause         = 50 ;

    static final int writerAbortSeqRepeats  = 4 ;
    static final int writerCommitSeqRepeats = 4 ;
    static final int writerMaxPause         = 25 ;

    
    public static void main(String...args)
    {
        if ( logging )
            log.info("START ("+ (MEM?"memory":"disk") + ", {} iterations)", Iterations) ;
        else
            printf("START (%s, %d iterations)\n", (MEM?"memory":"disk"), Iterations) ;
        
        int N = (Iterations < 10) ? 1 : Iterations / 10 ;
        N = Math.min(N, 100) ;
        int i ;
        
        for ( i = 0 ; i < Iterations ; i++ )
        {
            clean() ;
            
            if (!inlineProgress && logging)
                log.info(format("Iteration: %d\n", i)) ;
            if ( inlineProgress )
            {
                if ( i%N == 0 )
                    printf("%03d: ",i) ;
                printf(".") ;
                if ( i%N == (N-1) )
                    println() ;
            }
            new T_TransSystemMultiDatasets().manyReaderAndOneWriter() ;
        }
        if ( inlineProgress )
        {
            if ( i%N != 0 )
                System.out.println() ;
            println() ;
            printf("DONE (%03d)\n",i) ;
        }
        if (logging)
            log.info("FINISH ({})", i) ;
        else
            printf("FINISH") ;
    }
    
    private static void clean()
    {
    	for ( Location location : LOCATIONS ) {
    	    StoreConnection.release(location) ;
            if ( ! location.isMem() )
                FileOps.clearDirectory(location.getDirectoryPath()) ;			
		}
    }

    static class ReaderTx implements Callable<Object>
    {
        private final int repeats ;
        private final int maxpause ;
        private final T_TransSystemMultiDatasets tts ; 
    
        ReaderTx(T_TransSystemMultiDatasets tts, int numSeqRepeats, int pause)
        {
            this.repeats = numSeqRepeats ;
            this.maxpause = pause ;
            this.tts = tts ;
        }
    
        @Override
        public Object call()
        {
        	StoreConnection sConn = tts.getStoreConnection() ;
            DatasetGraphTxn dsg = null ;
            try
            {
                int id = gen.incrementAndGet() ;
                for (int i = 0; i < repeats; i++)
                {
                    dsg = sConn.begin(ReadWrite.READ) ;
                    log.debug("reader start " + id + "/" + i) ;

                    int x1 = count("SELECT * { ?s ?p ?o }", dsg) ;
                    pause(maxpause) ;
                    int x2 = count("SELECT * { ?s ?p ?o }", dsg) ;
                    // Eclispe Kepler (3.4) bug - otherwise the dsg.end() below 
                    // gets a warning ("The variable dsg can only be null at this location")/
                    // which is wrong.
                    dsg.getClass() ;
                    if ( x1 != x2 )
                        log.warn(format("READER: %s Change seen: %d/%d : id=%d: i=%d", dsg.getTransaction().getLabel(),
                                        x1, x2, id, i)) ;
                    dsg.end() ;
                    log.debug("reader finish " + id + "/" + i) ;
                    dsg = null ;
                }
                return null ;
            } catch (RuntimeException ex)
            {
                ex.printStackTrace(System.err) ;
                if ( dsg != null )
                {
                    dsg.abort() ;
                    dsg.end() ;
                    dsg = null ;
                }
                return null ;
            }
        }
    }

    static abstract class WriterTx implements Callable<Object>
    {
        private final int repeats ;
        private final int maxpause ;
        private final T_TransSystemMultiDatasets tts ;
        private final boolean commit ; 
    
        protected WriterTx(T_TransSystemMultiDatasets tts, int numSeqRepeats, int pause, boolean commit)
        {
            this.repeats = numSeqRepeats ;
            this.maxpause = pause ;
            this.tts = tts ;
            this.commit = commit ;
        }
        
        @Override
        public Object call()
        {
        	StoreConnection sConn = tts.getStoreConnection() ;
            DatasetGraphTxn dsg = null ;
            try { 
                int id = gen.incrementAndGet() ;
                for ( int i = 0 ; i < repeats ; i++ )
                {
                    log.debug("writer start "+id+"/"+i) ;                
                    dsg = sConn.begin(ReadWrite.WRITE) ;

                    int x1 = count("SELECT * { ?s ?p ?o }", dsg) ;
                    int z = change(dsg, id, i) ;
                    pause(maxpause) ;
                    int x2 = count("SELECT * { ?s ?p ?o }", dsg) ;
                    if ( x1+z != x2 )
                    {
                        TransactionManager txnMgr = dsg.getTransaction().getTxnMgr() ;
                        SysTxnState state = txnMgr.state() ;
                        String label = dsg.getTransaction().getLabel() ; 
                        log.warn(format("WRITER: %s Change seen: %d + %d != %d : id=%d: i=%d", label, x1, z, x2, id, i)) ;
                        log.warn(state.toString()) ;
                        dsg.abort() ;
                        dsg.end() ;
                        dsg = null ;
                        return null ;
                    }
                    if (commit) 
                        dsg.commit() ;
                    else
                        dsg.abort() ;
                    SysTxnState state = sConn.getTransMgrState() ;
                    log.debug(state.toString()) ;
                    log.debug("writer finish "+id+"/"+i) ;                
                    dsg.end() ;
                    dsg = null ;
                }
                return null ;
            }
            catch (RuntimeException ex)
            { 
                ex.printStackTrace(System.err) ;
                if ( dsg != null )
                {
                    dsg.abort() ;
                    dsg.end() ;
                    dsg = null ;
                }
                return null ;
            }
        }
    
        // return the delta.
        protected abstract int change(DatasetGraphTxn dsg, int id, int i) ;
    }

    static class Reader implements Callable<Object>
    {
        private final int repeats ;
        private final int maxpause ;
        private final T_TransSystemMultiDatasets tts ; 
    
        Reader(T_TransSystemMultiDatasets tts, int numSeqRepeats, int pause)
        {
            this.repeats = numSeqRepeats ;
            this.maxpause = pause ;
            this.tts = tts ;
        }
    
        @Override
        public Object call()
        {
            DatasetGraph dsg = null ; 
            Lock lock = null ; 
            try
            {
                dsg = tts.getDatasetGraph() ;
                lock = dsg.getLock() ;
                int id = gen.incrementAndGet() ;
                for (int i = 0; i < repeats; i++)
                {
                    try {
                        lock.enterCriticalSection(Lock.READ) ;
                        log.debug("reader start " + id + "/" + i) ;

                        int x1 = count("SELECT * { ?s ?p ?o }", dsg) ;
                        pause(maxpause) ;
                        int x2 = count("SELECT * { ?s ?p ?o }", dsg) ;
                        if (x1 != x2) log.warn(format("READER: %s Change seen: %d/%d : id=%d: i=%d",
                                                      "read-" + i, x1, x2, id, i)) ;
                        log.debug("reader finish " + id + "/" + i) ;
                    } catch (RuntimeException ex)
                    {
                        log.debug("reader error " + id + "/" + i) ;
                        ex.printStackTrace() ;
                    } finally {
                        lock.leaveCriticalSection() ;                        
                    }
                }
                return null ;
            } catch (RuntimeException ex)
            {
                ex.printStackTrace(System.err) ;
                return null ;
            }
        }
    }
    
    static abstract class Writer implements Callable<Object>
    {
        private final int repeats ;
        private final int maxpause ;
        private final T_TransSystemMultiDatasets tts ;
    
        protected Writer(T_TransSystemMultiDatasets tts, int numSeqRepeats, int pause)
        {
            this.repeats = numSeqRepeats ;
            this.maxpause = pause ;
            this.tts = tts ;
        }
        
        @Override
        public Object call()
        {
            DatasetGraph dsg = null ; 
            Lock lock = null ; 
            try {
                dsg = tts.getDatasetGraph() ;
                lock = dsg.getLock() ;
                int id = gen.incrementAndGet() ;
                for ( int i = 0 ; i < repeats ; i++ )
                {
                    try {
                        lock.enterCriticalSection(Lock.WRITE) ;
                        log.debug("writer start "+id+"/"+i) ;                

                        int x1 = count("SELECT * { ?s ?p ?o }", dsg) ;
                        int z = change(dsg, id, i) ;
                        pause(maxpause) ;
                        int x2 = count("SELECT * { ?s ?p ?o }", dsg) ;
                        if ( x1+z != x2 )
                        {
                            log.warn(format("WRITER: %s Change seen: %d + %d != %d : id=%d: i=%d", "write-" + i, x1, z, x2, id, i)) ;
                            return null ;
                        }
                        log.debug("writer finish "+id+"/"+i) ;                
                    } catch (RuntimeException ex)
                    {
                        log.debug("writer error "+id+"/"+i) ;         
                        System.err.println(ex.getMessage()) ;
                        ex.printStackTrace() ;
                    } finally {
                        lock.leaveCriticalSection() ;                        
                    }
                }
                return null ;
            } 
            catch (RuntimeException ex) 
            { 
                ex.printStackTrace(System.err) ;
                return null ;
            } 
        }
    
        // return the delta.
        protected abstract int change(DatasetGraph dsg, int id, int i) ;
    }
    
    @BeforeClass 
    public static void beforeClass()
    {
    	for ( Location location : LOCATIONS ) {
            if ( ! location.isMem() )
                FileOps.clearDirectory(location.getDirectoryPath()) ;    		
    	}
        StoreConnection.reset() ;
    }

    @AfterClass 
    public static void afterClass() {}

    private StoreConnection sConn ;
    private static Random random = new Random(System.currentTimeMillis()) ;

    protected synchronized StoreConnection getStoreConnection()
    {
        StoreConnection sConn = StoreConnection.make(LOCATIONS.get(random.nextInt(NUM_DATASETS))) ;
        //sConn.getTransMgr().recording(true) ;
        return sConn ;
    }
    
    protected synchronized DatasetGraph getDatasetGraph()
    {
        DatasetGraph dsg = TDBFactory.createDatasetGraph(LOCATIONS.get(random.nextInt(NUM_DATASETS))) ; 
        
        if ( dsg == null )
            throw new RuntimeException("DatasetGraph is null!") ;
        
        return dsg ;
    }
    
    public T_TransSystemMultiDatasets() {}
        
    //@Test
    public void manyRead()
    {
        final StoreConnection sConn = getStoreConnection() ;
        Callable<?> proc = new ReaderTx(this, 50, 200)  ;        // Number of repeats, max pause
            
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
        Callable<?> procRTx = new ReaderTx(this, readerSeqRepeats, readerMaxPause) ;      // Number of repeats, max pause
        Callable<?> procWTx_a = new WriterTx(this, writerAbortSeqRepeats, writerMaxPause, false)  // Number of repeats, max pause, commit. 
        {
            @Override
            protected int change(DatasetGraphTxn dsg, int id, int i)
            { return changeProc(dsg, id, i) ; }
        } ;
        Callable<?> procWTx_c = new WriterTx(this, writerCommitSeqRepeats, writerMaxPause, true)  // Number of repeats, max pause, commit. 
        {
            @Override
            protected int change(DatasetGraphTxn dsg, int id, int i)
            { return changeProc(dsg, id, i) ; }
        } ;

        Callable<?> procR = new Reader(this, readerSeqRepeats, readerMaxPause) ;
        Callable<?> procW = new Writer(this, writerCommitSeqRepeats, writerMaxPause) 
        {
            @Override
            protected int change(DatasetGraph dsg, int id, int i)
            { return changeProc(dsg, id, i) ; }
        } ;

        if ( USE_TRANSACTIONS ) {
            submit(execService, procRTx,   numReaderTasks) ;
            submit(execService, procWTx_c, numWriterTasksC) ;
            submit(execService, procWTx_a, numWriterTasksA) ;
        } else {
            submit(execService, procR, numReaderTasks) ;
            submit(execService, procW, numWriterTasksC) ;
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

    private void submit(ExecutorService execService2, Callable<?> proc, int numTasks)
    {
        for ( int i = 0 ; i < numTasks ; i++ )
            execService.submit(proc) ;
    }

    static int changeProc(DatasetGraph dsg, int id, int i)
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
        Node s = NodeFactory.createURI("S") ;
        Node p = NodeFactory.createURI("P") ;
        Node o = NodeFactory.createLiteral(Integer.toString(value), null, XSDDatatype.XSDinteger) ;
        return new Quad(g,s,p,o) ;
    }

    private static void println()
    {
        printf("\n") ; System.out.flush() ;
    }

    private static void printf(String string, Object...args)
    {
        System.out.printf(string, args) ;
    }

    private ExecutorService execService = Executors.newCachedThreadPool() ;

    static Quad q  = SSE.parseQuad("(_ <s> <p> <o>) ") ;

    static Quad q1 = SSE.parseQuad("(_ <s> <p> <o1>)") ;

    static Quad q2 = SSE.parseQuad("(_ <s> <p> <o2>)") ;

    static Quad q3 = SSE.parseQuad("(_ <s> <p> <o3>)") ;

    static Quad q4 = SSE.parseQuad("(_ <s> <p> <o4>)") ;

    private static int initCount = -1 ;

    //static final Location LOC = new Location(ConfigTest.getTestingDirDB()) ;
    static final AtomicInteger gen = new AtomicInteger() ;
    
}

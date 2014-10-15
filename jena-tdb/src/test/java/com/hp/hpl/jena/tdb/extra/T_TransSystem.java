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

package com.hp.hpl.jena.tdb.extra ;

import static com.hp.hpl.jena.tdb.transaction.TransTestLib.count ;
import static java.lang.Math.max ;
import static java.lang.Math.min ;
import static java.lang.String.format ;

import java.util.Iterator ;
import java.util.concurrent.* ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.RandomLib ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.FileMode ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.transaction.SysTxnState ;
import com.hp.hpl.jena.tdb.transaction.TransactionManager ;

/** System testing of the transactions. */
public class T_TransSystem
{
    static { org.apache.jena.atlas.logging.LogCtl.setLog4j() ; }
    private static Logger log = LoggerFactory.getLogger(T_TransSystem.class) ;

    /* Notes:
     * MS Windows does not allow memory mapped files to be deleted during the run of a JVM.
     * This means we can't delete a database and reuse it's directory (see clean()).
     * Therefore, this test program this does not run on MS Windows 64 bit mode.
     */
    
    static boolean MEM                  = true ;
    static String location              = true ? "/mnt/ssd1/tmp/DB163" : ConfigTest.getTestingDirDB() ;     // Using an SSD here is very helpful
    //static String location              = ConfigTest.getTestingDirDB() ;     // Using an SSD here is very helpful
    static final Location LOC           = MEM ? Location.mem() : new Location(location) ;
    
    static { 
        //SystemTDB.isWindows
        if ( false )
            SystemTDB.setFileMode(FileMode.direct) ;
        
        if ( SystemTDB.isWindows && SystemTDB.fileMode() == FileMode.mapped )
            log.error("**** Running with file mapped mode on MS Windows - expected test failure") ;
        
        FileOps.ensureDir(location) ;
    }

    private static boolean inlineProgress       = false ;   // Set true so that every transaction print a letter for what it does.
    private static boolean silent               = false ;   // No progress output 
    
    static {
        //TransactionManager.DEBUG = inlineProgress ;     // This cause one character details to be printed. 
        if ( TransactionManager.DEBUG != inlineProgress )
            log.warn("TransactionManager.DEBUG != inlineProgress (need change source code to make DEBUG not final)" ) ;
        

        // Various flags (may not still exist)
        //ObjectFileStorage.logging = true ;
        // FileBase.DEBUG = inlineProgress ;
        //NodeTableTrans.FIXUP = true ;
        //NodeTableTrans.APPEND_LOG = true ;
        // See also log4j.properties.
    }
    
    static final int Iterations                 = MEM ? 10000 : 10000 ;
    // Output style.
    static boolean logging                      = false ;
    
    // XXX Switch to threads choosing a mix of actions. 
    // Jena-163 - good number choice?
    // 1/0/2  8/10  3/3/10  4
    
    static final int numReaderTasks             = 5 ;   // 5
    static final int numWriterTasksA            = 3 ;   // 3
    static final int numWriterTasksC            = 5 ;   // 5

    static final int readerSeqRepeats           = 4 ;   // 8
    static final int readerMaxPause             = 20 ;  // 20

    static final int writerAbortSeqRepeats      = 4 ;   // 4
    static final int writerCommitSeqRepeats     = 4 ;   // 4
    static final int writerMaxPause             = 20 ;  // 20

    static final int numThreadsInPool           = 4 ;           // If <= 0 then use an unbounded thread pool.   
    private static ExecutorService execService  = null ;
    
    private static int iteration                = 0 ;
    private static int numIterationsPerBlock    = 100 ;
    private static int colCount                 = 0 ;
    private static int colMax                   = 200 ;
    
    // Queue treads starting
    private static Semaphore startPoint ;
    // Queue threads finishing.
    private static CountDownLatch doneSignal ;

    /** TODO
     * Ideally: better mixes of R, C and A.
     * One thread, processes a list of RCA choices.
     * Different mixes to different threads.
     * 
     * Random data
     */
    
    public static void main(String...args) throws InterruptedException
    {
        String x = (MEM?"memory":"disk["+SystemTDB.fileMode()+"]") ;
        
        // Make colMax >= numIterationsPerBlock in detailEveryTransaction = false mode
        if ( !inlineProgress )
            colMax = numIterationsPerBlock ;
        
        if ( logging )
            log.info("START ({}, {} iterations)", x, Iterations) ;
        else
            printf("START (%s, %d iterations)\n", x, Iterations) ;
        
        for ( iteration = 0 ; iteration < Iterations ; iteration++ )
        {
            clean() ;

            execService = ( numThreadsInPool > 0 ) 
                ? Executors.newFixedThreadPool(numThreadsInPool)
                : Executors.newCachedThreadPool() ;
            
            startTestIteration() ;         
            
            try {
                new T_TransSystem().manyReaderAndOneWriter() ;
            } catch (TDBException ex)
            {
                System.err.println() ;
                ex.printStackTrace(System.err) ;
                System.err.println() ;
            }
            
            // Should already be shutdown.
            execService.shutdown() ;
            if ( ! execService.awaitTermination(30, TimeUnit.SECONDS) )
                System.err.println("Shutdown didn't complete in time") ;
            endTestIteration() ;
        }
        
        endTest() ;
        if (logging)
            log.info("FINISH ({})", iteration) ;
        else
            println("FINISH") ;
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
            start() ;
            DatasetGraphTxn dsg = null ;
            try
            {
                int id = gen.incrementAndGet() ;
                for (int i = 0; i < repeats; i++)
                {
                    dsg = sConn.begin(ReadWrite.READ) ;
                    log.debug("reader start " + id + "/" + i) ;

                    // Original T_TransSystem code
//                    int x1 = count("SELECT * { ?s ?p ?o }", dsg) ;
//                    pause(maxpause) ;
//                    int x2 = count("SELECT * { ?s ?p ?o }", dsg) ;
//                    if (x1 != x2) log.warn(format("READER: %s Change seen: %d/%d : id=%d: i=%d",
//                                                  dsg.getTransaction().getLabel(), x1, x2, id, i)) ;
                    
                    // Add in an abort. 
                    long start = System.currentTimeMillis();
                    int x1 = count("SELECT * { ?s ?p ?o }", dsg) ;
                    pause(maxpause) ;

                    String qs1 = StrUtils.strjoinNL("PREFIX afn:     <http://jena.hpl.hp.com/ARQ/function#>",
                        "SELECT * { {FILTER(afn:wait(10))} UNION {?s ?p ?o }}") ;
                    String qs2 = StrUtils.strjoinNL("DESCRIBE ?s { ?s ?p ?o }") ;
                    try {
                        //countWithAbort(qs1, dsg, 5) ;
                        describeWithAbort(qs2, dsg, -1) ;
                    } catch (QueryCancelledException e) 
                    { 
                        txn("X", dsg);
                    }

                    log.debug("reader finish " + id + "/" + i) ;
                    dsg.end() ;
                    txn("R", dsg) ;
                    dsg = null ;
                }
                return null ;
            } catch (RuntimeException ex)
            {
                System.out.flush() ;
                System.err.println() ;
                ex.printStackTrace(System.err) ;
                if ( dsg != null )
                {
                    dsg.abort() ;
                    dsg.end() ;
                    txn("E", dsg) ;
                    dsg = null ;
                }
                System.exit(2) ;
                return null ;
            }
            finally { doneSignal.countDown(); }
        }
    }

    public static int countWithAbort(String queryStr, DatasetGraph dsg, long abortTime)
    {
        int counter = 0 ;
        Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ) ;
        try(QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.create(dsg))) {
            qExec.setTimeout(abortTime);
            ResultSet rs = qExec.execSelect() ;
            for (; rs.hasNext() ; )
            {
                rs.nextBinding() ;
                counter++ ;
            }
            return counter ;
        }
    }

    public static int describeWithAbort(String queryStr, DatasetGraph dsg, long abortTime)
    {
        int counter = 0 ;
        Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ) ;
        try(QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.create(dsg))) {
            qExec.setTimeout(abortTime);
            Model model = qExec.execDescribe();
            //ResultSet rs = qExec.execSelect() ;
            for(Iterator<Statement> stmIterator = model.listStatements(); stmIterator.hasNext();) {
                stmIterator.next();
                counter++;
            }
            return counter ;
        }
    }


    static class Writer implements Callable<Object>
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
            start() ;

            DatasetGraphTxn dsg = null ;
            try { 
                int id = gen.incrementAndGet() ;
                for ( int i = 0 ; i < repeats ; i++ )
                {
                    dsg = sConn.begin(ReadWrite.WRITE) ;
                    log.debug("writer start "+id+"/"+i) ;

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
                    {
                        dsg.commit() ;
                        txn("C", dsg) ;
                    }
                    else
                    {
                        dsg.abort() ;
                        txn("A", dsg) ;
                    }
                    SysTxnState state = sConn.getTransMgrState() ;
                    log.debug(state.toString()) ;
                    log.debug("writer finish "+id+"/"+i) ;  
                    Lib.sleep(20) ;
                    dsg.end() ;
                    dsg = null ;
                }
                return null ;
            }
            catch (RuntimeException ex)
            { 
                txn("E", dsg) ;
                System.err.println() ;
                ex.printStackTrace(System.err) ;
                System.exit(1) ;
                if ( dsg != null )
                {
                    dsg.abort() ;
                    dsg.end() ;
                    dsg = null ;
                }
                
                return null ;
            }
            finally { doneSignal.countDown(); }
        }
    
        // return the delta.
        protected int change(DatasetGraphTxn dsg, int id, int i)
        {  
            return changeProc(dsg, id, i) ; 
        }
    }
    
    public static void start()
    {
        if ( startPoint != null )
        {
            try { startPoint.acquire() ; }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
        pause(10) ;
    }

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
        dsg.end() ;
    }
    
    @AfterClass 
    public static void afterClass() {}

    private static void clean()
    {
        StoreConnection.release(LOC) ;
        if ( ! LOC.isMem() )
        {
            FileOps.clearDirectory(LOC.getDirectoryPath()) ;
            // Clean because it's new.
            //LOC = new Location(ConfigTest.getTestingDirUnique()) ;
        }
    }

    private StoreConnection sConn ;
    protected synchronized StoreConnection getStoreConnection()
    {
        
        
        StoreConnection sConn = StoreConnection.make(LOC) ;
        //sConn.getTransMgr().recording(true) ;
        return sConn ;
    }
    
    public T_TransSystem() {}
        
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
            e.printStackTrace(System.err) ;
        }
    }
    
    //@Test
    public void manyReaderAndOneWriter()
    {
        final StoreConnection sConn = getStoreConnection() ;
        
        Callable<?> procR = new Reader(sConn, readerSeqRepeats, readerMaxPause) ;      // Number of repeats, max pause
        Callable<?> procW_a = new Writer(sConn, writerAbortSeqRepeats, writerMaxPause, false)  ; // Number of repeats, max pause, commit. 
        Callable<?> procW_c = new Writer(sConn, writerCommitSeqRepeats, writerMaxPause, true)  ; // Number of repeats, max pause, commit. 

        // All threads start and queue on this - otherwise the thread start up as the executeor is loaded.
        // That can lead to uninterstin sequences of actions. 
        startPoint = null ; //new Semaphore(0) ;

        int RN1 = 1 ;
        int RN2 = min(1, numReaderTasks) ;
        int RN3 = max(numReaderTasks - RN1 - RN2,0);

        int WC1 =  numWriterTasksC/2 ;
        int WC2 =  2 ;
        int WC3 =  numWriterTasksC - WC1 - WC2 ;
        
        int WA1 =  numWriterTasksA/2 ;
        int WA2 =  numWriterTasksA - WA1 ;
        
        //System.out.println(RN1 + " " + RN2 + " " + RN3 + " " + WC1 + " " + WC2 + " " + WA1 + " " + WA2) ;
        int N = max(RN1,0) + max(RN2,0) + max(RN3,0) + max(WC1,0) + max(WC2,0) + max(WA1,0) + max(WA2,0) ;
        //System.out.println(N) ;
        doneSignal = new CountDownLatch(N) ;
        
        // Define the query mix.
        submit(execService, procW_c, WC1, "COMMIT-") ;
        submit(execService, procW_a, WA1, "ABORT-") ;
        submit(execService, procR,   RN1, "READ-") ;
        submit(execService, procW_c, WC2, "COMMIT-") ;
        submit(execService, procR,   RN2, "READ-") ;
        submit(execService, procW_a, WA2, "ABORT-") ;
        submit(execService, procR,   RN3, "READ-") ;
        submit(execService, procW_c, WC3, "COMMIT-") ;
        
        if ( startPoint != null )
            // Let them all go.
            startPoint.release(4000) ;

        // Wait until all done.
        try { doneSignal.await() ; }
        catch (InterruptedException e) { e.printStackTrace(System.err) ; }
        
        try { 
            // This is an orderly shutdown so followed by the awaitTermination
            // shoudl wait for all threads, making the CountDownLatch unnecessary.
            // CountDownLatch added as a precaution while searching for JENA-163
            // which seems to see occasional uncleared out node journal files.
            execService.shutdown() ;
            if ( ! execService.awaitTermination(100, TimeUnit.SECONDS) )
                System.err.println("Bad shutdown") ;
        } catch (InterruptedException e)
        {
            e.printStackTrace(System.err) ;
        } 
    }

    static class Callable2Runnable<T> implements Runnable
    {
        private Callable<T> callable ;

        Callable2Runnable(Callable<T> callable) { this.callable = callable ; }
        
        @Override public void run() { try { callable.call() ; } catch (Exception ex) {} }
    }
    
    private static int counter = 0 ;
    private <T> void submit(ExecutorService execService, Callable<T> proc, int numTasks, String label)
    {
        for ( int i = 0 ; i < numTasks ; i++ )
        {
            execService.submit(proc) ;
//            counter++ ;
//            Thread t = new Thread(new Callable2Runnable<T>(proc), label+counter) ;
//            t.start();
        }
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
    
    static void pause(int maxInternal)
    {
        int x = (int)Math.round(Math.random()*maxInternal) ;
        Lib.sleep(x) ;
    }
    
    static Quad genQuad(int value)
    {
        Node g1 = q.getGraph() ;
        int n1 = (int)Math.round(Math.random()*10000) ;
        int n2 = (int)Math.round(Math.random()*10000) ;
        
        Node g = Quad.defaultGraphNodeGenerated ; // urn:x-arq:DefaultGraphNode
        Node s = NodeFactory.createURI("S") ;
        Node p = NodeFactory.createURI("P"+value) ;
        // Integer - that gets inlined.
        Node o = NodeFactory.createLiteral(Integer.toString(value), null, XSDDatatype.XSDinteger) ;
        return new Quad(g,s,p,o) ;
    }

    static void txn(String label, DatasetGraphTxn dsg )
    {
        if ( ! inlineProgress )
            return ;
        checkCol() ;
        print(label) ;
        //print("["+dsg.getTransaction().getTxnId()+"]") ;
        colCount += label.length() ;
    }

    private static void startTestIteration()
    {
        checkCol() ;
        if ( iteration%numIterationsPerBlock == 0 )
        {
            if ( colCount != 0 )
            {
                println() ;
                colCount = 0 ;
            }
                
            printf("%03d: ", iteration) ;
            if ( inlineProgress )
                println() ;
        }
    }
    
    private static void endTestIteration()
    {
        if ( ! inlineProgress )
        {
            checkCol() ;
            printf(".") ;
            colCount += 1 ;
        }
        else
        {
            println() ;
            colCount = 0 ;
        }
    }
    
    private static void checkCol()
    {
        if ( colCount == colMax )
        {
            println() ;
            colCount = 0 ;
        }
    }

    private static void endTest()
    {
        if ( colCount > 0 || iteration%numIterationsPerBlock != 0 )
        {
            println() ;
            colCount = 0 ;
        }
        println() ;
    }

    private static void print(String str)
    {
        if ( silent ) return ;
        System.out.print(str) ;
    }

    private static void println(String string)
    {
        if ( silent ) return ;
        print(string) ;
        println() ; 
    }

    private static void println()
    {
        if ( silent ) return ;
        printf("\n") ; 
        System.out.flush() ;
    }

    private static void printf(String string, Object...args)
    {
        if ( silent ) return ;
        System.out.printf(string, args) ;
    }

    static Quad q  = SSE.parseQuad("(_ <s> <p> <o>) ") ;

    static Quad q1 = SSE.parseQuad("(_ <s> <p> <o1>)") ;

    static Quad q2 = SSE.parseQuad("(_ <s> <p> <o2>)") ;

    static Quad q3 = SSE.parseQuad("(_ <s> <p> <o3>)") ;

    static Quad q4 = SSE.parseQuad("(_ <s> <p> <o4>)") ;

    private static int initCount = -1 ;

    //static final Location LOC = new Location(ConfigTest.getTestingDirDB()) ;
    static final AtomicInteger gen = new AtomicInteger() ;
    
}

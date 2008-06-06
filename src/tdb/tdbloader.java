/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import lib.Tuple;
import tdb.cmdline.CmdTDB;
import arq.cmd.CmdUtils;
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.StringUtils;
import com.hp.hpl.jena.sparql.util.Timer;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.sparql.util.graph.GraphLoadMonitor;
import com.hp.hpl.jena.tdb.index.TripleIndex;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.PGraphBase;
import com.hp.hpl.jena.util.FileManager;

public class tdbloader extends CmdTDB
{
    ArgDecl argParallel = new ArgDecl(ArgDecl.NoValue, "parallel") ;
    ArgDecl argIncremental = new ArgDecl(ArgDecl.NoValue, "incr", "incrmenetal") ;
    
    boolean timing = true ;
    boolean doInParallel = false ;
    private boolean doIncremental = true ;
    private PGraphBase graph ;

    private TripleIndex triplesSPO ;
    private TripleIndex triplesPOS ;
    private TripleIndex triplesOSP ;
    
    static public void main(String... argv)
    { 
        CmdUtils.setLog4j() ;
        new tdbloader(argv).main() ;
    }

    protected tdbloader(String[] argv)
    {
        super(argv) ;
        super.add(argParallel, "--parallel", "Do rebuilding of secondary indexes in a parallel") ;
        super.add(argIncremental, "--incremental", "Do an incremental load (keep indexes during load, don't rebuild)") ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        doInParallel = super.contains(argParallel) ;
        doIncremental = super.contains(argIncremental) ;
    }
    
    @Override
    protected String getSummary()
    {
        return getCommandName()+" [--desc DATASET | -loc DIR] FILE ..." ;
    }

    @Override
    protected void exec()
    {
        if ( isVerbose())
            System.out.println(ARQ.getContext()) ;
        
        if ( isVerbose() )
            timing = true ;
        if ( isQuiet() )
            timing = false ;
        
        graph = getGraph() ;
        Model model = ModelFactory.createModelForGraph(graph) ;
        
        boolean rebuildIndexes = ! doIncremental ;
        if ( ! graph.isEmpty() )
            rebuildIndexes = false ;
        
        if ( rebuildIndexes )
        {
            println("** Load empty graph") ;
            // SPO only.
            dropSecondaryIndexes() ;
        }
        else
            println("** Load graph with existing data") ;
        
        Timer timer = new Timer() ;
        timer.startTimer() ;
        
        // Side effect : with no files to load, it copies the SPO index to the secondary indexes.
        if ( timing && super.getPositional().size() > 0 )
            println("** Load data") ;
        long count = 0 ;
        
        @SuppressWarnings("unchecked")
        Iterator<String> iter = getPositional().iterator() ;
        
        for ( ; iter.hasNext() ; )
        {
            now("-- Start data phase") ;
            String s = iter.next();
            count += loadOne(model, s) ;
            now("-- Finish data phase") ;
        }
        
        // Especially the node table.
        graph.sync(true) ;
        // Close other resourses (node table).
        
        if ( rebuildIndexes )
        {
            now("-- Start index phase") ;
            if ( timing )
                println("** Secondary indexes") ;
            // Now do secondary indexes.
            
            // Fork two separate processes? But it's disk bound!?
            createSecondaryIndexes(timing) ;
            now("-- Finish index phase") ;
        }

        if ( timing )
            println("** Close graph") ;
        graph.close() ;

        timer.endTimer() ;
        long time = timer.getTimeInterval() ;
        if ( timing )
        {
            long tps = 1000*count/time ;
            println() ;
            printf("Time for load: %.2fs [%,d triples/s]\n", time/1000.0, tps) ;
        }
    }

    private long loadOne(Model model, String s)
    {
        GraphLoadMonitor monitor = new GraphLoadMonitor(50000, false) ;
        if ( timing )
            model.getGraph().getEventManager().register(monitor) ;
        if ( ! s.equals("-") )
            FileManager.get().readModel(model, s) ;
        else
            // MUST BE N-TRIPLES
            model.read(System.in, null, "N-TRIPLES") ;
        
        if ( timing )
            model.getGraph().getEventManager().unregister(monitor) ;
        return timing ? monitor.getAddCount() : -1 ;
    }

    private void dropSecondaryIndexes()
    {
        // Remember first ...
        triplesSPO = graph.getIndexSPO() ;
        triplesPOS = graph.getIndexPOS() ;
        triplesOSP = graph.getIndexOSP() ;
        
        if ( graph.getIndexPOS() != null )
        {
            //graph.getIndexPOS().close();
            graph.setIndexPOS(null) ;
        }
        
        if ( graph.getIndexOSP() != null )
        {
            //graph.getIndexOSP().close();
            graph.setIndexOSP(null) ;
        }
    }

    private void createSecondaryIndexes(boolean printTiming)
    {
        if ( triplesPOS == null && triplesOSP == null )
            return ;
        
        if ( doInParallel )
            createSecondaryIndexesParallel(printTiming) ;
        else
            createSecondaryIndexesInterleaved(printTiming) ;
    }
    
    private void createSecondaryIndexesParallel(boolean printTiming)
    {
        if ( triplesPOS == null && triplesOSP == null )
            return ;
        
        println("** Parallel index building") ;
        Timer timer = new Timer() ;
        timer.startTimer() ;
        
        int semaCount = 0 ;
        Semaphore sema = new Semaphore(0) ;

        if ( triplesPOS != null )
        {
            Runnable builder1 = setup(sema, triplesSPO, triplesPOS, "POS", printTiming) ;
            new Thread(builder1).start() ;
            semaCount++ ;
        }
         
        if ( triplesOSP != null )
        {
            Runnable builder2 = setup(sema, triplesSPO, triplesOSP, "OSP", printTiming) ;
            new Thread(builder2).start() ;
            semaCount++ ;
        }
        
        try {  sema.acquire(semaCount) ; } catch (InterruptedException ex) { ex.printStackTrace(); }

        long time = timer.readTimer() ;
        if ( printTiming )
            printf("Time for POS/OSP indexing: %.2fs\n", time/1000.0) ;
        timer.endTimer() ;
        
        graph.setIndexOSP(triplesOSP) ;
        graph.setIndexPOS(triplesPOS) ;
    }
    
    private Runnable setup(final Semaphore sema, final TripleIndex srcIndex, final TripleIndex destIndex, final String label, final boolean printTiming)
    {
        Runnable builder = new Runnable(){
            @Override
            public void run()
            {
                copyIndex(srcIndex, new TripleIndex[]{destIndex}, label, printTiming) ;
                sema.release() ;
            }} ;
        
        return builder ;
    }

    private void createSecondaryIndexesSequential(boolean printTiming)
    {
        if ( triplesPOS == null && triplesOSP == null )
            return ;
        
        Timer timer = new Timer() ;
        timer.startTimer() ;
        
//        // ---- POS
//        RangeIndex idxPOS = graph.getIndexFactory().createRangeIndex(graph.getIndexRecordFactory(), "POS") ;
//        TripleIndex triplesPOS = new TripleIndex("POS", idxPOS) ;
        
        long time1 = timer.readTimer() ;
        if ( triplesPOS != null )
        {
            copyIndex(triplesSPO, new TripleIndex[]{triplesPOS}, "POS", printTiming) ;
            long time2 = timer.readTimer() ;
            if ( printTiming )
                printf("Time for POS indexing: %.2fs\n", (time2-time1)/1000.0) ;
        }
        
        long time2 = timer.readTimer() ; ;
//        // ---- OSP
//        RangeIndex idxOSP = graph.getIndexFactory().createRangeIndex(graph.getIndexRecordFactory(), "OSP") ;
//        TripleIndex triplesOSP = new TripleIndex("OSP", idxOSP) ;
        
        if ( triplesOSP != null )
        {
            copyIndex(triplesSPO, new TripleIndex[]{triplesOSP}, "OSP", printTiming) ;
            
            long time3 = timer.readTimer() ;
            if ( printTiming && triplesOSP != null )
                printf("Time for OSP indexing: %.2fs\n", (time3-time2)/1000.0) ;
            timer.endTimer() ;
        }
        
        graph.setIndexOSP(triplesOSP) ;
        graph.setIndexPOS(triplesPOS) ;

    }

    private void createSecondaryIndexesInterleaved(boolean printTiming)
    {
        if ( triplesPOS == null && triplesOSP == null )
            return ;
        
        Timer timer = new Timer() ;
        timer.startTimer() ;
        
//        // ---- POS
//        RangeIndex idxPOS = graph.getIndexFactory().createRangeIndex(graph.getIndexRecordFactory(), "POS") ;
//        TripleIndex triplesPOS = new TripleIndex("POS", idxPOS) ;
        
        long time1 = timer.readTimer() ;
        
        copyIndex(triplesSPO, new TripleIndex[]{triplesPOS, triplesOSP}, "POS/OSP", printTiming) ;
        
        long time2 = timer.readTimer() ;
        if ( printTiming )
            printf("Time for both POS and OSP indexes: %.2fs\n", (time2-time1)/1000.0) ;
        
        graph.setIndexOSP(triplesOSP) ;
        graph.setIndexPOS(triplesPOS) ;

    }
    
    private static Object lock = new Object() ;

    private static void copyIndex(TripleIndex srcIdx, TripleIndex[] destIndexes, String label, boolean printTiming)
    {
        long quantum = 100000 ;
        long quantum2 = 5*quantum ;

        Timer timer = new Timer() ;
        long cumulative = 0 ;
        long c = 0 ;
        long last = 0 ;
        timer.startTimer() ;
        
        Iterator<Tuple<NodeId>> iter = srcIdx.all() ;
        for ( int i = 0 ; iter.hasNext() ; i++ )
        {
            Tuple<NodeId> tuple = iter.next();
            for ( TripleIndex destIdx : destIndexes )
            {
                if ( destIdx != null )
                    destIdx.add(tuple.get(0), tuple.get(1), tuple.get(2)) ;
            }
            c++ ;
            cumulative++ ;
            if ( printTiming && tickPoint(cumulative,quantum) )
            {
                long t = timer.readTimer() ;
                long batchTime = t-last ;
                long elapsed = t ;
                last = t ;
                printf("Index %s: %,d slots (Batch: %,d slots/s / Run: %,d slots/s)\n", 
                                  label, cumulative, 1000*c/batchTime, 1000*cumulative/elapsed) ;
                if (tickPoint(cumulative, quantum2) )
                {
                    String timestamp = Utils.nowAsString() ;
                    String x = StringUtils.str(elapsed/1000F) ;
                    // XXX Print elapsed.  Common formatting with GraphLoadMonitor - but now to share?
                    printf("  Elapsed: %s seconds [%s]\n", x, timestamp) ;
                    //now(label) ; 
                }
                c = 0 ;
            }
        }
        
        for ( TripleIndex destIdx : destIndexes )
        {
            if ( destIdx != null )
                destIdx.sync(true) ;
        }
        
        long totalTime = timer.endTimer() ;
        
        if ( printTiming )
        {
            if ( cumulative > 0 )
            {
                if ( totalTime > 0 )
                    printf("Index %s: %,d triples indexed in %,.2fs [%,d slots/s]\n", 
                                      label, cumulative, totalTime/1000.0, 1000*cumulative/totalTime) ;
                else
                    printf("Index %s: %,d triples indexed in %,.2fs\n", label, cumulative, totalTime/1000.0) ;
            }
            else
                printf("Index %s: 0 triples indexed\n", label) ;
        }
    }
    
    private static boolean tickPoint(long counter, long quantum)
    {
        return counter%quantum == 0 ;
    }

    private static synchronized void printf(String fmt, Object ...args)
    { System.out.printf(fmt, args) ; }
    
    private static synchronized void println()
    { System.out.println() ; }
    
    private static synchronized void println(String str)
    { System.out.println(str) ; }
    
    private static synchronized void now(String str)
    { 
        if ( str != null )
        {
            System.out.print(str) ;
            System.out.print(" : ") ;
        }
        System.out.println(StringUtils.str(new Date())) ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
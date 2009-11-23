/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.io.FileOutputStream ;
import java.io.IOException ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.concurrent.Semaphore ;

import atlas.lib.ArrayUtils ;
import atlas.lib.MapUtils ;
import atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemWriter ;
import com.hp.hpl.jena.sparql.util.ALog ;
import com.hp.hpl.jena.sparql.util.IndentedWriter ;
import com.hp.hpl.jena.sparql.util.StringUtils ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.sparql.util.graph.GraphListenerBase ;
import com.hp.hpl.jena.sparql.util.graph.GraphLoadMonitor ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.solver.stats.StatsCollector ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.util.FileManager ;


/** To directly load data, including manipulating the indexes at a quite low level for efficiency.
 * Not efficent for small, incremental additions to a graph.  
 */

public class BulkLoader
{
    private GraphTDB graph ;
    private Symbol symTesting = SystemTDB.allocSymbol("testing") ;
    
    private boolean showProgress ;
    
    private boolean doInParallel = false ;
    private boolean doIncremental = false ;
    private boolean doInterleaved = false ;
    private boolean generateStats  = false ;

    private int          numIndexes ; 
    private TupleIndex   primaryIndex ;
    private TupleIndex[] secondaryIndexes ;
    
    private Item statsItem = null ;
    private NodeTupleTable nodeTupleTable ; 

    public static void load(GraphTDB graph, List<String> urls, boolean showProgress)
    {
        BulkLoader loader = new BulkLoader(graph, showProgress) ;
        loader.load(urls) ;
    }
    
    public static void load(GraphTDB graph, String url, boolean showProgress)
    {
        List<String> list = new ArrayList<String>() ;
        list.add(url) ;
        BulkLoader loader = new BulkLoader(graph, showProgress) ;
        loader.load(list) ;
    }
    
    public BulkLoader(GraphTDB graph, boolean showProgress)
    {
        this(graph, showProgress, false, false, false) ;
    }
    
    /** Create a bulkloader for a graph : showProgress/parallel/incremental/generate statistics */ 

    public BulkLoader(GraphTDB graph, boolean showProgress, boolean doInParallel, boolean doIncremental, boolean generateStats)
    {
        // Bulk loading restricted to triple indexes
        // Assumes that the NodeTupleTable is 3-way at the moment
        this.graph = graph ;
        this.nodeTupleTable = graph.getNodeTupleTable() ;
        
        if ( nodeTupleTable.getTupleTable().getTupleLen() != 3 )
            throw new TDBException("BulkLoader: Bulk mode only works on 3-tuples") ;
        
        this.showProgress = showProgress ;
        this.doInParallel = doInParallel ;
        this.doIncremental = doIncremental ;
        this.generateStats = generateStats ;
    }
    
    public void load(List<String> urls)
    {
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
        {
            println("** Load graph with existing data") ;
            generateStats = false ;
        }
        
        Timer timer = new Timer() ;
        timer.startTimer() ;
        
//        // Side effect : with no files to load, it copies the SPO index to the secondary indexes.
//        if ( timing && super.getPositional().size() > 0 )
//            println("** Load data") ;
        long count = 0 ;
        
        for ( String url : urls )
        {
            statsStart(model) ;
            now("-- Start data phase") ;
            count += loadOne(model, url, showProgress) ;
            now("-- Finish data phase") ;
            statsFinish(model) ;
        }
        
        if ( generateStats && statsItem != null )
        {
            String fn = graph.getLocation().getPath(Names.optStats) ;
            try {
                FileOutputStream fout = new FileOutputStream(fn) ;
                IndentedWriter out = new IndentedWriter(fout) ;
                ItemWriter.write(out, statsItem, null) ;
                out.ensureStartOfLine() ;
                out.flush() ;
                fout.close();
            } catch (IOException ex)
            {
                ALog.fatal(this, "Failed to write stats file: "+ex.getLocalizedMessage(), ex) ;
            }
        }
        
        // Especially the node table.
        graph.sync(true) ;
        // Close other resourses (node table).
        
        println() ;
        
        if ( rebuildIndexes )
        {
            now("-- Start index phase") ;
            if ( showProgress )
                println("** Secondary indexes") ;
            // Now do secondary indexes.
            
            // Fork two separate processes? But it's disk bound!?
            createSecondaryIndexes(showProgress) ;
            now("-- Finish index phase") ;
        }

        if ( showProgress )
            println("** Close graph") ;
        graph.close() ;

        timer.endTimer() ;
        long time = timer.getTimeInterval() ;
        if ( showProgress )
        {
            long tps = 1000*count/time ;
            println() ;
            printf("Time for load: %.2fs [%,d triples/s]\n", time/1000.0, tps) ;
        }
        
    }

    
    // --------
    // TODO Unique triples only.
    GraphStatsCollector statsMonitor = new GraphStatsCollector() ;
        
    static class GraphStatsCollector extends GraphListenerBase
    {
        Map<Node, Integer> predicates = new HashMap<Node, Integer>() ;
        long count = 0 ;
        @Override
        protected void addEvent(Triple t)
        {
            // Assumes unique.
            MapUtils.increment(predicates, t.getPredicate()) ;
            count++ ;
        }

        @Override
        protected void deleteEvent(Triple t)
        {}} ;
        
    private void statsStart(Model model)
    {
        if ( generateStats )
            model.getGraph().getEventManager().register(statsMonitor) ;
    }

    private void statsFinish(Model model)
    {
        if ( generateStats )
        {
            model.getGraph().getEventManager().unregister(statsMonitor) ;
            statsItem = StatsCollector.format(statsMonitor.predicates, statsMonitor.count) ;
        }
    }

    // --------
    
    /** Tick point for messages during loading of data */
    public static int LoadTickPoint = 50000 ;
    /** Tick point for messages during secodnary index creation */
    public static long IndexTickPoint = 100000 ;
    
    private static long loadOne(Model model, String s, boolean showProgress)
    {
        GraphLoadMonitor monitor = new GraphLoadMonitor(LoadTickPoint, false) ;
        if ( showProgress )
            model.getGraph().getEventManager().register(monitor) ;
        if ( ! s.equals("-") )
            FileManager.get().readModel(model, s) ;
        else
            // MUST BE N-TRIPLES
            model.read(System.in, null, "N-TRIPLES") ;
        
        if ( showProgress )
            model.getGraph().getEventManager().unregister(monitor) ;
        return showProgress ? monitor.getAddCount() : -1 ;
    }

    private <T> T[] copy(T[] array)
    {
        @SuppressWarnings("unchecked")
        T[] array2 = (T[])new Object[array.length] ;
        System.arraycopy(array, 0, array2, 0, array.length) ;
        return array2 ;
    }
    
    private void dropSecondaryIndexes()
    {
        // Remember first ...
        numIndexes = nodeTupleTable.getTupleTable().numIndexes() ;
        primaryIndex = nodeTupleTable.getTupleTable().getIndex(0) ;
        
        secondaryIndexes = ArrayUtils.alloc(TupleIndex.class, numIndexes-1) ;
        System.arraycopy(nodeTupleTable.getTupleTable().getIndexes(), 1, 
                         secondaryIndexes, 0,
                         numIndexes-1) ;
        // Set non-primary indexes to null.
        for ( int i = 1 ; i < numIndexes ; i++ )
            nodeTupleTable.getTupleTable().setTupleIndex(i, null) ;
    }

    private void createSecondaryIndexes(boolean printTiming)
    {
        if ( doInParallel )
            createSecondaryIndexesParallel(printTiming) ;
        else if ( doInterleaved )
            createSecondaryIndexesInterleaved(printTiming) ;
        else
            createSecondaryIndexesSequential(printTiming) ;
        
        // Re-attach the indexes.
        for ( int i = 1 ; i < numIndexes ; i++ )
            nodeTupleTable.getTupleTable().setTupleIndex(i, secondaryIndexes[i-1]) ;
        
    }
    
    private void createSecondaryIndexesParallel(boolean printTiming)
    {
        println("** Parallel index building") ;
        Timer timer = new Timer() ;
        timer.startTimer() ;
        
        TupleIndex primary = nodeTupleTable.getTupleTable().getIndex(0) ;
        
        int semaCount = 0 ;
        Semaphore sema = new Semaphore(0) ;

        for ( TupleIndex index : secondaryIndexes )
        {
            if ( index != null )
            {
                Runnable builder = setup(sema, primary, index, index.getLabel(), printTiming) ;
                new Thread(builder).start() ;
                semaCount++ ;
            }
        }
        
        try {  sema.acquire(semaCount) ; } catch (InterruptedException ex) { ex.printStackTrace(); }

        long time = timer.readTimer() ;
        timer.endTimer() ;
        if ( printTiming )
            printf("Time for parallel indexing: %.2fs\n", time/1000.0) ;
    }
    
    private Runnable setup(final Semaphore sema, final TupleIndex srcIndex, final TupleIndex destIndex, final String label, final boolean printTiming)
    {
        Runnable builder = new Runnable(){
            //@Override
            public void run()
            {
                copyIndex(srcIndex.all(), new TupleIndex[]{destIndex}, label, printTiming) ;
                sema.release() ;
            }} ;
        
        return builder ;
    }

    // Create each secondary indexes, doing one at a time.
    private void createSecondaryIndexesSequential(boolean printTiming)
    {
        Timer timer = new Timer() ;
        timer.startTimer() ;
        TupleIndex primary = nodeTupleTable.getTupleTable().getIndex(0) ;
        
//        RangeIndex rIdx = ((TupleIndexRecord)primary).getRangeIndex() ;
//        rIdx.iterator() ;
        
        for ( TupleIndex index : secondaryIndexes )
        {
            if ( index != null )
            {
                long time1 = timer.readTimer() ;
                copyIndex(primary.all(), new TupleIndex[]{index}, index.getLabel(), printTiming) ;
                long time2 = timer.readTimer() ; ;
//                if ( printTiming )
//                    printf("Time for %s indexing: %.2fs\n", index.getLabel(), (time2-time1)/1000.0) ;
                if ( printTiming )
                    println() ;
            }  
        }
    }

    // Do as one pass over the SPO index, creating both other indexes at the same time.
    // Can be hugely costly in system resources.
    private void createSecondaryIndexesInterleaved(boolean printTiming)
    {
        Timer timer = new Timer() ;
        timer.startTimer() ;
        
        long time1 = timer.readTimer() ;
        
        copyIndex(primaryIndex.all(), secondaryIndexes, "All", printTiming) ;
        
        long time2 = timer.readTimer() ;
        if ( printTiming )
            printf("Time for all indexes: %.2fs\n", (time2-time1)/1000.0) ;
    }
    
    private static Object lock = new Object() ;

    private void copyIndex(Iterator<Tuple<NodeId>> srcIter, TupleIndex[] destIndexes, String label, boolean printTiming)
    {
        long quantum2 = 5*IndexTickPoint ;
        Timer timer = new Timer() ;
        long cumulative = 0 ;
        long c = 0 ;
        long last = 0 ;
        timer.startTimer() ;
        
        for ( int counter = 0 ; srcIter.hasNext() ; counter++ )
        {
            Tuple<NodeId> tuple = srcIter.next();
            for ( TupleIndex destIdx : destIndexes )
            {
                if ( destIdx != null )
                    destIdx.add(tuple) ;
            }
            c++ ;
            cumulative++ ;
            if ( printTiming && tickPoint(cumulative,IndexTickPoint) )
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
        
        for ( TupleIndex destIdx : destIndexes )
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

    
    // ---- Misc utilities
    private synchronized void printf(String fmt, Object... args)
    {
        if (!showProgress) return ;
        System.out.printf(fmt, args) ;
    }

    private synchronized void println()
    {
        if (!showProgress) return ;
        System.out.println() ;
    }

    private synchronized void println(String str)
    {
        if (!showProgress) return ;
        System.out.println(str) ;
    }

    private synchronized void now(String str)
    {
        if (!showProgress) return ;

        if (str != null)
        {
            System.out.print(str) ;
            System.out.print(" : ") ;
        }
        System.out.println(StringUtils.str(new Date())) ;
    }
    
    
    // --------
    
    public static void loadSimple(Model model, List<String> urls, boolean showProgress)
    {
        Timer timer = new Timer() ;
        timer.startTimer() ;
        long count = 0 ;
        
        for ( String s : urls )
        {
            if ( showProgress ) 
                System.out.printf("Load: %s\n", s) ;
            count += loadOne(model, s, showProgress) ;
        }

        //long time = timer.endTimer() ;
        //System.out.printf("Time for load: %.2fs [%,d triples/s]\n", time/1000.0, (triples/time)) ;
        model.close();
    }
    
    
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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
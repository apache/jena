/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader;

import java.util.Date ;
import java.util.Iterator ;
import java.util.concurrent.Semaphore ;

import org.openjena.atlas.lib.ArrayUtils ;
import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.StringUtils ;
import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.lib.Sync ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.Session ;

/** Load into one NodeTupleTable (triples, quads, other) 
 */

public class LoaderNodeTupleTable implements Closeable, Sync
{
    private boolean showProgress    = false ;
    private boolean doInParallel    = false ;
    private boolean doIncremental   = false ;
    private boolean doInterleaved   = false ;
    private boolean generateStats   = false ;

    private int          numIndexes ; 
    private TupleIndex   primaryIndex ;
    private TupleIndex[] secondaryIndexes ;
    
    private NodeTupleTable nodeTupleTable ;
    
    // Variables across load operations
    private boolean dropAndRebuildIndexes ;
    private Timer timer ;
    private long count ;

    private Session session ; 

    // cases:
    // triple load : source => NodeTupleTable 
    // triple load to named graph 
    // quads : two NodeTupleTables
    
    public LoaderNodeTupleTable(Session session, NodeTupleTable nodeTupleTable, boolean showProgress)
    {
        this(session, nodeTupleTable, showProgress, false, false, false) ;
    }
    
    /** Create a bulkloader for triples or quads: showProgress/parallel/incremental/generate statistics */ 

    public LoaderNodeTupleTable(Session session, NodeTupleTable nodeTupleTable, boolean showProgress, boolean doInParallel, boolean doIncremental, boolean generateStats)
    {
        this.session = session ;
        this.nodeTupleTable = nodeTupleTable ;
        this.showProgress = showProgress ;
        this.doInParallel = doInParallel ;
        this.doIncremental = doIncremental ;
        this.generateStats = generateStats ;
    }

    // -- LoaderFramework
    
    protected void loadPrepare()
    {
        dropAndRebuildIndexes = ! doIncremental ;
        if ( ! nodeTupleTable.isEmpty() )
            dropAndRebuildIndexes = false ;
        
        if ( dropAndRebuildIndexes )
        {
            //XXX
            println("** Load empty XYZ") ;
            // SPO only.
            dropSecondaryIndexes() ;
        }
        else
        {
            println("** Load graph with existing data") ;
            generateStats = false ;
        }

        if ( generateStats )
            statsPrepare() ;
    }
        
    protected void loadFinalize()
    {
        if ( generateStats )
            statsFinalize() ;

        println() ;
        if ( dropAndRebuildIndexes )
        {
            now("-- Start index phase") ;
            if ( showProgress )
                println("** Secondary indexes") ;
            // Now do secondary indexes.
            createSecondaryIndexes(showProgress) ;
            now("-- Finish index phase") ;
        }

        if ( showProgress )
            println("** Close graph") ;
        
        session.finishUpdate() ;

        long time = timer.getTimeInterval() ;
        if ( showProgress )
        {
            long tps = 1000*count/time ;
            println() ;
            printf("Time for load: %.2fs [%,d triples/s]\n", time/1000.0, tps) ;
        }
    }

    // XXX
    protected void statsPrepare() {}
    protected void statsFinalize() {}

    public void load(Node... nodes)
    {
        nodeTupleTable.addRow(nodes) ;
    }
    
    public void sync(boolean force) {}
    public void sync() {}
    
    // --------
    
    public void close()
    { sync() ; }

    /** Tick point for messages during loading of data */
    public static int LoadTickPoint = 50000 ;
    /** Tick point for messages during secodnary index creation */
    public static long IndexTickPoint = 100000 ;
    
    private <T> T[] copyx(T[] array)
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
        BuilderSecondaryIndexes builder ;
        
        if ( doInParallel )
            builder = new BuilderSecondaryIndexesParallel() ;
        else if ( doInterleaved )
            builder = new BuilderSecondaryIndexesInterleaved() ;
        else
            builder = new BuilderSecondaryIndexesSequential() ;
        
        builder.createSecondaryIndexes(primaryIndex, secondaryIndexes, printTiming) ;
            
        // Re-attach the indexes.
        for ( int i = 1 ; i < numIndexes ; i++ )
            nodeTupleTable.getTupleTable().setTupleIndex(i, secondaryIndexes[i-1]) ;
        
    }
    
    class BuilderSecondaryIndexesParallel implements BuilderSecondaryIndexes
    {
        public void createSecondaryIndexes(TupleIndex   primaryIndex ,
                                           TupleIndex[] secondaryIndexes ,
                                           boolean printTiming)
        {
            println("** Parallel index building") ;
            Timer timer = new Timer() ;
            timer.startTimer() ;

            int semaCount = 0 ;
            Semaphore sema = new Semaphore(0) ;

            for ( TupleIndex index : secondaryIndexes )
            {
                if ( index != null )
                {
                    Runnable builder = setup(sema, primaryIndex, index, index.getLabel(), printTiming) ;
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
    }

    class BuilderSecondaryIndexesSequential implements BuilderSecondaryIndexes
    {
        // Create each secondary indexes, doing one at a time.
        public void createSecondaryIndexes(TupleIndex   primaryIndex ,
                                           TupleIndex[] secondaryIndexes ,
                                           boolean printTiming)
        {
            Timer timer = new Timer() ;
            timer.startTimer() ;
            TupleIndex primary = nodeTupleTable.getTupleTable().getIndex(0) ;

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
    }
    
    class BuilderSecondaryIndexesInterleaved implements BuilderSecondaryIndexes
    {

        // Do as one pass over the SPO index, creating both other indexes at the same time.
        // Can be hugely costly in system resources.
        public void createSecondaryIndexes(TupleIndex   primaryIndex ,
                                           TupleIndex[] secondaryIndexes ,
                                           boolean printTiming)
        {
            Timer timer = new Timer() ;
            timer.startTimer() ;

            long time1 = timer.readTimer() ;

            copyIndex(primaryIndex.all(), secondaryIndexes, "All", printTiming) ;

            long time2 = timer.readTimer() ;
            if ( printTiming )
                printf("Time for all indexes: %.2fs\n", (time2-time1)/1000.0) ;
        }
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
                    // Print elapsed.  Common formatting with GraphLoadMonitor - but now to share?
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
    
    
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
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
/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader;

import java.util.Iterator ;

import org.openjena.atlas.lib.ArrayUtils ;
import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Tuple ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.StringUtils ;
import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.lib.Sync ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** 
 * Load into one NodeTupleTable (triples, quads, other) 
 */

public class LoaderNodeTupleTable implements Closeable, Sync
{
    private LoadMonitor monitor          = null ;
    private boolean doIncremental   = false ;
    private boolean generateStats   = false ;

    private int          numIndexes ; 
    private TupleIndex   primaryIndex ;
    private TupleIndex[] secondaryIndexes ;
    
    private NodeTupleTable nodeTupleTable ;
    
    private boolean dropAndRebuildIndexes ;
    //private Timer timer ;
    private long count = 0 ;
    
    static private Logger logLoad = LoggerFactory.getLogger("com.hp.hpl.jena.tdb.loader") ;

    public LoaderNodeTupleTable(NodeTupleTable nodeTupleTable, LoadMonitor monitor)
    {
        this.nodeTupleTable = nodeTupleTable ;
        this.monitor = monitor ;
        this.doIncremental = false ;    // Until we know it's safe.
        this.generateStats = false ;
    }

    // -- LoaderFramework
    
    protected void loadPrepare()
    {
        dropAndRebuildIndexes = ! doIncremental ;
        if ( ! nodeTupleTable.isEmpty() )
            dropAndRebuildIndexes = false ;

        if ( dropAndRebuildIndexes )
        {
            monitor.print("** Load empty table") ;
            // SPO only.
            dropSecondaryIndexes() ;
        }
        else
        {
            monitor.print("** Load into table with existing data") ;
            generateStats = false ;
        }

        if ( generateStats )
            statsPrepare() ;
        
//        timer = new Timer() ;
//        timer.startTimer() ;
    }
        
    protected void loadSecondaryIndexes()
    {
        if ( generateStats )
            statsFinalize() ;

        if ( dropAndRebuildIndexes )
        {
            monitor.print("-- Start index phase") ;
            // Now do secondary indexes.
            createSecondaryIndexes() ;
            monitor.print("-- Finish index phase") ;
        }

//        long time = timer.getTimeInterval() ;
//        if ( showProgress )
//        {
//            long tps = 1000*count/time ;
//            println() ;
//            printf("Time for load: %.2fs [%,d triples/s]\n", time/1000.0, tps) ;
//        }
    }

    // XXX
    protected void statsPrepare() {}
    protected void statsFinalize() {}

    /** Notify start of loading process */
    public void loadStart()
    {
        monitor.startLoad() ;
        monitor.startDataPhase() ;
        loadPrepare() ;
    }
    
    /** Stream in items to load ... */
    public void load(Node... nodes)
    {
        count++ ;
        nodeTupleTable.addRow(nodes) ;
    }
    
    /** Notify End of data to load - this operation may 
     * undertake a significant amount of work.
     */
    public void loadFinish()
    {
        monitor.finishDataPhase() ;
        if ( count > 0 )
        {
            // Do index phase only if any items seen. 
            monitor.startIndexPhase() ;
            loadSecondaryIndexes() ;
            monitor.finishIndexPhase() ;
        }
        monitor.finishLoad() ;
    }

    public void sync(boolean force) {}
    public void sync() {}
    
    // --------
    
    public void close()
    { sync() ; }

    /** Tick point for messages during loading of data */
    public static int LoadTickPoint = 2000 ;
    /** Tick point for messages during secodnary index creation */
    public static long IndexTickPoint = 5000 ;
    
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

    private void createSecondaryIndexes()
    {
        BuilderSecondaryIndexes builder = new BuilderSecondaryIndexesSequential(monitor) ;
        
//        if ( doInParallel )
//            builder = new BuilderSecondaryIndexesParallel(printer) ;
//        else if ( doInterleaved )
//            builder = new BuilderSecondaryIndexesInterleaved(printer) ;
//        else
//            builder = new BuilderSecondaryIndexesSequential(printer) ;
        
        builder.createSecondaryIndexes(primaryIndex, secondaryIndexes) ;
            
        // Re-attach the indexes.
        for ( int i = 1 ; i < numIndexes ; i++ )
            nodeTupleTable.getTupleTable().setTupleIndex(i, secondaryIndexes[i-1]) ;
        
    }
    
    private static Object lock = new Object() ;

    static void copyIndex(Iterator<Tuple<NodeId>> srcIter, TupleIndex[] destIndexes, String label, LoadMonitor monitor)
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
            if ( tickPoint(cumulative,IndexTickPoint) )
            {
                long t = timer.readTimer() ;
                long batchTime = t-last ;
                long elapsed = t ;
                last = t ;
                monitor.print("Index %s: %,d slots (Batch: %,d slots/s / Run: %,d slots/s)", 
                       label, cumulative, 1000*c/batchTime, 1000*cumulative/elapsed) ;
                if (tickPoint(cumulative, quantum2) )
                {
                    String timestamp = Utils.nowAsString() ;
                    String x = StringUtils.str(elapsed/1000F) ;
                    // Print elapsed.  Common formatting with GraphLoadMonitor - but now to share?
                    monitor.print("  Elapsed: %s seconds [%s]", x, timestamp) ;
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

        if ( cumulative > 0 )
        {
            if ( totalTime > 0 )
                monitor.print("Index %s: %,d triples indexed in %,.2fs [%,d slots/s]", 
                              label, cumulative, totalTime/1000.0, 1000*cumulative/totalTime) ;
            else
                monitor.print("Index %s: %,d triples indexed in %,.2fs", label, cumulative, totalTime/1000.0) ;
        }
        else
            monitor.print("Index %s: 0 triples indexed", label) ;
    }
   
    private static boolean tickPoint(long counter, long quantum)
    {
        return counter%quantum == 0 ;
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
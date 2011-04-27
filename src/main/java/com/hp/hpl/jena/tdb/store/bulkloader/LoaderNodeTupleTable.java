/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader;

import java.util.Arrays ;
import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.ArrayUtils ;
import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sync ;
import org.openjena.atlas.lib.Tuple ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
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
    private String itemsName ;
    
    static private Logger logLoad = LoggerFactory.getLogger("com.hp.hpl.jena.tdb.loader") ;

    public LoaderNodeTupleTable(NodeTupleTable nodeTupleTable, String itemsName, LoadMonitor monitor)
    {
        this.nodeTupleTable = nodeTupleTable ;
        this.monitor = monitor ;
        this.doIncremental = false ;        // Until we know it's safe.
        this.generateStats = false ;
        this.itemsName = itemsName ;          // "triples", "quads", "tuples" (plural)
    }

    // -- LoaderFramework
    
    protected void loadPrepare()
    {
        dropAndRebuildIndexes = ! doIncremental ;
        if ( ! nodeTupleTable.isEmpty() )
            dropAndRebuildIndexes = false ;

        if ( dropAndRebuildIndexes )
        {
            monitor.print("** Load empty %s table", itemsName) ;
            // SPO only.
            dropSecondaryIndexes() ;
        }
        else
        {
            monitor.print("** Load into %s table with existing data", itemsName) ;
            generateStats = false ;
        }

        if ( generateStats )
            statsPrepare() ;
    }
        
    protected void loadSecondaryIndexes()
    {
        if ( generateStats )
            statsFinalize() ;

        if ( dropAndRebuildIndexes )
            // Now do secondary indexes.
            createSecondaryIndexes() ;
    }

    protected void statsPrepare() {}
    protected void statsFinalize() {}

    public void loadStart()     { monitor.startLoad() ; }
    public void loadFinish()    { monitor.finishLoad() ; }
    
    /** Notify start of loading process */
    public void loadDataStart()
    {
        monitor.startDataPhase() ;
        loadPrepare() ;
    }
    
    /** Stream in items to load ... */
    public void load(Node... nodes)
    {
        try {
            count++ ;           // Not zero the first time.
            monitor.dataItem() ;
            nodeTupleTable.addRow(nodes) ;

//            // Flush every so often.
//            // Seems to improve performance:maybe because a bunch of blcoks are
//            // flushed together meaning better disk access pattern 
//            if ( LoadFlushTickPrimary > 0 &&  count % LoadFlushTickPrimary == 0 )
//            {
//                System.out.println("FLUSH - primary") ;
//                nodeTupleTable.sync() ;
//            }
            
        } catch (RuntimeException ex)
        {
            System.err.println(Iter.asString(Arrays.asList(nodes))) ;
            ex.printStackTrace(System.err) ;
        }
    }
    
    /** Notify End of data to load - this operation may 
     * undertake a significant amount of work.
     */
    public void loadDataFinish()
    {
//      if ( LoadFlushTickPrimary > 0 &&  count % LoadFlushTickPrimary == 0 )
//          nodeTupleTable.sync() ;
        monitor.finishDataPhase() ;
    }
    
    public void loadIndexStart()
    {
        if ( count > 0 )
        {
            // Do index phase only if any items seen.
            monitor.startIndexPhase() ;
            loadSecondaryIndexes() ;
        }
    }

    public void loadIndexFinish()
    {
        if ( count > 0 )
            monitor.finishIndexPhase() ;
    }
    
    
    public void sync(boolean force) {}
    public void sync() {}
    
    // --------
    
    public void close()
    { sync() ; }

    private void dropSecondaryIndexes()
    {
        // Remember first ...
        // CAUTION - the TupleTable may be a view and these return the real tuple table.
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
        monitor.startIndex(label) ;
        long counter = 0 ;
        for ( ; srcIter.hasNext() ; )
        {
            counter++ ;
            Tuple<NodeId> tuple = srcIter.next();
            monitor.indexItem() ;
            for ( TupleIndex destIdx : destIndexes )
            {
                if ( destIdx != null )
                    destIdx.add(tuple) ;
            }
            
//            // Flush every so often.
//            if ( LoadFlushTickSecondary > 0 && counter % LoadFlushTickSecondary == 0 )
//            {
//                System.out.println("FLUSH - secondary") ;
//                sync(destIndexes ) ;
//            }
        }

//        // And finally ...
//        if ( LoadFlushTickSecondary > 0 && counter % LoadFlushTickSecondary != 0 )
//            sync(destIndexes) ;

        monitor.finishIndex(label) ;
    }


    static private void sync(TupleIndex[] indexes)
    {
        for ( TupleIndex idx : indexes )
        {
            if ( idx != null )
                idx.sync() ;
        }
    }
   
    private static boolean tickPoint(long counter, long quantum)
    {
        return counter%quantum == 0 ;
    }
    
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
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
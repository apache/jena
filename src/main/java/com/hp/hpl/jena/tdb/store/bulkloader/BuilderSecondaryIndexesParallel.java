/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader;

import java.util.concurrent.Semaphore ;

import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;

class BuilderSecondaryIndexesParallel implements BuilderSecondaryIndexes
{
    private LoadMonitor monitor ;

    BuilderSecondaryIndexesParallel(LoadMonitor monitor) { this.monitor = monitor ; } 
    
    public void createSecondaryIndexes(TupleIndex   primaryIndex ,
                                       TupleIndex[] secondaryIndexes)
    {
        monitor.print("** Parallel index building") ;
        Timer timer = new Timer() ;
        timer.startTimer() ;

        int semaCount = 0 ;
        Semaphore sema = new Semaphore(0) ;

        for ( TupleIndex index : secondaryIndexes )
        {
            if ( index != null )
            {
                Runnable builder = setup(sema, primaryIndex, index, index.getLabel()) ;
                new Thread(builder).start() ;
                semaCount++ ;
            }
        }

        try {  sema.acquire(semaCount) ; } catch (InterruptedException ex) { ex.printStackTrace(); }

        long time = timer.readTimer() ;
        timer.endTimer() ;
        monitor.print("Time for parallel indexing: %.2fs\n", time/1000.0) ;
    }

    private Runnable setup(final Semaphore sema, final TupleIndex srcIndex, final TupleIndex destIndex, final String label)
    {
        Runnable builder = new Runnable(){
            //@Override
            public void run()
            {
                LoaderNodeTupleTable.copyIndex(srcIndex.all(), new TupleIndex[]{destIndex}, label, monitor) ;
                sema.release() ;
            }} ;

            return builder ;
    }
}
/*
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
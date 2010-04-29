/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.StringUtils ;
import com.hp.hpl.jena.sparql.util.Timer ;

import org.openjena.atlas.event.Event ;
import org.openjena.atlas.event.EventManager ;
import org.slf4j.Logger ;

/** Abstract the load logging */
class LoadMonitor
{
    private final Logger log ;
    private final DatasetGraph dataset ;
    private final long dataTickPoint ;
    private final long indexTickPoint ;
    protected final Timer timer ;
    
    private long tickInterval ; 
    private long currentTicks = 0 ;
    private long totalTicks = 0 ;
    private long lastTime = 0 ;
    
    private long processStartTime = 0 ;
    private long processFinishTime = 0 ;
    private long processTime = 0 ;
    
    private long dataStartTime = 0 ;
    private long dataFinishTime = 0 ;
    private long dataTime = 0 ;
    
    private long indexStartTime = 0 ;
    private long indexFinishTime = 0 ;
    private long indexTime = 0 ;
    private String itemsName ;

    LoadMonitor(DatasetGraph dsg, Logger log, String itemsName,
                long dataTickPoint,  
                long indexTickPoint)
    {
        this.dataset = dsg ;
        this.log = log ;
        this.itemsName = itemsName ;
        this.dataTickPoint = dataTickPoint ;
        this.indexTickPoint = indexTickPoint ;
        this.timer = new Timer() ;
    }
    
    void startLoad()
    {
        EventManager.send(dataset, new Event(BulkLoader.evStartBulkload, null)) ;
        timer.startTimer() ;
        processStartTime = timer.readTimer() ;
    }
    
    void finishLoad()
    {
        timer.endTimer() ;
        processFinishTime = timer.getTimeInterval() ;
        processTime = processFinishTime - processStartTime ;

        print("-- Finish %s load", itemsName) ;
        if ( totalTicks > 0 )
            print("%,d %s loaded in %.2f seconds [Rate: %.2f per second]",
                  totalTicks,
                  itemsName,
                  processTime/1000.0F,
                  1000F*totalTicks/processTime) ;
        
        EventManager.send(dataset, new Event(BulkLoader.evFinishBulkload, null)) ;
    }
    
    void startDataPhase()
    {
        tickInterval = dataTickPoint ;
        print("-- Start %s data phase", itemsName) ;
        dataStartTime = timer.readTimer() ;
        EventManager.send(dataset, new Event(BulkLoader.evStartDataBulkload, null)) ;
    }
    
    void finishDataPhase()
    {
        EventManager.send(dataset, new Event(BulkLoader.evFinishDataBulkload, null)) ;
        dataFinishTime = timer.readTimer() ;
        dataTime = dataFinishTime - dataStartTime ; 
        
        print("-- Finish %s data phase", itemsName) ;
        if ( totalTicks > 0 )
            print("%,d %s loaded in %.2f seconds [Rate: %.2f per second]",
                  totalTicks,
                  itemsName,
                  dataTime/1000.0F,
                  1000F*totalTicks/dataTime) ;
    }

    void startIndexPhase()    
    {
        tickInterval = indexTickPoint ;
        print("-- Start %s index phase", itemsName) ;
        indexStartTime = timer.readTimer() ;
        EventManager.send(dataset, new Event(BulkLoader.evStartIndexBulkload, null)) ;
    }

    void finishIndexPhase()
    {
        EventManager.send(dataset, new Event(BulkLoader.evFinishIndexBulkload, null)) ;
        indexFinishTime = timer.readTimer() ;
        indexTime = indexFinishTime - indexStartTime ; 

        print("-- Finish %s index phase", itemsName) ;
        if ( totalTicks > 0 )
            print("%,d %s indexed in %.2f seconds [Rate: %.2f per second]",
                  totalTicks,
                  itemsName,
                  indexTime/1000.0F,
                  1000F*totalTicks/indexTime) ;
    }

    /** Note when one item (triple, quad) is loaded */
    void dataItem()
    {
        currentTicks ++ ;
        totalTicks ++ ; 
    
        if ( currentTicks % tickInterval == 0 )
        {
            tickPoint(currentTicks, totalTicks) ;
            currentTicks = 0 ;
        }
    }
    
    private void tickPoint(long incTicks, long totalTicks)
    {
        long timePoint = timer.readTimer() ;
        long thisTime = timePoint - lastTime ;

        // *1000L is milli to second conversion

        long batchAvgRate = (incTicks * 1000L) / thisTime;
        long runAvgRate   = (totalTicks * 1000L) / timePoint ;
        print("Add: %,d %s (Batch: %d / Run: %d)", totalTicks, itemsName, batchAvgRate, runAvgRate) ;
        lastTime = timePoint ;
    }

    void print(String fmt, Object...args)
    {
        if ( log != null && log.isInfoEnabled() )
        {
            String str = String.format(fmt, args) ;
            log.info(str) ;
        }
    }
 
    private static String num(long v)
    {
        return StringUtils.str(v) ;
    }
    
    private static String num(float value)
    {
        return StringUtils.str(value) ;
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
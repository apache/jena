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

package com.hp.hpl.jena.tdb.store.bulkloader;

import static com.hp.hpl.jena.sparql.util.Utils.nowAsString ;
import org.apache.jena.atlas.event.Event ;
import org.apache.jena.atlas.event.EventManager ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.StringUtils ;
import com.hp.hpl.jena.sparql.util.Timer ;

public final class LoadMonitor
{
    // c.f. ProgressLogger except this (earlier) class
    // has two explicit phases.
    /* Parameterize the load logging */
    private final Logger log ;
    private final DatasetGraph dataset ;
    private final long superTick = BulkLoader.superTick ;
    private final long dataTickPoint ;
    private final long indexTickPoint ;
    protected final Timer timer ;

    // Overall
    private long processStartTime = 0 ;
    private long processFinishTime = 0 ;
    private long processTime = 0 ;
    
    // Data phase variables
    private long totalDataItems = 0 ;
    
    private long dataStartTime = 0 ;
    private long dataFinishTime = 0 ;
    private long dataTime = 0 ;

    // Index phase variables.
    private long totalIndexItems = 0 ;
    
    // Time over all indexes
    private long indexStartTime = 0 ;
    private long indexFinishTime = 0 ;
    private long indexTime = 0 ;

    // Work variables.
    private long currentItems = 0 ;
    private long lastTime = 0 ;
    private long currentStartTime = 0 ;       // Used for each index
    private long currentFinishTime = 0 ;
    private long elapsedLastTime = 0 ;
    
    private String itemsName ;

    public LoadMonitor(DatasetGraph dsg, Logger log, String itemsName,
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
    
    // ---- Overall
    public void startLoad()
    {
        EventManager.send(dataset, new Event(BulkLoader.evStartBulkload, null)) ;
        timer.startTimer() ;
        processStartTime = timer.readTimer() ;
    }
    
    public void finishLoad()
    {
        timer.endTimer() ;
        processFinishTime = timer.getTimeInterval() ;
        processTime = processFinishTime - processStartTime ;

        print("-- Finish %s load", itemsName) ;
        if ( totalDataItems > 0 )
            print("** Completed: %,d %s loaded in %,.2f seconds [Rate: %,.2f per second]",
                  totalDataItems,
                  itemsName,
                  processTime/1000.0F,
                  1000F*totalDataItems/processTime) ;
        
        EventManager.send(dataset, new Event(BulkLoader.evFinishBulkload, null)) ;
    }
    
    // ---- Data phase
    
    public void startDataPhase()
    {
        print("-- Start %s data phase", itemsName) ;
        dataStartTime = timer.readTimer() ;
        currentStartTime = dataStartTime ;
        elapsedLastTime = dataStartTime ;
        currentItems = 0 ;
        totalDataItems = 0 ;
        EventManager.send(dataset, new Event(BulkLoader.evStartDataBulkload, null)) ;
    }
    
    public void finishDataPhase()
    {
        EventManager.send(dataset, new Event(BulkLoader.evFinishDataBulkload, null)) ;
        dataFinishTime = timer.readTimer() ;
        dataTime = dataFinishTime - dataStartTime ; 
        
        print("-- Finish %s data phase", itemsName) ;
        if ( totalDataItems > 0 )
            print("** Data: %,d %s loaded in %,.2f seconds [Rate: %,.2f per second]",
                  totalDataItems,
                  itemsName,
                  dataTime/1000.0F,
                  1000F*totalDataItems/dataTime) ;
    }

    /** Note when one item (triple, quad) is loaded */
    public final void dataItem()
    {
        currentItems++ ;
        totalDataItems ++ ; 
    
        if ( tickPoint(totalDataItems, dataTickPoint) )
        {
            long readTime = timer.readTimer() ;
            long timePoint = readTime - currentStartTime ;
            long thisTime = timePoint - lastTime ;
        
            // *1000L is milli to second conversion
        
            long batchAvgRate = (currentItems * 1000L) / thisTime;
            long runAvgRate   = (totalDataItems * 1000L) / timePoint ;
            print("Add: %,d %s (Batch: %,d / Avg: %,d)", totalDataItems, itemsName, batchAvgRate, runAvgRate) ;
            lastTime = timePoint ;

            if ( tickPoint(totalDataItems, superTick*dataTickPoint) )
                elapsed(readTime) ;
            currentItems = 0 ;
            lastTime = timePoint ;
        }
            
    }

    public void startIndexPhase()    
    {
        print("-- Start %s index phase", itemsName) ;
        indexStartTime = timer.readTimer() ;
        currentItems = 0 ;
        EventManager.send(dataset, new Event(BulkLoader.evStartIndexBulkload, null)) ;
    }

    public void finishIndexPhase()
    {
        EventManager.send(dataset, new Event(BulkLoader.evFinishIndexBulkload, null)) ;
        indexFinishTime = timer.readTimer() ;
        indexTime = indexFinishTime - indexStartTime ; 

        print("-- Finish %s index phase", itemsName) ;
        if ( totalIndexItems > 0 )
        {
            if ( indexTime > 0 )
                print("** %,d %s indexed in %,.2f seconds [Rate: %,.2f per second]",
                      totalIndexItems, itemsName, indexTime/1000.0F, 1000F*totalIndexItems/indexTime) ;
            else
                print("** %,d %s indexed", totalIndexItems, itemsName) ;
                
        }
    }

    String indexLabel ;
    public void startIndex(String label)
    {
        currentStartTime = timer.readTimer() ;
        indexLabel = label ;
        currentItems = 0 ;
        totalIndexItems = 0 ;
        elapsedLastTime = currentStartTime ;
        lastTime = 0 ;
    }
    
    public void finishIndex(String label)
    {
        currentFinishTime = timer.readTimer() ;
        long indexTime = currentFinishTime - currentStartTime ;
        
        if ( totalIndexItems > 0 )
        {
            if ( indexTime > 0 )
                print("** Index %s: %,d slots indexed in %,.2f seconds [Rate: %,.2f per second]",
                      label,
                      totalIndexItems,
                      indexTime/1000.0F,
                      1000F*totalIndexItems/indexTime) ;
            else
                print("** Index %s: %,d slots indexed", label, totalIndexItems) ;
            
        }
    }
    
    // ---- Indexing
    
    final void indexItem()
    {
        currentItems++ ;
        totalIndexItems++ ;
    
        if ( tickPoint(totalIndexItems, indexTickPoint) )
        {
            long readTimer = timer.readTimer() ;
            long timePoint = readTimer - currentStartTime ;
            long thisTime = timePoint - lastTime ;
            
            long batchAvgRate = (currentItems * 1000L) / thisTime;
            long runAvgRate   = (totalIndexItems * 1000L) / timePoint ;
            
            print("Index %s: %,d slots (Batch: %,d slots/s / Avg: %,d slots/s)", 
                  indexLabel, totalIndexItems, batchAvgRate, runAvgRate) ;
    
            if ( tickPoint(totalIndexItems, superTick*indexTickPoint) )
                elapsed(timer.readTimer()) ;
    
            currentItems = 0 ;
            lastTime = timePoint ;
        }
    
    
    }

    public void print(String fmt, Object...args)
    {
        if ( log != null && log.isInfoEnabled() )
        {
            String str = String.format(fmt, args) ;
            log.info(str) ;
        }
    }
 
    
    private void elapsed(long timerReading)
    {
        float elapsedSecs = (timerReading-processStartTime)/1000F ;
        print("  Elapsed: %,.2f seconds [%s]", elapsedSecs, nowAsString()) ;
    }

    private static boolean tickPoint(long counter, long quantum)
    {
        return counter%quantum == 0 ;
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

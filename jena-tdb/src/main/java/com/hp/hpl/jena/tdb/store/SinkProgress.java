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

package com.hp.hpl.jena.tdb.store;

import java.util.Date ;

import org.apache.jena.atlas.lib.Sink ;

import com.hp.hpl.jena.sparql.util.StringUtils ;
import com.hp.hpl.jena.sparql.util.Timer ;


public class SinkProgress<X> implements Sink<X>
{
    Timer timer = null ;
    private long lastTime = 0 ;
    private boolean displayMemory = false ;
    String label = null ;
    String summaryLabel = null ;
    
    private String format ;
    private long totalCount = 0 ;
    private long tickCount = 0 ;
    private int batchInterval = 0 ;
    private String units ;
    private boolean showProgress ;

    /** Format passed arguments 
     * @param showProgress */ 
    public SinkProgress(String label, String units, int interval, boolean showProgress)
    { 
        this.label = label ;
        this.units = units ;
        this.batchInterval = interval ;
        this.showProgress = showProgress ;

        this.format = "Add: %,d %s (Batch: %,d / Run: %,d)" ;
        this.timer = new Timer() ;
        timer.startTimer() ;
    }
    
    public long getCount() { return totalCount ; }
    
    @Override
    public void flush()
    {}

    @Override
    public void close()
    {
        printAtEnd() ;
    }
    
    public void startMonitor()
    {
        resetTimer() ;
    }
    
    public void finishMonitor()
    {
        if ( timer != null )
            timer.endTimer() ;
    }
    
    public void resetTimer()
    {
        if ( timer != null )
            timer.endTimer() ;
        timer = new Timer() ;
        timer.startTimer();
    }
    
    
    @Override
    public void send(X item)
    {
        oneItem() ;
    }

    protected void oneItem()
    {
        totalCount ++ ;
        if ( totalCount != 0 && (totalCount % batchInterval == 0) )
        {
            tickCount ++ ;
            if ( showProgress ) 
                oneTickDisplay() ;
        }
    }
    
    protected void oneTickDisplay()
    {
        long soFar = timer.readTimer() ;
        long thisTime = soFar - lastTime ;

        // *1000L is milli to second conversion
        //   addNotePoint/ (thisTime/1000L)
        long tpsBatch = (batchInterval * 1000L) / thisTime;
        long tpsAvg = (totalCount * 1000L) / soFar;

        String msg = String.format(format, totalCount, units, tpsBatch, tpsAvg) ;
        //String msg = "Add: "+StringUtils.str(totalCount)+" triples  (Batch: "+StringUtils.str(tpsBatch)+" / Run: "+num(tpsAvg)+")" ;

        if ( displayMemory )
        {
            long mem = Runtime.getRuntime().totalMemory() ;
            long free = Runtime.getRuntime().freeMemory() ;
            msg =  msg+String.format("   [M:%,d/F:%,d]", mem, free) ;
        }
        
        println(label, msg) ;

        if ( tickCount > 0 && (tickCount%10) == 0 )
        {
            String timestamp = StringUtils.str(new Date()) ; 
            String msg2 = String.format("  Elapsed: %f.2f seconds [%s]", soFar/1000F, timestamp) ; 
            println(label, msg2) ;
        }

        lastTime = soFar ;        
    }

    private void printAtEnd()
    {
        long timeMilli = timer.getTimeInterval() ;
        
        String x = String.format("%,d %s: loaded in %,.2f seconds [%,.2f %s/s]",
                                 totalCount,
                                 units,
                                 timeMilli/1000.0F,
                                 1000F*totalCount/timeMilli,
                                 units) ;
        println(summaryLabel, x) ;
    }
    
    private static void println(String label, String msg)
    {
        if ( label != null )
            System.out.print(label) ;
        System.out.println(msg) ;
    }

    
}

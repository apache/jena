/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.util.Date ;

import org.openjena.atlas.lib.Sink ;

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
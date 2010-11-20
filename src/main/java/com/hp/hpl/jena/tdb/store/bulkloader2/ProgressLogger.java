/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader2;

import static com.hp.hpl.jena.sparql.util.Utils.nowAsString ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.sparql.util.Timer ;

/** Progress monitor */
public class ProgressLogger
{
    // DELEET when an ARQ update sweeps through.
    private final Logger log ;
    private final long tickPoint ;
    private final int superTick ;
    private final Timer timer ;
    private final String label ;
    
    private long counterBatch = 0 ;
    private long counterTotal = 0 ;
    
    private long lastTime = 0 ;
    
    public ProgressLogger(Logger log, String label, long tickPoint, int superTick)
    {
        this.log = log ;
        this.label = label ;
        this.tickPoint = tickPoint ;
        this.superTick = superTick ;
        this.timer = new Timer() ;
    }
    
    public void start()
    {
        timer.startTimer() ;
        lastTime = 0 ;
    }

    public long finish()
    {
        long totalTime = timer.endTimer() ;
        return totalTime ;
    }
    
    public long getTicks()
    {
        return counterTotal ;
    }
    
    public void tick()
    {
        counterBatch++ ;
        counterTotal++ ;
    
        if ( tickPoint(counterTotal, tickPoint) )
        {
            long timePoint = timer.readTimer() ;
            long thisTime = timePoint - lastTime ;
        
            // *1000L is milli to second conversion
        
            long batchAvgRate = (counterBatch * 1000L) / thisTime;
            long runAvgRate   = (counterTotal * 1000L) / timePoint ;
            print("Add: %,d %s (Batch: %,d / Avg: %,d)", counterTotal, label, batchAvgRate, runAvgRate) ;
            lastTime = timePoint ;

            if ( tickPoint(counterTotal, superTick*tickPoint) )
                elapsed(timePoint) ;
            counterBatch = 0 ;
            lastTime = timePoint ;
        }
    }
    
    private void elapsed(long timerReading)
    {
        float elapsedSecs = timerReading/1000F ;
        print("  Elapsed: %,.2f seconds [%s]", elapsedSecs, nowAsString()) ;
    }
    
    private void print(String fmt, Object...args)
    {
        if ( log != null && log.isInfoEnabled() )
        {
            String str = String.format(fmt, args) ;
            log.info(str) ;
        }
    }
    
 
    
    static boolean tickPoint(long counter, long quantum)
    {
        return counter%quantum == 0 ;
    }

}
/*
 * (c) Copyright 2010 Epimorphics Ltd.
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
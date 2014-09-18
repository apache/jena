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

package org.apache.jena.atlas.logging;

import static com.hp.hpl.jena.sparql.util.Utils.nowAsString ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.sparql.util.Timer ;

/** Progress monitor */
public class ProgressLogger
{
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
    
    public void startMessage() { 
        print("Start:") ;
    }
    
    public void finishMessage() { 
        // Elapsed.
        long timePoint = timer.getTimeInterval() ;
    
        // *1000L is milli to second conversion
        if ( timePoint != 0 ) {
            double time = timePoint/1000.0 ;
            long runAvgRate   = (counterTotal * 1000L) / timePoint ;
            
            print("Finished: %,d %s %.2fs (Avg: %,d)", counterTotal, label, time, runAvgRate) ;
        }
        else
            print("Finished: %,d %s (Avg: ----)", counterTotal, label) ;
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
            if ( thisTime != 0 && timePoint != 0 ) {
                long batchAvgRate = (counterBatch * 1000L) / thisTime;
                long runAvgRate   = (counterTotal * 1000L) / timePoint ;
                print("Add: %,d %s (Batch: %,d / Avg: %,d)", counterTotal, label, batchAvgRate, runAvgRate) ;
            } else {
                print("Add: %,d %s (Batch: ---- / Avg: ----)", counterTotal, label) ;
            }
            
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
    
    /** Print a message in the form for this ProgressLogger */ 
    public void print(String fmt, Object...args)
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

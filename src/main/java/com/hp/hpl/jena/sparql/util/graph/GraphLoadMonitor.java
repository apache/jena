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

package com.hp.hpl.jena.sparql.util.graph;

import java.util.Date ;

import com.hp.hpl.jena.sparql.util.StringUtils ;
import com.hp.hpl.jena.sparql.util.Timer ;

// To be retired.
// Use a sink instead.
public class GraphLoadMonitor extends GraphListenerCounter
{
    Timer timer = null ;
    private long lastTime = 0 ;
    private boolean displayMemory = false ;
    String label = null ;
    String summaryLabel = null ;
 
        
    public GraphLoadMonitor(int addNotePoint, boolean displayMemory)
    {
        super(addNotePoint) ;
        this.displayMemory = displayMemory ;
        resetTimer() ;
    }
    
    public void setLabel(String label) { this.label = label ; }
    public void setSummaryLabel(String label) { this.summaryLabel = label ; }
    
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

    public long triplesLoaded() { return getAddCount() ; }
    
    @Override
    protected void addTick()
    {
        long soFar = timer.readTimer() ;
        long thisTime = soFar - lastTime ;
        long count = getAddCount() ;
        long ticks = getAddTicks() ;

        // *1000L is milli to second conversion
        //   addNotePoint/ (thisTime/1000L)
        long tpsBatch = (getAddTickSize() * 1000L) / thisTime;
        long tpsAvg = (count * 1000L) / soFar;

        String msg = "Add: "+num(count)+" triples  (Batch: "+num(tpsBatch)+" / Run: "+num(tpsAvg)+")" ;
        if ( label != null )
            msg = msg+label ;
        if ( displayMemory )
        {
            long mem = Runtime.getRuntime().totalMemory() ;
            long free = Runtime.getRuntime().freeMemory() ;
            msg = msg+"   [M:"+num(mem)+"/F:"+num(free)+"]" ;
        }
        println(label, msg) ;

        if ( ticks > 0 && (ticks%10) == 0 )
        {
            String x = num(soFar/1000F) ;
            String timestamp = StringUtils.str(new Date()) ; 
            println(label, "  Elapsed: "+x+" seconds ["+timestamp+"]") ;
        }

        lastTime = soFar ;        
    }

    private static String num(long v)
    {
        return StringUtils.str(v) ;
    }
    
    private static String num(float value)
    {
        return StringUtils.str(value) ;
    }
    
    @Override
    protected void deleteTick()
    {}
    
    @Override
    protected void startRead()
    { startMonitor() ; }
    
            
    @Override
    protected void finishRead()
    {
        finishMonitor() ;
        printAtEnd() ;
    }
    
    private void printAtEnd()
    {
        long timeMilli = timer.getTimeInterval() ;
        println(summaryLabel, num(getAddCount())+
                              " triples: loaded in "+
                              num(timeMilli/1000.0F)+
                              " seconds ["+
                              num(1000F*getAddCount()/timeMilli)+
                              " triples/s]") ;
    }
    
    private static void println(String label, String line)
    {
        if ( label != null )
            System.out.print(label) ;
        System.out.println(line) ;
    }
    
    
    
}

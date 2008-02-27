/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util.graph;

import java.text.DecimalFormat;

import com.hp.hpl.jena.sparql.util.StringUtils;
import com.hp.hpl.jena.sparql.util.Timer;

public class GraphLoadMonitor extends GraphListenerCounter
{
    Timer timer = null ;
    private long lastTime = 0 ;
    private boolean displayMemory = false ;
        
    public GraphLoadMonitor(int addNotePoint, boolean displayMemory)
    {
        super(addNotePoint) ;
        this.displayMemory = displayMemory ;
        resetTimer() ;
    }
    
    public void resetTimer()
    {
        timer = new Timer() ;
        timer.startTimer();
    }

    //@Override
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
        if ( displayMemory )
        {
            long mem = Runtime.getRuntime().totalMemory() ;
            long free = Runtime.getRuntime().freeMemory() ;
            msg = msg+"   [M:"+num(mem)+"/F:"+num(free)+"]" ;
        }
        System.out.println(msg) ;

        if ( ticks > 0 && (ticks%10) == 0 )
        {
            DecimalFormat f = new DecimalFormat("#,##0.0") ;
            String x = f.format(soFar/1000F) ;
            System.out.println("  Elapsed: "+x+" seconds") ;
        }

        lastTime = soFar ;        
    }

    private static String num(long v)
    {
        return StringUtils.str(v) ;
    }
    
    
    //@Override
    protected void deleteTick()
    {}
}
/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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
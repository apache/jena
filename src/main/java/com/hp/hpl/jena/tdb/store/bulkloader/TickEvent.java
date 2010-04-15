/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader;

import java.util.Date ;

import com.hp.hpl.jena.sparql.util.StringUtils ;

public class TickEvent extends Ticker
{
    long lastTime = 0 ;
    String label = null ;
    boolean displayMemory = false ;
    
    TickEvent(int tickInternal)
    {
        super(tickInternal, true) ;
    }

    @Override
    protected void _start() { }
    
    @Override
    protected void _finish(int totalTicks, int incTicks)
    {
        if ( incTicks != 0 )
            tickMessage(totalTicks, incTicks, super.tickInternal, timer.getTimeInterval()) ;
        printAtEnd(totalTicks, super.tickInternal) ;
    }
    
    private void printAtEnd(int totalTicks, int tickUnit)
    {
        long timeMilli = timer.getTimeInterval() ;
        println("HELLO", num(totalTicks)+
                              " ABC: loaded in "+
                              num(timeMilli/1000.0F)+
                              " seconds ["+
                              num(1000F*totalTicks/timeMilli)+
                              " ABC/s]") ;
    }
    
    @Override
    protected void tickPoint(int totalTicks, int incTicks)
    {
        tickMessage(totalTicks, incTicks, tickInternal, timer.readTimer()) ;
        if ( totalTicks != 0 && totalTicks % (10*super.tickInternal) == 0 )
        {
            String x = num(timer.readTimer()/1000F) ;
            String timestamp = StringUtils.str(new Date()) ; 
            println(label, "  Elapsed: "+x+" seconds ["+timestamp+"]") ;
        }
    }
        
    
    private void tickMessage(int totalTicks, int incTicks, int tickUnit, long timePoint)
    {
        long thisTime = timePoint - lastTime ;

      // *1000L is milli to second conversion
      //   addNotePoint/ (thisTime/1000L)

        long batchAvgRate = (incTicks * 1000L) / thisTime;
        long runAvgRate   = (totalTicks * 1000L) / timePoint ;

        // SORT OUT MESSAGE
        String msg = "Add: "+num(incTicks)+" ABC  (Batch: "+num(batchAvgRate)+" / Run: "+num(runAvgRate)+")" ;
      if ( label != null )
          msg = msg+label ;
      
      if ( displayMemory )
      {
          long mem = Runtime.getRuntime().totalMemory() ;
          long free = Runtime.getRuntime().freeMemory() ;
          msg = msg+"   [M:"+num(mem)+"/F:"+num(free)+"]" ;
      }
    
          println(label, msg) ;

      if ( totalTicks > 0 && (totalTicks%10) == 0 )
      {
          String x = num(timePoint/1000F) ;
          String timestamp = StringUtils.str(new Date()) ; 
          println(label, "  Elapsed: "+x+" seconds ["+timestamp+"]") ;
      }

      lastTime = timePoint ;        
    }
    
    private static String num(long v)
    {
        return StringUtils.str(v) ;
    }
    
    private static String num(float value)
    {
        return StringUtils.str(value) ;
    }
    
    private static void println(String label, String line)
    {
        if ( label != null )
            System.out.print(label) ;
        System.out.println(line) ;
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
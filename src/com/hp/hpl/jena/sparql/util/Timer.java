/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import com.hp.hpl.jena.sparql.ARQException ;


public class Timer
{

    protected long timeFinish = -1 ;
    protected boolean inTimer = false ;
    protected long timeStart  = 0 ;

    public Timer() { }

    public void startTimer()
    { 
        if ( inTimer )
            throw new ARQException("Already in timer") ;

        timeStart = System.currentTimeMillis() ;
        timeFinish = -1 ;
        inTimer = true ;
    }

    /** Return time in millisecods */
    public long endTimer()
    { 
        if ( ! inTimer )
            throw new ARQException("Not in timer") ;
        timeFinish = System.currentTimeMillis() ;
        inTimer = false ;
        return getTimeInterval() ;
    }

    public long readTimer() 
    {
        if ( ! inTimer )
            throw new ARQException("Not in timer") ;
        return System.currentTimeMillis()-timeStart  ;
    }

    public long getTimeInterval()
    {
        if ( inTimer )
            throw new ARQException("Still timing") ;
        if ( timeFinish == -1 )
            throw new ARQException("No valid interval") ;

        return  timeFinish-timeStart ;
    }

    static public String timeStr(long timeInterval)
    {
//        DecimalFormat f = new DecimalFormat("#0.###") ;
//        String s = f.format(timeInterval/1000.0) ;
//        return s ;
        //Java5
        return String.format("%.3f", timeInterval/1000.0) ;
    }

    protected String timeStr(long timePoint, long startTimePoint)
    {
        return timeStr(timePoint-startTimePoint) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
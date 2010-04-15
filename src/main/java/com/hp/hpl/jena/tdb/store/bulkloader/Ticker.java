/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader;

import com.hp.hpl.jena.sparql.util.Timer ;

public abstract class Ticker
{
    //With timer.
    
    protected final int tickInternal ;
    private int currentTicks = 0 ;
    private int totalTicks = 0 ;
    protected final Timer timer ;
    
    Ticker(int tickInternal) 
    {
        this(tickInternal, false) ;
    }

    Ticker(int tickInternal, boolean withTimer) 
    {
        this.tickInternal = tickInternal ;
        this.timer = ( withTimer ? new Timer() : null ) ;
    }

    public void start() { _start() ; timer.startTimer() ; }
    public final void finish()
    {
        timer.endTimer() ;
        _finish(currentTicks, totalTicks) ;
    }
    
    protected abstract void _start() ;
    
    
    public void tick()
    {
        currentTicks ++ ;
        totalTicks ++ ; 
    
        if ( currentTicks % tickInternal == 0 )
        {
            tickPoint(totalTicks, currentTicks) ;
            currentTicks = 0 ;
        }
            
    }

    protected abstract void tickPoint(int totalTicks, int incTicks) ;

    protected abstract void _finish(int totalTicks, int incTicks) ;
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
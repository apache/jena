/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util.graph;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;

/** Adds a regular "tick" based on the number of adds or deletes seen.
 *  The additions and deletions ticks are independent.  
 */

public abstract class GraphListenerCounter extends GraphListenerBase
{
    private long addCount = 0 ;
    private long deleteCount = 0 ;

    private long addTicks = 0 ;
    private long deleteTicks = 0 ;
    
    private final int addNotePoint ;
    private final int deleteNotePoint ;

    /** Create a listener that "ticks" on additions to the graph (-1 means "off")
     * 
     * @param addTickCount
     */    
    public GraphListenerCounter(int addTickCount)
    {
        this(addTickCount, -1) ;
    }
    
    /** Create a listener that "ticks" on additions and deletions
     *  (in each case, -1 means "off")
     * 
     * @param addTickCount      Notification tick for additions to the graph
     * @param deleteTickCount   Notification tick for deletions to the graph
     */
    public GraphListenerCounter(int addTickCount, int deleteTickCount)
    {
        this.addNotePoint = addTickCount ;
        this.deleteNotePoint = deleteTickCount ;
    }
    
    public void reset()
    { 
        addCount = 0 ;
        deleteCount = 0 ; 
    }

    @Override
    public void notifyEvent(Graph source, Object value)
    {
        super.notifyEvent(source, value) ;
    }

    @Override
    protected void addEvent(Triple t)
    {
        addCount++ ;
        if ( addNotePoint > 0 && (addCount%addNotePoint) == 0 )
        {
            addTicks++ ;
            addTick() ;
        }
    }

    @Override
    protected void deleteEvent(Triple t)
    {
        deleteCount++ ;
        if ( deleteNotePoint > 0 && (deleteCount%deleteNotePoint) == 0 )
        {
            deleteTicks++ ;
            deleteTick() ;
        }
    }

    public final int getAddTickSize() { return addNotePoint ; }
    public final int getDeleteTickSize() { return deleteNotePoint ; }
    
    public final long getAddCount() { return addCount ; }
    public final long getDeleteCount() { return deleteCount ; }

    public final long getAddTicks() { return addTicks ; }
    public final long getDeleteTicks() { return deleteTicks ; }

    protected abstract void deleteTick() ;
    protected abstract void addTick() ;
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

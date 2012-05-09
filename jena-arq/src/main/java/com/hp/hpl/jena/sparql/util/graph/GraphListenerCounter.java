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

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;

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

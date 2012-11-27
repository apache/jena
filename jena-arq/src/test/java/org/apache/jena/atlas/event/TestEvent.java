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

package org.apache.jena.atlas.event;

import org.apache.jena.atlas.event.Event ;
import org.apache.jena.atlas.event.EventListener ;
import org.apache.jena.atlas.event.EventManager ;
import org.apache.jena.atlas.event.EventType ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;


public class TestEvent extends BaseTest
{
    EventType ev1 = new EventType("1") ;
    EventType ev2 = new EventType("2") ;
    
    static class EventListenerLogger implements EventListener
    {
        public int eventCount = 0 ;
        public Object dest = null ;
        public Event event = null ;
        
        @Override
        public void event(Object dest, Event event)
        {
            eventCount++ ;
            this.dest = dest ;
            this.event = event ;
        }
    }
    
    @Test public void event1()
    {
        EventListenerLogger listener = new EventListenerLogger() ;
        Object obj = new Object() ;
        Object arg = new String("arg") ;
        
        assertEquals(0, listener.eventCount) ;
        EventManager.register(obj, ev1, listener) ;
        EventManager.send(obj, new Event(ev1, arg)) ;

        assertEquals(1, listener.eventCount) ;
        assertEquals(ev1, listener.event.getType()) ;
        assertEquals(arg, listener.event.getArgument()) ;
    }

    @Test public void event2()
    {
        EventListenerLogger listener = new EventListenerLogger() ;
        Object obj = new Object() ;
        Object arg = new String("arg") ;
        
        assertEquals(0, listener.eventCount) ;
        EventManager.register(obj, ev1, listener) ;
        
        EventManager.send(obj, new Event(ev1, arg)) ;
        assertEquals(1, listener.eventCount) ;
        
        EventManager.send(obj, new Event(ev2, arg)) ;
        assertEquals(1, listener.eventCount) ;
        
        EventManager.send(obj, new Event(ev1, arg)) ;
        assertEquals(2, listener.eventCount) ;
        
        assertEquals(ev1, listener.event.getType()) ;
        assertEquals(arg, listener.event.getArgument()) ;
    }
    
    @Test public void event3()
    {
        EventListenerLogger listener = new EventListenerLogger() ;
        Object obj = new Object() ;
        
        EventManager.send(obj, new Event(ev1, "foo")) ;
        assertEquals(0, listener.eventCount) ;
        
        EventManager.register(obj, ev1, listener) ;
        EventManager.send(obj, new Event(ev1, "foo")) ;
        assertEquals(1, listener.eventCount) ;
        
        EventManager.unregister(obj, ev1, listener) ;
        EventManager.send(obj, new Event(ev1, "foo")) ;
        assertEquals(1, listener.eventCount) ;
    }
}

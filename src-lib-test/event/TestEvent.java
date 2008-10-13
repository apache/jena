/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package event;

import org.junit.Test;
import test.BaseTest;


public class TestEvent extends BaseTest
{
    static class EventListenerLogger implements EventListener
    {
        public int eventCount = 0 ;
        public Object dest = null ;
        public EventType label = null ;
        public Object arg = null ;
        
        @Override
        public void event(Object dest, Event event)
        {
            eventCount++ ;
            this.dest = dest ;
            this.label = event.getLabel() ;
            this.arg = event.getArgument() ;
        }
    }
    
    @Test public void event1()
    {
        EventListenerLogger listener = new EventListenerLogger() ;
        Object obj = new Object() ;
        EventType ev1 = new EventType("1") ;
        EventType ev2 = new EventType("2") ;
        Object arg = new String("arg") ;
        
        assertEquals(0, listener.eventCount) ;
        EventManager.register(obj, listener) ;
        EventManager.send(obj, new Event(ev1, arg)) ;

        assertEquals(1, listener.eventCount) ;
        assertEquals(ev1, listener.label) ;
        assertEquals(arg, listener.arg) ;
    }

    @Test public void event2()
    {
        EventListenerLogger listener = new EventListenerLogger() ;
        Object obj = new Object() ;
        EventType ev1 = new EventType("1") ;
        EventType ev2 = new EventType("2") ;
        Object arg = new String("arg") ;
        
        assertEquals(0, listener.eventCount) ;
        EventManager.register(obj, listener) ;
        EventManager.send(obj, new Event(ev1, arg)) ;
        assertEquals(1, listener.eventCount) ;
        assertEquals(ev1, listener.label) ;
        
        EventManager.send(obj, new Event(ev2, arg)) ;
        assertEquals(2, listener.eventCount) ;
        assertEquals(ev2, listener.label) ;

        assertEquals(arg, listener.arg) ;
    }
    
    @Test public void event3()
    {
        EventListenerLogger listener = new EventListenerLogger() ;
        Object obj = new Object() ;
        EventType ev1 = new EventType("1") ;
        EventType ev2 = new EventType("2") ;
        Object arg = new String("arg") ;
        
        EventManager.send(obj, new Event(ev1, "foo")) ;
        assertEquals(0, listener.eventCount) ;
        
        EventManager.register(obj, listener) ;
        EventManager.send(obj, new Event(ev1, "foo")) ;
        assertEquals(1, listener.eventCount) ;
        
        EventManager.unregister(obj, listener) ;
        EventManager.send(obj, new Event(ev1, "foo")) ;
        assertEquals(1, listener.eventCount) ;
    }
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
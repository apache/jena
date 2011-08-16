/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.event;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

/** The event system - the single, global event manager control the registration and delivery of events.
 *  An event is a label and an argument. */
public class EventManager
{
    
    private static EventManager eventManager = new EventManager() ;
    
    // Public operations 
    static public void register(Object object, EventType type, EventListener listener) 
    { eventManager.register$(object, type, listener) ; }
    
    /** Unregister a listener for events */ 
    static public void unregister(Object object, EventType type, EventListener listener)
    { eventManager.unregister$(object, type, listener) ; }
    
    /** Send an event to all listeners on an object for the event's type */  
    static public void send(Object dest, Event event)
    { eventManager.send$(dest, event) ; }
    
    // All registered objects
    // All registered type
    
    // ---- The object EventManager itself
    
    // There are 2 event sets: one for specific objects and one for general event types (no object)  
    // MultipMap.MultiMapToList
    private Map<Object, Map<EventType, List<EventListener>>> listenersByObject = new HashMap<Object, Map<EventType, List<EventListener>>>() ;
    private Map<EventType, List<EventListener>> listenersAllObjects = new HashMap<EventType, List<EventListener>>() ;

    // Singleton above.
    private EventManager () {}
    
    private void register$(Object object, EventType type, EventListener listener) 
    {
        Map<EventType, List<EventListener>> x = get(object) ;
        if ( x == null )
        {
            // Because listeners2 is never null.
            x = new HashMap<EventType, List<EventListener>> () ;
            listenersByObject.put(object, x) ;
        }
        List<EventListener> z = x.get(type) ;
        if ( z == null )
        {
            //?? new CopyOnWriteArrayList<EventListener>() ;
            z = new ArrayList<EventListener>() ;
            x.put(type, z) ;
        }
        z.add(listener) ;
    }
    
    private void unregister$(Object object, EventType type, EventListener listener) 
    {
        List<EventListener> x = find(object, type) ;
        if ( x == null ) 
            return ;
        x.remove(listener);
        // if the list has gone to zero, remove it. 
        if ( x.size() != 0 )
            return ;

        if ( object == null )
        {
            listenersAllObjects.remove(type) ;
            return ;
        }

        listenersByObject.get(object).remove(type) ; 
        // if the map for this object has gone to zero, remove it.
        if ( listenersByObject.get(object).size() == 0 )
            listenersByObject.remove(object) ;
    }
    
    private void send$(Object dest, Event event)
    {
        // Send on the specific object channel. 
        if ( dest != null )
        {
            Map<EventType, List<EventListener>> map = listenersByObject.get(dest) ;
            if ( map != null )
                send(dest, event, map) ;
        }

//        // Check.
//            List<EventListener> x = find(dest, event.getType()) ;
//        if ( x == null ) 
//        {
//            deliveryFailure(dest, event) ;
//            return ;
//        }

        // Now send on the "all objects channel"
        send(dest, event, listenersAllObjects) ;
    }
    
    private void send(Object dest, Event event, Map<EventType, List<EventListener>> listeners)
    {
        List<EventListener> x = listeners.get(event.getType()) ;
        if ( x != null )
        {
            for ( EventListener listener : x )
                listener.event(dest, event) ;
        }
    }
    
    private void deliveryFailure(Object object, Event event)
    {}
    
    private Map<EventType, List<EventListener>> get(Object object)
    {
        if ( object == null )
            return listenersAllObjects ;
        else
            return listenersByObject.get(object) ;
    }

    private List<EventListener> find(Object object, EventType type)
    {
        Map<EventType, List<EventListener>> x = get(object) ;
        if ( x == null )
            return null ;
        List<EventListener> z = x.get(type) ;
        return z ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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
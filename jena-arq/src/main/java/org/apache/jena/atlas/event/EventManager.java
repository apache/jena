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
    private Map<Object, Map<EventType, List<EventListener>>> listenersByObject = new HashMap<>() ;
    private Map<EventType, List<EventListener>> listenersAllObjects = new HashMap<>() ;

    // Singleton above.
    private EventManager () {}
    
    private void register$(Object object, EventType type, EventListener listener) 
    {
        Map<EventType, List<EventListener>> x = get(object) ;
        if ( x == null )
        {
            // Because listeners2 is never null.
            x = new HashMap<>() ;
            listenersByObject.put(object, x) ;
        }
        List<EventListener> z = x.get(type) ;
        if ( z == null )
        {
            //?? new CopyOnWriteArrayList<EventListener>() ;
            z = new ArrayList<>() ;
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

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

package com.hp.hpl.jena.graph;

import java.util.*;

/**
    The component of a graph responsible for managing events and listeners.
    The interface extends GraphListener because most of the notificiations are
    the same; the special case to note is that an event manager expects to be
    handed iterator events as lists, not as iterators. 
    
 */
public interface GraphEventManager extends GraphListener
    {
    /**
        Attached <code>listener</code> to this manager; notification events
        sent to the manager are sent to all registered listeners. A listener may
        be registered multiple times, in which case it's called multiple times per
        event.
        
        A listener will be notified of an event if it is registered
        before the Graph method call that initiated the event, and 
        was not unregistered before that method call returned.
        In addition, a listener <em>may</em> (or may not) be notified 
        of an event if it is registered
        before such a method returns or is unregistered after such
        a method is called. For example, it may unregister itself
        in response to the event.
        
        If the registration and/or unregistration occur on different
        threads the usual thread uncertainties in such statements apply.
        
        @param listener a listener to be fed events
        @return this manager, for cascading
    */
    GraphEventManager register( GraphListener listener );
    
    /**
        If <code>listener</code> is attached to this manager, detach it, otherwise
        do nothing. Only a single registration is removed.
        
        @param listener the listener to be detached from the graph
        @return this manager, for cascading
    */
    GraphEventManager unregister( GraphListener listener );
    
    /**
        Answer true iff there is at least one attached listener.
    	@return true iff there is at least one attached listener
     */
    boolean listening();
    
    /**
        Notify all attached listeners that an iterator [of triples] has been added to
        the graph; its content has been captured in the list <code>triples</code>.
     */
    void notifyAddIterator( Graph g, List<Triple> triples );

    /**
        Notify all attached listeners that an iterator [of triples] has been removed from
        the graph; its content has been captured in the list <code>triples</code>.
     */
    void notifyDeleteIterator( Graph g, List<Triple> triples );
    }

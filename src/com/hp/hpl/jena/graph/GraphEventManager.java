/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: GraphEventManager.java,v 1.10 2003-08-27 13:01:00 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph;

import java.util.*;

/**
    The component of a graph responsible for managing events and listeners.
    The interface extends GraphListener because most of the notificiations are
    the same; the special case to note is that an event manager expects to be
    handed iterator events as lists, not as iterators. 
    
 	@author kers
*/
public interface GraphEventManager extends GraphListener
    {
    /**
        Attached <code>listener</code> to this manager; notification events
        sent to the manager are sent to all registered listeners. A listener may
        be registered multiple times, in which case it's called multiple times per
        event.
        
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
    void notifyAddIterator( List triples );

    /**
        Notify all attached listeners that an iterator [of triples] has been removed from
        the graph; its content has been captured in the list <code>triples</code>.
     */
    void notifyDeleteIterator( List triples );
    }

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
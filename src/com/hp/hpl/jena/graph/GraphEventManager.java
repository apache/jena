/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: GraphEventManager.java,v 1.5 2003-07-09 15:27:02 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

/**
    The component of a graph responsible for managing events and listeners.
 	@author kers
*/
public interface GraphEventManager
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
        Notify all attached listeners that the triple <code>t</code> has been added,
        by calling their <code>notifyAdd(Triple)</code> methods.
    */
    void notifyAdd( Triple t );
    
    /**
        Notify all attached listeners that the triple array <code>triples</code> has
        been added, by calling their <code>notifyAdd(Triple [])</code> methods.
    */
    void notifyAdd( Triple [] ts );
    
    /**
        Notify all attached listeners that the triple <code>t</code> has been removed,
        by calling their <code>notifyDelete(Triple)</code> methods.
    */
    void notifyDelete( Triple t );
    
    /**
        Notify all attached listeners that the triple array <code>triples</code> has
        been removed, by calling their <code>notifyDelete(Triple [])</code> methods,
    */
    void notifyDelete( Triple [] triples );
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
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
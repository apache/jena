/*
 (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
 [See end of file]
 $Id: ReifierFragmentsMap.java,v 1.2 2004-09-17 15:00:39 chris-dollin Exp $
 */

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.Fragments.Slot;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
     ReifierFragmentsMap - how a [Simple] Reifier manages its incomplete
     reification quads. WARNING: likely to change soon, because we may
     eliminate the notion of a Fragment (to avoid over-commiting to how
     implementations manage the map).
     
     @author kers
*/
public interface ReifierFragmentsMap
    {
    /**
         Answer the Fragments bound to the node <code>tag</code>, or null if there 
         are none.
     */
    public abstract Fragments getFragments( Node tag );

    /**
         Remove (all the) Fragments bound to the node <code>key</code>.
    */
    public abstract void removeFragments( Node key );

    /**
         update the map with (node -> fragment); return the fragment.
     */
    public abstract Fragments putFragments( Node key, Fragments value );

    /**
         Answer all the quadlets in this map which match <code>tm</code>.
    */
    public abstract ExtendedIterator allTriples( TripleMatch tm );

    /**
         Answer the fragment map as a read-only Graph of quadlets. 
    */
    public abstract Graph asGraph();

    /**
         Answer a Slot which can handle this fragment, or null if it isn't a quadlet.
    */
    public abstract Slot getFragmentSelector( Triple fragment );
    }

/*
     (c) Copyright 2004, Hewlett-Packard Development Company, LP
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
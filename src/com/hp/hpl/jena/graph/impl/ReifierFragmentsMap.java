/*
 (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
 [See end of file]
 $Id: ReifierFragmentsMap.java,v 1.7 2004-11-02 14:10:08 chris-dollin Exp $
 */

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
     ReifierFragmentsMap: how a SimpleReifier manages its incomplete reifications.
     Most of the active operations are deferred to FragmentHandler.
     
     @author kers
*/
public interface ReifierFragmentsMap
    {
    /**
         Answer the fragment map as a read-only Graph of quadlets. 
    */
    public abstract Graph asGraph();
    
    public ExtendedIterator find( TripleMatch m );
    
    public int size();

    /**
         Answer a FragmentHandler which can handle this fragment, or null if it isn't a
         reification fragment.
    */
    public abstract ReifierFragmentHandler getFragmentHandler( Triple fragment );

    /**
         Answer true iff this map has fragments associated with <code>tag</code>.
    */
    public abstract boolean hasFragments( Node tag );
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
/*
     (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
     [See end of file]
     $Id: ReifierFragmentHandler.java,v 1.1 2004-09-21 15:05:36 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
     ReifierFragmentHandler: instances of this class handle fragments of reifications,
     ie the triples (tag rdf:subject/predicate/object X) and (tag rdf:type Statement).
     They are delivered from FragmentHandler instances and remain bound to
     their originating instance.
     
     @author kers
*/
public interface ReifierFragmentHandler
    {
    /**
         
     * @param n
     * @param reified
     * @return
     */
    public abstract boolean clashedWith( Node n, Triple reified );

    /**
     * @param fragment
     * @param tag
     * @param object
     * @return
     */
    public abstract Triple reifyCompleteQuad( Triple fragment, Node tag,
            Node object );

    /**
     * @param tag
     * @param already
     * @param fragment
     * @return
     */
    public abstract Triple removeFragment( Node tag, Triple already,
            Triple fragment );
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
/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: ReifyingCaptureGraph.java,v 1.1 2003-05-28 11:13:50 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Reifier;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.util.iterator.*;

/**
    A Graph that is layered over another graph and defers all its
    operations to it, except that reification triples are captured by
    its reifier.
<p>
    @author kers
*/
public class ReifyingCaptureGraph extends GraphBase
    {
    Graph under;
    
    ReifyingCaptureGraph( Graph under )
        { this.under = under; }
        
    public Reifier getReifier() 
        {
        if (reifier == null) reifier = new SimpleReifier( this, true );
        return reifier;
        }
        
    public ExtendedIterator find( TripleMatch m ) 
        { return under.find( m ); }
        
    public boolean contains( Node s, Node p, Node o )
        { return under.contains( s, p, o ); }
        
    public void add( Triple t )
        { if (getReifier().handledAdd( t ) == false) under.add( t ); }
        
    public void delete( Triple t )
        { if (getReifier().handledRemove( t ) == false) under.delete( t ); }
        
    public int size()
        { return under.size(); }
        
    public String toString()
        { return "ReifyingCaptureGraph " + super.toString(); }
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
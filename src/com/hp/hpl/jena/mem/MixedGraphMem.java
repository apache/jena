/*
  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: MixedGraphMem.java,v 1.20 2009-03-16 15:45:28 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.TripleStore;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

/**
 @author hedgehog
*/
public class MixedGraphMem extends GraphMemBase implements Graph
    {    
    protected MixedGraphMemStore mixedStore = new MixedGraphMemStore( this );
    
    public MixedGraphMem()
        { this( ReificationStyle.Minimal ); }
    
    public MixedGraphMem( ReificationStyle style )
        { super( style ); }
    
    /**
        MixedGraphMem's don't use TripleStore's at present. 
    */
    @Override protected TripleStore createTripleStore()
        { return null; }
    
    @Override public void performAdd( Triple t )
        { if (!getReifier().handledAdd( t )) mixedStore.add( t ); }
    
    @Override public void performDelete( Triple t )
        { if (!getReifier().handledRemove( t )) mixedStore.remove( t ); }
    
    @Override public int graphBaseSize()  
        { return mixedStore.size(); }

    /**
        Answer true iff t matches some triple in the graph. If t is concrete, we
        can use a simple membership test; otherwise we resort to the generic
        method using find.
    */
    @Override public boolean graphBaseContains( Triple t ) 
        { return isSafeForEquality( t ) ? mixedStore.contains( t ) : containsByFind( t ); }
    
    @Override protected void destroy()
        { mixedStore = null; }
    
    @Override public boolean isEmpty()
        {
        checkOpen();
        return mixedStore.isEmpty();
        }
    
    @Override public void clear()
        { mixedStore.clear(); }
    
    @Override public ExtendedIterator<Triple> graphBaseFind( TripleMatch m ) 
        {
        Triple t = m.asTriple();
        Node S = t.getSubject(), P = t.getPredicate(), O = t.getObject();
        return 
        	S.isConcrete() ? mixedStore.iterator( S, t )
            : P.isConcrete() ? mixedStore.iterator( P, t )
            : O.isURI() || O.isBlank() ? mixedStore.iterator( O, t )
            : mixedStore.iterator( m.asTriple() )
            ; 
        }

    }


/*
    (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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

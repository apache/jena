/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ModelListenerAdapter.java,v 1.1 2009-06-29 08:55:32 castagna Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

/**
    Adapter class that converts a ModelChangedListener into a GraphListener.
    The only tricky bit is that we have to implement equality as equality of the
    underlying ModelChangedListeners/ModelCom pairs.
<p>
    This implementation only works for <code>ModelCom</code> models,
    because it relies on various service methods; this gives the model the
    opportunity to cache various mappings for efficiency.
    
    @author hedgehog
*/
public class ModelListenerAdapter implements GraphListener
    {
    protected ModelCom m;
    protected ModelChangedListener L;

    public ModelListenerAdapter( ModelCom m, ModelChangedListener L )
        { this.m = m; this.L = L; }

    @Override
    public void notifyAddArray( Graph graph, Triple [] triples )
        { L.addedStatements( m.asStatements( triples ) ); }
        
    @Override
    public void notifyDeleteArray( Graph g, Triple [] triples )
        { L.removedStatements( m.asStatements( triples ) ); }
        
    @Override
    public void notifyAddTriple( Graph g, Triple t )
        { L.addedStatement( m.asStatement( t ) ); }

    @Override
    public void notifyAddList( Graph g, List<Triple> triples )
        { L.addedStatements( m.asStatements( triples ) ); }  
              
    @Override
    public void notifyAddIterator( Graph g, Iterator<Triple> it )
        { L.addedStatements( m.asStatements( it ) ); }
        
    @Override
    public void notifyAddGraph( Graph g, Graph added )
        { L.addedStatements( m.asModel( added ) ); }
        
    @Override
    public void notifyDeleteIterator( Graph g, Iterator<Triple> it )
        { L.removedStatements( m.asStatements( it ) ); }
        
    @Override
    public void notifyDeleteTriple( Graph g, Triple t )
        { L.removedStatement( m.asStatement( t ) ); }
        
    public void notifyAddIterator( Graph g, List<Triple> triples )
        { L.addedStatements( m.asStatements( triples ) ); }
        
    @Override
    public void notifyDeleteList( Graph g, List<Triple> triples )
        { L.removedStatements( m.asStatements( triples ) ); }

    @Override
    public void notifyDeleteGraph( Graph g, Graph removed )
        { L.removedStatements( m.asModel( removed ) ); }
    
    @Override
    public void notifyEvent( Graph g, Object event )
        { L.notifyEvent( m, event ); }
        
    @Override
    public boolean equals( Object other )
        { 
        return 
            other instanceof ModelListenerAdapter 
            && ((ModelListenerAdapter) other).sameAs( this )
            ; 
        }
        
    public boolean sameAs( ModelListenerAdapter other )
        { return this.L.equals( other.L ) && this.m.equals( other.m ); }
    }

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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

/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: ModelListenerAdapter.java,v 1.9 2003-07-11 13:34:20 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

/**
    Adapter class that converts a ModelChangedListener into a GraphListener.
    The only tricky bit is that we have to implement equality as equality of the
    underlying ModelChangedListeners/ModelCom pairs.
    
    @author hedgehog
*/
public class ModelListenerAdapter implements GraphListener
    {
    protected ModelCom m;
    protected ModelChangedListener L;

    ModelListenerAdapter( ModelCom m, ModelChangedListener L )
        { this.m = m; this.L = L; }

    public void notifyAdd( Triple [] triples )
        { L.addedStatements( m.asStatements( triples ) ); }
        
    public void notifyDelete( Triple [] triples )
        { L.removedStatements( m.asStatements( triples ) ); }
        
    public void notifyAdd( Triple t )
        { L.addedStatement( m.asStatement( t ) ); }
        
    public void notifyAdd( Iterator it )
        { L.addedStatements( m.asStatements( it ) ); }
        
    public void notifyAdd( Graph g )
        { 
        }
        
    public void notifyDelete( Iterator it )
        { L.removedStatements( m.asStatements( it ) ); }
        
    public void notifyDelete( Triple t )
        { L.removedStatement( m.asStatement( t ) ); }
        
    public void notifyAdd( List triples )
        { L.addedStatements( m.asStatements( triples ) ); }
        
    public void notifyDelete( List triples )
        { L.removedStatements( m.asStatements( triples ) ); }

    public void notifyDelete( Graph g )
        {
        }
        
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

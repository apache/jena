/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: SimpleBulkUpdateHandler.java,v 1.3 2003-07-09 15:27:02 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import java.util.*;

import com.hp.hpl.jena.graph.*;

/**
    A simple-minded implementation of the bulk update interface. This only
    operates on (subclasses of) GraphBase, since it needs access to the
    performAdd/performDelete operations.
    
 	@author kers
*/

public class SimpleBulkUpdateHandler implements BulkUpdateHandler
    {
    private GraphBase graph;
    private GraphEventManager manager;
    
    public SimpleBulkUpdateHandler( GraphBase graph )
        { 
        this.graph = graph; 
        this.manager = graph.getEventManager();
        }

    public void add( Triple [] triples )
        { 
        for (int i = 0; i < triples.length; i += 1) graph.performAdd( triples[i] ); 
        manager.notifyAdd( triples );
        }
        
    public void add( List triples )
        { for (int i = 0; i < triples.size(); i += 1) graph.add( (Triple) triples.get(i) ); }
        
    public void add( Iterator it )
        { while (it.hasNext()) graph.add( (Triple) it.next() ); }
        
    public void add( Graph g )
        { add( GraphUtil.findAll( g ) );  }

    public void delete( Triple [] triples )
        { 
        for (int i = 0; i < triples.length; i += 1) graph.performDelete( triples[i] ); 
        manager.notifyDelete( triples );
        }
    
    public void delete( List triples )
        { for (int i = 0; i < triples.size(); i += 1) graph.delete( (Triple) triples.get(i) );}
    
    public void delete( Iterator it )
        {  while (it.hasNext()) graph.delete( (Triple) it.next() ); }
    
    public void delete( Graph g )
        { delete( GraphUtil.findAll( g ) ); }
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
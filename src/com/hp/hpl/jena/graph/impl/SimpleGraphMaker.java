/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: SimpleGraphMaker.java,v 1.10 2003-09-10 14:00:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

/**
	@author hedgehog
    
    A SimpleGraphFactory produces memory-based graphs and records them
    in a local map.
*/

public class SimpleGraphMaker extends BaseGraphMaker
	{
    /**
        Initialise a SimpleGraphMaker with the given style.
        @param style the reification style of all the graphs we create
    */
    public SimpleGraphMaker( ReificationStyle style )
        { super( style ); }
       
    /**
        Initialise a SimpleGraphMaker with reification style Minimal
    */ 
    public SimpleGraphMaker()
        { this( ReificationStyle.Minimal ); }
       
    /**
        Answer the RDFS class of this Maker
        @return JMS.MemMakerClass [as node]
    */ 
    public Node getMakerClass()
        { return JMS.MemMakerSpec.asNode(); }
       
    /**
        Augment the general description of a GraphMaker with any Simple triples [ie none]
    */ 
    protected void augmentDescription( Graph d, Node self )
        {}
    
    /**
        The mapping from the names of graphs to the Graphs themselves.
    */    
    private Map graphs = new HashMap();
    
    public Graph create()
        { return new GraphMem(); }
    
    /**
        Create a graph and record it with the given name in the local map.
     */
    public Graph createGraph( String name, boolean strict )
        {
        GraphMem already = (GraphMem) graphs.get( name );
        if (already == null)
            {
            Graph result = new GraphMem( style );
            graphs.put( name, result );
            return result;            
            }
        else if (strict)
            throw new AlreadyExistsException( name );
        else
            return already.openAgain();
        }
        
    /**
        Open (aka find) a graph with the given name in the local map.
     */
    public Graph openGraph( String name, boolean strict )
        {
        GraphMem already = (GraphMem) graphs.get( name );
        if (already == null) 
            if (strict) throw new DoesNotExistException( name );
            else return createGraph( name, true );
        else
            return already.openAgain();
        }
        
    /**
        Remove the mapping from name to any graph from the local map.
     */
    public void removeGraph( String name )
        {
        if (!graphs.containsKey( name )) throw new DoesNotExistException( name );
        graphs.remove( name );
        }
        
    /**
        Return true iff we have a graph with the given name
    */
    public boolean hasGraph( String name )
        { return graphs.containsKey( name ); }
             
    /**
        Close this factory - we choose to do nothing.
     */
    public void close()
        { /* nothing to do */ }
        
    public ExtendedIterator listGraphs()
        { return WrappedIterator.create( graphs.keySet().iterator() ); }
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
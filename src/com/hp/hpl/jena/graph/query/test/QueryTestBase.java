/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: QueryTestBase.java,v 1.1 2004-07-22 10:11:47 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.util.iterator.Map1;

/**
 	Various things that are common to some of the query/expression
 	tests, so pulled up here into a shared superclass.
 	
 	@author hedgehog
*/
public abstract class QueryTestBase extends GraphTestBase
    {
    public QueryTestBase( String name ) 
        { super( name ); }
    
    /**
     	An expression that is true if x and y differ. Variable nodes
     	are treated as variables, non-variable nodes as constants.
    */
    protected Expression notEqual( Node x, Node y )
        { return ExampleCreate.NE( x, y );  }
        
    /**
     	An expression that is true if x and y are equal. Variable nodes
     	are treated as variables, non-variable nodes as constants.
    */
    protected Expression areEqual( Node x, Node y )
        { return ExampleCreate.EQ( x, y );  }
        
    /**
     	An expression that is true if x and y "match", in the sense
     	that x contains y. Variable nodes 	are treated as variables, 
     	non-variable nodes as constants.
    */
    protected Expression matches( Node x, Node y )
        { return ExampleCreate.MATCHES( x, y ); }
    
    /**
     	A Map1 (suitable for a .mapWith iterator conversion) which 
     	assumes the elements are lists and extracts their first elements.
    */
    protected static Map1 getFirst = new Map1() 
    	{ public Object map1( Object x ) { return ((List) x).get(0); } };  
    
    /**
       An IndexValues with no elements - ever slot maps to null
    */
    protected static final IndexValues noIVs = new IndexValues() 
        { public Object get( int i ) { return null; } };

    /**
         A mapping with no elements pre-defined
    */
    protected static final Mapping emptyMapping = new Mapping( new Node[0] );

    /**
     	A mapping from variables to their indexes where no variables
     	are defined (all names map to the non-existant slot -1).
     */
    protected VariableIndexes noVariables = new VariableIndexes()
        { public int indexOf( String name ) { return -1; } };
        
    /**
     	A Node with spelling "X".
    */
    protected static final Node X = Query.X;

    /**
 		A Node with spelling "Y".
    */
    protected static final Node Y = Query.Y;
    
    /**
 		A Node with spelling "Z".
    */
    protected static final Node Z = Query.Z;

    /**
        A convenient way to refer to Node.ANY
    */
    protected static final Node ANY = Node.ANY;
    
    /**
     	An array containing just the node X.
    */
    protected final Node [] justX = new Node [] {X};
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
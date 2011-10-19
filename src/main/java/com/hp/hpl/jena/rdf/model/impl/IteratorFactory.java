/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: IteratorFactory.java,v 1.1 2009-06-29 08:55:32 castagna Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;


import java.util.*;



import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;

import com.hp.hpl.jena.graph.*;


/**
 * @author jjc
 *  Builds Jena Iterators and other things (RDFNode and Statement)
 *  needed in a Model.
 */
public final class IteratorFactory
    {
    private IteratorFactory(){}

	/**
	 * 
	 */
	static public StmtIterator asStmtIterator( Iterator<Triple> i, final ModelCom m ) 
	    {
	    Map1<Triple, Statement> asStatement = new Map1<Triple, Statement>() 
	        { @Override
            public Statement map1( Triple t ) { return m.asStatement( t ); }};
	    return new StmtIteratorImpl( WrappedIterator.create( i ).mapWith( asStatement ) );
	   }

	/**
	 * 
	 */
	static public ResIterator asResIterator( Iterator<Node> i, final ModelCom m) 
	    {
		Map1<Node, Resource> asResource = new Map1<Node, Resource>() 
		    { @Override
            public Resource map1( Node o) { return (Resource) m.asRDFNode( o ); }};
		return new ResIteratorImpl( WrappedIterator.create( i ).mapWith( asResource ), null );
	    }

	/**
	 * 
	 */
	static public NodeIterator asRDFNodeIterator( Iterator<Node> i, final ModelCom m) 
	    {      
	    Map1<Node, RDFNode> asRDFNode = new Map1<Node, RDFNode>() 
	        { @Override
            public RDFNode map1( Node o) { return m.asRDFNode( o ); }};
	    return new NodeIteratorImpl( WrappedIterator.create( i ).mapWith( asRDFNode ), null );
	    }
	    
    static  Resource asResource( Node n, ModelCom m )  
        { return asResource( n, Resource.class, m );  }	    
    
    static Property asProperty( Node n, ModelCom m ) 
        { return (Property)asResource( n, Property.class, m ); }
    
    static Literal asLiteral(Node n,ModelCom m) 
        { return m.getNodeAs( n, Literal.class ); }
    
    static <X extends RDFNode> Resource asResource( Node n, Class<X> cl, ModelCom m ) 
        { return (Resource) m.getNodeAs( n, cl ); }
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

/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: IteratorFactory.java,v 1.2 2003-02-19 10:53:39 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;


import java.util.*;



import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;
//import com.hp.hpl.jena.rdf.model.personality.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * @author jjc
 *  Builds Jena Iterators and other things (RDFNode and Statement)
 *  needed in a Model.
 */
public final class IteratorFactory {

    private IteratorFactory(){}
	/**
	 * @see Personality#asRDFNode(Node, Model)
	 */
	static public RDFNode asRDFNode(Node n, ModelCom m) {
		if ( n.isLiteral() ) 
		  return asLiteral(n,m);
		else
		  return asResource(n,m);
	}
	/*
	public Resource anonResource( Model m )
		{
		return (Resource) asRDFNode( Node.makeAnon( new AnonId() ), m );
		}
		*/
		
	/**
	 * @see Personality#asStatement(Triple, Model)
	 */
	static public Statement asStatement(Triple t, ModelCom m) {
		return new StatementImpl(
		asResource(t.getSubject(),m),
		asProperty(t.getPredicate(),m),
		asRDFNode(t.getObject(),m),
		m
		);
	}

	/**
	 * @see Personality#asStmtIterator(Iterator, Model)
	 */
	static public StmtIterator asStmtIterator(Iterator i, final ModelCom m) {
		return new StmtIteratorImpl(new Map1Iterator(new Map1(){
			public Object map1(Object o) {
				return asStatement((Triple)o,m);
			}
		},i),null);
	}

	/**
	 * @see Personality#asResIterator(Iterator, Model)
	 */
	static public ResIterator asResIterator(Iterator i, final ModelCom m) {
		return new ResIteratorImpl(new Map1Iterator(new Map1(){
			public Object map1(Object o) {
				// return (Resource)asRDFNode((Node)o,m);
				return asRDFNode((Node)o,m);			}
		},i),null);
	}

	/**
	 * @see Personality#asRDFNodeIterator(Iterator, Model)
	 */
	static public NodeIterator asRDFNodeIterator(Iterator i, final ModelCom m) {
		return new NodeIteratorImpl(new Map1Iterator(new Map1(){
			public Object map1(Object o) {
				return asRDFNode((Node)o,m);
			}
		},i),null);
	}
	    
    static  Resource asResource(Node n,ModelCom m) {
    	return asResource(n,Resource.class,m);
    	
    }	    
    
    static  Property asProperty(Node n,ModelCom m) {
    	return (Property)asResource(n,Property.class,m);
    }
    
    static  Literal asLiteral(Node n,ModelCom m) {
    	// return (Literal) m.getNodeAs( n, Literal.class );
        return (Literal) m.getNodeAs( n, Literal.class );
    }
    
    static  Resource asResource(Node n, Class cl,ModelCom m) {
    	return (Resource)m.getNodeAs(n,cl);
    }
}

/*
    (c) Copyright Hewlett-Packard Company 2002
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

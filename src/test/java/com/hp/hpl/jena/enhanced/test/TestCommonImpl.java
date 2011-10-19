/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestCommonImpl.java,v 1.3 2010-01-19 10:06:17 chris-dollin Exp $
*/

package com.hp.hpl.jena.enhanced.test;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.*;

/**
 *
 * @author  jjc
 */
class TestCommonImpl extends EnhNode implements TestNode {

    /** Creates new TestCommonImpl */
    TestCommonImpl(Node n, EnhGraph m ) {
        super(n,m);
    }
    
    /**
       We can't return TestModel now, because it clashes with the getModel()
       in RDFNode, which we have to inherit because of the personality tests.
       Fortunately the EnhGraph test set doesn't /need/ getModel, so we give
       it return type Model and throw an exception if it's ever called.
       
       @author chris
    */
    public Model getModel() 
        { throw new JenaException( "getModel() should not be called in the EnhGraph/Node tests" ); }
    
    public Resource asResource()
        { throw new JenaException( "asResource() should not be called in the EnhGraph/Node tests" ); }
    
    public Literal asLiteral()
        { throw new JenaException( "asLiteral() should not be called in the EnhGraph/Node tests" ); }

    Triple findSubject()
        { return findNode( node, null, null ); }
        
    Triple findPredicate()
        { return findNode( null, node, null ); }
        
    Triple findObject()
        { return findNode( null, null, node ); }
        
    Triple findNode( Node s, Node p, Node o )
        {
        ClosableIterator<Triple> it = enhGraph.asGraph().find( s, p, o );
        try { return it.hasNext() ? it.next() : null; }
        finally { it.close(); }
        }
        
    // Convenience routines, that wrap the generic
    // routines from EnhNode.
    @Override
    public TestSubject asSubject() {
        return asInternal(TestSubject.class);
    }
    
    @Override
    public TestObject asObject() {
        return asInternal(TestObject.class);
    }
    
    @Override
    public TestProperty asProperty() {
        return asInternal(TestProperty.class);
    }

    public RDFNode inModel( Model m )
        {
        
        return null;
        }

    public Object visitWith( RDFVisitor rv )
        {
        
        return null;
        }
    
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

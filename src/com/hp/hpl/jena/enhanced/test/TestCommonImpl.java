/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestCommonImpl.java,v 1.6 2005-02-21 12:03:41 andy_seaborne Exp $
*/

package com.hp.hpl.jena.enhanced.test;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
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
    TestModel getModel() {
        return (TestModel)enhGraph;
    }

    Triple findSubject()
        { return findNode( node, null, null ); }
        
    Triple findPredicate()
        { return findNode( null, node, null ); }
        
    Triple findObject()
        { return findNode( null, null, node ); }
        
    Triple findNode( Node s, Node p, Node o )
        {
        ClosableIterator it = enhGraph.asGraph().find( s, p, o );
        try { return it.hasNext() ? (Triple) it.next() : null; }
        finally { it.close(); }
        }
        
    // Convenience routines, that wrap the generic
    // routines from EnhNode.
    public TestSubject asSubject() {
        return (TestSubject)asInternal(TestSubject.class);
    }
    
    public TestObject asObject() {
        return (TestObject)asInternal(TestObject.class);
    }
    
    public TestProperty asProperty() {
        return (TestProperty)asInternal(TestProperty.class);
    }
    
}

/*
	(c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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

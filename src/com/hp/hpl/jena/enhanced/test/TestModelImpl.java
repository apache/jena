/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestModelImpl.java,v 1.4 2003-08-27 12:59:59 andy_seaborne Exp $
*/

package com.hp.hpl.jena.enhanced.test;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;

/**
 *
 * @author  jjc
 */
public class TestModelImpl extends EnhGraph implements TestModel {
    
    /** Creates a new instance of TestModelImpl */
    public TestModelImpl(Graph g, Personality p) {
        super(g,p);
    }
    private Triple aTriple() {
        ClosableIterator it = null;
        try {
        it = graph.find(null,null,null);
            return it.hasNext()?(Triple)it.next():null;
        }
        finally {
            if (it!=null) 
                it.close();
        }
    }
        
    public TestObject anObject() {
        return (TestObject)getNodeAs(aTriple().getObject(),TestObject.class);
    }
    
    public TestProperty aProperty() {
        return (TestProperty)getNodeAs(aTriple().getPredicate(),TestProperty.class);
    }
    
    public TestSubject aSubject() {
        return (TestSubject)getNodeAs(aTriple().getSubject(),TestSubject.class);
    }
    
}

/*
	(c) Copyright 2002 Hewlett-Packard Development Company, LP
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


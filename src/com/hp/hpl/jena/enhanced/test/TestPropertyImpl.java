/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestPropertyImpl.java,v 1.3 2003-04-08 22:12:04 ian_dickinson Exp $
*/

package com.hp.hpl.jena.enhanced.test;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;

/**
 * @see TestObjectImpl
 * @author  jjc
 */
public class TestPropertyImpl  extends TestCommonImpl implements TestProperty {
    private static Class [] myTypes = new Class []{
       TestProperty.class
    };
    public static final Implementation factory = new Implementation() {
    public Class [] implementedTypes() {
        return myTypes;
    }
    public EnhNode wrap(Node n,EnhGraph eg) {
        return new TestPropertyImpl(n,eg);
    }
};
    
    /** Creates a new instance of TestAllImpl */
    private TestPropertyImpl(Node n,EnhGraph eg) {
        super(n,eg,myTypes);
    }
    
    public boolean supports( Class t )
        { return t.isInstance( this ) && isProperty(); }
        
    public boolean isProperty() {
        return find(P)!=null;
    }
        
    public TestObject anObject() {
        if (!isProperty())
            throw new IllegalStateException("Node is not the property of a triple.");
        return (TestObject)enhGraph.getNodeAs(find(P).getObject(),TestObject.class);
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


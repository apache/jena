/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestObjectImpl.java,v 1.4 2003-05-20 12:42:09 chris-dollin Exp $
*/

package com.hp.hpl.jena.enhanced.test;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;

/**
 * See {@link TestObject} for more detailed documentation.
 * @author  jjc
 */
public class TestObjectImpl extends TestCommonImpl implements TestObject {

    /** The  required field is the factory field, of
     * class Implementation.
     * This tells how to construct a new EnhNode of this typ
     * from a Node. Note that caching has already happened, so
     * there is no point in implementing another cache here.
     */
    public static final Implementation factory = new Implementation() {
    /** This method should probably always just call a constructor.
     *  Note the constructor can/should be private.
     */
    public EnhNode wrap(Node n,EnhGraph eg) {
        return new TestObjectImpl(n,eg);
    }
    public boolean canWrap( Node n, EnhGraph eg )
        { return true; }
};
    
    /** Creates a new instance of TestAllImpl */
    private TestObjectImpl(Node n,EnhGraph eg) {
        super( n, eg );
    }
    
    public boolean supports( Class t )
        { return t.isInstance( this) && isObject(); }
        
    public boolean isObject() {
        return findObject() != null;
    }
    /**
     * The code first checks that the interface is appropriate at this point.
     * This is not obligatory but should be considered.
     * (If the underlying graph has changed for the worse will
     * users prefer an early and unambiguous exception at this point).
     * 
     * @see com.hp.hpl.jena.enhanced.test.TestObject#aSubject()
     */
    public TestSubject aSubject() {
        if (!isObject())
            throw new IllegalStateException("Node is not the object of a triple.");
        return (TestSubject)enhGraph.getNodeAs(findObject().getSubject(),TestSubject.class);
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


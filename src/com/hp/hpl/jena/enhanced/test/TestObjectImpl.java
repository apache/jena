/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestObjectImpl.java,v 1.2 2003-02-19 10:54:23 chris-dollin Exp $
*/

package com.hp.hpl.jena.enhanced.test;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;

/**
 * See {@link TestObject} for more detailed documentation.
 * @author  jjc
 */
public class TestObjectImpl extends TestCommonImpl implements TestObject {
	/**
	 * Implementation classes have two required fields.
	 * The first one always lists the type objects from the implemented
	 * interfaces (all of them, proceeding up the interface hierachy if
	 * necessary).
	 */
    private static Class [] myTypes = new Class []{
       TestObject.class
    };
    /** The second required field is the factory field, of
     * class Implementation.
     * This tells how to construct a new EnhNode of this typ
     * from a Node. Note that caching has already happened, so
     * there is no point in implementing another cache here.
     */
    public static final Implementation factory = new Implementation() {
    	/**
    	 * This method should always return the constant array already
    	 * constructed.
    	 */
    public Class [] implementedTypes() {
        return myTypes;
    }
    /** This method should probably always just call a constructor.
     *  Note the constructor can/should be private.
     */
    public EnhNode wrap(Node n,EnhGraph eg) {
        return new TestObjectImpl(n,eg);
    }
};
    
    /** Creates a new instance of TestAllImpl */
    private TestObjectImpl(Node n,EnhGraph eg) {
    	// Note that the myTypes array should either be 
    	// passed into the superclass constructor
    	// or applied immediately after using 
    	// the Polymorphic.setTypes() method.
        super(n,eg,myTypes);
    }
    
    public boolean supports( Class t )
        { return t.isInstance( this) && isObject(); }
        
    public boolean isObject() {
        return find(O)!=null;
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
        return (TestSubject)enhGraph.getNodeAs(find(O).getSubject(),TestSubject.class);
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


/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestObject.java,v 1.1.1.1 2002-12-19 19:13:15 bwm Exp $
*/

package com.hp.hpl.jena.enhanced.test;
import com.hp.hpl.jena.enhanced.*;

/**
 * An interface for viewing object nodes in the graph.
 * @author  jjc
 */
public interface TestObject extends TestNode {
    
    /**
     * For EnhNode interfaces that participate in polymorphism
     * it is necessary to copy and modify code like this.
     *  The name type is conventional.
     */
    public static final Type type = new Type() {
    	/** This method should probably always be implemented this way.
    	* It is a runtime check that the class is appropriate (ignoring the
    	* underlying graph). **/
        public boolean accepts( Polymorphic p ) { return p instanceof TestObject;}
        
        /** This method is the runtime check that the graph is appropriate.
         *  A  precondition before it being called is that accepts() returns
         * true, so the cast in this example is guaranteed not to fail.
         * Code to verify its applicability can either be delegated to the
         * implementation classes (as here), or in-line at this point.
         * The latter choice might make it difficult for different
         * implementations to be different.
         */
        public boolean supportedBy( Polymorphic p ) { return ((TestObject)p).isObject(); }
        public String toString() { return "TestObject.type"; }
    };
    /**
     * Checks whether this node is right now the object of some
     * triple in the graph.
     * @return true if this interface is currently working.
     */
    boolean isObject();
    /** The subject of a triple of which I am object.
     * 
     * @return the subject of a triple.
     */
    TestSubject aSubject();
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


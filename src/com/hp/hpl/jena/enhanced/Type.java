/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Type.java,v 1.1.1.1 2002-12-19 19:13:12 bwm Exp $
*/

package com.hp.hpl.jena.enhanced;

/**
 * A Type denotes a view of a polymorphic object.
 * 
 * @author <a href="mailto:Chris.Dollin@hp.com">Chris Dollin</a> (original code)<br>
 *         <a href="mailto:Ian.Dickinson@hp.com">Ian Dickinson</a> (tidying up and comments)
*/
public interface Type
{
    /** 
     * Answer true iff this Type accepts the polymorphic object p. This encodes the
     * test that an implementation of this type can be constructed from p based on
     * p's static properties (such as p's Java class).
     * @param p A polymorphic object to test
     * @return True if an implementation of this type can be constructed from p
     */
    public boolean accepts( Polymorphic p );
    
    /** 
     * Answer true if this type is currently a valid presentation of the given
     * polymorphic object at this moment. This may depend on dynamic properties,
     * such as the current triples in some graph.
     * @param p A polymorphic object to test
     * @return True if the current environment means that p can validly be
     *         presented as an object of this type.
     */
    public boolean supportedBy( Polymorphic p );
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
